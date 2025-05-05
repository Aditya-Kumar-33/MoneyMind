package com.example.moneymind.data // Or your preferred package

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

  // Insert transaction, ignore conflicts
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertTransaction(transaction: Transaction)

  // Insert multiple transactions
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertTransactions(transactions: List<Transaction>)

  // Get all transactions (newest first)
  @Query("SELECT * FROM transactions ORDER BY sms_timestamp DESC")
  fun getAllTransactions(): Flow<List<Transaction>>

  // Get all transactions synchronously (newest first)
  @Query("SELECT * FROM transactions ORDER BY sms_timestamp DESC")
  suspend fun getAllTransactionsSync(): List<Transaction>

  // Get transactions for a specific date - by the transaction date field (not timestamp)
  @Query("SELECT * FROM transactions WHERE transaction_date = :date ORDER BY sms_timestamp DESC")
  suspend fun getTransactionsByDate(date: String): List<Transaction>
  
  // Get transactions for today - by the transaction date field
  @Query("SELECT * FROM transactions WHERE transaction_date = :todayDate ORDER BY sms_timestamp DESC")
  suspend fun getTodayTransactions(todayDate: String): List<Transaction>

  // Update transaction category
  @Query("UPDATE transactions SET category = :category WHERE transaction_date = :date AND sms_timestamp = :timestamp")
  suspend fun updateTransactionCategory(date: String, timestamp: Long, category: String)
  
  // Update transaction date
  @Query("UPDATE transactions SET transaction_date = :newDate WHERE sms_timestamp = :timestamp")
  suspend fun updateTransactionDate(timestamp: Long, newDate: String)
  
  // Delete a specific transaction
  @Query("DELETE FROM transactions WHERE sms_timestamp = :timestamp")
  suspend fun deleteTransaction(timestamp: Long)
  
  // Get uncategorized transactions count
  @Query("SELECT COUNT(*) FROM transactions WHERE category = :uncategorizedValue")
  fun getUncategorizedCount(uncategorizedValue: String = TransactionCategories.UNCATEGORIZED): Flow<Int>
  
  // Update transaction
  @Update
  suspend fun updateTransaction(transaction: Transaction)

  // Clear all transactions (optional)
  @Query("DELETE FROM transactions")
  suspend fun clearAllTransactions()
}