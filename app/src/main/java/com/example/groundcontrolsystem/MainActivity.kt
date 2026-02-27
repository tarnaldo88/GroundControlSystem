package com.example.groundcontrolsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.groundcontrolsystem.ui.theme.GroundControlSystemTheme
import com.example.groundcontrolsystem.ui.AppShell


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GroundControlSystemTheme {
                AppShell()
            }
        }
    }
}