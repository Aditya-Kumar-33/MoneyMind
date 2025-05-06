package com.example.moneymind.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object for SavingsRecord table
 */
@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings_records ORDER BY date DESC")
    fun getAllRecords(): LiveData<List<SavingsRecord>>
    
    @Query("SELECT * FROM savings_records WHERE id = :id")
    suspend fun getRecordById(id: Long): SavingsRecord?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SavingsRecord): Long
    
    @Update
    suspend fun updateRecord(record: SavingsRecord)
    
    @Delete
    suspend fun deleteRecord(record: SavingsRecord)
} 