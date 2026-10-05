package com.example.spotly.data.repository

import com.example.spotly.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUserProfile(
        onSuccess: (User) -> Unit,
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
                if (!document.exists()) {
                    onError("No se encontró el perfil.")
                    return@addOnSuccessListener
                }

                val user = User(
                    uid = uid,
                    username = document.getString("username").orEmpty(),
                    email = document.getString("email").orEmpty(),
                    description = document.getString("description").orEmpty(),
                    profileImageUrl = document.getString("profileImageUrl").orEmpty()
                )

                onSuccess(user)
            }
            .addOnFailureListener {
                onError("Error al cargar el perfil.")
            }
    }

    fun updateDescription(
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError("Usuario no autenticado.")
            return
        }

        db.collection("users")
            .document(uid)
            .update("description", description)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError("Error al actualizar el perfil.")
            }
    }
}