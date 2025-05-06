package com.example.moneymind

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneymind.data.Transaction
import com.example.moneymind.data.TransactionDao
import com.example.moneymind.utils.TransactionUpdateBroadcaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionViewModel(private val transactionDao: TransactionDao) : ViewModel() {
    
    // Get all transactions
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    /**
     * Add a new transaction to the database
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    transactionDao.insertTransaction(transaction)
                    Log.d("TransactionViewModel", "Added new transaction: $transaction")
                }
                
                // Notify other components that a transaction was added
                TransactionUpdateBroadcaster.notifyTransactionChangedAsync()
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error adding transaction: ${e.message}", e)
            }
        }
    }
    
    /**
     * Delete a transaction
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    transactionDao.deleteTransaction(transaction.smsTimestamp)
                    Log.d("TransactionViewModel", "Deleted transaction: ${transaction.smsTimestamp}")
                }
                
                // Notify other components that a transaction was deleted
                TransactionUpdateBroadcaster.notifyTransactionChangedAsync()
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error deleting transaction: ${e.message}", e)
            }
        }
    }
    
    /**
     * Update a transaction's category
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateTransactionCategory(transaction: Transaction, newCategory: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    transactionDao.updateTransactionCategory(
                        transaction.transactionDate,
                        transaction.smsTimestamp,
                        newCategory
                    )
                    Log.d("TransactionViewModel", "Updated category for transaction: $newCategory")
                }
                
                // Notify listeners about the category change
                TransactionUpdateBroadcaster.notifyCategoryChangedAsync(transaction.smsTimestamp)
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error updating category: ${e.message}", e)
            }
        }
    }
    
    /**
     * Get transactions for a specific date
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTransactionsByDate(date: LocalDate): List<Transaction> {
        return withContext(Dispatchers.IO) {
            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            transactionDao.getTransactionsByDate(dateString)
        }
    }
}

/**
 * Factory for creating a TransactionViewModel with a dependency
 */
class TransactionViewModelFactory(private val transactionDao: TransactionDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(transactionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 