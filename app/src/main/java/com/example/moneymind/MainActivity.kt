package com.example.moneymind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.example.moneymind.ui.theme.MoneyMindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()

        setContent {
            MoneyMindTheme {
                // The Scaffold is now moved to NavigationController for better control
                NavigationController(authViewModel = authViewModel)
            }
        }
    }
}

