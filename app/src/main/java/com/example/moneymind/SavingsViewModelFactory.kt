package com.example.moneymind // Or your viewmodel package

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneymind.data.AppDatabase
import com.example.moneymind.util.SmsTransactionParser // Import the parser

// Factory now provides both DAO and Parser
class SavingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(SavingsViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      // Get DAO from database instance
      val dao = AppDatabase.getDatabase(context.applicationContext).transactionDao()
      // Create instance of the parser
      val smsParser = SmsTransactionParser()
      // Provide both to the ViewModel
      return SavingsViewModel(dao, smsParser) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
  }
}