package com.example.moneymind.data

import androidx.room.Dao
// import androidx.room.Database // REMOVE THIS IMPORT
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import java.time.LocalDate
import java.time.LocalTime
// import androidx.room.RoomDatabase // REMOVE THIS IMPORT

// Entity definition remains the same
@Entity(tableName = "savings")
data class SavingsRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String, // Firebase UID
    val amount: Double, // Amount saved/spent
    val type: String, // "Income" or "Expense"
    val category: String, // Category of saving/expense
    val payment: String, // Payment method
    val subCategory: String?, // Optional subcategory
    val description: String?, // Optional description
    val date: LocalDate, // Date of record
    val time: LocalTime // Time of record
)

// DAO interface remains the same
@Dao
interface SavingsDao {
    @Insert
    suspend fun insertSavings(savingsRecord: SavingsRecord)

    @Query("SELECT * FROM savings WHERE userId = :userId")
    suspend fun getSavingsRecord(userId: String): List<SavingsRecord>

    // Query using LocalDate works because TypeConverter is registered in AppDatabase
    @Query("SELECT * FROM savings WHERE userId = :userId AND date = :date")
    suspend fun getTodaySavings(userId: String, date: LocalDate): List<SavingsRecord>

    @Query("DELETE FROM savings WHERE id = :id")
    suspend fun deleteSavingRecord(id: Int)
}

/*
// REMOVE the redundant Database definition from here
@Database(entities = [SavingsRecord::class], version = 1)
abstract class SavingsDatabase : RoomDatabase() {
    abstract fun savingsDao(): SavingsDao
}
*/
