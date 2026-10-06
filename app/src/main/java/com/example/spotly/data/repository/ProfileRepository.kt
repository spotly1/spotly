package com.example.spotly.data.repository

import com.example.spotly.data.model.AppError

import com.example.spotly.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.android.gms.tasks.Tasks

class ProfileRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUserProfile(
        onSuccess: (User) -> Unit,
        onError: (AppError) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError(AppError.Unauthenticated)
            return
        }

        // User reúne datos para la pantalla propia; el email se almacena aparte.
        Tasks.whenAllSuccess<DocumentSnapshot>(
            db.collection("users").document(uid).get(),
            db.collection("privateUsers").document(uid).get()
        ).addOnSuccessListener { documents ->
                val document = documents[0]
                val privateDocument = documents[1]
                if (!document.exists() || !privateDocument.exists()) {
                    onError(AppError.ProfileNotFound)
                    return@addOnSuccessListener
                }

                val user = User(
                    uid = uid,
                    username = document.getString("username").orEmpty(),
                    email = privateDocument.getString("email").orEmpty(),
                    description = document.getString("description").orEmpty(),
                    profileImageUrl = document.getString("profileImageUrl").orEmpty(),
                    profileImagePublicId = document.getString("profileImagePublicId").orEmpty()
                )

                onSuccess(user)
            }
            .addOnFailureListener {
                onError(AppError.ProfileLoadFailed)
            }
    }

    fun updateProfile(
        description: String,
        profileImageUrl: String,
        profileImagePublicId: String,
        onSuccess: () -> Unit,
        onError: (AppError) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError(AppError.Unauthenticated)
            return
        }

        val updates = mapOf(
            "description" to description,
            "profileImageUrl" to profileImageUrl,
            "profileImagePublicId" to profileImagePublicId
        )

        db.collection("users")
            .document(uid)
            .update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(AppError.ProfileUpdateFailed)
            }
    }
}
