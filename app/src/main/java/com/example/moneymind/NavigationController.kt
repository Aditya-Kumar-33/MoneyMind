package com.example.moneymind

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.moneymind.pages.*
import com.example.moneymind.accessibility.AccessibilityViewModel
import com.example.moneymind.language.LanguageViewModel
import com.example.moneymind.ui.components.BottomNavBar
import com.example.moneymind.ui.components.BottomNavItem
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.example.moneymind.data.AppDatabase
import com.example.moneymind.data.Transaction
import com.example.moneymind.ui.components.TransactionDialog

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationController(
    modifier: Modifier = Modifier, 
    authViewModel: AuthViewModel,
    accessibilityViewModel: AccessibilityViewModel,
    languageViewModel: LanguageViewModel
) {
    val navController = rememberNavController()
    
    // Observe auth state from ViewModel
    val authState by authViewModel.authState.observeAsState()

    // Determine if user is authenticated
    val isAuthenticated = authState is AuthState.Authenticated

    // Get app context for database access
    val context = LocalContext.current
    
    // Create TransactionViewModel
    val transactionDao = remember { AppDatabase.getDatabase(context).transactionDao() }
    val transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(transactionDao)
    )
    
    // State for transaction dialog
    var showTransactionDialog by remember { mutableStateOf(false) }

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
                if (isAuthenticated) {
                    BottomNavBar(navController)
                }
            },
            floatingActionButton = {
                if (isAuthenticated) {
                    FloatingActionButton(
                        onClick = { 
                            showTransactionDialog = true 
                        },
                        shape = CircleShape,
                        containerColor = Color(0xFF7FBB92),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp),
                        modifier = Modifier.size(64.dp).offset(y = 50.dp).semantics {
                            contentDescription = "Add new transaction or record"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            content = { innerPadding ->
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

                    // Main app screens with bottom navigation
                    composable(BottomNavItem.Notes.route) {
                        val application = LocalContext.current.applicationContext as Application
                        val notesViewModel: NotesViewModel = viewModel(
                            factory = NotesViewModelFactory(application)
                        )
                        NotesPage(modifier, navController, authViewModel, notesViewModel)
                    }
                    composable(BottomNavItem.Savings.route) {
                        Savings(modifier, navController, authViewModel)
                    }
                    composable(BottomNavItem.Chart.route) {
                        ChartPage(modifier, navController, authViewModel)
                    }
                    composable(BottomNavItem.Profile.route) {
                        ProfilePage(
                            modifier = modifier,
                            navController = navController,
                            authViewModel = authViewModel,
                            accessibilityViewModel = accessibilityViewModel,
                            languageViewModel = languageViewModel
                        )
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
                }
            }
        )
        
        // Show the transaction dialog if needed
        if (showTransactionDialog) {
            TransactionDialog(
                isVisible = true,
                onDismiss = { showTransactionDialog = false },
                onSave = { transaction ->
                    // Add transaction to database using ViewModel
                    transactionViewModel.addTransaction(transaction)
                }
            )
        }
    }
}