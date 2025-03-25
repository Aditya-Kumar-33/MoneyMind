package com.example.moneymind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymind.ui.theme.MoneyMindTheme

class InputFieldActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneyMindTheme {
                InputFieldPage()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputFieldPage() {
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
                    .padding(16.dp)
            ) {
                // Material3 colors for outlined text field
                val borderColor = Color(0xFF323F36)
                val focusedBorderColor = Color(0xFF81A38A)

                var fullName by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var phoneNumber by remember { mutableStateOf("") }
                var country by remember { mutableStateOf("") }
                var currency by remember { mutableStateOf("") }

                // Input boxes with Material3 styling
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = { Text("User's full name") },
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
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email Address") },
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
                    placeholder = { Text("Password") },
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
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = { Text("Phone Number (Optional)") },
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        placeholder = { Text("Country") },
                        modifier = Modifier
                            .weight(1f),
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
                        value = currency,
                        onValueChange = { currency = it },
                        placeholder = { Text("Currency") },
                        modifier = Modifier
                            .weight(1f),
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
                }
            }
        }

        // Button at the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InputFieldPagePreview() {
    MoneyMindTheme {
        InputFieldPage()
    }
}