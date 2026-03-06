package com.example.groundcontrolsystem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.groundcontrolsystem.ui.navigation.Routes
import com.example.groundcontrolsystem.ui.screens.camera.CameraScreen
import com.example.groundcontrolsystem.ui.screens.dashboard.DashboardScreen
import com.example.groundcontrolsystem.ui.screens.gps.GpsScreen
import com.example.groundcontrolsystem.ui.screens.logs.LogsScreen
import com.example.groundcontrolsystem.ui.screens.settings.SettingsScreen
import com.example.groundcontrolsystem.ui.screens.missionplan.MissionPlanScreen
import com.example.groundcontrolsystem.ui.screens.statistics.StatisticsScreen
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    isNightVision: Boolean,
    onNightVisionToggle: (Boolean) -> Unit,
    telemetryViewModel: TelemetryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard.route
    ) {
        composable(Routes.Dashboard.route) { 
            DashboardScreen(telemetryViewModel) 
        }
        composable(Routes.Camera.route) { 
            CameraScreen(telemetryViewModel) 
        }
        composable(Routes.Settings.route) { 
            SettingsScreen(
                isNightVision = isNightVision,
                onNightVisionToggle = onNightVisionToggle
            ) 
        }
        composable(Routes.Gps.route) { 
            GpsScreen(telemetryViewModel) 
        }
        composable(Routes.Logs.route) { 
            LogsScreen(telemetryViewModel) 
        }
        composable(Routes.MissionPlan.route) { 
            MissionPlanScreen(telemetryViewModel) 
        }
        composable(Routes.Statistics.route) {
            StatisticsScreen(telemetryViewModel)
        }
    }
}
