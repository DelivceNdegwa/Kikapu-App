package com.delivce.kikapu.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home_tab", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    TRIPS("trips_tab", "Trips", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    ITEMS("items_tab", "Items", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    ME("profile_tab", "Me", Icons.Filled.Person, Icons.Outlined.Person),
}
