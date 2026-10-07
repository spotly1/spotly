package com.example.spotly.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import android.widget.Toast
import com.example.spotly.core.ui.R
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

    var pendingImageUri by rememberSaveable { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingImageUri
        pendingImageUri = null
        if (success && uri != null) onImageSelected(Uri.parse(uri))
    }

    return {
        try {
            // Cada captura tiene su archivo: cancelar o repetir no modifica la foto anterior.
            val imageUri = createImageUri(context)
            pendingImageUri = imageUri.toString()
            cameraLauncher.launch(imageUri)
        } catch (_: android.content.ActivityNotFoundException) {
            pendingImageUri = null
            Toast.makeText(context, R.string.camera_unavailable, Toast.LENGTH_LONG).show()
        } catch (_: SecurityException) {
            pendingImageUri = null
            Toast.makeText(context, R.string.camera_unavailable, Toast.LENGTH_LONG).show()
        } catch (_: java.io.IOException) {
            pendingImageUri = null
            Toast.makeText(context, R.string.camera_unavailable, Toast.LENGTH_LONG).show()
        }
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
