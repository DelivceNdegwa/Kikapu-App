package com.delivce.kikapu.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.delivce.kikapu.ui.navigation.AppDestinations
import com.delivce.kikapu.ui.navigation.HomeNavGraph
import com.delivce.kikapu.ui.navigation.TripRoutes

@Composable
fun KikapuApp(
    onLogout: () -> Unit = {},
    pendingTripId: String? = null,
    onDeepLinkHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(pendingTripId) {
        if (pendingTripId != null) {
            navController.navigate(TripRoutes.activeTripRoute(pendingTripId))
            onDeepLinkHandled()
        }
    }

    val suiteItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = Color.Transparent, 
        )
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                item(
                    icon = {
                        Icon(if (isSelected) destination.selectedIcon else destination.unselectedIcon, destination.label)
                    },
                    label = { Text(destination.label) },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = suiteItemColors
                )
            }
        }
    ) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
            HomeNavGraph(
                navController = navController,
                onLogout = onLogout,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
