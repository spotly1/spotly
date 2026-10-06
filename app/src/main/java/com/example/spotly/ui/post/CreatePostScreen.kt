package com.example.spotly.ui.post

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.spotly.R
import com.example.spotly.ui.components.rememberGalleryImagePicker
import com.example.spotly.ui.components.rememberCameraImagePicker
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.CreatePostViewModel
import com.example.spotly.viewmodel.CreatePostUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.example.spotly.ui.components.LocationMapButton

private const val DESCRIPTION_LIMIT = 1000

@Composable
fun CreatePostScreen(viewModel: CreatePostViewModel = viewModel(), onPublished: () -> Unit = {}) {
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
    LaunchedEffect(state.published) {
        if (state.published) {
            imageUri = null
            description = ""
            viewModel.resetPublished()
            onPublished()
        }
    }

    CreatePostContent(
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

@Composable
internal fun CreatePostContent(
    imageUri: String?,
    description: String,
    onChooseImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTakePhoto: () -> Unit = {},
    onLocate: () -> Unit = {},
    onRemoveLocation: () -> Unit = {},
    state: CreatePostUiState = CreatePostUiState(),
    onPublish: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().imePadding()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.create_post), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.post_intro),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            modifier = Modifier.fillMaxWidth().height(260.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            if (imageUri == null) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    Icon(Icons.Outlined.AddPhotoAlternate, null, Modifier.size(48.dp))
                    Text(stringResource(R.string.post_empty_photo))
                }
            } else {
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(R.string.post_photo_preview),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        OutlinedButton(onClick = onTakePhoto, enabled = !state.isPublishing, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.take_photo))
        }
        OutlinedButton(onClick = onChooseImage, enabled = !state.isPublishing, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (imageUri == null) R.string.choose_from_gallery else R.string.post_change_photo))
        }
        if (imageUri != null) {
            TextButton(onClick = onRemoveImage, enabled = !state.isPublishing, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.delete_photo))
            }
        }
        OutlinedTextField(
            value = description,
            enabled = !state.isPublishing,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.description)) },
            placeholder = { Text(stringResource(R.string.post_description_hint)) },
            supportingText = {
                Text(stringResource(R.string.post_character_count, description.length, DESCRIPTION_LIMIT))
            },
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Text(stringResource(R.string.location_notice), style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = onLocate, enabled = !state.locating && !state.isPublishing, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (state.locating) R.string.location_loading else R.string.location_add))
        }
        state.location?.let { point ->
            LocationMapButton(point)
            TextButton(onClick = onRemoveLocation, enabled = !state.locating && !state.isPublishing) {
                Text(stringResource(R.string.location_remove))
            }
        }
        if (state.locationError) Text(stringResource(R.string.location_failed), color = MaterialTheme.colorScheme.error)
        state.error?.let { Text(it.localizedMessage(), color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = onPublish,
            enabled = !state.isPublishing && !state.locating && imageUri != null && description.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isPublishing) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(stringResource(if (state.isPublishing) R.string.post_publishing else R.string.post_publish))
        }
    }
}
