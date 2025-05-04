package com.example.moneymind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import com.example.moneymind.accessibility.AccessibilityViewModel
import com.example.moneymind.accessibility.AccessibilityViewModelFactory
import com.example.moneymind.ui.theme.MoneyMindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize ViewModels
        val authViewModel: AuthViewModel by viewModels()
        
        // Initialize AccessibilityViewModel with factory
        val accessibilityViewModel: AccessibilityViewModel by viewModels { 
            AccessibilityViewModelFactory(this) 
        }
        
        setContent {
            // Get current accessibility settings
            val accessibilitySettings = accessibilityViewModel.settings.value
            
            // Apply accessibility settings to theme if needed
            MoneyMindTheme(
                // Apply settings like dark mode, dynamic colors, etc. if needed
                // darkTheme = accessibilitySettings.highContrastEnabled
            ) {
                // The Scaffold is now moved to NavigationController for better control
                NavigationController(
                    authViewModel = authViewModel,
                    accessibilityViewModel = accessibilityViewModel
                )
            }
        }
    }
}

