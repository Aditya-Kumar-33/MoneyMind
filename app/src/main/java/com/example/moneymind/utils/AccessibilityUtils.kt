package com.example.moneymind.utils

import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics

/**
 * Utilities for accessibility features, especially TalkBack
 */
object AccessibilityUtils {

    /**
     * Check if TalkBack is currently enabled on the device
     * @param context The application context
     * @return true if TalkBack is enabled, false otherwise
     */
    fun isTalkBackEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        
        // For API 33+ (Tiramisu), this would be preferred
        // But since we're targeting minSDK 24, we need to use the deprecated method
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //     return accessibilityManager.isScreenReaderEnabled
        // }
        
        // This method works on all API levels we support
        return accessibilityManager.isTouchExplorationEnabled
    }

    // Custom semantic properties for improved TalkBack accessibility
    val DescriptionForAccessibility = SemanticsPropertyKey<String>("DescriptionForAccessibility")
    val HintForAccessibility = SemanticsPropertyKey<String>("HintForAccessibility")
    val ErrorMessageForAccessibility = SemanticsPropertyKey<String>("ErrorMessageForAccessibility")

    // Extension functions for SemanticsPropertyReceiver
    var SemanticsPropertyReceiver.descriptionForAccessibility by DescriptionForAccessibility
    var SemanticsPropertyReceiver.hintForAccessibility by HintForAccessibility
    var SemanticsPropertyReceiver.errorMessageForAccessibility by ErrorMessageForAccessibility
} 