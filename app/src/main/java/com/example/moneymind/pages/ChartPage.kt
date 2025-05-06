package com.example.moneymind.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneymind.AuthViewModel
import com.example.moneymind.ChartViewModel
import com.example.moneymind.ChartViewModelFactory
import com.example.moneymind.R
import com.example.moneymind.RecommendationViewModel
import com.example.moneymind.RecommendationViewModelFactory
import com.example.moneymind.data.AppDatabase
import com.example.moneymind.ui.components.ApiKeyDialog
import com.example.moneymind.ui.components.RecommendationDialog
import java.text.SimpleDateFormat
import java.time.format.TextStyle
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChartPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    onBackPressed: () -> Unit = {}
) {
    // Get the TransactionDao for the ChartViewModel
    val context = LocalContext.current
    val transactionDao = remember { AppDatabase.getDatabase(context).transactionDao() }

    // Initialize the ChartViewModel
    val chartViewModel: ChartViewModel = viewModel(
        factory = ChartViewModelFactory(transactionDao)
    )

    // Initialize the recommendation ViewModel
    val recommendationViewModel: RecommendationViewModel = viewModel(
        factory = RecommendationViewModelFactory(context)
    )

    // Observe state from ViewModels
    val currentYearMonth by chartViewModel.currentYearMonth
    val isExpenseTab by chartViewModel.isExpenseTab

    // Recommendation dialog states
    val showRecommendation by recommendationViewModel.showRecommendation
    val isLoadingRecommendation by recommendationViewModel.isLoading
    val recommendationText by recommendationViewModel.recommendationText.collectAsState()

    // API key dialog states
    val showApiKeyInput by recommendationViewModel.showApiKeyInput
    val apiKey by recommendationViewModel.apiKey
    val hasApiKey = recommendationViewModel.hasApiKey()

    // Format the current month and year for display
    val currentMonthText = remember(currentYearMonth) {
        "${currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentYearMonth.year}"
    }

    // Collect category data based on selected tab
    val expenseCategorySummary by chartViewModel.expenseCategorySummary.collectAsState()
    val incomeCategorySummary by chartViewModel.incomeCategorySummary.collectAsState()
    val totalExpense by chartViewModel.totalExpense.collectAsState()
    val totalIncome by chartViewModel.totalIncome.collectAsState()

    // Select the appropriate data based on the selected tab
    val categories = if (isExpenseTab) expenseCategorySummary else incomeCategorySummary
    val totalAmount = if (isExpenseTab) totalExpense else totalIncome

    // Display recommendation dialog
    RecommendationDialog(
        isVisible = showRecommendation,
        isLoading = isLoadingRecommendation,
        recommendationText = recommendationText,
        onDismiss = { recommendationViewModel.dismissRecommendation() }
    )

    // Display API key input dialog
    ApiKeyDialog(
        isVisible = showApiKeyInput,
        apiKey = apiKey,
        onApiKeyChange = { recommendationViewModel.updateApiKey(it) },
        onSave = { recommendationViewModel.saveApiKey() },
        onDismiss = { recommendationViewModel.toggleApiKeyInput() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070906))
    ) {
        // Background gradient with blur effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.43f)
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
        }

        // Content area
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, top = 40.dp)
                .verticalScroll(scrollState)
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
                // Prepare all string resources before using them
                val expensesText = stringResource(R.string.expenses)
                val incomeText = stringResource(R.string.income)
                val selectedText = stringResource(R.string.tab_selected)
                val notSelectedText = stringResource(R.string.tab_not_selected)

                // Create accessibility labels
                val expensesAccessibilityLabel = stringResource(
                    R.string.tab_accessibility_label,
                    expensesText,
                    if (isExpenseTab) selectedText else notSelectedText
                )

                val incomeAccessibilityLabel = stringResource(
                    R.string.tab_accessibility_label,
                    incomeText,
                    if (!isExpenseTab) selectedText else notSelectedText
                )

                Tab(
                    selected = isExpenseTab,
                    onClick = { chartViewModel.setExpenseTab(true) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isExpenseTab) Color(0xFF81A38A) else Color.Transparent)
                        .semantics {
                            contentDescription = expensesAccessibilityLabel
                        }
                ) {
                    Text(
                        text = expensesText,
                        color = if (isExpenseTab) Color.White else Color.Gray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Tab(
                    selected = !isExpenseTab,
                    onClick = { chartViewModel.setExpenseTab(false) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (!isExpenseTab) Color(0xFF81A38A) else Color.Transparent)
                        .semantics {
                            contentDescription = incomeAccessibilityLabel
                        }
                ) {
                    Text(
                        text = incomeText,
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
                // Get string resource outside semantics block
                val previousMonthText = stringResource(R.string.previous_month)
                val previousMonthAccessibilityDesc = stringResource(
                    R.string.previous_month_accessibility,
                    previousMonthText,
                    currentMonthText
                )

                IconButton(
                    onClick = { chartViewModel.previousMonth() },
                    modifier = Modifier.semantics {
                        contentDescription = previousMonthAccessibilityDesc
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = previousMonthText,
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
                        text = currentMonthText,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                // Get string resource outside semantics block
                val nextMonthText = stringResource(R.string.next_month)
                val nextMonthAccessibilityDesc = stringResource(
                    R.string.next_month_accessibility,
                    nextMonthText,
                    currentMonthText
                )

                IconButton(
                    onClick = { chartViewModel.nextMonth() },
                    modifier = Modifier.semantics {
                        contentDescription = nextMonthAccessibilityDesc
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = nextMonthText,
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
                // Get string resources for the chart section
                val totalExpenseLabel = stringResource(id = R.string.total_expense)
                val totalIncomeLabel = stringResource(id = R.string.total_income)
                val formattedLabel = if (isExpenseTab) totalExpenseLabel else totalIncomeLabel

                // Get string resources for no data text
                val noExpenseDataText = stringResource(id = R.string.no_expense_data_short)
                val noIncomeDataText = stringResource(id = R.string.no_income_data_short)

                val chartAccessibilityDesc = stringResource(
                    R.string.chart_accessibility_description,
                    formattedLabel,
                    String.format("₹%.2f", totalAmount),
                    categories.size
                )

                if (categories.isEmpty() || totalAmount <= 0) {
                    // Show a message when there's no data
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.semantics {
                            contentDescription = if (isExpenseTab) noExpenseDataText else noIncomeDataText
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isExpenseTab) noExpenseDataText else noIncomeDataText,
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Canvas(
                        modifier = Modifier
                            .size(200.dp)
                            .padding(16.dp)
                            .semantics {
                                contentDescription = chartAccessibilityDesc
                            }
                    ) {
                        var startAngle = 0f
                        val radius = size.minDimension / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        categories.forEach { category ->
                            val sweepAngle = (category.percentage * 3.6f).toFloat()
                            val percentage = category.percentage.toInt()

                            // Draw the arc with style similar to first design
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
                    val centerChartDesc = stringResource(
                        R.string.center_chart_accessibility_description,
                        formattedLabel,
                        String.format("₹%.2f", totalAmount)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.semantics {
                            contentDescription = centerChartDesc
                        }
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
            }

            Spacer(modifier = Modifier.height(32.dp))

            // AI Recommendation Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        recommendationViewModel.getRecommendations(
                            totalExpense = totalExpense,
                            totalIncome = totalIncome,
                            expenseCategories = expenseCategorySummary
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF81A38A)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.get_budget_recommendations),
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // API Key configuration button
                OutlinedButton(
                    onClick = { recommendationViewModel.toggleApiKeyInput() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(Color.Gray.copy(alpha = 0.5f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasApiKey)
                            stringResource(id = R.string.update_gemini_api_key)
                        else
                            stringResource(id = R.string.set_gemini_api_key),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                if (hasApiKey) {
                    Text(
                        text = stringResource(id = R.string.using_gemini_ai),
                        color = Color(0xFF81A38A),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.using_local_recommendations),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category list header
            Text(
                text = stringResource(R.string.breakdown),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(bottom = 16.dp, start = 8.dp)
                    .semantics { heading() }
            )

            // List of categories with details
            if (categories.isEmpty()) {
                Text(
                    text = stringResource(if (isExpenseTab) R.string.no_expense_data else R.string.no_income_data),
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                )
            } else {
                categories.forEach { category ->
                    // Improved CategoryItem similar to first code
                    EnhancedCategoryItem(
                        category = category,
                        isExpense = isExpenseTab
                    )
                }
            }

            // Extra space at the bottom
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun EnhancedCategoryItem(
    category: CategoryData,
    isExpense: Boolean
) {
    val formattedAmount = String.format("₹%.2f", category.amount)

    val expenseOrIncomeText = if (isExpense) stringResource(R.string.expenses) else stringResource(R.string.income)
    val categoryDesc = stringResource(
        R.string.category_accessibility_description,
        category.name,
        formattedAmount,
        String.format("%.1f%%", category.percentage),
        expenseOrIncomeText
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clickable { /* Navigate to category details */ }
            .semantics {
                contentDescription = categoryDesc
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .padding(top = 10.dp, bottom = 10.dp),
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
                text = if (isExpense) "-$formattedAmount" else "+$formattedAmount",
                color = if (isExpense) Color.White else Color(0xFF34A853),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}