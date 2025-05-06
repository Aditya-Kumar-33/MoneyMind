package com.example.moneymind.accessibility

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * These utilities help make custom gestures more accessible
 * by providing accessibility-friendly alternatives
 */

// Custom semantic property for swipe gestures
val SwipeActionDescription = SemanticsPropertyKey<String>("SwipeActionDescription")
var SemanticsPropertyReceiver.swipeActionDescription by SwipeActionDescription

/**
 * Makes tap gestures accessible to TalkBack users
 */
fun Modifier.accessibleTapGesture(
    description: String,
    onTap: () -> Unit
): Modifier = composed {
    val accessibilityManager = MoneyMindAccessibilityManager.getInstance(LocalContext.current)
    val isTalkBackEnabled = accessibilityManager.isTalkBackEnabled()
    
    if (isTalkBackEnabled) {
        // Use semantic click for TalkBack users
        semantics {
            onClick(label = description) { 
                onTap()
                true
            }
            role = Role.Button
        }
    } else {
        // Use regular gesture detection for non-TalkBack users
        pointerInput(Unit) {
            detectTapGestures(
                onTap = { onTap() }
            )
        }
    }
}

/**
 * Makes swipe gestures accessible to TalkBack users
 * by providing an alternative tap action with proper semantic description
 */
fun Modifier.accessibleSwipeAction(
    description: String,
    onSwipe: () -> Unit
): Modifier = composed {
    val accessibilityManager = MoneyMindAccessibilityManager.getInstance(LocalContext.current)
    val isTalkBackEnabled = accessibilityManager.isTalkBackEnabled()
    
    if (isTalkBackEnabled) {
        // For screen reader users, convert swipe to a semantic tap action
        semantics {
            swipeActionDescription = description
            onClick(label = description) { 
                onSwipe()
                true
            }
            role = Role.Button
        }
    } else {
        // Regular modifier for gesture detection would be applied here
        // The actual swipe gesture detection would be implemented elsewhere
        this
    }
}

/**
 * Makes long press gestures accessible to TalkBack users
 */
fun Modifier.accessibleLongPress(
    description: String,
    onLongPress: () -> Unit
): Modifier = composed {
    val accessibilityManager = MoneyMindAccessibilityManager.getInstance(LocalContext.current)
    val isTalkBackEnabled = accessibilityManager.isTalkBackEnabled()
    
    if (isTalkBackEnabled) {
        // Use semantic click for TalkBack users
        semantics {
            onClick(label = description) { 
                onLongPress()
                true
            }
            role = Role.Button
        }
    } else {
        // Use regular gesture detection for non-TalkBack users
        pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { onLongPress() }
            )
        }
    }
}

/**
 * Completely clears and replaces semantics with an accessible action
 * Useful for elements that need special handling for accessibility
 */
fun Modifier.replaceWithAccessibleAction(
    description: String,
    onAction: () -> Unit
): Modifier = clearAndSetSemantics {
    onClick(label = description) {
        onAction()
        true
    }
    role = Role.Button
} 