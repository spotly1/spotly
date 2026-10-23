package com.example.spotly.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spotly.core.ui.R
import com.example.spotly.domain.model.NotificationType
import com.example.spotly.ui.components.UserAvatar
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.NotificationsContent
import com.example.spotly.viewmodel.NotificationsUiState
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(state: NotificationsUiState, onBack: () -> Unit, onRetry: () -> Unit,
    onFilter: (Boolean) -> Unit, onOpen: (String) -> Unit, onSettings: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.notifications)) }, windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
            actions = { IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, stringResource(R.string.local_notifications_settings))
            } })
        FilterChip(selected = state.onlyUnread, onClick = { onFilter(!state.onlyUnread) },
            modifier = Modifier.padding(horizontal = 16.dp), label = { Text(stringResource(R.string.notifications_unread)) })
        state.actionError?.let { Text(it.localizedMessage(), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error) }
        when (val content = state.content) {
            NotificationsContent.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is NotificationsContent.Error -> Column(Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(content.error.localizedMessage())
                TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            is NotificationsContent.Success -> {
                if (content.items.isEmpty()) Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(if (state.onlyUnread) R.string.notifications_none_unread else R.string.notifications_empty))
                } else LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(content.items, key = { it.id }) { notice ->
                        Surface(color = if (notice.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.secondaryContainer) {
                            Row(Modifier.fillMaxWidth().clickable(enabled = state.openingId == null, role = Role.Button) { onOpen(notice.id) }
                                .padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(imageUrl = notice.profileImageUrl, username = notice.username)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val actor = notice.username.ifBlank { stringResource(R.string.notification_unknown_user) }
                                    Text(stringResource(if (notice.type == NotificationType.Follow) R.string.notification_follow else R.string.notification_like, actor),
                                        fontWeight = if (notice.read) FontWeight.Normal else FontWeight.SemiBold)
                                    notice.createdAt?.let {
                                        Text(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault()).format(it),
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                if (state.openingId == notice.id) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                    if (content.items.size == 50) item { Text(stringResource(R.string.notifications_limit), Modifier.padding(16.dp)) }
                }
            }
        }
    }
}
