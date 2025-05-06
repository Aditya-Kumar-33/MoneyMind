package com.example.moneymind

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneymind.data.*
import com.example.moneymind.pages.CategoryData
import com.example.moneymind.utils.TransactionUpdateBroadcaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class ChartViewModel(private val transactionDao: TransactionDao) : ViewModel() {
    
    // Current month and year for filtering
    private val _currentYearMonth = mutableStateOf(YearMonth.now())
    val currentYearMonth: State<YearMonth> = _currentYearMonth
    
    // Tab selection state
    private val _isExpenseTab = mutableStateOf(true)
    val isExpenseTab: State<Boolean> = _isExpenseTab
    
    // Transactions data for the current month
    private val _monthlyTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val monthlyTransactions: StateFlow<List<Transaction>> = _monthlyTransactions.asStateFlow()
    
    // Categorized data for displaying in the UI
    private val _expenseCategorySummary = MutableStateFlow<List<CategoryData>>(emptyList())
    val expenseCategorySummary: StateFlow<List<CategoryData>> = _expenseCategorySummary
    
    private val _incomeCategorySummary = MutableStateFlow<List<CategoryData>>(emptyList())
    val incomeCategorySummary: StateFlow<List<CategoryData>> = _incomeCategorySummary
    
    // Total amounts
    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense
    
    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome
    
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fetchTransactionsForCurrentMonth()
            
            // Listen for transaction updates
            viewModelScope.launch {
                TransactionUpdateBroadcaster.events.collect { event ->
                    when (event) {
                        is TransactionUpdateBroadcaster.UpdateEvent.TransactionChanged,
                        is TransactionUpdateBroadcaster.UpdateEvent.CategoryChanged -> {
                            Log.d("ChartViewModel", "Received transaction update event, refreshing data")
                            fetchTransactionsForCurrentMonth()
                        }
                    }
                }
            }
        }
    }
    
    // Category icon mapping
    private val categoryIcons = mapOf(
        // Expense categories
        "Food" to Pair(Icons.Default.Fastfood, Color(0xFFFF9800)),
        "Transport" to Pair(Icons.Default.DirectionsCar, Color(0xFF3F51B5)),
        "Housing" to Pair(Icons.Default.Home, Color(0xFF4CAF50)),
        "Utilities" to Pair(Icons.Default.House, Color(0xFF009688)),
        "Health" to Pair(Icons.Default.HealthAndSafety, Color(0xFFE91E63)),
        "Personal" to Pair(Icons.Default.Person, Color(0xFF9C27B0)),
        "Entertainment" to Pair(Icons.Default.TheaterComedy, Color(0xFF673AB7)),
        "Shopping" to Pair(Icons.Default.Store, Color(0xFF2196F3)),
        "Dependents" to Pair(Icons.Default.PeopleAlt, Color(0xFF795548)),
        "Debt" to Pair(Icons.Default.CreditCard, Color(0xFFFF5722)),
        "Investments" to Pair(Icons.Default.TrendingUp, Color(0xFF607D8B)),
        "Gifts Given" to Pair(Icons.Default.CardGiftcard, Color(0xFFFFEB3B)),
        "Fees" to Pair(Icons.Default.AttachMoney, Color(0xFFCDDC39)),
        "Taxes" to Pair(Icons.Default.Payments, Color(0xFF8BC34A)),
        "Misc Expense" to Pair(Icons.Default.Style, Color(0xFFF44336)),
        
        // Income categories
        "Earned" to Pair(Icons.Default.WorkOutline, Color(0xFF4CAF50)),
        "Invested" to Pair(Icons.Default.TrendingUp, Color(0xFF2196F3)),
        "Gifts" to Pair(Icons.Default.CardGiftcard, Color(0xFFE91E63)),
        "Misc Income" to Pair(Icons.Default.Payments, Color(0xFF9C27B0)),
        
        // Default for uncategorized
        TransactionCategories.UNCATEGORIZED to Pair(Icons.Default.Category, Color(0xFF9E9E9E))
    )
    
    // Switch between expense and income tabs
    fun setExpenseTab(isExpense: Boolean) {
        _isExpenseTab.value = isExpense
    }
    
    // Navigate to previous month
    @RequiresApi(Build.VERSION_CODES.O)
    fun previousMonth() {
        _currentYearMonth.value = _currentYearMonth.value.minusMonths(1)
        fetchTransactionsForCurrentMonth()
    }
    
    // Navigate to next month
    @RequiresApi(Build.VERSION_CODES.O)
    fun nextMonth() {
        _currentYearMonth.value = _currentYearMonth.value.plusMonths(1)
        fetchTransactionsForCurrentMonth()
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchTransactionsForCurrentMonth() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val yearMonth = _currentYearMonth.value
                val startDate = yearMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val endDate = yearMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                Log.d("ChartViewModel", "Fetching transactions from $startDate to $endDate")
                
                // Get all transactions
                val transactions = transactionDao.getAllTransactionsSync()
                
                // Filter transactions for the current month
                val monthlyTransactions = transactions.filter { transaction ->
                    transaction.transactionDate >= startDate && transaction.transactionDate <= endDate
                }
                
                _monthlyTransactions.value = monthlyTransactions
                
                processTransactions(monthlyTransactions)
                
                Log.d("ChartViewModel", "Loaded ${monthlyTransactions.size} transactions for ${yearMonth.month} ${yearMonth.year}")
            } catch (e: Exception) {
                Log.e("ChartViewModel", "Error loading transactions: ${e.message}")
                _monthlyTransactions.value = emptyList()
                resetCategorySummaries()
            }
        }
    }
    
    // Public method to force a refresh
    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshData() {
        fetchTransactionsForCurrentMonth()
    }
    
    private fun resetCategorySummaries() {
        _expenseCategorySummary.value = emptyList()
        _incomeCategorySummary.value = emptyList()
        _totalExpense.value = 0.0
        _totalIncome.value = 0.0
    }
    
    private fun processTransactions(transactions: List<Transaction>) {
        // Group transactions by type
        val expenseTransactions = transactions.filter { it.type == TransactionType.DEBIT }
        val incomeTransactions = transactions.filter { it.type == TransactionType.CREDIT }
        
        // Calculate totals
        _totalExpense.value = expenseTransactions.sumOf { it.amount }
        _totalIncome.value = incomeTransactions.sumOf { it.amount }
        
        // Process expense categories
        processExpenseCategories(expenseTransactions)
        
        // Process income categories
        processIncomeCategories(incomeTransactions)
    }
    
    private fun processExpenseCategories(transactions: List<Transaction>) {
        // Group by category and sum amounts
        val categorizedExpenses = transactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toMutableMap()
        
        // Calculate percentages and create category data objects
        val total = _totalExpense.value
        val categoryDataList = categorizedExpenses.map { (category, amount) ->
            val percentage = if (total > 0) (amount / total) * 100 else 0.0
            val (icon, color) = categoryIcons[category] ?: categoryIcons[TransactionCategories.UNCATEGORIZED]!!
            
            CategoryData(
                name = category,
                icon = icon,
                color = color,
                amount = amount,
                percentage = percentage,
                transactions = transactions.filter { it.category == category }
            )
        }.sortedByDescending { it.amount }
        
        _expenseCategorySummary.value = categoryDataList
    }
    
    private fun processIncomeCategories(transactions: List<Transaction>) {
        // Group by category and sum amounts
        val categorizedIncome = transactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toMutableMap()
        
        // Calculate percentages and create category data objects
        val total = _totalIncome.value
        val categoryDataList = categorizedIncome.map { (category, amount) ->
            val percentage = if (total > 0) (amount / total) * 100 else 0.0
            val (icon, color) = categoryIcons[category] ?: categoryIcons[TransactionCategories.UNCATEGORIZED]!!
            
            CategoryData(
                name = category,
                icon = icon,
                color = color,
                amount = amount,
                percentage = percentage,
                transactions = transactions.filter { it.category == category }
            )
        }.sortedByDescending { it.amount }
        
        _incomeCategorySummary.value = categoryDataList
    }
}

class ChartViewModelFactory(private val transactionDao: TransactionDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChartViewModel(transactionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 