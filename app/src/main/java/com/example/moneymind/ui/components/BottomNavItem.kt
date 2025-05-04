package com.example.moneymind.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Savings

// Define the navigation items with their routes, icons, and labels
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Notes : BottomNavItem("notes", Icons.Outlined.EditNote, "Notes")
    object Savings : BottomNavItem("savings", Icons.Outlined.Savings, "Savings")
    object Chart : BottomNavItem("chart", Icons.Outlined.PieChart, "Chart")
    object Profile : BottomNavItem("profile", Icons.Outlined.AccountCircle, "Profile")
}

// List of all bottom navigation items (split with null for gap)
val bottomNavItems = listOf(
    BottomNavItem.Notes,
    BottomNavItem.Savings,
    null, // Gap for FAB
    BottomNavItem.Chart,
    BottomNavItem.Profile
)

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val backgroundColor = Color.Black
    val selectedColor = Color(0xFF81A38A)
    val unselectedColor = Color.Gray

    NavigationBar(
        containerColor = backgroundColor,
        tonalElevation = 0.dp,
        modifier = Modifier
            .height(64.dp),
        windowInsets = NavigationBarDefaults.windowInsets // ensures icons are centered
    ) {
        bottomNavItems.forEach { item ->
            when (item) {
                null -> Spacer(Modifier.weight(1f)) // dynamic space for FAB
                else -> AddItem(
                    navItem = item,
                    currentDestination = currentDestination,
                    navController = navController,
                    selectedColor = selectedColor,
                    unselectedColor = unselectedColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RowScope.AddItem(
    navItem: BottomNavItem,
    currentDestination: NavDestination?,
    navController: NavController,
    selectedColor: Color,
    unselectedColor: Color,
    modifier: Modifier = Modifier
) {
    val selected = currentDestination?.hierarchy?.any { it.route == navItem.route } == true

    NavigationBarItem(
        modifier = modifier,
        icon = {
            Icon(
                imageVector = navItem.icon,
                contentDescription = navItem.label,
                modifier = Modifier.size(24.dp)
            )
        },
        selected = selected,
        alwaysShowLabel = true,
        onClick = {
            navController.navigate(navItem.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor
        )
    )
} 