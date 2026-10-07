package com.example.spotly.ui.post

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.ui.components.rememberCameraImagePicker
import com.example.spotly.ui.components.rememberGalleryImagePicker
import com.example.spotly.viewmodel.CreatePostViewModel
import com.example.spotly.viewmodel.CreatePostUiState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun CreatePostRoute(viewModel: CreatePostViewModel, onPublished: () -> Unit = {}) {
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var description by rememberSaveable { mutableStateOf("") }
    val openGallery = rememberGalleryImagePicker { imageUri = it.toString() }
    val openCamera = rememberCameraImagePicker { imageUri = it.toString() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locationPermissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) viewModel.locate() else viewModel.locationDenied()
    }
    val requestLocation = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) viewModel.locate()
        else locationPermissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnPublished by rememberUpdatedState(onPublished)
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.uiState.collect { current ->
                if (current is CreatePostUiState.Published && viewModel.onPublishedHandled()) {
                    imageUri = null
                    description = ""
                    currentOnPublished()
                }
            }
        }
    }

    CreatePostScreen(
        imageUri = imageUri,
        description = description,
        onChooseImage = openGallery,
        onTakePhoto = openCamera,
        onRemoveImage = { imageUri = null },
        onDescriptionChange = { if (it.length <= DESCRIPTION_LIMIT) description = it },
        state = state,
        onLocate = requestLocation,
        onRemoveLocation = viewModel::removeLocation,
        onPublish = { viewModel.publish(imageUri, description) }
    )
}
