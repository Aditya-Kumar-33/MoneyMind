package com.example.moneymind

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    // Additional state for UI consumption
    val isAuthenticated = mutableStateOf(false)

    init {
        // Check auth status on init
        checkAuthStatus()

        // Update the isAuthenticated state based on authState changes
        authState.observeForever { state ->
            isAuthenticated.value = state is AuthState.Authenticated
        }
    }

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
            isAuthenticated.value = false
        } else {
            _authState.value = AuthState.Authenticated
            isAuthenticated.value = true
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.postValue(AuthState.Authenticated)
                    isAuthenticated.value = true
                } else {
                    _authState.postValue(AuthState.Error("Authentication failed: ${task.exception?.localizedMessage}"))
                    isAuthenticated.value = false
                }
            }
    }

    fun signup(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.postValue(AuthState.Authenticated)
                    isAuthenticated.value = true
                } else {
                    _authState.postValue(AuthState.Error("Sign-up failed: ${task.exception?.localizedMessage}"))
                    isAuthenticated.value = false
                }
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        isAuthenticated.value = false
    }

    // Helper function to get current user
    fun getCurrentUser() = auth.currentUser
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}