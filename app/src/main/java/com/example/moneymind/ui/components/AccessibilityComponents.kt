package com.example.moneymind.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.moneymind.R
import com.example.moneymind.utils.accessibilityHeading
import com.example.moneymind.utils.accessibilitySemantics
import com.example.moneymind.utils.accessibleClickable
import com.example.moneymind.utils.accessibleText

/**
 * Accessibility-aware components for MoneyMind app
 */

/**
 * Accessible button with improved TalkBack support
 */
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accessibilityLabel: String,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.accessibilitySemantics(
            description = accessibilityLabel,
            isEnabled = enabled
        ),
        enabled = enabled
    ) {
        content()
    }
}

/**
 * Accessible text field with improved TalkBack support
 */
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    accessibilityLabel: String = label,
    errorMessage: String? = null,
    isPassword: Boolean = false
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = errorMessage != null,
            modifier = Modifier
                .fillMaxWidth()
                .accessibilitySemantics(
                    description = accessibilityLabel,
                    errorMessage = errorMessage
                ),
            singleLine = true,
            // Handle password input if needed
            visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() 
                                else androidx.compose.ui.text.input.VisualTransformation.None
        )
        
        // Error message with accessibility support
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .accessibleText(
                        contentDesc = "Error: $errorMessage"
                    )
            )
        }
    }
}

/**
 * Accessible dropdown selector with improved TalkBack support
 */
@Composable
fun <T> AccessibleDropdown(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemToString: (T) -> String,
    label: String,
    modifier: Modifier = Modifier,
    accessibilityLabel: String = label
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // Button to expand dropdown
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .accessibilitySemantics(
                    description = "$accessibilityLabel. Current selection: ${selectedItem?.let { itemToString(it) } ?: "None"}"
                )
        ) {
            Text(
                text = selectedItem?.let { itemToString(it) } ?: label,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemToString(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    modifier = Modifier.accessibilitySemantics(
                        description = itemToString(item)
                    )
                )
            }
        }
    }
}

/**
 * Accessible card for transaction items
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibleTransactionCard(
    title: String,
    description: String,
    amount: String,
    date: String,
    isCredit: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactionType = if (isCredit) 
        stringResource(R.string.accessibility_credit_transaction)
    else 
        stringResource(R.string.accessibility_debit_transaction)
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .accessibilitySemantics(
                description = stringResource(
                    R.string.accessibility_transaction_item,
                    title,
                    amount,
                    date
                ),
                hint = "$transactionType, $description"
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.accessibilityHeading()
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCredit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Accessible top app bar with improved TalkBack support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibleTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier.accessibilityHeading()
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.accessibility_back_button),
                    modifier = Modifier.accessibleClickable(
                        contentDesc = stringResource(R.string.accessibility_back_button),
                        onClick = onBackClick
                    )
                )
            }
        },
        actions = {
            actions?.invoke()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

/**
 * Accessible scaffold with improved TalkBack support
 */
@Composable
fun AccessibleScaffold(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            AccessibleTopBar(
                title = title,
                onBackClick = onBackClick,
                actions = actions
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
} 