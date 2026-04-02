package com.example.groundcontrolsystem.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.ModifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    isNightVision: Boolean,
    onNightVisionToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("gcs_settings", Context.MODE_PRIVATE) }
    
    var checklistBattery by remember { mutableStateOf(prefs.getBoolean("check_battery", true)) }
    var checklistGps by remember { mutableStateOf(prefs.getBoolean("check_gps", true)) }
    var checklistNfz by remember { mutableStateOf(prefs.getBoolean("check_nfz", true)) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("System Settings", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Visuals", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Night Vision Mode", modifier = Modifier.weight(1f))
                    Switch(checked = isNightVision, onCheckedChange = onNightVisionToggle)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pre-Flight Checklist Customization", style = MaterialTheme.typography.titleMedium)
                
                CheckSetting("Require Battery > 50%", checklistBattery) { 
                    checklistBattery = it
                    prefs.edit().putBoolean("check_battery", it).apply()
                }
                CheckSetting("Require GPS Lock", checklistGps) { 
                    checklistGps = it
                    prefs.edit().putBoolean("check_gps", it).apply()
                }
                CheckSetting("Require NFZ Check", checklistNfz) { 
                    checklistNfz = it
                    prefs.edit().putBoolean("check_nfz", it).apply()
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("External Resources", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Learn more about UAV safety and regulations.",
                    style= MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                        val webpage: Uri = Uri.parse("https://www.faa.gov/uas")
                        val intent = Intent(Intent.ACTION_VIEW, webpage)
                        context.startActivity(intent)
                    },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Visit Official UAV Portal")
                }
            }
        }
    }
}

@Composable
fun CheckSetting(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}
