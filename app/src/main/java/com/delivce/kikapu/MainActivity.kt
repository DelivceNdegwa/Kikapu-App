package com.delivce.kikapu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroBorder
import com.delivce.kikapu.ui.foundation.retroShadow
import com.delivce.kikapu.ui.screens.ChecklistScreen
import com.delivce.kikapu.ui.screens.HomeScreen
import com.delivce.kikapu.ui.screens.TripPlanScreen
import com.delivce.kikapu.ui.theme.KikapuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KikapuTheme(darkTheme = false) {
                KikapuApp()
            }
        }
    }
}

@Composable
fun KikapuApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    // Define standard item colors to remove the default M3 "pill" indicator
    // Fix: Use 'indicatorColor' instead of 'selectedIndicatorColor'
    val suiteItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = Color.Transparent, 
        )
    )

    // Fix: Hoist remember outside the navigationSuiteItems lambda
    val addInteractionSource = remember { MutableInteractionSource() }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                val isSelected = currentDestination == destination
                item(
                    icon = {
                        if (destination == AppDestinations.ADD) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .size(48.dp)
                                    .retroBorder(
                                        borderColor = RetroTheme.BorderColor,
                                        borderWidth = 2.dp,
                                        shape = CircleShape
                                    )
                                    .retroShadow(
                                        shadowColor = RetroTheme.ShadowColor,
                                        shape = CircleShape,
                                        offset = 3.dp
                                    )
                            ) {
                                Icon(
                                    imageVector = destination.selectedIcon,
                                    contentDescription = destination.label,
                                    tint = Color.White,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.label
                            )
                        }
                    },
                    label = { 
                        if (destination != AppDestinations.ADD) {
                            Text(destination.label) 
                        }
                    },
                    selected = isSelected,
                    onClick = { currentDestination = destination },
                    colors = suiteItemColors,
                    interactionSource = if (destination == AppDestinations.ADD) addInteractionSource else null
                )
            }
        },
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surface,
            navigationBarContentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            val modifier = Modifier.padding(innerPadding)
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(modifier)
                AppDestinations.TRIPS -> TripPlanScreen(modifier)
                AppDestinations.CHECKLIST -> ChecklistScreen(modifier)
                AppDestinations.ME -> Text("Profile Screen", modifier)
                AppDestinations.ADD -> HomeScreen(modifier)
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    TRIPS("Trips", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    ADD("Add", Icons.Default.Add, Icons.Default.Add),
    CHECKLIST("Spend", Icons.Filled.List, Icons.Outlined.List),
    ME("Me", Icons.Filled.Person, Icons.Outlined.Person),
}
