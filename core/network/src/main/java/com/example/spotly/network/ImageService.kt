package com.example.spotly.network

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.preprocess.ImagePreprocessChain
import com.cloudinary.android.preprocess.BitmapEncoder

fun initializeImageService(context: Context) {
    MediaManager.init(context, mapOf("cloud_name" to "iufz7yvd"))
}

fun postImagePreprocessing(uri: Uri) =
    ImagePreprocessChain.limitDimensionsChain(1600, 1600)
        .loadWith(OrientedBitmapDecoder(uri))
        .saveWith(BitmapEncoder(BitmapEncoder.Format.JPEG, 80))
