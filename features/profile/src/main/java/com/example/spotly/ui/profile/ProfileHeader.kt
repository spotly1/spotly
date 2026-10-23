package com.example.spotly.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.spotly.ui.components.UserAvatar
import androidx.compose.ui.res.stringResource
import com.example.spotly.core.ui.R
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role

@Composable
fun ProfileHeader(
    username: String,
    description: String,
    profileImageUrl: String,
    postsCount: String,
    followersCount: String,
    followingCount: String,
    isOwnProfile: Boolean,
    onEditProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onFollowClick: () -> Unit = {},
    isFollowing: Boolean = false,
    followEnabled: Boolean = true,
    followBusy: Boolean = false,
    onFollowersClick: (() -> Unit)? = null,
    onFollowingClick: (() -> Unit)? = null
) {
    Column {
        Spacer(Modifier.height(24.dp))

        UserAvatar(
            imageUrl = profileImageUrl,
            username = username,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            size = 100.dp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "@$username",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (description.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStat(postsCount, stringResource(R.string.posts))
            ProfileStat(followersCount, stringResource(R.string.followers), onFollowersClick)
            ProfileStat(followingCount, stringResource(R.string.following), onFollowingClick)
        }

        Spacer(Modifier.height(24.dp))

        if (isOwnProfile) {
            OutlinedButton(
                onClick = onEditProfileClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.edit_profile))
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.logout))
            }
        } else {
            Button(
                onClick = onFollowClick,
                enabled = followEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(when {
                    followBusy -> R.string.follow_saving
                    isFollowing -> R.string.unfollow
                    else -> R.string.follow
                }))
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String, onClick: (() -> Unit)? = null) {
    val modifier = if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
    Column(modifier.heightIn(min = 48.dp).padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
