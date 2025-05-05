package com.example.moneymind.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "savings_records")
data class SavingsRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String, // Firebase UID
    val amount: Double,
    val type: String, // "Income" or "Expense"
    val category: String,
    val payment: String,
    val subCategory: String?, // optional
    val description: String?, // optional
    val date: LocalDate,
    val time: LocalTime
)

//userId: use FirebaseAuth.getInstance().currentUser?.uid
