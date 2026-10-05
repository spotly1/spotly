package com.example.spotly.data.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

class ImageRepository(
    private val context: Context
) {

    fun uploadImage(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        MediaManager.get()
            .upload(imageUri)
            .unsigned("spotly_unsigned")
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

                    if (imageUrl != null) {
                        onSuccess(imageUrl)
                    } else {
                        onError("No se pudo obtener la URL de la imagen.")
                    }
                }

                override fun onError(
                    requestId: String,
                    error: ErrorInfo
                ) {
                    onError(error.description)
                }

                override fun onReschedule(
                    requestId: String,
                    error: ErrorInfo
                ) = Unit
            })
            .dispatch()
    }
}