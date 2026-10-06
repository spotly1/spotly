package com.example.spotly.data.repository

import com.example.spotly.data.model.Post
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class PostRepository {
    private val db = FirebaseFirestore.getInstance()

    fun newId(): String = db.collection("posts").document().id

    fun save(id: String, uid: String, image: UploadedImage, description: String,
             location: com.google.firebase.firestore.GeoPoint? = null,
             onSuccess: () -> Unit, onError: () -> Unit) {
        db.collection("users").document(uid).get().addOnSuccessListener { user ->
            val username = user.getString("username")
            if (username == null) {
                onError()
                return@addOnSuccessListener
            }
            val ref = db.collection("posts").document(id)
            // Un ID estable evita duplicados si se reintenta después de perder la respuesta.
            db.runTransaction { transaction ->
                if (!transaction.get(ref).exists()) {
                    transaction.set(ref, mapOf(
                        "authorId" to uid, "username" to username,
                        "imageUrl" to image.url, "imagePublicId" to image.publicId,
                        "description" to description, "createdAt" to FieldValue.serverTimestamp(),
                        "location" to location
                    ))
                }
            }.addOnSuccessListener { onSuccess() }.addOnFailureListener { onError() }
        }.addOnFailureListener { onError() }
    }

    fun observeFeed() = observeQuery(db.collection("posts")
        .orderBy("createdAt", Query.Direction.DESCENDING).limit(50))

    // No se limita al feed: el total del perfil incluye todas las publicaciones propias.
    fun observeUserPosts(uid: String) = observeQuery(
        db.collection("posts").whereEqualTo("authorId", uid)
    )

    private fun observeQuery(query: Query) = callbackFlow<Result<List<Post>>> {
        val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) trySend(Result.failure(error))
                else if (snapshot != null) {
                    val result = runCatching {
                        snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Post::class.java)?.copy(id = doc.id)
                        }.sortedByDescending { it.createdAt }
                    }
                    trySend(result)
                }
            }
        awaitClose { listener.remove() }
    }
}
