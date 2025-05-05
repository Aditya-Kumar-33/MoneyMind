package com.example.moneymind // Or your viewmodel package

import android.content.ContentResolver
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymind.data.TransactionDao
import com.example.moneymind.data.Transaction
import com.example.moneymind.util.SmsTransactionParser
import com.example.moneymind.utils.TransactionUpdateBroadcaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn // Import stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZoneId

class SavingsViewModel(
  private val transactionDao: TransactionDao,
  private val smsParser: SmsTransactionParser
) : ViewModel() {

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _summaryStatus = MutableStateFlow<String?>(null)
  val summaryStatus: StateFlow<String?> = _summaryStatus.asStateFlow()
  
  // Selected date for filtering
  private val _selectedDate = MutableStateFlow(LocalDate.now())
  val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

  // Expose all transactions as StateFlow
  val allTransactions: StateFlow<List<Transaction>> = transactionDao.getAllTransactions()
    .stateIn( // Convert Flow to StateFlow
      scope = viewModelScope,
      // Keep flow active 5s after last UI collector stops
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = emptyList() // Start with empty list
    )
    
  // Transactions for the selected date
  private val _filteredTransactions = MutableStateFlow<List<Transaction>>(emptyList())
  val filteredTransactions: StateFlow<List<Transaction>> = _filteredTransactions.asStateFlow()
  
  // Update selected date - completely isolated date filtering
  @RequiresApi(Build.VERSION_CODES.O)
  fun updateSelectedDate(date: LocalDate) {
    Log.d("SavingsViewModel", "Date selected: ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
    
    // Always update the date first
    _selectedDate.value = date
    
    // Then load the transactions for this specific date
    refreshTransactionsForSelectedDate()
  }
  
  // Refresh transactions for the currently selected date
  @RequiresApi(Build.VERSION_CODES.O)
  private fun refreshTransactionsForSelectedDate() {
    val date = _selectedDate.value
    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    
    Log.d("SavingsViewModel", "Refreshing transactions for date: $dateStr")
    
    // Cancel any previous job and start a new one
    viewModelScope.launch(Dispatchers.IO) {
      try {
        // First clear the list to avoid showing wrong data
        _filteredTransactions.value = emptyList()
        
        // Get transactions for this specific date using the transaction_date field
        val transactions = transactionDao.getTransactionsByDate(dateStr)
        
        Log.d("SavingsViewModel", "Loaded ${transactions.size} transactions for date $dateStr")
        
        // Update the filtered transactions list
        _filteredTransactions.value = transactions
      } catch (e: Exception) {
        Log.e("SavingsViewModel", "Error loading transactions for date: ${e.message}")
        _filteredTransactions.value = emptyList()
      }
    }
  }

  // Update transaction category
  @RequiresApi(Build.VERSION_CODES.O)
  suspend fun updateTransactionCategory(transaction: Transaction, newCategory: String) {
    withContext(Dispatchers.IO) {
      try {
        // Update database
        transactionDao.updateTransactionCategory(
          transaction.transactionDate,
          transaction.smsTimestamp,
          newCategory
        )
        
        // Completely refresh the filtered transactions to ensure proper isolation
        refreshTransactionsForSelectedDate()
        
        // Notify listeners about the category change
        TransactionUpdateBroadcaster.notifyCategoryChanged(transaction.smsTimestamp)
        
        Log.d("SavingsViewModel", "Updated category for transaction on ${transaction.transactionDate}: $newCategory")
      } catch (e: Exception) {
        Log.e("SavingsViewModel", "Error updating category: ${e.message}")
      }
    }
  }

  // Trigger SMS reading and parsing
  @RequiresApi(Build.VERSION_CODES.O)
  fun startSmsSummarization(contentResolver: ContentResolver) {
    viewModelScope.launch {
      _isLoading.value = true // Show loading
      _summaryStatus.value = "Reading SMS..."
      Log.d("SavingsViewModel", "Starting SMS Summarization")
      try {
        // Delegate SMS reading/parsing to the parser
        val transactions = smsParser.readTransactionsFromSms(contentResolver)

        if (transactions.isNotEmpty()) {
          // Insert into db off main thread
          insertTransactions(transactions)
          _summaryStatus.value = "Added ${transactions.size} transactions."
          Log.i("SavingsViewModel", "Successfully added ${transactions.size} transactions.")
          
          // Refresh filtered transactions after adding new ones
          refreshTransactionsForSelectedDate()
        } else {
          _summaryStatus.value = "No new transactions found in SMS."
          Log.i("SavingsViewModel", "No new transactions found.")
        }
      } catch (e: SecurityException) {
        Log.e("SavingsViewModel", "SMS Read Permission Denied", e)
        _summaryStatus.value = "Error: SMS Read Permission Denied."
      } catch (e: Exception) {
        Log.e("SavingsViewModel", "Error during SMS summarization", e)
        _summaryStatus.value = "Error processing SMS: ${e.message}"
      } finally {
        _isLoading.value = false // Hide loading
        Log.d("SavingsViewModel", "Finished SMS Summarization attempt.")
      }
    }
  }

  // Insert transactions into database
  private suspend fun insertTransactions(transactions: List<Transaction>) {
    withContext(Dispatchers.IO) { // Use IO thread
      try {
        transactionDao.insertTransactions(transactions)
      } catch(e: Exception) {
        Log.e("SavingsViewModel", "Error inserting transactions", e)
        // Update status maybe?
        _summaryStatus.value = "DB Error while saving."
      }
    }
  }

  // Clear status message
  fun clearSummaryStatus() {
    _summaryStatus.value = null
  }
  
  // Get count of uncategorized transactions
  val uncategorizedCount: StateFlow<Int> = transactionDao.getUncategorizedCount()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = 0
    )
  
  // Initialize state when ViewModel is created
  init {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        updateSelectedDate(LocalDate.now())
        
        // Log initialization success
        Log.d("SavingsViewModel", "Successfully initialized SavingsViewModel")
      } catch (e: Exception) {
        // Log any initialization errors
        Log.e("SavingsViewModel", "Error initializing SavingsViewModel: ${e.message}", e)
        _summaryStatus.value = "Database error: ${e.message}. Try reinstalling the app if this persists."
      }
    }
  }
}