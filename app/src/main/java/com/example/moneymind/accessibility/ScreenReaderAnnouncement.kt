package com.example.moneymind.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Helper composable for announcing messages to screen readers
 * Can be used to announce events like form submissions, data loading, errors, etc.
 */
@Composable
fun ScreenReaderAnnouncement(
    shouldAnnounce: Boolean,
    announcement: String,
    onAnnouncementComplete: () -> Unit = {}
) {
    if (shouldAnnounce) {
        val accessibilityManager = rememberAccessibilityManager()
        
        LaunchedEffect(shouldAnnounce, announcement) {
            accessibilityManager.announce(announcement)
            // Add a small delay to ensure the announcement is completed
            delay(500)
            onAnnouncementComplete()
        }
    }
}

/**
 * Helper composable for handling loading states with screen reader announcements
 */
@Composable
fun LoadingAnnouncement(
    isLoading: Boolean,
    loadingMessage: String = "Loading, please wait",
    completeMessage: String = "Loading complete"
) {
    var hasAnnounced by remember { mutableStateOf(false) }
    
    if (isLoading && !hasAnnounced) {
        val accessibilityManager = rememberAccessibilityManager()
        
        LaunchedEffect(isLoading) {
            accessibilityManager.announce(loadingMessage)
            hasAnnounced = true
        }
    } else if (!isLoading && hasAnnounced) {
        val accessibilityManager = rememberAccessibilityManager()
        
        LaunchedEffect(isLoading) {
            accessibilityManager.announce(completeMessage)
            hasAnnounced = false
        }
    }
}

/**
 * Helper composable for announcing errors with screen readers
 */
@Composable
fun ErrorAnnouncement(
    error: String?,
    onErrorAnnounced: () -> Unit = {}
) {
    if (!error.isNullOrEmpty()) {
        val accessibilityManager = rememberAccessibilityManager()
        
        LaunchedEffect(error) {
            accessibilityManager.announce("Error: $error")
            delay(500)
            onErrorAnnounced()
        }
    }
}

/**
 * Helper composable for announcing success messages with screen readers
 */
@Composable
fun SuccessAnnouncement(
    message: String?,
    onAnnounced: () -> Unit = {}
) {
    if (!message.isNullOrEmpty()) {
        val accessibilityManager = rememberAccessibilityManager()
        
        LaunchedEffect(message) {
            accessibilityManager.announce(message)
            delay(500)
            onAnnounced()
        }
    }
} 