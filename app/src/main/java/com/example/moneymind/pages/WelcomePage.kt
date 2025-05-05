package com.example.moneymind.pages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymind.AuthViewModel

@Composable
fun Welcome(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    var isClicked by remember { mutableStateOf(false) }
    var isAnimationFinished by remember { mutableStateOf(false) }

    val animatedHeightFraction by animateFloatAsState(
        targetValue = if (isClicked) 0.7f else 1f,
        animationSpec = tween(durationMillis = 600), // Adjust animation speed
        label = "boxHeightAnimation"
    )

    val animatedCorner by animateDpAsState(
        targetValue = if (isClicked) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 600), // Adjust animation speed
        label = "boxCornerAnimation"
    )


    LaunchedEffect(animatedHeightFraction) {
        isAnimationFinished = animatedHeightFraction == 1f
    }

    // Handle back press
    BackHandler(enabled = isClicked) {
        isClicked = false
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(animatedHeightFraction)
            .clip(RoundedCornerShape(bottomStart = animatedCorner, bottomEnd = animatedCorner))
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
        // Blurred semi-circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(150.dp) // Adjust blur intensity as needed
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val diameter = size.width * 0.9f
                val shiftRight = size.width / 2  // Shift by half the screen width

                drawArc(
                    color = Color(0xFF323F36),
                    startAngle = 90f,
                    sweepAngle = 180f,
                    useCenter = true,
                    size = Size(diameter, diameter),
                    topLeft = Offset(size.width - diameter + shiftRight, 0f) // Shift right
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 160.dp, start = 24.dp, end = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Align texts at the top
        ) {
            Text(
                text = "MoneyMind",
                fontSize = 50.sp, // Adjust font size
                fontWeight = FontWeight.Bold, // Make text bold
                color = Color.White, // Text color
                textAlign = TextAlign.Left, // Align text to the left
                lineHeight = 40.sp, // Adjust line spacing (increase value for more spacing)
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (isAnimationFinished && !isClicked) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 550.dp, start = 24.dp, end = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top // Align texts at the top
            ) {
                Text(
                    text = "Every expense, effortlessly managed.",
                    fontSize = 30.sp, // Adjust font size
                    fontWeight = FontWeight.Bold, // Make text bold
                    color = Color.White, // Text color
                    textAlign = TextAlign.Left, // Align text to the left
                    lineHeight = 40.sp, // Adjust line spacing (increase value for more spacing)
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "An app that empowers mindful spenders to track expenses, save efficiently, and make smarter financial decisions effortlessly.",
                    fontSize = 18.sp, // Adjust font size
                    fontWeight = FontWeight.Medium,
                    color = Color.LightGray, // Light gray text color for contrast
                    textAlign = TextAlign.Left, // Center align text
                    modifier = Modifier
                        .fillMaxWidth(1f) // Adjust text width
                )
            }
        }

        // "Let's Start" button (only when fully expanded)
        if (isAnimationFinished && !isClicked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = { isClicked = true },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF81A38A),
                    )
                ) {
                    Text(text = "Let's Start", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }


    if (isClicked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp), // Add spacing between buttons
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        navController.navigate("signup")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF81A38A), // Custom button color
                    )
                ) {
                    Text(text = "Sign Up")
                }
                Button(
                    onClick = {
                        navController.navigate("login")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF81A38A), // Custom button color
                    )
                ) {
                    Text(text = "Log In")
                }
            }
        }
    }

}
