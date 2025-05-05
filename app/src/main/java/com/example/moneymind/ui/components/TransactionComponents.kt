package com.example.moneymind.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.example.moneymind.data.Transaction
import com.example.moneymind.data.TransactionCategories
import com.example.moneymind.data.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Reusable components for transaction UI
 */

// Global background brush for consistent app background
val MoneyMindBackgroundBrush = Brush.verticalGradient(
  colors = listOf(
    Color(0xFF161C18),
    Color(0xFF080A07),
    Color(0xFF070906)
  )
)

// Composable for displaying a single transaction item
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionListItem(
  transaction: Transaction, 
  formatter: DateTimeFormatter,
  onCategorySelected: (String) -> Unit,
  onDeleteRequested: (Transaction) -> Unit = {}
) {
  var expanded by remember { mutableStateOf(false) }
  
  // Decide if we need to highlight this transaction as uncategorized
  val isUncategorized = transaction.category == TransactionCategories.UNCATEGORIZED
  
  // Card elevation and border for uncategorized items
  val elevation = if (isUncategorized) 4.dp else 2.dp
  
  val borderModifier = if (isUncategorized) {
    Modifier.border(1.dp, Color.Red.copy(alpha = 0.7f), shape = RoundedCornerShape(12.dp))
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

// Badge for displaying transaction category
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
    Icon(
      imageVector = Icons.Default.ArrowDropDown,
      contentDescription = "Select Category",
      tint = if (isUncategorized) Color.White else Color.LightGray,
      modifier = Modifier.size(14.dp)
    )
  }
}

// Helper function to format currency
@Composable
fun formatCurrency(amount: Double): String {
  return "₹${String.format(Locale.ENGLISH, "%.2f", amount)}"
}

// Loading indicator with message
@Composable
fun LoadingIndicator(message: String = "Loading...") {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier.fillMaxSize()
  ) {
    CircularProgressIndicator(color = Color.White)
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = message,
      color = Color.LightGray,
      fontSize = 14.sp
    )
  }
}

// Empty state for when no transactions are found
@Composable
fun EmptyTransactionsList(message: String) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier.fillMaxSize().padding(16.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Default.Category,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = Color.Gray.copy(alpha = 0.5f)
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = message,
        color = Color.Gray,
        fontSize = 16.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
    }
  }
} 