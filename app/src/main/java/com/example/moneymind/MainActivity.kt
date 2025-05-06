package com.example.moneymind

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
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
import com.example.moneymind.language.AppLanguage
import com.example.moneymind.ui.theme.MoneyMindTheme
import java.util.Locale
import android.widget.Toast
import java.io.File
import android.content.SharedPreferences

class MainActivity : ComponentActivity() {
    // Initialize ViewModels at class level for lifecycle awareness
    private lateinit var languageViewModel: LanguageViewModel
    
    // We need to override attachBaseContext to set the locale before the Activity is created
    override fun attachBaseContext(newBase: Context) {
        // Use SharedPreferences for initial context creation to avoid DataStore conflicts
        val sharedPreferences = newBase.getSharedPreferences("language_settings_prefs", Context.MODE_PRIVATE)
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
        
        // Check if this is a restart due to language change
        val isLanguageChangeRestart = intent.getBooleanExtra("LANGUAGE_CHANGE", false)
        if (isLanguageChangeRestart) {
            Log.d("MainActivity", "Restarting due to language change")
        }
        
        // Handle database initialization errors if they occur
        handleDatabaseErrors()
        
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
        val currentLanguage = languageViewModel.selectedLanguage.value
        
        // Also save to SharedPreferences for attachBaseContext to use on next launch
        saveLanguageToSharedPrefs(currentLanguage.code)
        
        Log.d("MainActivity", "Current language: ${currentLanguage.displayName} (${currentLanguage.code})")
        languageViewModel.updateLocale(this, currentLanguage.code)
        
        setContent {
            // Get current accessibility settings
            val accessibilitySettings = accessibilityViewModel.settings.value
            
            // Observe language changes
            val currentLanguage by languageViewModel.selectedLanguage.collectAsState()
            Log.d("MainActivity", "Language in Compose: ${currentLanguage.displayName}")
            
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
    
    // Helper method to save language selection to SharedPreferences 
    // for use in attachBaseContext
    private fun saveLanguageToSharedPrefs(languageCode: String) {
        val sharedPreferences = getSharedPreferences("language_settings_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("selected_language", languageCode).apply()
    }
    
    /**
     * Handle database initialization errors by checking if we need to delete the database
     * This is a safety measure in case the migration fails
     */
    private fun handleDatabaseErrors() {
        try {
            // Check for database error flags that might have been set
            val sharedPrefs = getSharedPreferences("database_settings", Context.MODE_PRIVATE)
            val needsRecovery = sharedPrefs.getBoolean("needs_recovery", false)
            
            if (needsRecovery) {
                // Clear the flag first
                sharedPrefs.edit().putBoolean("needs_recovery", false).apply()
                
                // Delete the database file
                val dbFile = getDatabasePath("money_mind_database")
                if (dbFile.exists()) {
                    val deleted = dbFile.delete()
                    if (deleted) {
                        Log.i("MainActivity", "Deleted corrupted database file")
                        Toast.makeText(this, "Database reset due to previous errors", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("MainActivity", "Failed to delete corrupted database file")
                    }
                }
                
                // Delete related files
                val dbShm = File(dbFile.path + "-shm")
                if (dbShm.exists()) dbShm.delete()
                
                val dbWal = File(dbFile.path + "-wal")
                if (dbWal.exists()) dbWal.delete()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error handling database recovery", e)
        }
    }
    
    // Update resource configuration when language changes
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::languageViewModel.isInitialized) {
            val languageCode = languageViewModel.selectedLanguage.value.code
            languageViewModel.updateLocale(this, languageCode)
            // Also update SharedPreferences
            saveLanguageToSharedPrefs(languageCode)
        }
    }
}

