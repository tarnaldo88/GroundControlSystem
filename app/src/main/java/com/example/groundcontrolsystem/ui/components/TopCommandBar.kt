package com.example.groundcontrolsystem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopCommandBar(
    modifier: Modifier = Modifier,
    statusText: String
) {
    val title = "Ground Control System"

    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(statusText, style = MaterialTheme.typography.labelMedium)
            }
        },
        actions = {
            TextButton(onClick = { /*TODO*/ }) { Text("Log View") }
            TextButton(onClick = { /*TODO*/ }) { Text("Options") }
            Spacer(Modifier.width(8.dp))
        }
    )
}