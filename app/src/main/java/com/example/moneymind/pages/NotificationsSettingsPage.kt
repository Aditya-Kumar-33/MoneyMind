package com.example.moneymind.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymind.R
import com.example.moneymind.utils.accessibilityHeading

// Route constant for notifications settings
const val NOTIFICATIONS_SETTINGS_ROUTE = "notifications_settings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsPage(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    // Notification states (would typically come from a ViewModel)
    var allNotificationsEnabled by remember { mutableStateOf(true) }
    var transactionNotifications by remember { mutableStateOf(true) }
    var budgetNotifications by remember { mutableStateOf(true) }
    var reminderNotifications by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.profile_notifications),
                        modifier = Modifier.accessibilityHeading()
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.semantics {
                            contentDescription = "Back"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Text(
                        text = stringResource(id = R.string.profile_notifications),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .semantics { heading() }
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Master toggle
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Enable All Notifications",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .accessibilityHeading()
                            )
                            
                            Switch(
                                checked = allNotificationsEnabled,
                                onCheckedChange = { 
                                    allNotificationsEnabled = it
                                    if (!it) {
                                        // Disable all notifications
                                        transactionNotifications = false
                                        budgetNotifications = false
                                        reminderNotifications = false
                                    }
                                },
                                modifier = Modifier.semantics {
                                    contentDescription = "All notifications, currently ${if (allNotificationsEnabled) "enabled" else "disabled"}"
                                }
                            )
                        }
                    }
                }
                
                // If master toggle is on, show individual notification settings
                if (allNotificationsEnabled) {
                    // Transaction notifications
                    NotificationSettingItem(
                        title = "Transaction Notifications",
                        description = "Get notified about new transactions and payments",
                        icon = Icons.Default.Payments,
                        isEnabled = transactionNotifications,
                        onToggle = { transactionNotifications = it }
                    )
                    
                    // Budget notifications
                    NotificationSettingItem(
                        title = "Budget Alerts",
                        description = "Receive alerts when you're close to budget limits",
                        icon = Icons.Default.Wallet,
                        isEnabled = budgetNotifications,
                        onToggle = { budgetNotifications = it }
                    )
                    
                    // Reminder notifications
                    NotificationSettingItem(
                        title = "Payment Reminders",
                        description = "Get reminders for upcoming bills and payments",
                        icon = Icons.Default.Timer,
                        isEnabled = reminderNotifications,
                        onToggle = { reminderNotifications = it }
                    )
                } else {
                    // Notifications are disabled
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = "Notifications are disabled",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            Text(
                                text = "Enable notifications to receive important updates about your finances",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun NotificationSettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics {
                    contentDescription = "$title, ${if (isEnabled) "enabled" else "disabled"}"
                }
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Text content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            // Toggle switch
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
} 