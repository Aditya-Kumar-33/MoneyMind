package com.example.moneymind.pages

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Import items extension
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.* // Use Material 3
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneymind.SavingsViewModel
import com.example.moneymind.SavingsViewModelFactory
import com.example.moneymind.data.Transaction
import com.example.moneymind.data.TransactionCategories
import com.example.moneymind.data.TransactionDao
import com.example.moneymind.data.AppDatabase
import com.example.moneymind.data.TransactionType
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import com.example.moneymind.ui.components.*

/**
 * Transaction list page showing transactions for a specific date.
 * This is kept strictly isolated from other screens to ensure proper filtering.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListPage(
  navController: NavController,
  dateString: String? = null,
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  
  // State for the current date and transactions
  val date = remember { 
    if (dateString != null) {
      try {
        LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
      } catch (e: Exception) {
        Log.e("TransactionListPage", "Error parsing date: $dateString", e)
        LocalDate.now()
      }
    } else {
      LocalDate.now()
    }
  }

  // State for transactions
  var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
  var isLoading by remember { mutableStateOf(true) }
  
  // Get TransactionDao directly
  val transactionDao = remember { AppDatabase.getDatabase(context).transactionDao() }
  
  // Load transactions for the specific date
  LaunchedEffect(date) {
    isLoading = true
    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    Log.d("TransactionListPage", "Loading transactions for date: $dateStr")
    
    try {
      withContext(Dispatchers.IO) {
        // Get transactions for this specific date string
        val transactionsForDate = transactionDao.getTransactionsByDate(dateStr)
        Log.d("TransactionListPage", "Loaded ${transactionsForDate.size} transactions for date $dateStr")
        transactions = transactionsForDate
      }
    } catch (e: Exception) {
      Log.e("TransactionListPage", "Error loading transactions: ${e.message}", e)
      transactions = emptyList()
    } finally {
      isLoading = false
    }
  }

  // Formatters for display
  val dateTimeFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a") }
  val displayDateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

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
              "Transactions", 
              color = Color.White,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              "Date: ${date.format(displayDateFormatter)}", 
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
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color(0xFF1A1F1B)
        )
      )
    },
    contentWindowInsets = WindowInsets.safeDrawing,
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
        LoadingIndicator(message = "Loading transactions...")
      } else if (transactions.isEmpty()) {
        // Show message if no transactions
        EmptyTransactionsList(
          message = "No transactions found for ${date.format(displayDateFormatter)}.\nTry another date or summarizing SMS first."
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
            // Create a composite key with timestamp + amount + description hash to ensure uniqueness
            key = { "${it.smsTimestamp}_${it.amount}_${it.description?.hashCode() ?: 0}" }
          ) { transaction ->
            TransactionListItem(
              transaction = transaction, 
              formatter = dateTimeFormatter,
              onCategorySelected = { newCategory ->
                coroutineScope.launch {
                  updateTransactionCategory(transactionDao, transaction, newCategory)
                  
                  // Refresh transactions after category update
                  withContext(Dispatchers.IO) {
                    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    // Re-fetch transactions for this specific date
                    transactions = transactionDao.getTransactionsByDate(dateStr)
                    Log.d("TransactionListPage", "Re-loaded ${transactions.size} transactions after category update")
                  }
                }
              },
              onDeleteRequested = {
                coroutineScope.launch {
                  deleteTransaction(transactionDao, it)
                }
              }
            )
          }
        }
      }
    }
  }
}

// Helper function to update transaction category
@RequiresApi(Build.VERSION_CODES.O)
private suspend fun updateTransactionCategory(
  dao: TransactionDao,
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
      Log.d("TransactionListPage", "Updated category for transaction: $newCategory")
    } catch (e: Exception) {
      Log.e("TransactionListPage", "Error updating category: ${e.message}")
    }
  }
}

// Helper function to delete a transaction
@RequiresApi(Build.VERSION_CODES.O)
private suspend fun deleteTransaction(
  dao: TransactionDao,
  transaction: Transaction
) {
  withContext(Dispatchers.IO) {
    try {
      dao.deleteTransaction(transaction.smsTimestamp)
      Log.d("TransactionListPage", "Deleted transaction: ${transaction.smsTimestamp}")
    } catch (e: Exception) {
      Log.e("TransactionListPage", "Error deleting transaction: ${e.message}")
    }
  }
}

// Composable for a single transaction item
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionListItem(
  transaction: Transaction, 
  formatter: DateTimeFormatter,
  onCategorySelected: (String) -> Unit,
  onDeleteRequested: (Transaction) -> Unit = {} // Add delete callback
) {
  var expanded by remember { mutableStateOf(false) }
  
  // Decide if we need to highlight this transaction as uncategorized
  val isUncategorized = transaction.category == TransactionCategories.UNCATEGORIZED
  
  // Card elevation and border for uncategorized items
  val elevation = if (isUncategorized) 4.dp else 2.dp
  
  val borderModifier = if (isUncategorized) {
    Modifier.border(1.dp, Color.Red.copy(alpha = 0.7f), shape = MaterialTheme.shapes.medium)
  } else {
    Modifier
  }
  
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .then(borderModifier)
      .padding(vertical = 2.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    colors = CardDefaults.cardColors(
      containerColor = if (isUncategorized) 
        Color(0xAAA03030) // Reddish background for uncategorized
      else 
        Color(0xAA2D332F) // Normal background
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(
      modifier = Modifier.clickable { expanded = true } // Make the whole card clickable
    ) {
      Row(
        modifier = Modifier
          .padding(horizontal = 16.dp, vertical = 12.dp)
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Space out columns
      ) {
        // Left Column: Description and Date
        Column(
          modifier = Modifier
            .weight(1f)
            .padding(end = 8.dp)
        ) {
          Text(
            // Display description or fallback
            text = transaction.description ?: "Unknown Transaction",
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color.White,
            maxLines = 2, // Limit lines
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Date chip
            Surface(
              color = Color(0xFF444444),
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier.padding(vertical = 2.dp)
            ) {
              Text(
                text = transaction.transactionDate,
                fontSize = 11.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            
            // Add delete button if needed
            if (transaction.transactionDate == LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) {
              IconButton(
                onClick = { onDeleteRequested(transaction) },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Delete",
                  tint = Color.Red.copy(alpha = 0.8f),
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }

        // Right Column: Amount and Type
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            // Format amount with currency
            text = formatCurrency(transaction.amount),
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            // Color based on type
            color = if (transaction.type == TransactionType.CREDIT) Color(0xFF4CAF50) else Color(0xFFE57373) // Green/Red
          )
          
          // Transaction type chip
          Surface(
            color = if (transaction.type == TransactionType.CREDIT) 
              Color(0xFF0D5B23).copy(alpha = 0.7f) else Color(0xFF8B3131).copy(alpha = 0.7f),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = transaction.type.name, // CREDIT or DEBIT
              fontSize = 11.sp,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          
          Spacer(modifier = Modifier.height(2.dp))
          
          // Category Badge
          CategoryBadge(
            category = transaction.category,
            isUncategorized = isUncategorized,
            onClick = { expanded = true }
          )
        }
      }
      
      // Dropdown for category selection
      DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        properties = PopupProperties(focusable = true),
        modifier = Modifier
          .background(Color(0xFF2A2A2A))
          .padding(8.dp)
      ) {
        Text(
          "Select Category:",
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(8.dp)
        )
        
        // Get appropriate categories for this transaction type
        val categories = TransactionCategories.getCategoriesForType(transaction.type)
        
        // Add each category as a dropdown item
        categories.forEach { category ->
          DropdownMenuItem(
            text = { Text(category, color = Color.White) },
            onClick = {
              // Close dropdown
              expanded = false
              
              // Call the callback to update the category
              onCategorySelected(category)
            },
            // Highlight selected category
            modifier = if (transaction.category == category) {
              Modifier.background(Color(0xFF444444))
            } else {
              Modifier
            }
          )
        }
      }
    }
  }
}

@Composable
fun CategoryBadge(
  category: String,
  isUncategorized: Boolean,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .background(
        color = if (isUncategorized) Color.Red.copy(alpha = 0.3f) else Color(0xFF444444),
        shape = RoundedCornerShape(16.dp)
      )
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .clickable(onClick = onClick),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      imageVector = Icons.Default.Category,
      contentDescription = "Category",
      tint = if (isUncategorized) Color.Red.copy(alpha = 0.9f) else Color.LightGray,
      modifier = Modifier.size(12.dp)
    )
    Text(
      text = category,
      fontSize = 11.sp,
      fontWeight = if (isUncategorized) FontWeight.Medium else FontWeight.Normal,
      color = if (isUncategorized) Color.White else Color.LightGray
    )
    Spacer(modifier = Modifier.width(2.dp))
    Icon(
      imageVector = Icons.Default.ArrowDropDown,
      contentDescription = "Select Category",
      tint = if (isUncategorized) Color.White else Color.LightGray,
      modifier = Modifier.size(14.dp)
    )
  }
}

// Helper function to format currency (simple version)
@Composable
private fun formatCurrency(amount: Double): String {
  // You might want a more robust currency formatting solution
  return "₹${String.format(Locale.ENGLISH, "%.2f", amount)}"
}