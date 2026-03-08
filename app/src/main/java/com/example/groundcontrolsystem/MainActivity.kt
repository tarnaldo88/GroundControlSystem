package com.example.groundcontrolsystem

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.groundcontrolsystem.ui.theme.GroundControlSystemTheme
import com.example.groundcontrolsystem.ui.AppShell
import org.osmdroid.config.Configuration
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize osmdroid configuration
        val osmConfig = Configuration.getInstance()
        
        // Use a generic user agent to avoid being blocked by some tile servers
        osmConfig.userAgentValue = "Mozilla/5.0 (Android 15; Mobile; rv:115.0) Gecko/115.0 Firefox/115.0"
        
        // Redirect cache to internal storage to avoid permission issues
        val basePath = File(filesDir, "osmdroid")
        if (!basePath.exists()) basePath.mkdirs()
        osmConfig.osmdroidBasePath = basePath
        
        val tilePath = File(basePath, "tiles")
        if (!tilePath.exists()) tilePath.mkdirs()
        osmConfig.osmdroidTileCache = tilePath

        // Load configuration from shared preferences
        osmConfig.load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA
            )
        )

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

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 1)
        }
    }
}
