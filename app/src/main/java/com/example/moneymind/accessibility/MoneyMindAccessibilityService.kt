package com.example.moneymind.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

/**
 * Custom accessibility service to enhance TalkBack features specifically for MoneyMind app
 */
class MoneyMindAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "MoneyMindAccessibility"
        var isServiceRunning = false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "MoneyMind Accessibility Service connected")
        
        // Configure service to receive specific events
        val serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_ANNOUNCEMENT
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
        }
        
        serviceInfo.packageNames = arrayOf("com.example.moneymind")
        this.serviceInfo = serviceInfo
        isServiceRunning = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            val eventType = event.eventType
            val packageName = event.packageName?.toString() ?: return
            
            // Only process events from our app
            if (packageName != "com.example.moneymind") return
            
            when (eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    // A new screen has been loaded
                    processWindowChange(event)
                }
                
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    // User clicked on something
                    processClick(event)
                }
                
                AccessibilityEvent.TYPE_ANNOUNCEMENT -> {
                    // App made an announcement (our custom events)
                    processAnnouncement(event)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing accessibility event", e)
        }
    }

    private fun processWindowChange(event: AccessibilityEvent) {
        val nodeInfo = event.source ?: return
        val className = event.className?.toString() ?: return
        
        // Identify which screen we're on based on class name
        // This would need to be customized based on actual screen classes
        when {
            className.contains("MainActivity") -> {
                // Main activity loaded
                speakFeedback("MoneyMind app launched")
            }
            className.contains("Login") -> {
                // Login screen
                speakFeedback("Login screen. Please enter your credentials")
            }
            className.contains("Savings") -> {
                // Savings screen
                speakFeedback("Savings screen opened")
            }
            className.contains("Transaction") -> {
                // Transaction screen
                speakFeedback("Transaction details page opened")
            }
        }
        
        nodeInfo.recycle() // Always recycle node info to avoid memory leaks
    }

    private fun processClick(event: AccessibilityEvent) {
        val nodeInfo = event.source ?: return
        val clickedText = event.text?.joinToString(" ") ?: ""
        
        if (clickedText.isNotEmpty()) {
            Log.d(TAG, "Clicked on: $clickedText")
            // Could provide custom feedback based on what was clicked
        }
        
        nodeInfo.recycle()
    }

    private fun processAnnouncement(event: AccessibilityEvent) {
        val announcement = event.text?.joinToString(" ") ?: return
        speakFeedback(announcement)
    }

    /**
     * Helper to speak text via the accessibility service
     */
    fun speakFeedback(text: String) {
        if (text.isNotEmpty()) {
            Log.d(TAG, "Speaking: $text")
            
            try {
                // Create a simple accessibility event
                val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
                event.text.add(text)
                event.packageName = packageName
                
                // Use the accessibility manager to broadcast the event
                val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
                accessibilityManager.sendAccessibilityEvent(event)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending announcement", e)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "MoneyMind Accessibility Service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceRunning = false
        Log.d(TAG, "MoneyMind Accessibility Service unbound")
        return super.onUnbind(intent)
    }
} 