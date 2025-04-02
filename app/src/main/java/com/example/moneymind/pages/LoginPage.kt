package com.example.moneymind.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymind.AuthViewModel

@Composable
fun Login(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel)
{

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background with gradient
        Box(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF161C18),
                            Color(0xFF080A07),
                            Color(0xFF070906)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(150.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val diameter = size.width * 0.9f
                    val shiftRight = size.width / 2

                    drawArc(
                        color = Color(0xFF323F36),
                        startAngle = 90f,
                        sweepAngle = 180f,
                        useCenter = true,
                        size = Size(diameter, diameter),
                        topLeft = Offset(size.width - diameter + shiftRight, 0f)
                    )
                }
            }

            // Content column with text fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Material3 colors for outlined text field
                val borderColor = Color(0xFF323F36)
                val focusedBorderColor = Color(0xFF81A38A)


                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

                // Input boxes with Material3 styling


                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label={
                        Text("Email")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = borderColor,
                        focusedBorderColor = focusedBorderColor,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedPlaceholderColor = Color(0xBBBBBBBB),
                        focusedPlaceholderColor = Color(0xBBBBBBBB)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label={
                        Text("Password")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = borderColor,
                        focusedBorderColor = focusedBorderColor,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedPlaceholderColor = Color(0xBBBBBBBB),
                        focusedPlaceholderColor = Color(0xBBBBBBBB)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = PasswordVisualTransformation()
                )

            }
        }

        // Button at the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF81A38A),
                    )
                ) {
                    Text(text = "Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Create new account?",
                    fontSize = 18.sp,
                    modifier = Modifier.clickable {
                        navController.navigate("signup")
                    }
                )
            }

        }
    }
}
