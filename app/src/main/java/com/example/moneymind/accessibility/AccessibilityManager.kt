package com.example.moneymind.accessibility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.moneymind.utils.AccessibilityUtils

/**
 * Central manager for accessibility features in the app
 */
class MoneyMindAccessibilityManager(private val context: Context) {

    private val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    /**
     * Check if the device has TalkBack enabled
     */
    fun isTalkBackEnabled(): Boolean {
        return AccessibilityUtils.isTalkBackEnabled(context)
    }

    /**
     * Makes an accessibility announcement that will be read by TalkBack
     */
    fun announce(text: String, interruptCurrent: Boolean = true) {
        if (text.isBlank()) return
        
        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        event.text.add(text)
        event.className = javaClass.name
        event.packageName = context.packageName
        
        accessibilityManager.sendAccessibilityEvent(event)
    }

    /**
     * Navigate to system accessibility settings
     */
    fun openAccessibilitySettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        activity.startActivity(intent)
    }

    companion object {
        @Volatile private var INSTANCE: MoneyMindAccessibilityManager? = null

        fun getInstance(context: Context): MoneyMindAccessibilityManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MoneyMindAccessibilityManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

/**
 * Composable to easily access the AccessibilityManager
 */
@Composable
fun rememberAccessibilityManager(): MoneyMindAccessibilityManager {
    val context = LocalContext.current
    return remember { MoneyMindAccessibilityManager.getInstance(context) }
}

/**
 * Announce text when a composable enters composition
 */
@Composable
fun AnnouncementEffect(message: String) {
    val accessibilityManager = rememberAccessibilityManager()
    
    DisposableEffect(message) {
        if (message.isNotEmpty()) {
            accessibilityManager.announce(message)
        }
        onDispose { }
    }
} 