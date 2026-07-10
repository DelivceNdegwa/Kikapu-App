package com.delivce.kikapu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.delivce.kikapu.ui.screens.items.ItemsScreen
import com.delivce.kikapu.ui.screens.home.HomeScreen
import com.delivce.kikapu.ui.screens.profile.ProfileScreen
import com.delivce.kikapu.ui.screens.progress.ProgressScreen
import com.delivce.kikapu.ui.screens.trips.CompleteTripScreen
import com.delivce.kikapu.ui.screens.trips.CreateTripScreen
import com.delivce.kikapu.ui.screens.trips.TripDetailScreen
import com.delivce.kikapu.ui.screens.trips.TripPlanScreen

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
            HomeScreen(
                onViewAllTrips = { navController.navigate(AppDestinations.TRIPS.route) },
                onTripClick = { tripId -> navController.navigate(TripRoutes.activeTripRoute(tripId)) }
            )
        }
        composable(AppDestinations.TRIPS.route) {
            TripPlanScreen(
                onCreateTrip = { navController.navigate(TripRoutes.CREATE_TRIP) },
                onTripClick = { tripId -> navController.navigate(TripRoutes.activeTripRoute(tripId)) }
            )
        }
        composable(AppDestinations.ITEMS.route) {
            ItemsScreen()
        }
        composable(AppDestinations.PROGRESS.route) {
            ProgressScreen()
        }
        composable(AppDestinations.ME.route) {
            ProfileScreen(onLogout = onLogout)
        }
        composable(TripRoutes.CREATE_TRIP) {
            CreateTripScreen(
                onTripCreated = { tripId ->
                    navController.navigate(TripRoutes.activeTripRoute(tripId)) {
                        popUpTo(AppDestinations.TRIPS.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = TripRoutes.ACTIVE_TRIP,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) {
            TripDetailScreen(
                onBack = { navController.popBackStack() },
                onTripCancelled = {
                    navController.navigate(AppDestinations.TRIPS.route) {
                        popUpTo(AppDestinations.TRIPS.route) { inclusive = true }
                    }
                },
                onStartCompleteWizard = { tripId ->
                    navController.navigate(TripRoutes.completeTripRoute(tripId))
                }
            )
        }
        composable(
            route = TripRoutes.COMPLETE_TRIP,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) {
            CompleteTripScreen(
                onBack = { navController.popBackStack() },
                onCompleted = { navController.popBackStack() }
            )
        }
    }
}
