package com.example.moneymind.pages

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.List // Icon for view transactions
import androidx.compose.material.icons.filled.Summarize // Icon for summarize
import androidx.compose.material.icons.filled.Category // Icon for uncategorized transactions
import androidx.compose.ui.draw.clip // For rounding button corners
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel // Import viewModel
import androidx.navigation.NavController
import com.example.moneymind.AuthViewModel // Keep if used
import com.example.moneymind.SavingsViewModel
import com.example.moneymind.SavingsViewModelFactory // Import Factory
import kotlinx.coroutines.delay
import com.example.moneymind.data.TransactionCategories
import com.example.moneymind.data.Transaction
import com.example.moneymind.util.SmsTransactionParser
import com.example.moneymind.ui.components.MoneyMindBackgroundBrush

// Define route for transaction list page
const val TRANSACTION_LIST_ROUTE = "transaction_list"
// Define special route for today's transactions
const val TODAYS_TRANSACTIONS_ROUTE = "todays_transactions"

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Savings(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel, // Keep if needed
    // Use factory to get ViewModel instance
    savingsViewModel: SavingsViewModel = viewModel(
        factory = SavingsViewModelFactory(LocalContext.current)
    )
) {
    // Get selected date from ViewModel for consistency
    val selectedDate by savingsViewModel.selectedDate.collectAsState()
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy") // Use full year
    val context = LocalContext.current

    val isLoading by savingsViewModel.isLoading.collectAsState()
    val summaryStatus by savingsViewModel.summaryStatus.collectAsState()
    
    // We don't need this effect anymore since we're using the date from the ViewModel
    // LaunchedEffect(selectedDate) {
    //     savingsViewModel.updateSelectedDate(selectedDate)
    // }

    // Remember DatePickerDialog
    val datePickerDialog = remember {
        DatePickerDialog(
            context, { _, year, month, dayOfMonth ->
                // Update ViewModel directly when date changes
                val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                savingsViewModel.updateSelectedDate(newDate)
            },
            selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth
        )
    }

    // Permission Launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "SMS Permission Granted", Toast.LENGTH_SHORT).show()
            savingsViewModel.startSmsSummarization(context.contentResolver)
        } else {
            Toast.makeText(context, "SMS Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to check and request permission
    fun checkAndRequestSmsPermission() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED -> {
                savingsViewModel.startSmsSummarization(context.contentResolver)
            }
            // Optional: Add shouldShowRequestPermissionRationale case here if needed
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
            }
        }
    }

    // Show Toast for summary status changes
    LaunchedEffect(summaryStatus) {
        summaryStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            delay(3500) // Show toast a bit longer
            savingsViewModel.clearSummaryStatus() // Clear after showing
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings Overview", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1F1B) // Match list page app bar
                )
                // Add Actions here if needed
            )
        },
        containerColor = Color.Transparent // Let Box handle background
    ) { paddingValues ->
        Box( // Use Box for background gradient
            modifier = Modifier
                .fillMaxSize()
                .background(brush = MoneyMindBackgroundBrush)
                .padding(paddingValues) // Apply scaffold padding
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp), // Overall padding
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between elements
            ) {

                // --- Date Selector Row ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly, // Distribute space
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp) // Space below date selector
                    // Optional: Add a divider below
                    // .drawBehind { ... drawLine ... }
                ) {
                    IconButton(onClick = { savingsViewModel.updateSelectedDate(selectedDate.minusDays(1)) }) {
                        Icon(Icons.Filled.ChevronLeft, "Previous Day", tint = Color.White)
                    }
                    Text( // Make date clickable
                        text = selectedDate.format(dateFormatter),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    )
                    IconButton(onClick = { savingsViewModel.updateSelectedDate(selectedDate.plusDays(1)) }) {
                        Icon(Icons.Filled.ChevronRight, "Next Day", tint = Color.White)
                    }
                }

                Divider(color = Color.Gray.copy(alpha = 0.5f), thickness = 1.dp) // Visual separator

                Spacer(modifier = Modifier.height(10.dp)) // Add some space

                // --- Action Buttons Row ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally) // Center buttons with space
                ) {
                    // Summarise Button
                    ActionButton(
                        text = "Summarise SMS",
                        icon = Icons.Filled.Summarize,
                        onClick = { checkAndRequestSmsPermission() },
                        isLoading = isLoading,
                        enabled = !isLoading // Disable when loading
                    )

                    // View All Transactions Button
                    ActionButton(
                        text = "View All",
                        icon = Icons.AutoMirrored.Filled.List,
                        onClick = { 
                            // Check if it's today's date
                            if (selectedDate.equals(LocalDate.now())) {
                                // Use special route for today's transactions
                                navController.navigate(TODAYS_TRANSACTIONS_ROUTE)
                            } else {
                                // For other dates, use the regular route with date parameter
                                navController.navigate("$TRANSACTION_LIST_ROUTE/${selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                            }
                        },
                        enabled = !isLoading // Optionally disable if summarising
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- Placeholder for future content ---
                // You can add charts, summaries for the selected date etc. here later
                Text(
                    "Summary for ${selectedDate.format(dateFormatter)} (TODO)",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                
                // Show uncategorized transactions notice
                val uncategorizedCount by savingsViewModel.uncategorizedCount.collectAsState()
                if (uncategorizedCount > 0) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    UncategorizedTransactionsAlert(
                        count = uncategorizedCount,
                        onClick = {
                            // Navigate to transaction list with current date
                            navController.navigate("$TRANSACTION_LIST_ROUTE/${selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                        }
                    )
                }

            } // End Column
        } // End Box for Background
    } // End Scaffold
}

@Composable
fun UncategorizedTransactionsAlert(
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xEE992222) // Dark red background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = "Uncategorized transactions",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Uncategorized Transactions",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "You have $count transaction${if (count > 1) "s" else ""} that need${if (count == 1) "s" else ""} to be categorized",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = count.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

// Reusable Action Button Composable
@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp), // Consistent height
        shape = MaterialTheme.shapes.medium, // Rounded corners
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = LocalContentColor.current, // Use button's content color
                    strokeWidth = 2.dp
                )
            } else {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
            Text(text = text, fontWeight = FontWeight.Medium)
        }
    }
}