package com.example.groundcontrolsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.groundcontrolsystem.ui.theme.GroundControlSystemTheme
import com.example.groundcontrolsystem.ui.AppShell


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var nightVision by remember { mutableStateOf(false) }
            
            GroundControlSystemTheme(nightVision = nightVision) {
                AppShell(
                    onNightVisionToggle = { nightVision = it },
                    isNightVision = nightVision
                )
            }
        }
    }
}