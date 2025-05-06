package com.example.moneymind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: TransactionCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<TransactionCategory>)

    @Query("SELECT * FROM transaction_categories WHERE type = :type")
    fun getCategoriesByType(type: String): Flow<List<TransactionCategory>>

    @Query("SELECT * FROM transaction_categories")
    fun getAllCategories(): Flow<List<TransactionCategory>>

    @Query("DELETE FROM transaction_categories WHERE name = :name")
    suspend fun deleteCategory(name: String)
} 