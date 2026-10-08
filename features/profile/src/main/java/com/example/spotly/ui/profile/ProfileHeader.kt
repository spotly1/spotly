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
    isFollowing: Boolean = false
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
            ProfileStat(postsCount, "Publicaciones")
            ProfileStat(followersCount, "Seguidores")
            ProfileStat(followingCount, "Seguidos")
        }

        Spacer(Modifier.height(24.dp))

        if (isOwnProfile) {
            OutlinedButton(
                onClick = onEditProfileClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar perfil")
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        } else {
            Button(
                onClick = onFollowClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isFollowing) "Dejar de seguir" else "Seguir")
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
