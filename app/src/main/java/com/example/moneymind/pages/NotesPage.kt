package com.example.moneymind.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymind.AuthViewModel
import com.example.moneymind.utils.accessibilityHeading

@Composable
fun NotesPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Notes Page",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier
                .padding(16.dp)
                .accessibilityHeading() // Mark as heading for screen readers
                .semantics {
                    // Additional semantics for better screen reader support
                    heading()
                }
        )
    }
} 