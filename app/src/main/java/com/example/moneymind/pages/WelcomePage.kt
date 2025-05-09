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
import androidx.compose.ui.res.stringResource
import com.example.moneymind.R

@Composable
fun Welcome(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    var isClicked by remember { mutableStateOf(false) }
    var isAnimationFinished by remember { mutableStateOf(false) }

    val animatedHeightFraction by animateFloatAsState(
        targetValue = if (isClicked) 0.7f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "boxHeightAnimation"
    )

    val animatedCorner by animateDpAsState(
        targetValue = if (isClicked) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 600),
        label = "boxCornerAnimation"
    )

    LaunchedEffect(animatedHeightFraction) {
        isAnimationFinished = animatedHeightFraction == 1f
    }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 160.dp, start = 24.dp, end = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Left,
                lineHeight = 40.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (isAnimationFinished && !isClicked) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 550.dp, start = 24.dp, end = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = stringResource(id = R.string.tagline),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Left,
                    lineHeight = 40.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.description),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.LightGray,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

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
                    Text(
                        text = stringResource(id = R.string.lets_start),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navController.navigate("signup") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF81A38A)
                    )
                ) {
                    Text(text = stringResource(id = R.string.sign_up))
                }
                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF81A38A)
                    )
                ) {
                    Text(text = stringResource(id = R.string.log_in))
                }
            }
        }
    }
}
