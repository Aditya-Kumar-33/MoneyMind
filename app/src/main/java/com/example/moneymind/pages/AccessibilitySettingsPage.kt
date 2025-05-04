package com.example.moneymind.pages

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymind.R
import com.example.moneymind.accessibility.AccessibilityViewModel
import com.example.moneymind.accessibility.AnnouncementEffect
import com.example.moneymind.accessibility.rememberAccessibilityManager
import com.example.moneymind.accessibility.rememberAccessibilitySettings
import com.example.moneymind.ui.components.AccessibleButton
import com.example.moneymind.ui.components.AccessibleScaffold
import com.example.moneymind.utils.accessibilityHeading
import com.example.moneymind.utils.accessibilitySemantics

/**
 * Settings page for accessibility features
 */
@Composable
fun AccessibilitySettingsPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    accessibilityViewModel: AccessibilityViewModel
) {
    // Initial announcement to help TalkBack users understand they're on the accessibility settings page
    AnnouncementEffect("Accessibility settings page")
    
    val accessibilityManager = rememberAccessibilityManager()
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Get settings from ViewModel
    val settings = accessibilityViewModel.settings.collectAsState().value
    
    AccessibleScaffold(
        title = "Accessibility Settings",
        onBackClick = { navController.popBackStack() }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TalkBack status section
            Text(
                text = "Screen Reader",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .accessibilityHeading()
            )
            
            // Status of TalkBack
            val talkBackStatus = if (accessibilityManager.isTalkBackEnabled()) 
                "TalkBack is currently enabled" else "TalkBack is currently disabled"
            
            Text(
                text = talkBackStatus,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .accessibilitySemantics(description = talkBackStatus)
            )
            
            // Button to open system accessibility settings
            AccessibleButton(
                onClick = { 
                    activity?.let { accessibilityManager.openAccessibilitySettings(it) }
                },
                accessibilityLabel = "Open system accessibility settings to enable or configure TalkBack",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null
                )
                Text(text = "Open System Accessibility Settings")
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Text size settings
            Text(
                text = "Text Size",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .accessibilityHeading()
            )
            
            // Text size slider
            Text(
                text = "Adjust Text Size",
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            )
            
            Slider(
                value = settings.textScaleFactor,
                onValueChange = { accessibilityViewModel.setTextScaleFactor(it) },
                valueRange = 0.8f..1.5f,
                steps = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .accessibilitySemantics(
                        description = "Text size adjustment slider, current value ${(settings.textScaleFactor * 100).toInt()} percent",
                        hint = "Slide right to increase text size, slide left to decrease"
                    )
            )
            
            // Preview text at the selected scale
            Text(
                text = "Preview text at this size",
                fontSize = (16 * settings.textScaleFactor).sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Large text toggle
            SwitchSetting(
                title = "Large Text",
                description = "Use larger text throughout the app",
                isChecked = settings.largeTextEnabled,
                onCheckedChange = { accessibilityViewModel.setLargeTextEnabled(it) }
            )
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Display settings
            Text(
                text = "Display Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .accessibilityHeading()
            )
            
            // High contrast mode toggle
            SwitchSetting(
                title = "High Contrast",
                description = "Increase contrast for better visibility",
                isChecked = settings.highContrastEnabled,
                onCheckedChange = { accessibilityViewModel.setHighContrastEnabled(it) }
            )
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Announcement settings
            Text(
                text = "Screen Reader Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .accessibilityHeading()
            )
            
            // Verbose announcements toggle
            SwitchSetting(
                title = "Verbose Announcements",
                description = "Provide more detailed descriptions for screen reader",
                isChecked = settings.verboseAnnouncementsEnabled,
                onCheckedChange = { accessibilityViewModel.setVerboseAnnouncementsEnabled(it) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Test announcement button
            AccessibleButton(
                onClick = { 
                    accessibilityManager.announce("This is a test announcement to demonstrate TalkBack functionality")
                },
                accessibilityLabel = "Test screen reader announcement",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Accessibility,
                    contentDescription = null
                )
                Text(text = "Test Screen Reader Announcement")
            }
            
            // Reset to defaults button
            AccessibleButton(
                onClick = { accessibilityViewModel.resetToDefaults() },
                accessibilityLabel = "Reset all accessibility settings to default values",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
                Text(text = "Reset to Defaults")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Reusable component for switch settings with accessibility support
 */
@Composable
private fun SwitchSetting(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val stateDescription = if (isChecked) "enabled" else "disabled"
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .accessibilitySemantics(
                description = "$title, $description. Currently $stateDescription",
                hint = "Double tap to ${if (isChecked) "disable" else "enable"}"
            )
    ) {
        androidx.compose.material3.ListItem(
            headlineContent = { Text(text = title) },
            supportingContent = { Text(text = description) },
            trailingContent = {
                Switch(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange
                )
            }
        )
    }
} 