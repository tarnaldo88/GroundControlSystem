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

@Composable
public fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard.route
    ) {
        composable(Routes.Dashboard.route) { DashboardScreen() }
        composable(Routes.Camera.route) { CameraScreen() }
        composable(Routes.Settings.route) { SettingsScreen() }
        composable(Routes.Gps.route) { GpsScreen() }
        composable(Routes.Logs.route) { LogsScreen() }
    }
}