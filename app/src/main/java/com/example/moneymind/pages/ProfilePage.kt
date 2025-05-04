package com.example.moneymind.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymind.AuthViewModel
import com.example.moneymind.utils.accessibilityHeading
import com.example.moneymind.utils.accessibleClickable

@Composable
fun ProfilePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile & Settings",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .accessibilityHeading() // Mark as heading for screen readers
            )

            // Display user email if available
            authViewModel.getCurrentUser()?.let { user ->
                Text(
                    text = "Email: ${user.email}",
                    modifier = Modifier.padding(8.dp)
                        .semantics {
                            contentDescription = "Your email is ${user.email}"
                        }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout button
            Button(
                onClick = {
                    authViewModel.signout()
                },
                modifier = Modifier.semantics {
                    contentDescription = "Sign out from your account"
                }
            ) {
                Text("Sign Out")
            }
        }
    }
} 