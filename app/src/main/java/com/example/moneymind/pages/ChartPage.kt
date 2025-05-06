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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneymind.AuthViewModel
import com.example.moneymind.ChartViewModel
import com.example.moneymind.ChartViewModelFactory
import com.example.moneymind.RecommendationViewModel
import com.example.moneymind.RecommendationViewModelFactory
import com.example.moneymind.data.AppDatabase
import com.example.moneymind.data.TransactionCategories
import com.example.moneymind.ui.components.ApiKeyDialog
import com.example.moneymind.ui.components.RecommendationDialog
import com.example.moneymind.utils.accessibilityHeading
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import com.example.moneymind.R

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

    // Use a Scaffold to handle system insets properly
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            Surface(
                color = Color(0xFF161C18),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.analytics),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.accessibilityHeading()
                    )

                    Row(
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

                        Text(
                            text = currentMonthText,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

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
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070906))
                .padding(innerPadding)
        ) {
            // Background gradient
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
            
            // Make the entire content scrollable
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp) // Add padding at the bottom to avoid overlapping with system navigation
            ) {
                // Tab selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                ) {
                    // Expense/Income Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1F1B)),
                        horizontalArrangement = Arrangement.SpaceEvenly
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
                            text = expensesText,
                            isSelected = isExpenseTab,
                            onClick = { chartViewModel.setExpenseTab(true) },
                            accessibilityLabel = expensesAccessibilityLabel
                        )
                        
                        Tab(
                            text = incomeText,
                            isSelected = !isExpenseTab,
                            onClick = { chartViewModel.setExpenseTab(false) },
                            accessibilityLabel = incomeAccessibilityLabel
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Pie Chart section
                    PieChartSection(
                        categories = categories,
                        totalAmount = totalAmount,
                        isExpense = isExpenseTab
                    )
                }

                // Add AI Recommendation Button and API key settings
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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

                // Category breakdown section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.breakdown),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .semantics { heading() }
                    )

                    // Show message if no categories available
                    if (categories.isEmpty()) {
                        Text(
                            text = stringResource(if (isExpenseTab) R.string.no_expense_data else R.string.no_income_data),
                            color = Color.Gray,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        // Display category items
                        categories.forEach { category ->
                            CategoryItem(
                                category = category,
                                isExpense = isExpenseTab
                            )
                        }
                    }
                }
                
                // Extra space at the bottom to ensure content doesn't get cut off
                Spacer(modifier = Modifier.height(72.dp))
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
    categories: List<CategoryData>,
    totalAmount: Double,
    isExpense: Boolean
) {
    // Formatted amount with currency symbol
    val formattedAmount = String.format("₹%.2f", totalAmount)
    
    // Get string resources in Composable scope
    val totalExpenseLabel = stringResource(id = R.string.total_expense)
    val totalIncomeLabel = stringResource(id = R.string.total_income)
    val formattedLabel = if (isExpense) totalExpenseLabel else totalIncomeLabel
    
    // Get string resources for no data text
    val noExpenseDataText = stringResource(id = R.string.no_expense_data_short)
    val noIncomeDataText = stringResource(id = R.string.no_income_data_short)

    val chartAccessibilityDesc = stringResource(
        R.string.chart_accessibility_description,
        formattedLabel,
        formattedAmount,
        categories.size
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(top = 16.dp)
            .semantics {
                contentDescription = chartAccessibilityDesc
            },
        contentAlignment = Alignment.Center
    ) {
        // Check if we have data to display
        if (categories.isEmpty() || totalAmount <= 0) {
            // Show a message when there's no data
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isExpense) noExpenseDataText else noIncomeDataText,
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            // Pie chart implementation
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
                
                categories.forEach { category ->
                    // Calculate sweep angle based on percentage (each percent = 3.6 degrees)
                    val sweepAngle = (category.percentage * 3.6).toFloat()
                    
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
            val centerChartDesc = stringResource(
                R.string.center_chart_accessibility_description,
                formattedLabel,
                formattedAmount
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.semantics {
                    contentDescription = centerChartDesc
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
}

@Composable
fun CategoryItem(
    category: CategoryData,
    isExpense: Boolean
) {
    val formattedAmount = String.format("₹%.2f", category.amount)
    val formattedPercentage = String.format("%.1f%%", category.percentage)
    
    val expenseOrIncomeText = if (isExpense) stringResource(R.string.expenses) else stringResource(R.string.income)
    val categoryDesc = stringResource(
        R.string.category_accessibility_description,
        category.name,
        formattedAmount,
        formattedPercentage,
        expenseOrIncomeText
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .semantics {
                contentDescription = categoryDesc
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