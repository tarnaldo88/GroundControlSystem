package com.example.groundcontrolsystem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.groundcontrolsystem.ui.navigation.Routes
import com.example.groundcontrolsystem.CameraScreen
import com.example.groundcontrolsystem.DashboardScreen
import com.example.groundcontrolsystem.GpsScreen
import com.example.groundcontrolsystem.LogsScreen
import com.example.groundcontrolsystem.SettingsScreen

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