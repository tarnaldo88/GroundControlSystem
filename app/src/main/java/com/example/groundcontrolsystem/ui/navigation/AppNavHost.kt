package com.example.groundcontrolsystem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.groundcontrolsystem.AppRoute
import com.example.groundcontrolsystem.CameraScreen
import com.example.groundcontrolsystem.DashboardScreen
import com.example.groundcontrolsystem.GpsScreen
import com.example.groundcontrolsystem.LogsScreen
import com.example.groundcontrolsystem.SettingsScreen

@Composable
public fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Dashboard.route
    ) {
        composable(AppRoute.Dashboard.route) { DashboardScreen() }
        composable(AppRoute.Camera.route) { CameraScreen() }
        composable(AppRoute.Settings.route) { SettingsScreen() }
        composable(AppRoute.Gps.route) { GpsScreen() }
        composable(AppRoute.Logs.route) { LogsScreen() }
    }
}