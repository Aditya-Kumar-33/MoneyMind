package com.example.moneymind

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.moneymind.accessibility.AccessibilityViewModel
import com.example.moneymind.accessibility.AccessibilityViewModelFactory
import com.example.moneymind.language.LanguageViewModel
import com.example.moneymind.language.LanguageViewModelFactory
import com.example.moneymind.ui.theme.MoneyMindTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    // Initialize ViewModels at class level for lifecycle awareness
    private lateinit var languageViewModel: LanguageViewModel
    
    // We need to override attachBaseContext to set the locale before the Activity is created
    override fun attachBaseContext(newBase: Context) {
        // Get the saved language preference
        val sharedPreferences = newBase.getSharedPreferences("language_settings", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("selected_language", Locale.getDefault().language) ?: "en"
        
        // Apply the locale
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        
        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize ViewModels
        val authViewModel: AuthViewModel by viewModels()
        
        // Initialize AccessibilityViewModel with factory
        val accessibilityViewModel: AccessibilityViewModel by viewModels { 
            AccessibilityViewModelFactory(this) 
        }
        
        // Initialize LanguageViewModel with factory
        languageViewModel = viewModels<LanguageViewModel> { 
            LanguageViewModelFactory(this)
        }.value
        
        // Apply current language settings
        languageViewModel.updateLocale(this, languageViewModel.selectedLanguage.value.code)
        
        setContent {
            // Get current accessibility settings
            val accessibilitySettings = accessibilityViewModel.settings.value
            
            // Observe language changes
            val currentLanguage by languageViewModel.selectedLanguage.collectAsState()
            
            // Apply accessibility settings to theme if needed
            MoneyMindTheme(
                // Apply settings like dark mode, dynamic colors, etc. if needed
                darkTheme = accessibilitySettings.highContrastEnabled,
                // Apply text scale factor to the theme's typography
                textScale = accessibilitySettings.textScaleFactor
            ) {
                // The Scaffold is now moved to NavigationController for better control
                NavigationController(
                    authViewModel = authViewModel,
                    accessibilityViewModel = accessibilityViewModel,
                    languageViewModel = languageViewModel
                )
            }
        }
    }
    
    // Update resource configuration when language changes
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::languageViewModel.isInitialized) {
            languageViewModel.updateLocale(this, languageViewModel.selectedLanguage.value.code)
        }
    }
}

