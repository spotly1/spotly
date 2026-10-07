package com.example.spotly.data.repository

import com.example.spotly.domain.repository.AuthRepository

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.example.spotly.network.UserDto
import com.example.spotly.network.PrivateUserDto
import com.example.spotly.network.UsernameReservationDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseAuthRepository(services: com.example.spotly.network.FirebaseServices) : AuthRepository {

    private val auth = services.auth
    private val db = services.firestore

    override fun currentUserId(): String? = auth.currentUser?.uid

    override fun login(
        email: String,
        password: String,
        onResult: (AppResult<Unit>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(AppResult.Success(Unit))
                } else {
                    onResult(AppResult.Error(getAuthErrorMessage(task.exception)))
                }
            }
    }

    override fun register(
        username: String,
        email: String,
        password: String,
        onResult: (AppResult<Unit>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->

                if (!authTask.isSuccessful) {
                    onResult(AppResult.Error(getAuthErrorMessage(authTask.exception)))
                    return@addOnCompleteListener
                }

                val user = auth.currentUser

                if (user == null) {
                    onResult(AppResult.Error(AppError.Generic))
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
                        UsernameReservationDto(uid)
                    )

                    transaction.set(
                        userRef,
                        UserDto(username = username)
                    )

                    transaction.set(
                        privateUserRef,
                        PrivateUserDto(email = user.email ?: email)
                    )

                }.addOnSuccessListener {

                    onResult(AppResult.Success(Unit))

                }.addOnFailureListener { exception ->

                    user.delete()

                    if (exception.message == "USERNAME_TAKEN") {
                        onResult(AppResult.Error(AppError.UsernameTaken))
                    } else {
                        onResult(AppResult.Error(AppError.RegistrationFailed))
                    }
                }
            }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun observeAuthentication(): Flow<Boolean> = callbackFlow {
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
