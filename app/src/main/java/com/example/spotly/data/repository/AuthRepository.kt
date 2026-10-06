package com.example.spotly.data.repository

import com.example.spotly.data.model.AppError

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (AppError) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(getAuthErrorMessage(task.exception))
                }
            }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (AppError) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->

                if (!authTask.isSuccessful) {
                    onError(getAuthErrorMessage(authTask.exception))
                    return@addOnCompleteListener
                }

                val user = auth.currentUser

                if (user == null) {
                    onError(AppError.Generic)
                    return@addOnCompleteListener
                }

                val uid = user.uid
                val usernameRef = db.collection("usernames").document(username)
                val userRef = db.collection("users").document(uid)
                val privateUserRef = db.collection("privateUsers").document(uid)

                db.runTransaction { transaction ->

                    val usernameDocument = transaction.get(usernameRef)

                    if (usernameDocument.exists()) {
                        throw Exception("USERNAME_TAKEN")
                    }

                    transaction.set(
                        usernameRef,
                        mapOf("uid" to uid)
                    )

                    transaction.set(
                        userRef,
                        mapOf(
                            "username" to username,
                            "description" to "",
                            "profileImageUrl" to "",
                            "profileImagePublicId" to ""
                        )
                    )

                    transaction.set(
                        privateUserRef,
                        mapOf("email" to (user.email ?: email))
                    )

                }.addOnSuccessListener {

                    onSuccess()

                }.addOnFailureListener { exception ->

                    user.delete()

                    if (exception.message == "USERNAME_TAKEN") {
                        onError(AppError.UsernameTaken)
                    } else {
                        onError(AppError.RegistrationFailed)
                    }
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun observeAuthentication(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private fun getAuthErrorMessage(exception: Exception?): AppError {
        return when (exception) {
            is FirebaseAuthWeakPasswordException ->
                AppError.WeakPassword

            is FirebaseAuthInvalidCredentialsException,
            is FirebaseAuthInvalidUserException ->
                AppError.InvalidCredentials

            is FirebaseAuthUserCollisionException ->
                AppError.EmailAlreadyUsed

            else ->
                AppError.Generic
        }
    }
}
