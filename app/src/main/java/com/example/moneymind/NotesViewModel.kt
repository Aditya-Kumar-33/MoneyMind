package com.example.moneymind

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.moneymind.data.AppDatabase
import com.example.moneymind.data.Note
import com.example.moneymind.data.NoteDao
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

/**
 * ViewModel for handling Note-related operations
 */
class NotesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val noteDao: NoteDao
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    // LiveData for user's notes
    private val _userNotes = MutableLiveData<List<Note>>(emptyList())
    val userNotes: LiveData<List<Note>> get() = _userNotes
    
    // For storing text input
    val inputText = MutableLiveData<String>("")
    
    // Error message LiveData
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> get() = _errorMessage
    
    init {
        val database = AppDatabase.getDatabase(application)
        noteDao = database.noteDao()
        
        // Initial setup - get current user's notes
        updateUserNotes()
        
        // Listen for auth state changes and update notes
        auth.addAuthStateListener { 
            updateUserNotes()
        }
    }
    
    /**
     * Update the user's notes based on the current auth state
     */
    private fun updateUserNotes() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId.isNotEmpty()) {
                // We have a logged-in user, get their notes
                val notes = withContext(Dispatchers.IO) {
                    noteDao.getNotesByUserIdSync(userId)
                }
                _userNotes.value = notes
            } else {
                // No user logged in, show empty list
                _userNotes.value = emptyList()
            }
        }
    }
    
    /**
     * Get the current user ID or empty string if not logged in
     */
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }
    
    /**
     * Insert a new note with the given text
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertNote(text: String) {
        if (text.isBlank()) return
        
        // Get current user ID
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            _errorMessage.value = "You must be logged in to create notes"
            return
        }
        
        viewModelScope.launch {
            val note = Note(
                userId = userId,
                text = text,
                createdAt = Instant.now().toEpochMilli(),
                updatedAt = Instant.now().toEpochMilli()
            )
            withContext(Dispatchers.IO) {
                noteDao.insertNote(note)
            }
            // Clear input text after successful insertion
            inputText.value = ""
            _errorMessage.value = null
            
            // Update the notes list
            updateUserNotes()
        }
    }
    
    /**
     * Delete a note by ID
     */
    fun deleteNote(id: Long) {
        viewModelScope.launch {
            // First check if the note belongs to the current user
            val note = withContext(Dispatchers.IO) {
                noteDao.getNoteById(id)
            }
            
            // Only allow deletion if the note belongs to the current user
            if (note != null && note.userId == getCurrentUserId()) {
                withContext(Dispatchers.IO) {
                    noteDao.deleteNoteById(id)
                }
                _errorMessage.value = null
                
                // Update the notes list
                updateUserNotes()
            } else {
                _errorMessage.value = "You can only delete your own notes"
            }
        }
    }
    
    /**
     * Update input text
     */
    fun updateInputText(text: String) {
        inputText.value = text
    }
    
    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
} 