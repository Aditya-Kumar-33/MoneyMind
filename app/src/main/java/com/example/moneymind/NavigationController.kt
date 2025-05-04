package com.example.moneymind

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moneymind.pages.ChartPage
import com.example.moneymind.pages.Login
import com.example.moneymind.pages.NotesPage
import com.example.moneymind.pages.ProfilePage
import com.example.moneymind.pages.Savings
import com.example.moneymind.pages.SignUp
import com.example.moneymind.pages.Welcome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationController(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    // Observe auth state from ViewModel
    val authState by authViewModel.authState.observeAsState()

    // Determine if user is authenticated
    val isAuthenticated = authState is AuthState.Authenticated

    // Check current authentication status when the composable is first created
    LaunchedEffect(key1 = true) {
        authViewModel.checkAuthStatus()
    }

    // Navigate based on auth state changes
    LaunchedEffect(key1 = authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // If coming from login or signup, navigate to main app section
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute == "login" || currentRoute == "signup" || currentRoute == "welcome") {
                    navController.navigate(BottomNavItem.Notes.route) {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                // Navigate to welcome screen if not authenticated
                navController.navigate("welcome") {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {} // Handle loading and error states as needed
        }
    }

    // Content with custom FAB placement
    Box(modifier = Modifier.zIndex(1f)) {
        Scaffold(
            bottomBar = {
                // Only show bottom nav bar when authenticated
                if (isAuthenticated) {
                    BottomNavBar(navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "welcome",
                modifier = Modifier.padding(innerPadding)
            ) {
                // Auth screens
                composable("welcome") {
                    Welcome(modifier, navController, authViewModel)
                }
                composable("login") {
                    Login(modifier, navController, authViewModel)
                }
                composable("signup") {
                    SignUp(modifier, navController, authViewModel)
                }

                // Main app screens (post-authentication)
                composable(BottomNavItem.Notes.route) {
                    NotesPage(modifier, navController, authViewModel)
                }
                composable(BottomNavItem.History.route) {
                    Savings(modifier, navController, authViewModel)
                }
                composable(BottomNavItem.Chart.route) {
                    ChartPage(modifier, navController, authViewModel)
                }
                composable(BottomNavItem.Profile.route) {
                    ProfilePage(modifier, navController, authViewModel)
                }
            }
        }

        // Centered FAB on top of the navigation bar
        if (isAuthenticated) {
            FloatingActionButton(
                onClick = { /* Add your action here */ },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp)
                    .zIndex(2f),
                shape = CircleShape,
                containerColor = Color(0xFF7FBB92), // Green color from the example
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                AddButton()
            }
        }
    }
}