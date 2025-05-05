package com.example.moneymind.utils

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * A singleton broadcaster for transaction updates.
 * This allows different parts of the app to stay in sync when transactions are modified.
 */
object TransactionUpdateBroadcaster {
    
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