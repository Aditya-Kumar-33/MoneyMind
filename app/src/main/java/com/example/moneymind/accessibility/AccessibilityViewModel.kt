package com.example.moneymind.accessibility

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Settings data class for accessibility
 */
data class AccessibilitySettings(
    val largeTextEnabled: Boolean = false,
    val highContrastEnabled: Boolean = false,
    val verboseAnnouncementsEnabled: Boolean = true,
    val textScaleFactor: Float = 1.0f
)

/**
 * ViewModel for managing accessibility settings
 */
class AccessibilityViewModel(private val context: Context) : ViewModel() {

    companion object {
        // DataStore setup
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "accessibility_settings")
        
        // Preference keys
        private val LARGE_TEXT_KEY = booleanPreferencesKey("large_text_enabled")
        private val HIGH_CONTRAST_KEY = booleanPreferencesKey("high_contrast_enabled")
        private val VERBOSE_ANNOUNCEMENTS_KEY = booleanPreferencesKey("verbose_announcements_enabled")
        private val TEXT_SCALE_FACTOR_KEY = floatPreferencesKey("text_scale_factor")
    }

    // Flow of the settings from DataStore
    private val accessibilitySettingsFlow: Flow<AccessibilitySettings> = context.dataStore.data
        .map { preferences ->
            AccessibilitySettings(
                largeTextEnabled = preferences[LARGE_TEXT_KEY] ?: false,
                highContrastEnabled = preferences[HIGH_CONTRAST_KEY] ?: false,
                verboseAnnouncementsEnabled = preferences[VERBOSE_ANNOUNCEMENTS_KEY] ?: true,
                textScaleFactor = preferences[TEXT_SCALE_FACTOR_KEY] ?: 1.0f
            )
        }

    // StateFlow for settings to be observed by the UI
    val settings = accessibilitySettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccessibilitySettings()
    )

    /**
     * Set large text mode
     */
    fun setLargeTextEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[LARGE_TEXT_KEY] = enabled
            }
        }
    }

    /**
     * Set high contrast mode
     */
    fun setHighContrastEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[HIGH_CONTRAST_KEY] = enabled
            }
        }
    }

    /**
     * Set verbose announcements mode
     */
    fun setVerboseAnnouncementsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[VERBOSE_ANNOUNCEMENTS_KEY] = enabled
            }
        }
    }

    /**
     * Set text scale factor
     */
    fun setTextScaleFactor(factor: Float) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[TEXT_SCALE_FACTOR_KEY] = factor
            }
        }
    }
    
    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[LARGE_TEXT_KEY] = false
                preferences[HIGH_CONTRAST_KEY] = false
                preferences[VERBOSE_ANNOUNCEMENTS_KEY] = true
                preferences[TEXT_SCALE_FACTOR_KEY] = 1.0f
            }
        }
    }
}

/**
 * Factory for creating AccessibilityViewModel
 */
class AccessibilityViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccessibilityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccessibilityViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * Composable function to get current accessibility settings as state
 */
@Composable
fun rememberAccessibilitySettings(viewModel: AccessibilityViewModel): State<AccessibilitySettings> {
    return viewModel.settings.collectAsState()
} 