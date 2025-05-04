package com.example.moneymind.data // Or your preferred package

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

// Define Transaction type enum
enum class TransactionType {
  CREDIT, DEBIT
}

@Entity(
  tableName = "transactions",
  // Use date and arrival time for primary key
  primaryKeys = ["transaction_date", "sms_timestamp"]
)
data class Transaction(
  @ColumnInfo(name = "amount")
  val amount: Double,

  @ColumnInfo(name = "type")
  val type: TransactionType, // CREDIT or DEBIT

  // Date parsed from SMS content
  @ColumnInfo(name = "transaction_date")
  val transactionDate: String, // Store as ISO String (YYYY-MM-DD)

  // SMS arrival time (epoch milliseconds)
  @ColumnInfo(name = "sms_timestamp")
  val smsTimestamp: Long,

  @ColumnInfo(name = "description")
  val description: String?, // Optional description parsed

  @ColumnInfo(name = "account_identifier")
  val accountIdentifier: String?, // Optional masked account
  
  @ColumnInfo(name = "category")
  val category: String = TransactionCategories.UNCATEGORIZED // Default to uncategorized
) {
  // Helper for LocalDateTime from timestamp
  @RequiresApi(Build.VERSION_CODES.O)
  fun getSmsArrivalDateTime(): LocalDateTime {
    return LocalDateTime.ofEpochSecond(smsTimestamp / 1000, (smsTimestamp % 1000).toInt() * 1_000_000, ZoneOffset.UTC)
  }
  
  // Helper to create a copy with updated category
  fun withCategory(newCategory: String): Transaction {
    return copy(category = newCategory)
  }
  
  // Check if transaction is categorized
  fun isCategorized(): Boolean {
    return category != TransactionCategories.UNCATEGORIZED
  }
}