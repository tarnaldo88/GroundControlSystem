package com.example.groundcontrolsystem.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.groundcontrolsystem.ui.navigation.AppNavHost
import com.example.groundcontrolsystem.ui.components.LeftNavRail
import com.example.groundcontrolsystem.ui.components.TopCommandBar

@Composable
fun AppShell() {
    val navController = rememberNavController()

    Row(modifier = Modifier.fillMaxSize()) {
        LeftNavRail(
            navController = navController,
            modifier = Modifier.fillMaxHeight().width(96.dp)
        )

        VerticalDivider(
            modifier = Modifier.fillMaxHeight().width(1.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TopCommandBar(
                modifier = Modifier.fillMaxWidth(),
                statusText = "TODO will change to method that returns whether connected or not"
            )

            HorizontalDivider(modifier = Modifier.fillMaxWidth().height(1.dp))

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                AppNavHost(navController = navController)
            }
        }
    }
}