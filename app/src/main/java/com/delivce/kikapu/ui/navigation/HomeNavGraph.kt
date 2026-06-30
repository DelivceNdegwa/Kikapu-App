package com.delivce.kikapu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.delivce.kikapu.ui.screens.checklist.ChecklistScreen
import com.delivce.kikapu.ui.screens.home.HomeScreen
import com.delivce.kikapu.ui.screens.trips.TripPlanScreen
import com.delivce.kikapu.ui.ProfileContent

@Composable
fun HomeNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME.route,
        modifier = modifier
    ) {
        composable(AppDestinations.HOME.route) {
            HomeScreen()
        }
        composable(AppDestinations.TRIPS.route) {
            TripPlanScreen()
        }
        composable(AppDestinations.CHECKLIST.route) {
            ChecklistScreen()
        }
        composable(AppDestinations.ME.route) {
            ProfileContent(onLogout = onLogout)
        }
        composable(AppDestinations.ADD.route) {
            // Logic for Add button screen
            HomeScreen() 
        }
    }
}
