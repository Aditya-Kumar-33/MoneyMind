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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Savings
import com.example.moneymind.R

// Define the navigation items with their routes, icons, and string resource IDs
sealed class BottomNavItem(
    val route: String, 
    val icon: ImageVector, 
    val labelResId: Int
) {
    object Notes : BottomNavItem("notes", Icons.Outlined.EditNote, R.string.nav_notes)
    object Savings : BottomNavItem("savings", Icons.Outlined.Savings, R.string.nav_savings)
    object Chart : BottomNavItem("chart", Icons.Outlined.PieChart, R.string.nav_chart)
    object Profile : BottomNavItem("profile", Icons.Outlined.AccountCircle, R.string.nav_profile)
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

    // Force re-fetching of string resources for each tab label recomposition
    NavigationBar(
        containerColor = backgroundColor,
        tonalElevation = 0.dp,
        modifier = Modifier
            .height(80.dp)
            .semantics {
                contentDescription = "Bottom navigation bar with 4 items"
            },
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
    val selectionState = if (selected) stringResource(R.string.tab_selected) else stringResource(R.string.tab_not_selected)
    val label = stringResource(id = navItem.labelResId)
    
    val accessibilityLabel = stringResource(
        id = R.string.tab_accessibility_label,
        label,
        selectionState
    )

    // Force recomposition with proper label text when language changes
    NavigationBarItem(
        modifier = modifier.semantics {
            contentDescription = accessibilityLabel
        },
        icon = {
            Icon(
                imageVector = navItem.icon,
                contentDescription = label,
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