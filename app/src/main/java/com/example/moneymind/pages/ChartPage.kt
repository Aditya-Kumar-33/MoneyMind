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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymind.AuthViewModel
import com.example.moneymind.data.Transaction
import com.example.moneymind.utils.accessibilityHeading
import com.example.moneymind.utils.accessibleClickable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// Data class for category display
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
                transactions = emptyList()
            ),
            Category(
                name = "Travel",
                color = Color(0xFF4285F4), // Blue
                icon = Icons.Default.DirectionsCar,
                transactions = emptyList()
            ),
            Category(
                name = "Household",
                color = Color(0xFF34A853), // Green
                icon = Icons.Default.Home, // Better suited than House
                transactions = emptyList()
            ),
            Category(
                name = "Lifestyle",
                color = Color(0xFF9C27B0), // Purple
                icon = Icons.Default.Style,
                transactions = emptyList()
            ),
            Category(
                name = "HealthCare",
                color = Color(0xFFEA4335), // Red
                icon = Icons.Default.MedicalServices, // Alternative to HealthAndSafety
                transactions = emptyList()
            ),
            Category(
                name = "Miscellaneous",
                color = Color(0xFF607D8B), // Blue Grey
                icon = Icons.Default.Category,
                transactions = emptyList()
            )
        )
    }

    val dummyIncomeCategories = remember {
        listOf(
            Category(
                name = "Salary",
                color = Color(0xFF4285F4), // Blue
                icon = Icons.Default.AttachMoney, // Represents income/salary
                transactions = emptyList()
            ),
            Category(
                name = "Business",
                color = Color(0xFF0F9D58), // Green
                icon = Icons.Default.Store, // Storefront icon fits business
                transactions = emptyList()
            ),
            Category(
                name = "Investments",
                color = Color(0xFFF4B400), // Amber/Gold
                icon = Icons.Default.TrendingUp, // Investment growth
                transactions = emptyList()
            ),
            Category(
                name = "Passive & Other Income",
                color = Color(0xFF9C27B0), // Purple
                icon = Icons.Default.Payments, // General income symbol
                transactions = emptyList()
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
                            Color(0xFF0A0F0C)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top section with header and tabs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with month navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Analytics",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.accessibilityHeading()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.semantics {
                                contentDescription = "Previous month, currently showing $currentMonth"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous month",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = currentMonth,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = {},
                            modifier = Modifier.semantics {
                                contentDescription = "Next month, currently showing $currentMonth"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next month",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Expense/Income Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1F1B)),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Tab(
                        text = "Expenses",
                        isSelected = isExpenseTab,
                        onClick = { isExpenseTab = true },
                        accessibilityLabel = "Show expenses tab, ${if (isExpenseTab) "currently selected" else "not selected"}"
                    )
                    Tab(
                        text = "Income",
                        isSelected = !isExpenseTab,
                        onClick = { isExpenseTab = false },
                        accessibilityLabel = "Show income tab, ${if (!isExpenseTab) "currently selected" else "not selected"}"
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Pie Chart section
                PieChartSection(
                    categories = if (isExpenseTab) dummyExpenseCategories else dummyIncomeCategories,
                    totalAmount = if (isExpenseTab) 630.55 else 6870.00,
                    isExpense = isExpenseTab
                )
            }

            // Bottom section with transactions list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                item {
                    Text(
                        text = "Breakdown",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .semantics { heading() }
                    )
                }

                items(if (isExpenseTab) dummyExpenseCategories else dummyIncomeCategories) { category ->
                    CategoryItem(
                        category = category,
                        amount = when (category.name) {
                            "Food" -> 72.50
                            "Travel" -> 49.30
                            "Household" -> 305.50
                            "Lifestyle" -> 88.00
                            "HealthCare" -> 75.50
                            "Salary" -> 3500.00
                            "Business" -> 2400.00
                            "Investments" -> 820.00
                            "Passive & Other Income" -> 150.00
                            else -> 43.75
                        },
                        percentage = when (category.name) {
                            "Food" -> 11.5
                            "Travel" -> 7.8
                            "Household" -> 48.5
                            "Lifestyle" -> 14.0
                            "HealthCare" -> 12.0
                            "Salary" -> 51.0
                            "Business" -> 35.0
                            "Investments" -> 12.0
                            "Passive & Other Income" -> 2.0
                            else -> 6.2
                        },
                        isExpense = isExpenseTab
                    )
                }
            }
        }
    }
}

@Composable
fun Tab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accessibilityLabel: String
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF81A38A) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 24.dp)
            .semantics {
                contentDescription = accessibilityLabel
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun PieChartSection(
    categories: List<Category>,
    totalAmount: Double,
    isExpense: Boolean
) {
    // Formatted amount with currency symbol
    val formattedAmount = String.format("$%.2f", totalAmount)
    val formattedLabel = if (isExpense) "Total Expense" else "Total Income"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(top = 16.dp)
            .semantics {
                contentDescription = "$formattedLabel: $formattedAmount. Chart showing ${categories.size} categories."
            },
        contentAlignment = Alignment.Center
    ) {
        // This is a simplified pie chart implementation
        val chartSize = 200.dp
        Canvas(
            modifier = Modifier.size(chartSize)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = canvasWidth / 2.5f
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2
            
            var startAngle = 0f
            
            categories.forEachIndexed { index, category ->
                val sweepAngle = when (category.name) {
                    "Food" -> 41.4f
                    "Travel" -> 28.08f
                    "Household" -> 174.6f
                    "Lifestyle" -> 50.4f
                    "HealthCare" -> 43.2f
                    "Salary" -> 183.6f
                    "Business" -> 126.0f
                    "Investments" -> 43.2f
                    "Passive & Other Income" -> 7.2f
                    else -> 22.32f
                }
                
                // Draw the arc
                drawArc(
                    color = category.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 40f, cap = StrokeCap.Round)
                )
                
                startAngle += sweepAngle
            }
        }
        
        // Center text showing total
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics {
                contentDescription = "Total ${if (isExpense) "expense" else "income"}: $formattedAmount"
            }
        ) {
            Text(
                text = formattedAmount,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formattedLabel,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    amount: Double,
    percentage: Double,
    isExpense: Boolean
) {
    val formattedAmount = String.format("$%.2f", amount)
    val formattedPercentage = String.format("%.1f%%", percentage)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .semantics {
                contentDescription = "${category.name}: $formattedAmount, ${formattedPercentage} of total ${if (isExpense) "expenses" else "income"}"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category color indicator and icon
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = category.color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Category name and percentage
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Text(
                text = formattedPercentage,
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
        
        // Amount
        Text(
            text = formattedAmount,
            color = if (isExpense) Color(0xFFEA4335) else Color(0xFF34A853),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
} 