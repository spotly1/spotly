package com.example.spotly.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotly.R

// Comparte el tema con el perfil y el resto de la aplicación.
@Composable
internal fun AuthLayout(register: Boolean, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(colors.background, colors.primary.copy(alpha = 0.10f)))
        ).imePadding()
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                Modifier.widthIn(max = 440.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(56.dp).clip(CircleShape)) {
                        // El recurso del launcher incluye un margen de seguridad de 1/8 por lado.
                        Image(
                            painter = painterResource(R.mipmap.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().scale(4f / 3f)
                        )
                    }
                    Column {
                        Text(stringResource(R.string.app_name), color = colors.onSurface, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.auth_tagline), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(if (register) R.string.auth_register_title else R.string.auth_login_title),
                        color = colors.onSurface, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(if (register) R.string.auth_register_subtitle else R.string.auth_login_subtitle),
                        color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge
                    )
                }
                Surface(shape = RoundedCornerShape(28.dp), color = colors.surface, tonalElevation = 1.dp) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
                }
                Text(stringResource(R.string.auth_footer), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
internal fun AuthField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    error: String? = null,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    last: Boolean = false,
    onDone: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    OutlinedTextField(
        value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(),
        label = { Text(label) }, singleLine = true, enabled = enabled,
        shape = RoundedCornerShape(14.dp), isError = error != null,
        leadingIcon = { Icon(icon, contentDescription = null) },
        trailingIcon = if (password) ({
            IconButton(onClick = { visible = !visible }, enabled = enabled) {
                Icon(
                    if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    stringResource(if (visible) R.string.hide_password else R.string.show_password)
                )
            }
        }) else null,
        supportingText = if (error != null) ({ Text(error) }) else null,
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (password) KeyboardType.Password else keyboardType,
            imeAction = if (last) ImeAction.Done else ImeAction.Next,
            autoCorrectEnabled = false
        ),
        keyboardActions = KeyboardActions(
            onNext = { focus.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) },
            onDone = { if (enabled) onDone() }
        )
    )
}
