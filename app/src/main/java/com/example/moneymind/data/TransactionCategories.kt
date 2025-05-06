package com.example.moneymind.data

// Utility object to store transaction categories
object TransactionCategories {
    // Categories for credit transactions
    val CREDIT_CATEGORIES = listOf(
        "Earned",     // Salary, freelance, business income
        "Invested",   // Dividends, interest, rental
        "Gifts",      // Monetary gifts received
        "Misc Income" // Other income sources
    )

    // Categories for debit transactions
    val DEBIT_CATEGORIES = listOf(
        "Housing",      // Rent, mortgage, repairs, insurance
        "Transport",    // Car, fuel, public transit, tolls
        "Food",         // Groceries, dining out, coffee
        "Utilities",    // Electric, gas, water, internet
        "Insurance",    // Non-auto/housing policies
        "Health",       // Medical, dental, prescriptions, care
        "Personal",     // Clothing, grooming, personal care
        "Entertainment",// Movies, hobbies, events, streaming
        "Shopping",     // General retail purchases
        "Dependents",   // Childcare, education, activities
        "Debt",         // Loan payments, credit cards
        "Investments",  // Contributions to investment accounts
        "Gifts Given",  // Gifts to others
        "Fees",         // Bank, service, late fees
        "Taxes",        // Income, property taxes
        "Misc Expense"  // Infrequent or minor costs
    )
    
    // Special value for uncategorized transactions
    const val UNCATEGORIZED = "Uncategorized"
    
    // Get appropriate categories based on transaction type
    fun getCategoriesForType(type: TransactionType): List<String> {
        return when (type) {
            TransactionType.CREDIT -> CREDIT_CATEGORIES
            TransactionType.DEBIT -> DEBIT_CATEGORIES
        }
    }
} 