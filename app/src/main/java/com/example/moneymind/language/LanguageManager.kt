package com.example.moneymind.language

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

// Supported languages enum
enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    HINDI("hi", "हिन्दी");
    
    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().find { it.code == code } ?: ENGLISH
        }
    }
}

/**
 * ViewModel for managing language settings
 */
class LanguageViewModel(private val context: Context) : ViewModel() {

    companion object {
        // DataStore setup
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "language_settings")
        
        // Preference key
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
    }

    // Flow of the selected language from DataStore
    private val languageFlow: Flow<AppLanguage> = context.dataStore.data
        .map { preferences ->
            val languageCode = preferences[LANGUAGE_KEY] ?: Locale.getDefault().language
            AppLanguage.fromCode(languageCode)
        }

    // StateFlow for language to be observed by the UI
    val selectedLanguage = languageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppLanguage.ENGLISH
    )

    /**
     * Set the app language and refresh UI
     */
    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            try {
                // Update the stored preference
                context.dataStore.edit { preferences ->
                    preferences[LANGUAGE_KEY] = language.code
                }
                
                // Log the language change
                android.util.Log.d("LanguageViewModel", "Language changed to: ${language.displayName} (${language.code})")
                
                // Apply the locale change
                updateLocale(context, language.code)
                
                // Ensure the locale was set correctly
                val currentLocale = Locale.getDefault()
                android.util.Log.d("LanguageViewModel", "Current locale: ${currentLocale.language}, ${currentLocale.displayName}")
                
                // Restart activity to apply changes to all UI components
                restartActivity(context)
            } catch (e: Exception) {
                android.util.Log.e("LanguageViewModel", "Error changing language", e)
            }
        }
    }
    
    /**
     * Update the locale configuration
     */
    fun updateLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        // Apply the configuration to the context's resources
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    /**
     * Restart the current activity to refresh all UI components
     */
    private fun restartActivity(context: Context) {
        try {
            // Find the activity from context
            val activity = getActivity(context)
            
            if (activity == null) {
                android.util.Log.e("LanguageViewModel", "Could not find activity from context")
                return
            }
            
            android.util.Log.d("LanguageViewModel", "Restarting activity: ${activity.javaClass.simpleName}")
            
            // Create a new intent for the current activity
            val intent = Intent(activity, activity::class.java)
            // Clear the back stack
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            
            // Add a special flag to indicate this is a language change restart
            intent.putExtra("LANGUAGE_CHANGE", true)
            
            // Start the activity
            activity.startActivity(intent)
            
            // Apply transition animation
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            
            // Finish the current activity
            activity.finish()
        } catch (e: Exception) {
            android.util.Log.e("LanguageViewModel", "Error restarting activity", e)
        }
    }
    
    /**
     * Get activity from context
     */
    private fun getActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }
}

/**
 * Factory for creating LanguageViewModel
 */
class LanguageViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LanguageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LanguageViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * Composable function to get current language as state
 */
@Composable
fun rememberSelectedLanguage(viewModel: LanguageViewModel): State<AppLanguage> {
    return viewModel.selectedLanguage.collectAsState()
} 