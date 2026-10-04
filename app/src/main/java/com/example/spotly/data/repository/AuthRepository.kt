package com.example.spotly.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
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
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->

                if (!authTask.isSuccessful) {
                    onError(getAuthErrorMessage(authTask.exception))
                    return@addOnCompleteListener
                }

                val user = auth.currentUser

                if (user == null) {
                    onError("Ocurrió un error. Intentá nuevamente.")
                    return@addOnCompleteListener
                }

                val uid = user.uid
                val usernameRef = db.collection("usernames").document(username)
                val userRef = db.collection("users").document(uid)

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
                            "email" to email,
                            "description" to "",
                            "profileImageUrl" to ""
                        )
                    )

                }.addOnSuccessListener {

                    onSuccess()

                }.addOnFailureListener { exception ->

                    user.delete()

                    if (exception.message == "USERNAME_TAKEN") {
                        onError("Este nombre de usuario ya está en uso.")
                    } else {
                        onError("Ocurrió un error al crear la cuenta.")
                    }
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    private fun getAuthErrorMessage(exception: Exception?): String {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException,
            is FirebaseAuthInvalidUserException ->
                "Correo o contraseña incorrectos."

            is FirebaseAuthUserCollisionException ->
                "Ya existe una cuenta con este correo."

            is FirebaseAuthWeakPasswordException ->
                "La contraseña es demasiado débil."

            else ->
                "Ocurrió un error. Intentá nuevamente."
        }
    }

    fun getCurrentUsername(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError("Usuario no autenticado.")
            return
        }

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val username = document.getString("username")

                if (username != null) {
                    onSuccess(username)
                } else {
                    onError("No se encontró el nombre de usuario.")
                }
            }
            .addOnFailureListener {
                onError("Error al cargar el perfil.")
            }
    }
}