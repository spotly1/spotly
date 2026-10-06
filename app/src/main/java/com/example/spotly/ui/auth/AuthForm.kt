package com.example.spotly.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spotly.R
import com.example.spotly.data.model.AppError
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.AuthUiState

@Composable
internal fun AuthForm(
    register: Boolean,
    state: AuthUiState,
    onEdit: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit,
    onSwitch: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    // Las contraseñas no se guardan en el estado persistido de la actividad.
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    val focus = LocalFocusManager.current
    val submit = {
        if (!state.isLoading) {
            focus.clearFocus()
            onSubmit(username, email, password, confirmation)
        }
    }
    val error = state.error
    val missing = error == AppError.RequiredFields
    val usernameError = error.takeIf {
        register && (it == AppError.UsernameTooShort || it == AppError.UsernameTaken || (missing && username.isBlank()))
    }
    val emailError = error.takeIf { it == AppError.InvalidEmail || it == AppError.EmailAlreadyUsed || (missing && email.isBlank()) }
    val passwordError = error.takeIf { it == AppError.PasswordTooShort || it == AppError.WeakPassword || (missing && password.isBlank()) }
    val confirmationError = error.takeIf { register && (it == AppError.PasswordsDoNotMatch || (missing && confirmation.isBlank())) }
    val generalError = error.takeUnless {
        it == usernameError || it == emailError || it == passwordError || it == confirmationError
    }

    AuthLayout(register) {
        if (register) AuthField(
            username, { username = it; onEdit() }, stringResource(R.string.username),
            Icons.Outlined.Person, !state.isLoading, usernameError?.localizedMessage()
        )
        AuthField(
            email, { email = it; onEdit() }, stringResource(R.string.email),
            Icons.Outlined.AlternateEmail, !state.isLoading, emailError?.localizedMessage(),
            keyboardType = KeyboardType.Email
        )
        AuthField(
            password, { password = it; onEdit() }, stringResource(R.string.password),
            Icons.Outlined.Lock, !state.isLoading, passwordError?.localizedMessage(),
            password = true, last = !register, onDone = submit
        )
        if (register) {
            AuthField(
                confirmation, { confirmation = it; onEdit() }, stringResource(R.string.confirm_password),
                Icons.Outlined.Lock, !state.isLoading, confirmationError?.localizedMessage(),
                password = true, last = true, onDone = submit
            )
            Text(stringResource(R.string.auth_password_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        generalError?.let {
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                Text(
                    it.localizedMessage(), Modifier.fillMaxWidth().padding(12.dp).semantics { liveRegion = LiveRegionMode.Polite },
                    color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Button(
            onClick = submit, enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
            }
            Text(stringResource(
                if (state.isLoading) {
                    if (register) R.string.creating_account else R.string.logging_in
                } else {
                    if (register) R.string.create_account else R.string.login
                }
            ))
            if (!state.isLoading) {
                Spacer(Modifier.width(10.dp))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(18.dp))
            }
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(if (register) R.string.auth_has_account else R.string.auth_no_account),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onSwitch, enabled = !state.isLoading) {
                Text(stringResource(if (register) R.string.login else R.string.create_account))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginPreview() {
    AuthForm(false, AuthUiState(), {}, { _, _, _, _ -> }, {})
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RegisterPreview() {
    AuthForm(true, AuthUiState(), {}, { _, _, _, _ -> }, {})
}
