package com.example.moneymind.pages

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.moneymind.data.Transaction

/**
 * Data class to represent category information for analytics
 */
data class CategoryData(
    val name: String,
    val color: Color,
    val icon: ImageVector,
    val amount: Double,
    val percentage: Double,
    val transactions: List<Transaction> = emptyList()
) 