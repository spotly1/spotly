package com.example.spotly.data.repository

import com.example.spotly.data.model.AppError

import android.net.Uri
import android.content.Context
import com.cloudinary.android.preprocess.ImagePreprocessChain
import com.cloudinary.android.preprocess.BitmapEncoder
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth

data class UploadedImage(
    val url: String,
    val publicId: String
)

class ImageRepository(context: Context) {
    private val appContext = context.applicationContext

    fun uploadImage(
        imageUri: Uri,
        forPost: Boolean = false,
        onSuccess: (UploadedImage) -> Unit,
        onError: (AppError) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onError(AppError.Unauthenticated)
            return
        }

        val request = MediaManager.get()
            .upload(imageUri)
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
                    val imageUrl = resultData["secure_url"] as? String
                    val publicId = resultData["public_id"] as? String

                    if (imageUrl != null && publicId != null) {
                        onSuccess(UploadedImage(imageUrl, publicId))
                    } else {
                        onError(AppError.ImageResponseInvalid)
                    }
                }

                override fun onError(
                    requestId: String,
                    error: ErrorInfo
                ) {
                    onError(AppError.ImageUploadFailed)
                }

                override fun onReschedule(
                    requestId: String,
                    error: ErrorInfo
                ) = Unit
            })
        if (forPost) request.preprocess(postImagePreprocessing(imageUri))
        request.dispatch(appContext)
    }
}

internal fun postImagePreprocessing(uri: Uri) =
    ImagePreprocessChain.limitDimensionsChain(1600, 1600)
        .loadWith(OrientedBitmapDecoder(uri))
        .saveWith(BitmapEncoder(BitmapEncoder.Format.JPEG, 80))
