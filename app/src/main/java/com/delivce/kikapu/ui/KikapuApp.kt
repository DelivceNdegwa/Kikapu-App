package com.delivce.kikapu.ui

import android.graphics.Rect
import android.os.Build
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
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

    // Bottom-nav items sit flush against the screen edge under enableEdgeToEdge(); on
    // gesture-nav devices the leftmost item (Home) can have taps swallowed by the system's
    // back-swipe zone. Excluding each item's bounds from gesture detection fixes that.
    val view = LocalView.current
    val density = LocalDensity.current
    val exclusionInflatePx = with(density) { 24.dp.toPx() }
    val navItemBounds = remember { mutableStateMapOf<AppDestinations, Rect>() }
    SideEffect {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.systemGestureExclusionRects = navItemBounds.values.toList()
        }
    }

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
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.label,
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                val bounds = coordinates.boundsInWindow()
                                navItemBounds[destination] = Rect(
                                    (bounds.left - exclusionInflatePx).toInt().coerceAtLeast(0),
                                    (bounds.top - exclusionInflatePx).toInt().coerceAtLeast(0),
                                    (bounds.right + exclusionInflatePx).toInt(),
                                    (bounds.bottom + exclusionInflatePx).toInt()
                                )
                            }
                        )
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
