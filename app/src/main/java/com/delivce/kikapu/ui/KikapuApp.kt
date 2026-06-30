package com.delivce.kikapu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroBorder
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.foundation.retroShadow
import com.delivce.kikapu.ui.navigation.AppDestinations
import com.delivce.kikapu.ui.navigation.HomeNavGraph

@Composable
fun KikapuApp(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
                        if (destination == AppDestinations.ADD) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(48.dp)
                                    .retroBorder(RetroTheme.BorderColor, shape=CircleShape, borderWidth = 2.dp)
                                    .retroShadow(RetroTheme.ShadowColor, shape=CircleShape, offset = 3.dp)
                            ) {
                                Icon(destination.selectedIcon, destination.label, tint = Color.White, modifier = Modifier.padding(8.dp))
                            }
                        } else {
                            Icon(if (isSelected) destination.selectedIcon else destination.unselectedIcon, destination.label)
                        }
                    },
                    label = { if (destination != AppDestinations.ADD) Text(destination.label) },
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

@Composable
fun ProfileContent(modifier: Modifier = Modifier, onLogout: () -> Unit) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Profile Screen", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .retroFrame(RetroTheme.BorderColor, RetroTheme.ShadowColor, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(12.dp))
                .clickable { onLogout() },
            contentAlignment = Alignment.Center
        ) {
            Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
