package com.example.spotly.ui.post

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.spotly.core.ui.R
import com.example.spotly.ui.components.*
import com.example.spotly.viewmodel.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    state: PostDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onEdit: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onUserClick: (String) -> Unit,
) {
    val content = state as? PostDetailUiState.Content
    val busy = content?.action?.let { it != PostAction.None } == true
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    BackHandler(enabled = busy || content?.isEditing == true) {
        if (!busy) onCancelEdit()
    }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(if (content?.isEditing == true) R.string.post_edit else R.string.post_detail),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(enabled = !busy, onClick = {
                    if (content?.isEditing == true) onCancelEdit() else onBack()
                }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
            }
        )
        when (state) {
            PostDetailUiState.Loading, PostDetailUiState.Deleted -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(32.dp), strokeWidth = 3.dp)
            }
            is PostDetailUiState.Error -> Column(Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.error.localizedMessage(), textAlign = TextAlign.Center)
                OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            is PostDetailUiState.Content -> Column(
                Modifier.align(Alignment.CenterHorizontally).widthIn(max = 720.dp).fillMaxWidth()
                    .weight(1f).imePadding().verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatar(
                        imageUrl = state.post.profileImageUrl,
                        username = state.post.username,
                        size = 44.dp,
                        modifier = Modifier.clickable {
                            onUserClick(state.post.authorId)
                        }
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            stringResource(R.string.profile_username, state.post.username),
                            modifier = Modifier.clickable {
                                onUserClick(state.post.authorId)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        state.post.createdAt?.let { date ->
                            val formatted = remember(date) {
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                                    .withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault()).format(date)
                            }
                            Text(formatted, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (state.isOwner && !state.isEditing) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }, enabled = !busy) {
                                Icon(Icons.Default.MoreVert, stringResource(R.string.post_options))
                            }
                            DropdownMenu(expanded = menuExpanded && !busy, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(text = { Text(stringResource(R.string.post_edit)) },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = { menuExpanded = false; onEdit() })
                                DropdownMenuItem(text = {
                                    Text(stringResource(R.string.post_delete), color = MaterialTheme.colorScheme.error)
                                }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error) },
                                    onClick = { menuExpanded = false; confirmDelete = true })
                            }
                        }
                    }
                }
                PostImage(
                    model = state.post.imageUrl,
                    contentDescription = state.post.description,
                    maxPixels = 1600,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )

                if (state.isEditing) {
                    Text(stringResource(R.string.post_edit_notice), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(value = state.description, onValueChange = onDescriptionChange,
                        modifier = Modifier.fillMaxWidth(), enabled = !busy, minLines = 3, maxLines = 8,
                        shape = RoundedCornerShape(16.dp),
                        label = { Text(stringResource(R.string.description)) },
                        supportingText = { Text(stringResource(R.string.post_character_count, state.description.length, 1000)) })
                } else Text(
                    text = state.post.description,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    state.post.location?.let {
                        LocationMapButton(it)
                    }
                }
                state.post.location?.let { location ->
                    LocationMap(
                        point = location,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(horizontal = 8.dp)
                    )
                }
                state.error?.let {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer) {
                        Text(it.localizedMessage(), Modifier.fillMaxWidth().padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                if (state.saved) Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(stringResource(R.string.post_saved), Modifier.fillMaxWidth().padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                if (state.isOwner) {
                    if (state.isEditing) {
                        Button(onClick = onSave, enabled = !busy && state.description.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                            Text(stringResource(if (state.action == PostAction.Saving) R.string.saving else R.string.save_changes))
                        }
                        TextButton(onClick = onCancelEdit, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
    }
    if (confirmDelete && content?.isOwner == true && !busy) {
        AlertDialog(onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.post_delete)) },
            text = { Text(stringResource(R.string.post_delete_confirm)) },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) {
                Text(stringResource(R.string.post_delete), color = MaterialTheme.colorScheme.error)
            } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } })
    }
}
