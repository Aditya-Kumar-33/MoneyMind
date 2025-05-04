package com.example.moneymind

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
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

// Define the navigation items with their routes, icons, and labels
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Notes : BottomNavItem("notes", Icons.Default.Description, "Notes")
    object Savings : BottomNavItem("history", Icons.Default.History, "Savings")
    object Chart : BottomNavItem("chart", Icons.Default.PieChart, "Chart")
    object Profile : BottomNavItem("profile", Icons.Default.AccountCircle, "Profile")
}

// List of all bottom navigation items
val bottomNavItems = listOf(
    BottomNavItem.Notes,
    BottomNavItem.Savings,
    BottomNavItem.Chart,
    BottomNavItem.Profile
)

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Styling for bottom navigation
    val backgroundColor = Color.Black
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = Color.Gray

    NavigationBar(
        containerColor = backgroundColor,
        contentColor = selectedColor
    ) {
        bottomNavItems.forEach { item ->
            AddItem(
                navItem = item,
                currentDestination = currentDestination,
                navController = navController,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    navItem: BottomNavItem,
    currentDestination: NavDestination?,
    navController: NavController,
    selectedColor: Color,
    unselectedColor: Color
) {
    val selected = currentDestination?.hierarchy?.any { it.route == navItem.route } == true

    NavigationBarItem(
        icon = {
            Icon(
                imageVector = navItem.icon,
                contentDescription = navItem.label,
                modifier = Modifier.size(24.dp),
                tint = if (selected) selectedColor else unselectedColor
            )
        },
        label = {
            Text(
                text = navItem.label,
                color = if (selected) selectedColor else unselectedColor
            )
        },
        selected = selected,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            unselectedIconColor = unselectedColor,
            selectedTextColor = selectedColor,
            unselectedTextColor = unselectedColor
        ),
        onClick = {
            navController.navigate(navItem.route) {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }
        }
    )
}

// This is a floating action button for adding new entries
@Composable
fun AddButton() {
    // A green circular button with a + icon as shown in the image
    val greenColor = Color(0xFF7FBB92) // Matches the color in your example

    Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Add",
        tint = Color.White,
        modifier = Modifier.size(24.dp)
    )
}