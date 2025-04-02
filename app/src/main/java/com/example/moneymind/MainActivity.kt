package com.example.moneymind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import com.example.moneymind.ui.theme.MoneyMindTheme
import androidx.compose.material3.Scaffold


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        setContent {
            MoneyMindTheme {
                Scaffold (modifier = Modifier.fillMaxSize()){ innerPadding ->
                    NavigationController(modifier = Modifier.padding(innerPadding),authViewModel=authViewModel)
                }
            }
        }
    }
}

