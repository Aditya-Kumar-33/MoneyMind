package com.example.moneymind.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.moneymind.R
import com.example.moneymind.data.Transaction
import com.example.moneymind.data.TransactionCategories
import com.example.moneymind.data.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    // State for all form fields
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var accountIdentifier by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf(TransactionType.DEBIT) } // Default to expense
    
    // Category dropdown state
    var selectedCategory by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }
    
    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    
    // Get today's date in ISO format
    val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val selectedDate = remember(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        } ?: LocalDate.now()
    }
    
    // Validation state
    var amountError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    
    // Focus requester for amount field
    val amountFocusRequester = remember { FocusRequester() }
    
    // Reset form when dialog opens
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Reset form fields
            amount = ""
            description = ""
            accountIdentifier = ""
            transactionType = TransactionType.DEBIT
            selectedCategory = ""
            amountError = false
            categoryError = false
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1F1B))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = Color(0xFF81A38A)
                        )
                        Text(
                            text = stringResource(id = R.string.add_transaction),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
                
                Divider(
                    color = Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Transaction Type Selection
                Text(
                    text = stringResource(id = R.string.transaction_type),
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { transactionType = TransactionType.DEBIT }
                            .weight(1f)
                    ) {
                        RadioButton(
                            selected = transactionType == TransactionType.DEBIT,
                            onClick = { transactionType = TransactionType.DEBIT },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF81A38A),
                                unselectedColor = Color.Gray
                            )
                        )
                        Text(
                            text = stringResource(id = R.string.expense),
                            color = Color.White,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { transactionType = TransactionType.CREDIT }
                            .weight(1f)
                    ) {
                        RadioButton(
                            selected = transactionType == TransactionType.CREDIT,
                            onClick = { transactionType = TransactionType.CREDIT },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF81A38A),
                                unselectedColor = Color.Gray
                            )
                        )
                        Text(
                            text = stringResource(id = R.string.transaction_income),
                            color = Color.White,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Amount Field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        // Only allow digits and decimal point
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amount = it
                            amountError = it.isEmpty() || it.toDoubleOrNull() == null
                        }
                    },
                    label = { Text(stringResource(id = R.string.amount), color = Color.Gray) },
                    isError = amountError,
                    supportingText = {
                        if (amountError) {
                            Text(
                                text = stringResource(id = R.string.amount_error),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    leadingIcon = {
                        Text(
                            text = "₹",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(amountFocusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF81A38A),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF81A38A)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date Field
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.date), color = Color.Gray) },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF81A38A),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White
                    ),
                    enabled = false
                )
                
                // Date Picker Dialog
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            Button(
                                onClick = { showDatePicker = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF81A38A)
                                )
                            ) {
                                Text(stringResource(id = R.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text(stringResource(id = R.string.cancel))
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category Selection
                Box {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(id = R.string.category), color = Color.Gray) },
                        isError = categoryError,
                        supportingText = {
                            if (categoryError) {
                                Text(
                                    text = stringResource(id = R.string.category_error),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select category",
                                tint = Color.White
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCategory = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF81A38A),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.White
                        ),
                        enabled = false
                    )
                    
                    DropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color(0xFF212121))
                    ) {
                        // Get categories based on transaction type
                        val categories = TransactionCategories.getCategoriesForType(transactionType)
                        
                        Text(
                            text = stringResource(id = R.string.select_category),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                        
                        Divider(color = Color.Gray.copy(alpha = 0.3f))
                        
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = Color.White) },
                                onClick = {
                                    selectedCategory = category
                                    categoryError = false
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description Field (Optional)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(id = R.string.description), color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF81A38A),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF81A38A)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Account Identifier Field (Optional)
                OutlinedTextField(
                    value = accountIdentifier,
                    onValueChange = { accountIdentifier = it },
                    label = { Text(stringResource(id = R.string.account_id), color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF81A38A),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF81A38A)
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(stringResource(id = R.string.cancel), color = Color.Gray)
                    }
                    
                    Button(
                        onClick = {
                            // Validate required fields
                            val amountValue = amount.toDoubleOrNull()
                            
                            if (amountValue == null) {
                                amountError = true
                                return@Button
                            }
                            
                            if (selectedCategory.isEmpty()) {
                                categoryError = true
                                return@Button
                            }
                            
                            // Create new transaction
                            val transaction = Transaction(
                                amount = amountValue,
                                type = transactionType,
                                transactionDate = selectedDate.format(dateFormatter),
                                smsTimestamp = System.currentTimeMillis(),
                                description = description.takeIf { it.isNotEmpty() },
                                accountIdentifier = accountIdentifier.takeIf { it.isNotEmpty() },
                                category = selectedCategory
                            )
                            
                            // Save transaction
                            onSave(transaction)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF81A38A)
                        )
                    ) {
                        Text(stringResource(id = R.string.save))
                    }
                }
            }
        }
    }
} 