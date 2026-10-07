package com.example.spotly.domain.repository

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.Post
import com.example.spotly.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun newId(): String
    fun save(
        id: String, uid: String, image: UploadedImage, description: String,
        location: LocationPoint? = null, onResult: (AppResult<Unit>) -> Unit
    )
    fun observeFeed(): Flow<AppResult<List<Post>>>
    fun observeUserPosts(uid: String): Flow<AppResult<List<Post>>>
}
