package com.example.spotly.ui.profile

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.spotly.core.ui.R
import com.example.spotly.domain.model.User
import com.example.spotly.ui.components.rememberCameraImagePicker
import com.example.spotly.ui.components.rememberGalleryImagePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User,
    isLoading: Boolean,
    errorMessage: String?,
    onSaveClick: (String, Uri?, Boolean) -> Unit
) {
    var description by remember(user.description) { mutableStateOf(user.description) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var removeCurrentImage by remember { mutableStateOf(false) }
    var showImageOptions by remember { mutableStateOf(false) }

    val openGallery = rememberGalleryImagePicker { uri ->
        selectedImageUri = uri
        removeCurrentImage = false
    }
    val openCamera = rememberCameraImagePicker { uri ->
        selectedImageUri = uri
        removeCurrentImage = false
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.edit_profile), style = MaterialTheme.typography.titleLarge)

        if (!removeCurrentImage && (selectedImageUri != null || user.profileImageUrl.isNotBlank())) {
            AsyncImage(
                model = selectedImageUri ?: user.profileImageUrl,
                contentDescription = stringResource(R.string.profile_photo),
                modifier = Modifier.size(110.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = stringResource(R.string.profile_photo),
                modifier = Modifier.size(110.dp)
            )
        }

        TextButton(onClick = { showImageOptions = true }) {
            Text(stringResource(R.string.edit_image))
        }
        OutlinedTextField(
            value = user.username,
            onValueChange = {},
            label = { Text(stringResource(R.string.username)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier.fillMaxWidth()
        )
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { onSaveClick(description, selectedImageUri, removeCurrentImage) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) stringResource(R.string.saving) else stringResource(R.string.save_changes))
        }
    }

    if (showImageOptions) {
        ModalBottomSheet(onDismissRequest = { showImageOptions = false }) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.profile_photo), style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = {
                        showImageOptions = false
                        openCamera()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.take_photo)) }
                OutlinedButton(
                    onClick = {
                        showImageOptions = false
                        openGallery()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.choose_from_gallery)) }
                if (user.profileImageUrl.isNotBlank()) {
                    TextButton(
                        onClick = {
                            selectedImageUri = null
                            removeCurrentImage = true
                            showImageOptions = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.delete_photo)) }
                }
            }
        }
    }
}
