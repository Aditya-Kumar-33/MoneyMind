package com.example.moneymind

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.moneymind.pages.Login
import com.example.moneymind.pages.SignUp
import com.example.moneymind.AuthViewModel
import com.example.moneymind.accessibility.AccessibilityViewModel
import com.example.moneymind.pages.AccessibilitySettingsPage
import com.example.moneymind.pages.Savings
import com.example.moneymind.pages.TODAYS_TRANSACTIONS_ROUTE
import com.example.moneymind.pages.TRANSACTION_LIST_ROUTE
import com.example.moneymind.pages.TransactionListPage
import com.example.moneymind.pages.TodayTransactionsPage
import com.example.moneymind.pages.Welcome

// Route constant for accessibility settings
const val ACCESSIBILITY_SETTINGS_ROUTE = "accessibility_settings"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationController(
    modifier: Modifier = Modifier, 
    authViewModel: AuthViewModel,
    accessibilityViewModel: AccessibilityViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome", builder = {
        composable("welcome"){
            Welcome(modifier, navController, authViewModel)
        }
        composable("login"){
            Login(modifier, navController, authViewModel)
        }
        composable("signup"){
            SignUp(modifier, navController, authViewModel)
        }
        composable("savings"){
            Savings(modifier, navController, authViewModel)
        }

        // Route for all transactions (no specific date)
        composable(TRANSACTION_LIST_ROUTE) {
            TransactionListPage(
                navController = navController
                // Use default date (today)
            )
        }
        
        // Special route just for today's transactions
        composable(TODAYS_TRANSACTIONS_ROUTE) {
            TodayTransactionsPage(navController = navController)
        }
        
        // Route with date parameter
        composable("$TRANSACTION_LIST_ROUTE/{date}") { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            TransactionListPage(
                navController = navController,
                dateString = date
            )
        }
        
        // Accessibility settings route
        composable(ACCESSIBILITY_SETTINGS_ROUTE) {
            AccessibilitySettingsPage(
                navController = navController,
                accessibilityViewModel = accessibilityViewModel
            )
        }
    })
}