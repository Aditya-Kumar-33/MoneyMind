package com.example.moneymind.pages

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymind.data.AppDatabase
import com.example.moneymind.data.Transaction
import com.example.moneymind.data.TransactionCategories
import com.example.moneymind.data.TransactionType
import com.example.moneymind.ui.components.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import com.example.moneymind.utils.TransactionUpdateBroadcaster

/**
 * Special page ONLY for today's transactions to prevent any possible filtering issues.
 * This is completely isolated from other pages and transaction lists.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTransactionsPage(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Today's date in ISO format (yyyy-MM-dd)
    val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    
    // State for transactions
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Get TransactionDao directly
    val transactionDao = remember { AppDatabase.getDatabase(context).transactionDao() }
    
    // Load ONLY today's transactions using the transaction date filter
    LaunchedEffect(Unit) {
        isLoading = true
        Log.d("TodayTransactionsPage", "Loading today's transactions: $todayDate")
        
        try {
            withContext(Dispatchers.IO) {
                // Use the dedicated method for today's transactions
                transactions = transactionDao.getTodayTransactions(todayDate)
                Log.d("TodayTransactionsPage", "Loaded ${transactions.size} transactions for today")
            }
        } catch (e: Exception) {
            Log.e("TodayTransactionsPage", "Error loading today's transactions: ${e.message}", e)
            transactions = emptyList()
        } finally {
            isLoading = false
        }
    }

    // Formatter for display date/time
    val dateTimeFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a") }
    val displayDateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val today = remember { LocalDate.now() }

    // Add delete functionality
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun deleteTransaction(
        dao: com.example.moneymind.data.TransactionDao,
        transaction: Transaction
    ) {
        withContext(Dispatchers.IO) {
        try {
                dao.deleteTransaction(transaction.smsTimestamp)
                
                // Notify listeners about the transaction change
                TransactionUpdateBroadcaster.notifyTransactionChanged()
                
                Log.d("TodayTransactionsPage", "Deleted transaction: ${transaction.description}")
            } catch (e: Exception) {
                Log.e("TodayTransactionsPage", "Error deleting transaction: ${e.message}")
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            // Apply system bars padding to ensure content stays within visible area
            .systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Today's Transactions", 
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Date: ${today.format(displayDateFormatter)}", 
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Improve the fix button with a better icon and label
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                fixTodaysTransactions(transactionDao, todayDate)
                                
                                // Refresh transactions after fix
                                isLoading = true
                                withContext(Dispatchers.IO) {
                                    transactions = transactionDao.getTodayTransactions(todayDate)
                                }
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Yellow
                        ),
                        border = BorderStroke(1.dp, Color.Yellow.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category, 
                                contentDescription = "Fix Dates",
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Fix Dates", fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1F1B)
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        // Add bottom padding to avoid overlapping with system UI
        bottomBar = {
            Spacer(modifier = Modifier.height(8.dp))
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = MoneyMindBackgroundBrush)
                .padding(paddingValues)
                // Add padding for IME (keyboard)
                .imePadding()
        ) {
            if (isLoading) {
                // Show loading indicator
                LoadingIndicator(message = "Loading today's transactions...")
            } else if (transactions.isEmpty()) {
                // Show message if no transactions
                EmptyTransactionsList(
                    message = "No transactions found for today (${today.format(displayDateFormatter)}).\nTry summarizing SMS first."
                )
            } else {
                // Display list using LazyColumn
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = transactions,
                        // Create a composite key with timestamp + amount to ensure uniqueness
                        key = { "${it.smsTimestamp}_${it.amount}_${it.description?.hashCode() ?: 0}" }
                    ) { transaction ->
                        // Use the existing TransactionListItem with normal styling
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            TransactionListItem(
                                transaction = transaction,
                                formatter = dateTimeFormatter,
                                onCategorySelected = { newCategory ->
                                    coroutineScope.launch {
                                        updateTransactionCategory(transactionDao, transaction, newCategory)
                                        
                                        // Refresh the transaction list after category update
                                        withContext(Dispatchers.IO) {
                                            // Re-fetch today's transactions
                                            transactions = transactionDao.getTodayTransactions(todayDate)
                                            Log.d("TodayTransactionsPage", "Re-loaded ${transactions.size} transactions after category update")
                                        }
                                    }
                                },
                                onDeleteRequested = { transaction ->
                                    coroutineScope.launch {
                                        deleteTransaction(transactionDao, transaction)
                                        
                                        // Refresh transactions after deletion
                                        withContext(Dispatchers.IO) {
                                            transactions = transactionDao.getTodayTransactions(todayDate)
                                            Log.d("TodayTransactionsPage", "Refreshed transactions after deletion: ${transactions.size} remaining")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper function to update transaction category (copied to keep isolation)
@RequiresApi(Build.VERSION_CODES.O)
private suspend fun updateTransactionCategory(
    dao: com.example.moneymind.data.TransactionDao,
    transaction: Transaction,
    newCategory: String
) {
    withContext(Dispatchers.IO) {
        try {
            dao.updateTransactionCategory(
                transaction.transactionDate,
                transaction.smsTimestamp,
                newCategory
            )
            Log.d("TodayTransactionsPage", "Updated category for transaction: $newCategory")
            
            // Notify listeners about the category change
            TransactionUpdateBroadcaster.notifyCategoryChanged(transaction.smsTimestamp)
        } catch (e: Exception) {
            Log.e("TodayTransactionsPage", "Error updating category: ${e.message}")
        }
    }
}

// Helper function to fix transactions that incorrectly have today's date
@RequiresApi(Build.VERSION_CODES.O)
private suspend fun fixTodaysTransactions(
    dao: com.example.moneymind.data.TransactionDao,
    todayDate: String
) {
    withContext(Dispatchers.IO) {
        try {
            // Get today's transactions by timestamp
            val todaysTransactions = dao.getTodayTransactions(todayDate)
            Log.d("TodayTransactionsPage", "Found ${todaysTransactions.size} transactions for today")
            
            // For each transaction, use its SMS timestamp to get a more accurate date
            var fixedCount = 0
            
            todaysTransactions.forEach { transaction ->
                // Get date from SMS timestamp
                val smsDate = Instant.ofEpochMilli(transaction.smsTimestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                
                // Use SMS date minus 1 day as a better estimate (transactions usually happened before SMS)
                val adjustedDate = smsDate.minusDays(1)
                val newDateStr = adjustedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                // Only update if the new date is different from today
                if (newDateStr != todayDate) {
                    dao.updateTransactionDate(transaction.smsTimestamp, newDateStr)
                    fixedCount++
                }
            }
            
            Log.d("TodayTransactionsPage", "Fixed $fixedCount transactions with incorrect dates")
        } catch (e: Exception) {
            Log.e("TodayTransactionsPage", "Error fixing transaction dates: ${e.message}", e)
        }
    }
} 