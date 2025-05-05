package com.example.moneymind.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moneymind.R
import com.example.moneymind.accessibility.rememberAccessibilityManager
import com.example.moneymind.data.Transaction
import com.example.moneymind.data.TransactionCategories
import com.example.moneymind.data.TransactionType
import com.example.moneymind.utils.accessibilityHeading
import com.example.moneymind.utils.accessibilitySemantics
import com.example.moneymind.utils.accessibleClickable

/**
 * Accessible version of transaction item card
 * This can be used as a drop-in replacement for the existing transaction card
 * with enhanced accessibility features
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AccessibleTransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onCategoryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibilityManager = rememberAccessibilityManager()
    
    // Format transaction amount with proper currency symbol
    val formattedAmount = String.format("₹ %.2f", transaction.amount)
    
    // Transaction type for accessibility announcements
    val transactionType = if (transaction.type == TransactionType.CREDIT) 
        stringResource(R.string.accessibility_credit_transaction)
    else 
        stringResource(R.string.accessibility_debit_transaction)
    
    // Detailed description for screen readers
    val accessibilityDescription = stringResource(
        R.string.accessibility_transaction_item,
        transaction.description ?: "Unknown transaction",
        formattedAmount,
        transaction.transactionDate
    )
    
    // State for category dropdown
    var showCategoryDropdown by remember { mutableStateOf(false) }
    
    // Background color based on transaction type
    val backgroundColor = when (transaction.type) {
        TransactionType.CREDIT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        TransactionType.DEBIT -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    }

    Card(
        onClick = {
            // Announce what was clicked when TalkBack is active
            if (accessibilityManager.isTalkBackEnabled()) {
                accessibilityManager.announce("Selected $transactionType, $formattedAmount")
            }
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .accessibilitySemantics(
                description = accessibilityDescription,
                hint = "Double tap to view details"
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            // Transaction description as heading for better navigation
            Text(
                text = transaction.description ?: "Transaction",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.accessibilityHeading()
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Transaction amount with color based on type
            Text(
                text = formattedAmount,
                style = MaterialTheme.typography.bodyLarge,
                color = if (transaction.type == TransactionType.CREDIT) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date - for better accessibility, format clearly
            Text(
                text = "Date: ${transaction.transactionDate}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Optional account information if available
            if (!transaction.accountIdentifier.isNullOrEmpty()) {
                Text(
                    text = "Account: ${transaction.accountIdentifier}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Category selector with accessibility support
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .accessibilitySemantics(
                        description = "Category: ${transaction.category}",
                        hint = "Double tap to change category"
                    )
                    .accessibleClickable(
                        contentDesc = "Change transaction category",
                        onClick = { showCategoryDropdown = true }
                    )
            ) {
                Text(
                    text = "Category: ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
                
                DropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    // Get appropriate categories for this transaction type
                    val categories = TransactionCategories.getCategoriesForType(transaction.type)
                    
                    // Always add the uncategorized option
                    DropdownMenuItem(
                        text = { Text(TransactionCategories.UNCATEGORIZED) },
                        onClick = {
                            onCategoryChanged(TransactionCategories.UNCATEGORIZED)
                            showCategoryDropdown = false
                            
                            // Announce the change to screen reader
                            accessibilityManager.announce("Category changed to uncategorized")
                        }
                    )
                    
                    // Add all other categories
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                onCategoryChanged(category)
                                showCategoryDropdown = false
                                
                                // Announce the change to screen reader
                                accessibilityManager.announce("Category changed to $category")
                            },
                            modifier = Modifier.accessibilitySemantics(
                                description = "Set category to $category"
                            )
                        )
                    }
                }
            }
        }
    }
} 