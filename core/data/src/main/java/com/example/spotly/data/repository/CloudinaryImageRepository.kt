package com.example.spotly.data.repository

import com.example.spotly.domain.repository.ImageRepository
import com.example.spotly.domain.repository.UploadedImage
import com.example.spotly.network.postImagePreprocessing

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult

import android.net.Uri
import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.spotly.network.ImageUploadDto

class CloudinaryImageRepository(context: Context, private val services: com.example.spotly.network.FirebaseServices) : ImageRepository {
    private val appContext = context.applicationContext

    override fun uploadImage(
        imageUri: String,
        forPost: Boolean,
        onResult: (AppResult<UploadedImage>) -> Unit
    ) {
        val uid = services.auth.currentUser?.uid
        if (uid == null) {
            onResult(AppResult.Error(AppError.Unauthenticated))
            return
        }

        try {
            val request = MediaManager.get()
                .upload(Uri.parse(imageUri))
                .unsigned("spotly_unsigned")
                .option("folder", "spotly/${if (forPost) "posts" else "profiles"}/$uid")
                .callback(object : UploadCallback {

                    override fun onStart(requestId: String) = Unit

                    override fun onProgress(
                        requestId: String,
                        bytes: Long,
                        totalBytes: Long
                    ) = Unit

                    override fun onSuccess(
                        requestId: String,
                        resultData: Map<*, *>
                    ) {
                        val image = ImageUploadDto.fromResponse(resultData).toDomain()

                        if (image != null) {
                            onResult(AppResult.Success(image))
                        } else {
                            onResult(AppResult.Error(AppError.ImageResponseInvalid))
                        }
                    }

                    override fun onError(
                        requestId: String,
                        error: ErrorInfo
                    ) {
                        onResult(AppResult.Error(AppError.ImageUploadFailed))
                    }

                    override fun onReschedule(
                        requestId: String,
                        error: ErrorInfo
                    ) = Unit
                })
            if (forPost) request.preprocess(postImagePreprocessing(Uri.parse(imageUri)))
            request.dispatch(appContext)
        } catch (_: Exception) {
            onResult(AppResult.Error(AppError.ImageUploadFailed))
        }
    }
}
