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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.spotly.data.model.User
import com.example.spotly.ui.components.rememberCameraImagePicker
import com.example.spotly.ui.components.rememberGalleryImagePicker
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User,
    isLoading: Boolean,
    errorMessage: String?,
    onSaveClick: (String, Uri?, Boolean) -> Unit
) {
    var description by remember(user.description) {
        mutableStateOf(user.description)
    }

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var removeCurrentImage by remember {
        mutableStateOf(false)
    }

    var showImageOptions by remember {
        mutableStateOf(false)
    }

    val openGallery = rememberGalleryImagePicker { uri ->
        selectedImageUri = uri
        removeCurrentImage = false
    }

    val openCamera = rememberCameraImagePicker { uri ->
        selectedImageUri = uri
        removeCurrentImage = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Editar perfil",
            style = MaterialTheme.typography.titleLarge
        )

        if (!removeCurrentImage &&
            (selectedImageUri != null || user.profileImageUrl.isNotBlank())
        ) {
            AsyncImage(
                model = selectedImageUri ?: user.profileImageUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(110.dp)
            )
        }

        TextButton(
            onClick = {
                showImageOptions = true
            }
        ) {
            Text("Editar imagen")
        }

        OutlinedTextField(
            value = user.username,
            onValueChange = {},
            label = {
                Text("Nombre de usuario")
            },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
            },
            label = {
                Text("Descripción")
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                onSaveClick(
                    description,
                    selectedImageUri,
                    removeCurrentImage
                )
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isLoading) {
                    "Guardando..."
                } else {
                    "Guardar cambios"
                }
            )
        }
    }

    if (showImageOptions) {
        ModalBottomSheet(
            onDismissRequest = {
                showImageOptions = false
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Foto de perfil",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedButton(
                    onClick = {
                        showImageOptions = false
                        openCamera()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tomar una foto")
                }

                OutlinedButton(
                    onClick = {
                        showImageOptions = false
                        openGallery()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Elegir de la galería")
                }

                if (user.profileImageUrl.isNotBlank()) {
                    TextButton(
                        onClick = {
                            selectedImageUri = null
                            removeCurrentImage = true
                            showImageOptions = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eliminar foto")
                    }
                }
            }
        }
    }
}