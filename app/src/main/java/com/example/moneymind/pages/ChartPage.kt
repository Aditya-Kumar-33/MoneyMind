package com.example.moneymind.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymind.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// Data classes
data class Transaction(
    val id: String,
    val category: String,
    val amount: Double,
    val date: Date,
    val isExpense: Boolean = true
)

data class Category(
    val name: String,
    val color: Color,
    val icon: ImageVector,
    val transactions: List<Transaction> = emptyList()
)

@Composable
fun ChartPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel,
              onBackPressed: () -> Unit = {}
) {
    // Create dummy data
    val dummyExpenseCategories = remember {
        listOf(
            Category(
                name = "Food",
                color = Color(0xFFFBBC05), // Yellow
                icon = Icons.Default.Fastfood,
                transactions = List(10) {
                    Transaction(
                        id = "food_$it",
                        category = "Food",
                        amount = 72.50,
                        date = Date(),
                        isExpense = true
                    )
                }
            ),
            Category(
                name = "Travel",
                color = Color(0xFF4285F4), // Blue
                icon = Icons.Default.DirectionsCar,
                transactions = List(5) {
                    Transaction(
                        id = "travel_$it",
                        category = "Travel",
                        amount = 49.30,
                        date = Date(),
                        isExpense = true
                    )
                }
            ),
            Category(
                name = "Household",
                color = Color(0xFF34A853), // Green
                icon = Icons.Default.Home, // Better suited than House
                transactions = List(3) {
                    Transaction(
                        id = "household_$it",
                        category = "Household",
                        amount = 101.50,
                        date = Date(),
                        isExpense = true
                    )
                }
            ),
            Category(
                name = "Lifestyle",
                color = Color(0xFF9C27B0), // Purple
                icon = Icons.Default.Style,
                transactions = List(4) {
                    Transaction(
                        id = "lifestyle_$it",
                        category = "Lifestyle",
                        amount = 88.00,
                        date = Date(),
                        isExpense = true
                    )
                }
            ),
            Category(
                name = "HealthCare",
                color = Color(0xFFEA4335), // Red
                icon = Icons.Default.MedicalServices, // Alternative to HealthAndSafety
                transactions = List(2) {
                    Transaction(
                        id = "healthcare_$it",
                        category = "HealthCare",
                        amount = 275.50,
                        date = Date(),
                        isExpense = true
                    )
                }
            ),
            Category(
                name = "Miscellaneous",
                color = Color(0xFF607D8B), // Blue Grey
                icon = Icons.Default.Category,
                transactions = List(2) {
                    Transaction(
                        id = "misc_$it",
                        category = "Miscellaneous",
                        amount = 43.75,
                        date = Date(),
                        isExpense = true
                    )
                }
            )
        )
    }


    val dummyIncomeCategories = remember {
        listOf(
            Category(
                name = "Salary",
                color = Color(0xFF4285F4), // Blue
                icon = Icons.Default.AttachMoney, // Represents income/salary
                transactions = List(2) {
                    Transaction(
                        id = "salary_$it",
                        category = "Salary",
                        amount = 3500.00,
                        date = Date(),
                        isExpense = false
                    )
                }
            ),
            Category(
                name = "Business",
                color = Color(0xFF0F9D58), // Green
                icon = Icons.Default.Store, // Storefront icon fits business
                transactions = List(2) {
                    Transaction(
                        id = "business_$it",
                        category = "Business",
                        amount = 2400.00,
                        date = Date(),
                        isExpense = false
                    )
                }
            ),
            Category(
                name = "Investments",
                color = Color(0xFFF4B400), // Amber/Gold
                icon = Icons.Default.TrendingUp, // Investment growth
                transactions = List(1) {
                    Transaction(
                        id = "investments_$it",
                        category = "Investments",
                        amount = 820.00,
                        date = Date(),
                        isExpense = false
                    )
                }
            ),
            Category(
                name = "Passive & Other Income",
                color = Color(0xFF9C27B0), // Purple
                icon = Icons.Default.Payments, // General income symbol
                transactions = List(3) {
                    Transaction(
                        id = "passive_$it",
                        category = "Passive & Other Income",
                        amount = 150.00,
                        date = Date(),
                        isExpense = false
                    )
                }
            )
        )
    }


    var isExpenseTab by remember { mutableStateOf(true) }
    val currentMonth = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070906))
    ) {
        // Background gradient with blur effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
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
                    val diameter = size.width *.9f
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
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, top = 40.dp)
        ) {

            // Expense/Income tabs
            TabRow(
                selectedTabIndex = if (isExpenseTab) 0 else 1,
                containerColor = Color.Black,
                contentColor = Color.White,
                indicator = {},
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Tab(
                    selected = isExpenseTab,
                    onClick = { isExpenseTab = true },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isExpenseTab) Color(0xFF81A38A) else Color.Transparent)
                ) {
                    Text(
                        text = "Expense",
                        color = if (isExpenseTab) Color.White else Color.Gray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Tab(
                    selected = !isExpenseTab,
                    onClick = { isExpenseTab = false },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (!isExpenseTab) Color(0xFF81A38A) else Color.Transparent)
                ) {
                    Text(
                        text = "Income",
                        color = if (!isExpenseTab) Color.White else Color.Gray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Month selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentMonth,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pie chart box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                val categories = if (isExpenseTab) dummyExpenseCategories else dummyIncomeCategories
                val totalAmount = categories.sumOf { category ->
                    category.transactions.sumOf { it.amount }
                }

                Canvas(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                ) {
                    var startAngle = 0f
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    categories.forEach { category ->
                        val categoryAmount = category.transactions.sumOf { it.amount }
                        val sweepAngle = (categoryAmount / totalAmount * 360f).toFloat()
                        val percentage = (categoryAmount / totalAmount * 100).toInt()

                        // Draw the arc
                        drawArc(
                            color = category.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 50f, cap = StrokeCap.Butt)
                        )

                        // Compute mid angle for label
                        val midAngle = startAngle + sweepAngle / 2f
                        val labelRadius = radius + 80f
                        val labelX = center.x + cos(Math.toRadians(midAngle.toDouble())) * labelRadius
                        val labelY = center.y + sin(Math.toRadians(midAngle.toDouble())) * labelRadius

                        // Draw the label as text
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                "$percentage%",
                                labelX.toFloat(),
                                labelY.toFloat(),
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 40f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                    isFakeBoldText = true
                                }
                            )
                        }

                        startAngle += sweepAngle
                    }
                }


                // Center text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total ${if (isExpenseTab) "Expenses" else "Income"}",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "₹${String.format("%.2f", totalAmount)}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


            Spacer(modifier = Modifier.height(60.dp))

            // List of categories with details
            LazyColumn {
                items(if (isExpenseTab) dummyExpenseCategories else dummyIncomeCategories) { category ->
                    val categoryAmount = category.transactions.sumOf { it.amount }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end=18.dp)
                            .clickable { /* Navigate to category details */ },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(top = 18.dp, bottom = 18.dp)
                                .clickable {  },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(category.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = category.name,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = "${category.transactions.size} transaction${if (category.transactions.size > 1) "s" else ""}",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isExpenseTab) "-₹${String.format("%.2f", categoryAmount)}" else "+₹${String.format("%.2f", categoryAmount)}",
                                color = if (isExpenseTab) Color.White else Color(0xFF34A853),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}