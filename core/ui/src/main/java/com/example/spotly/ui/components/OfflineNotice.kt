package com.example.spotly.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.spotly.core.ui.R

val LocalOnline = staticCompositionLocalOf { true }

@Composable
fun OfflineBanner() {
    if (!LocalOnline.current) Surface(modifier = Modifier.statusBarsPadding(),
        color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(stringResource(R.string.offline_banner),
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun OnlineRequired(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.error_offline))
        TextButton(onClick = onBack) { Text(stringResource(R.string.home)) }
    }
}
