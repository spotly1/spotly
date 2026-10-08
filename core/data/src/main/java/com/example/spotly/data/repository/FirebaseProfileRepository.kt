package com.example.spotly.data.repository

import com.example.spotly.domain.repository.ProfileRepository

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult

import com.example.spotly.domain.model.User
import com.example.spotly.network.UserDto
import com.example.spotly.network.PrivateUserDto
import com.google.firebase.firestore.DocumentSnapshot
import com.google.android.gms.tasks.Tasks

class FirebaseProfileRepository(services: com.example.spotly.network.FirebaseServices) : ProfileRepository {

    private val auth = services.auth
    private val db = services.firestore

    override fun getCurrentUserProfile(
        onResult: (AppResult<User>) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onResult(AppResult.Error(AppError.Unauthenticated))
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
                    onResult(AppResult.Error(AppError.ProfileNotFound))
                    return@addOnSuccessListener
                }

                val user = runCatching {
                    val publicProfile = document.toObject(UserDto::class.java)
                        ?: error("Missing public profile")
                    val privateProfile = privateDocument.toObject(PrivateUserDto::class.java)
                        ?: error("Missing private profile")
                    publicProfile.toDomain(uid, privateProfile)
                }
                user.fold({ onResult(AppResult.Success(it)) }, { onResult(AppResult.Error(AppError.ProfileLoadFailed)) })
            }
            .addOnFailureListener {
                onResult(AppResult.Error(AppError.ProfileLoadFailed))
            }
    }

    override fun updateProfile(
        description: String,
        profileImageUrl: String,
        profileImagePublicId: String,
        onResult: (AppResult<Unit>) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onResult(AppResult.Error(AppError.Unauthenticated))
            return
        }

        val updates = UserDto(
            description = description,
            profileImageUrl = profileImageUrl,
            profileImagePublicId = profileImagePublicId
        ).toProfileUpdates()

        db.collection("users")
            .document(uid)
            .update(updates)
            .addOnSuccessListener {
                onResult(AppResult.Success(Unit))
            }
            .addOnFailureListener {
                onResult(AppResult.Error(AppError.ProfileUpdateFailed))
            }
    }

    override fun getUserProfile(
        uid: String,
        onResult: (AppResult<User>) -> Unit
    ) {
        if (auth.currentUser == null) {
            onResult(AppResult.Error(AppError.Unauthenticated))
            return
        }

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onResult(AppResult.Error(AppError.ProfileNotFound))
                    return@addOnSuccessListener
                }

                val user = runCatching {
                    val dto = document.toObject(UserDto::class.java)
                        ?: error("Missing public profile")

                    dto.toPublicDomain(document.id)
                }

                user.fold(
                    onSuccess = { onResult(AppResult.Success(it)) },
                    onFailure = { onResult(AppResult.Error(AppError.ProfileLoadFailed)) }
                )
            }
            .addOnFailureListener {
                onResult(AppResult.Error(AppError.ProfileLoadFailed))
            }
    }
}
