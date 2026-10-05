package com.example.spotly.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spotly.data.model.User

@Composable
fun EditProfileScreen(
    user: User,
    isLoading: Boolean,
    onSaveClick: (String) -> Unit
) {
    var description by remember(user.description) {
        mutableStateOf(user.description)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Editar perfil")

        Text("@${user.username}")

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = {
                Text("Descripción")
            }
        )

        Button(
            onClick = {
                onSaveClick(description)
            },
            enabled = !isLoading
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
}