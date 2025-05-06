package com.example.moneymind

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneymind.pages.CategoryData
import com.example.moneymind.services.RecommendationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecommendationViewModel(private val context: Context) : ViewModel() {

    // States for recommendation UI
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _recommendationText = MutableStateFlow<String?>(null)
    val recommendationText: StateFlow<String?> = _recommendationText.asStateFlow()
    
    private val _showRecommendation = mutableStateOf(false)
    val showRecommendation: State<Boolean> = _showRecommendation
    
    // API key input states
    private val _apiKey = mutableStateOf("")
    val apiKey: State<String> = _apiKey
    
    private val _showApiKeyInput = mutableStateOf(false)
    val showApiKeyInput: State<Boolean> = _showApiKeyInput
    
    // Persisted API key
    private var savedApiKey: String? = null
    
    init {
        // Try to load a saved API key (in a real app, use secure storage)
        // This is a simplified example
        savedApiKey = context.getSharedPreferences("recommendation_prefs", Context.MODE_PRIVATE)
            .getString("api_key", null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getRecommendations(
        totalExpense: Double,
        totalIncome: Double,
        expenseCategories: List<CategoryData>
    ) {
        _isLoading.value = true
        _showRecommendation.value = true
        
        viewModelScope.launch {
            try {
                val recommendation = RecommendationService.getRecommendation(
                    context = context,
                    totalExpense = totalExpense,
                    totalIncome = totalIncome,
                    expenseCategories = expenseCategories,
                    apiKey = savedApiKey
                )
                
                _recommendationText.value = recommendation
            } catch (e: Exception) {
                Log.e("RecommendationViewModel", "Error getting recommendations: ${e.message}", e)
                _recommendationText.value = "Sorry, we couldn't generate recommendations at this time. Please try again later."
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun dismissRecommendation() {
        _showRecommendation.value = false
        _recommendationText.value = null
    }
    
    fun updateApiKey(newKey: String) {
        _apiKey.value = newKey
    }
    
    fun saveApiKey() {
        if (_apiKey.value.isNotBlank()) {
            savedApiKey = _apiKey.value
            
            // Save to preferences (in a real app, use secure storage)
            context.getSharedPreferences("recommendation_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("api_key", savedApiKey)
                .apply()
        }
        
        toggleApiKeyInput()
    }
    
    fun toggleApiKeyInput() {
        _showApiKeyInput.value = !_showApiKeyInput.value
        if (_showApiKeyInput.value) {
            // Set the current API key to the input field
            _apiKey.value = savedApiKey ?: ""
        }
    }
    
    fun hasApiKey(): Boolean {
        return !savedApiKey.isNullOrBlank()
    }
}

class RecommendationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecommendationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecommendationViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 