package com.example.moneymind.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

/**
 * Extension functions for Compose accessibility
 */

/**
 * Adds accessibility heading semantics to a component
 * Headings are important landmarks for screen reader users
 */
fun Modifier.accessibilityHeading(): Modifier = semantics {
    heading()
}

/**
 * Adds content description for screen readers with state description if needed
 */
fun Modifier.accessibleText(
    contentDesc: String,
    stateDesc: String? = null
): Modifier = semantics {
    contentDescription = contentDesc
    if (!stateDesc.isNullOrEmpty()) {
        stateDescription = stateDesc
    }
}

/**
 * Adds accessibility semantics to clickable elements
 */
fun Modifier.accessibleClickable(
    contentDesc: String,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    ).semantics {
        contentDescription = contentDesc
    }
}

/**
 * Enhanced semantic properties for complex components
 * Combines description, hints and error messages
 */
fun Modifier.accessibilitySemantics(
    description: String,
    hint: String? = null,
    errorMessage: String? = null,
    isEnabled: Boolean = true
): Modifier = composed {
    semantics {
        contentDescription = description
        if (!hint.isNullOrEmpty()) {
            stateDescription = hint
        }
        if (!errorMessage.isNullOrEmpty() && (hint == null || hint.isEmpty())) {
            stateDescription = "Error: $errorMessage"
        }
    }
}

/**
 * Clears existing semantics and sets only the provided ones
 * Useful when you need complete control over what's announced
 */
fun Modifier.clearAndSetAccessibility(
    contentDesc: String
): Modifier = clearAndSetSemantics {
    contentDescription = contentDesc
}

/**
 * Combines accessibility semantic properties with TalkBack detection
 * Only applies enhanced semantics if TalkBack is enabled
 */
fun Modifier.conditionalAccessibility(
    contentDesc: String,
    stateDesc: String? = null
): Modifier = composed {
    val context = LocalContext.current
    val isTalkBackEnabled = AccessibilityUtils.isTalkBackEnabled(context)
    
    if (isTalkBackEnabled) {
        accessibleText(contentDesc, stateDesc)
    } else {
        this
    }
} 