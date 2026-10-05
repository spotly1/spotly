package com.example.spotly.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun rememberGalleryImagePicker(
    onImageSelected: (Uri) -> Unit
): () -> Unit {

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onImageSelected(it)
        }
    }

    return {
        galleryLauncher.launch("image/*")
    }
}

@Composable
fun rememberCameraImagePicker(
    onImageSelected: (Uri) -> Unit
): () -> Unit {

    val context = LocalContext.current

    val imageUri = remember {
        createImageUri(context)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onImageSelected(imageUri)
        }
    }

    return {
        cameraLauncher.launch(imageUri)
    }
}

private fun createImageUri(context: Context): Uri {

    val imageDirectory = File(
        context.cacheDir,
        "images"
    ).apply {
        mkdirs()
    }

    val imageFile = File.createTempFile(
        "spotly_",
        ".jpg",
        imageDirectory
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}