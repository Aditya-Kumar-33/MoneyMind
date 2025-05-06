package com.example.moneymind.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.time.Instant

/**
 * Entity representing a note in the database
 * Notes are associated with users through userId
 */
@Entity(
    tableName = "notes",
    indices = [Index("userId")] // Index for faster queries by userId
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String, // Firebase user ID
    val text: String,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val updatedAt: Long = Instant.now().toEpochMilli()
) 