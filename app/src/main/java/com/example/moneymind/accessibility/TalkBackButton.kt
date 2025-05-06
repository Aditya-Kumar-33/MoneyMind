package com.example.moneymind.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.moneymind.R

/**
 * Checks if TalkBack is currently enabled
 */
fun isTalkBackEnabled(context: Context): Boolean {
    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
        AccessibilityServiceInfo.FEEDBACK_SPOKEN
    )
    
    // Check if any spoken feedback service (like TalkBack) is enabled
    return enabledServices.any { it.resolveInfo.serviceInfo.packageName.contains("talkback") || 
                                it.resolveInfo.serviceInfo.packageName.contains("accessibility") }
}

/**
 * A button to toggle TalkBack accessibility service
 */
@Composable
fun TalkBackButton(
    modifier: Modifier = Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
) {
    val context = LocalContext.current
    var talkBackEnabled by remember { mutableStateOf(false) }
    
    // Check if TalkBack is enabled
    LaunchedEffect(Unit) {
        talkBackEnabled = isTalkBackEnabled(context)
    }
    
    val buttonText = stringResource(id = R.string.talkback_button)
    val contentDesc = if (talkBackEnabled) 
        "TalkBack is currently enabled. Tap to go to accessibility settings" 
    else 
        "TalkBack is currently disabled. Tap to go to accessibility settings"
    
    Button(
        onClick = {
            // Open accessibility settings so user can enable/disable TalkBack
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        modifier = modifier
            .padding(vertical = 8.dp)
            .semantics {
                contentDescription = contentDesc
            }
    ) {
        Text(buttonText)
    }
} 