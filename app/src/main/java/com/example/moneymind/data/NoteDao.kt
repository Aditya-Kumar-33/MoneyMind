package com.example.moneymind.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object for Notes table
 */
@Dao
interface NoteDao {
    /**
     * Get all notes regardless of user (admin function)
     */
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): LiveData<List<Note>>
    
    /**
     * Get notes for a specific user (LiveData version)
     */
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getNotesByUserId(userId: String): LiveData<List<Note>>
    
    /**
     * Get notes for a specific user (suspend function version)
     */
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY updatedAt DESC")
    suspend fun getNotesByUserIdSync(userId: String): List<Note>
    
    /**
     * Get a specific note by ID
     */
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?
    
    /**
     * Insert a new note
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long
    
    /**
     * Update an existing note
     */
    @Update
    suspend fun updateNote(note: Note)
    
    /**
     * Delete a note
     */
    @Delete
    suspend fun deleteNote(note: Note)
    
    /**
     * Delete a note by ID
     */
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
    
    /**
     * Delete all notes for a specific user
     */
    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteAllNotesByUserId(userId: String)
} 