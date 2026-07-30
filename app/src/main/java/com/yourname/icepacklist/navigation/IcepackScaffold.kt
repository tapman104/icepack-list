package com.yourname.icepacklist.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

// ── Bottom-bar item descriptor ─────────────────────────────────────────────

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = Routes.Home.route,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    BottomNavItem(
        route = Routes.Search.route,
        label = "Search",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
    ),
    BottomNavItem(
        route = Routes.Settings.route,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    ),
)

// ── Scaffold ───────────────────────────────────────────────────────────────

/**
 * App-level scaffold that renders a [NavigationBar] at the bottom for the
 * three top-level destinations (Home, Search, Settings).
 *
 * The Detail destination intentionally has no bottom-bar entry — it is a
 * nested screen launched from the Home grid.
 *
 * @param content Slot lambda that receives the [Modifier] containing the
 *                scaffold inner-padding so the NavHost fills correctly.
 */
@Composable
fun IcepackScaffold(
    navController: NavController,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show the bottom bar on top-level routes
    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.route } == true
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color(0xFF0D0D0D),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF1C1C1E),
                    contentColor = Color(0xFFE50914),
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected =
                            currentDestination?.hierarchy?.any { it.route == item.route } == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination to avoid
                                    // building up a large back stack
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon
                                                  else item.unselectedIcon,
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFE50914),
                                selectedTextColor = Color(0xFFE50914),
                                unselectedIconColor = Color(0xFF888888),
                                unselectedTextColor = Color(0xFF888888),
                                indicatorColor = Color(0xFF2C2C2E),
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        content(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
