package com.example.moneymind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_categories")
data class TransactionCategory(
    @PrimaryKey
    val name: String,
    val type: String // "CREDIT" or "DEBIT"
) 