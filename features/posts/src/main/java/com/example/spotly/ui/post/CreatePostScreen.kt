
package com.example.spotly.ui.post

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.spotly.core.ui.R
import com.example.spotly.ui.components.LocationMapButton
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.CreatePostUiState

internal const val DESCRIPTION_LIMIT = 1000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    imageUri: String?,
    description: String,
    onBack: () -> Unit,
    onChooseImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTakePhoto: () -> Unit = {},
    onLocate: () -> Unit = {},
    onRemoveLocation: () -> Unit = {},
    state: CreatePostUiState = CreatePostUiState.Editing(),
    onPublish: () -> Unit = {}
) {
    val canEdit = state is CreatePostUiState.Editing ||
            state is CreatePostUiState.Locating

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.create_post),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    enabled = canEdit
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.post_intro),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (imageUri == null) 200.dp else 260.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (imageUri == null) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            12.dp,
                            Alignment.CenterVertically
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onTakePhoto,
                    enabled = canEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Cámara")
                }

                OutlinedButton(
                    onClick = onChooseImage,
                    enabled = canEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Galería")
                }
            }

            if (imageUri != null) {
                TextButton(
                    onClick = onRemoveImage,
                    enabled = canEdit,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.delete_photo))
                }
            }

            OutlinedTextField(
                value = description,
                enabled = canEdit,
                onValueChange = onDescriptionChange,
                label = {
                    Text(stringResource(R.string.description))
                },
                placeholder = {
                    Text(stringResource(R.string.post_description_hint))
                },
                supportingText = {
                    Text(
                        stringResource(
                            R.string.post_character_count,
                            description.length,
                            DESCRIPTION_LIMIT
                        )
                    )
                },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                stringResource(R.string.location_notice),
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedButton(
                onClick = onLocate,
                enabled = !state.locating && canEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        if (state.locating)
                            R.string.location_loading
                        else
                            R.string.location_add
                    )
                )
            }

            state.location?.let { point ->
                LocationMapButton(point)

                TextButton(
                    onClick = onRemoveLocation,
                    enabled = !state.locating && canEdit
                ) {
                    Text(stringResource(R.string.location_remove))
                }
            }

            if (state.locationError) {
                Text(
                    stringResource(R.string.location_failed),
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.error?.let {
                Text(
                    it.localizedMessage(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onPublish,
                enabled = canEdit &&
                        !state.locating &&
                        imageUri != null &&
                        description.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isPublishing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    stringResource(
                        if (state.isPublishing)
                            R.string.post_publishing
                        else
                            R.string.post_publish
                    )
                )
            }
        }
    }
}
