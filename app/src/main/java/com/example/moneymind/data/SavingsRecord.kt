package com.example.moneymind.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import java.time.LocalDate
import java.time.LocalTime
import androidx.room.RoomDatabase

@Entity(tableName = "savings")
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

@Dao
interface SavingsDao {
    @Insert
    suspend fun  insertSavings(savingsRecord: SavingsRecord)

    @Query("SELECT * FROM savings WHERE userId = :userId")
    suspend fun getSavingsRecord(userId: String): List<SavingsRecord>

    @Query("SELECT * FROM savings WHERE userId = :userId AND date = :date")
    suspend fun getTodaySavings(userId: String, date: LocalDate): List<SavingsRecord>

    @Query("DELETE FROM savings WHERE id = :id")
    suspend fun deleteSavingRecord(id: Int)
}

@Database(entities = [SavingsRecord::class], version = 1)
abstract class SavingsDatabase : RoomDatabase() {
    abstract fun savingsDao(): SavingsDao
}