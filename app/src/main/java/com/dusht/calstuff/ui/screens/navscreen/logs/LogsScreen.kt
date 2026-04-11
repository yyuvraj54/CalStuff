package com.dusht.calstuff.ui.screens.navscreen.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dusht.core.logging.AppLogger

@Composable
fun LogsScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Activity & API logs use Timber (tags: API, NAV, LIFECYCLE, APP).",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            onClick = {
                AppLogger.app(message = "User requested logout from Logs screen")
                onLogout()
            },
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Log out")
        }
    }
}
