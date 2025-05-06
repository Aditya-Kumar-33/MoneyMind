package com.example.moneymind

import android.app.Application
import android.content.Context
import android.util.Log
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Custom Application class for MoneyMind app
 * Handles global error handling and database initialization
 */
class MoneyMindApplication : Application() {
    
    private val executor: Executor = Executors.newSingleThreadExecutor()
    
    override fun onCreate() {
        super.onCreate()
        
        // Set up database error handling
        setupDatabaseErrorHandling()
    }
    
    /**
     * Set up a global uncaught exception handler to handle database errors
     * If a database error occurs, mark it for recovery on next app start
     */
    private fun setupDatabaseErrorHandling() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Check if it's a database-related error
            if (isDatabaseError(throwable)) {
                Log.e("MoneyMindApplication", "Database error detected", throwable)
                
                // Mark database for recovery on next app start
                executor.execute {
                    try {
                        val sharedPrefs = getSharedPreferences("database_settings", Context.MODE_PRIVATE)
                        sharedPrefs.edit().putBoolean("needs_recovery", true).apply()
                        Log.i("MoneyMindApplication", "Marked database for recovery")
                    } catch (e: Exception) {
                        Log.e("MoneyMindApplication", "Failed to mark database for recovery", e)
                    }
                }
            }
            
            // Let the default handler handle the exception
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * Check if an exception is related to database errors
     */
    private fun isDatabaseError(throwable: Throwable): Boolean {
        // Check the exception and its causes
        var cause: Throwable? = throwable
        while (cause != null) {
            // Check for typical database error classes by name
            val errorMessage = cause.toString().lowercase()
            if (errorMessage.contains("sqlite") || 
                errorMessage.contains("database") ||
                errorMessage.contains("room") || 
                errorMessage.contains("sql") ||
                errorMessage.contains("migration")) {
                return true
            }
            cause = cause.cause
            // Avoid infinite loops
            if (cause == throwable) break
        }
        return false
    }
} 