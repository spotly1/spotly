package com.example.spotly.viewmodel

import android.net.Uri
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.spotly.data.model.AppError
import com.example.spotly.data.repository.ImageRepository
import com.example.spotly.data.repository.PostRepository
import com.example.spotly.data.repository.UploadedImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.firestore.GeoPoint
import com.example.spotly.data.repository.LocationRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

data class CreatePostUiState(
    val isPublishing: Boolean = false,
    val published: Boolean = false,
    val error: AppError? = null,
    val location: GeoPoint? = null,
    val locating: Boolean = false,
    val locationError: Boolean = false
)

class CreatePostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PostRepository()
    private val images = ImageRepository(application)
    private val locations = LocationRepository(application)
    private val state = MutableStateFlow(CreatePostUiState())
    val uiState = state.asStateFlow()
    private var pendingKey: Pair<String, String>? = null
    private var pendingImage: UploadedImage? = null
    private var pendingId: String? = null

    fun locate() {
        if (state.value.locating || state.value.isPublishing) return
        state.value = state.value.copy(locating = true, locationError = false)
        viewModelScope.launch {
            try {
                val point = locations.currentLocation()
                state.value = state.value.copy(location = point, locating = false, locationError = point == null)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { locationDenied() }
        }
    }

    fun locationDenied() { state.value = state.value.copy(locating = false, locationError = true) }
    fun removeLocation() {
        if (!state.value.isPublishing && !state.value.locating) state.value = state.value.copy(location = null, locationError = false)
    }

    fun publish(uri: String?, description: String) {
        if (state.value.isPublishing || state.value.published || state.value.locating) return
        if (uri == null || description.isBlank() || description.length > 1000) {
            fail(AppError.RequiredFields)
            return
        }
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) { fail(AppError.Unauthenticated); return }
        val key = uid to uri
        if (pendingKey != key) {
            pendingKey = key
            pendingImage = null
            pendingId = repository.newId()
        }
        val location = state.value.location
        state.value = state.value.copy(isPublishing = true, error = null)
        fun save(image: UploadedImage) {
            if (FirebaseAuth.getInstance().currentUser?.uid != uid) {
                fail(AppError.Unauthenticated)
                return
            }
            repository.save(requireNotNull(pendingId), uid, image, description.trim(), location,
                onSuccess = { state.value = CreatePostUiState(published = true) },
                onError = { fail(AppError.PostSaveFailed) })
        }
        val image = pendingImage
        if (image != null) save(image)
        else try {
            images.uploadImage(Uri.parse(uri), forPost = true,
                onSuccess = { uploaded -> pendingImage = uploaded; save(uploaded) },
                onError = ::fail)
        } catch (_: Exception) { fail(AppError.ImageUploadFailed) }
    }

    fun resetPublished() {
        state.value = CreatePostUiState()
        pendingKey = null
        pendingImage = null
        pendingId = null
    }

    private fun fail(error: AppError) { state.value = state.value.copy(isPublishing = false, error = error) }
}
