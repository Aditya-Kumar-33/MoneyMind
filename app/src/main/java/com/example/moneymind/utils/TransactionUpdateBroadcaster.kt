package com.example.moneymind.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * A singleton broadcaster for transaction updates.
 * This allows different parts of the app to stay in sync when transactions are modified.
 */
object TransactionUpdateBroadcaster {
    
    // Coroutine scope for broadcasting events
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Events that can be broadcast
    sealed class UpdateEvent {
        // A transaction was added, updated or deleted
        object TransactionChanged : UpdateEvent()
        
        // A transaction category was updated
        data class CategoryChanged(val transactionId: Long) : UpdateEvent()
    }
    
    private val _events = MutableSharedFlow<UpdateEvent>(replay = 0)
    val events: SharedFlow<UpdateEvent> = _events.asSharedFlow()
    
    /**
     * Notify listeners that a transaction was changed (non-suspend version)
     */
    fun notifyTransactionChangedAsync() {
        scope.launch {
            try {
                _events.emit(UpdateEvent.TransactionChanged)
                Log.d("TransactionBroadcaster", "Broadcasting transaction changed event")
            } catch (e: Exception) {
                Log.e("TransactionBroadcaster", "Error broadcasting transaction changed event", e)
            }
        }
    }
    
    /**
     * Notify listeners that a transaction category was changed (non-suspend version)
     */
    fun notifyCategoryChangedAsync(transactionId: Long) {
        scope.launch {
            try {
                _events.emit(UpdateEvent.CategoryChanged(transactionId))
                Log.d("TransactionBroadcaster", "Broadcasting category changed event for transaction $transactionId")
            } catch (e: Exception) {
                Log.e("TransactionBroadcaster", "Error broadcasting category changed event", e)
            }
        }
    }
    
    /**
     * Notify listeners that a transaction was changed
     */
    suspend fun notifyTransactionChanged() {
        Log.d("TransactionBroadcaster", "Broadcasting transaction changed event")
        _events.emit(UpdateEvent.TransactionChanged)
    }
    
    /**
     * Notify listeners that a transaction category was changed
     */
    suspend fun notifyCategoryChanged(transactionId: Long) {
        Log.d("TransactionBroadcaster", "Broadcasting category changed event for transaction $transactionId")
        _events.emit(UpdateEvent.CategoryChanged(transactionId))
    }
} 