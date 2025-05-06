package com.example.moneymind.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.example.moneymind.pages.CategoryData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * Service to provide budget recommendations based on transaction data
 */
object RecommendationService {
    private const val TAG = "RecommendationService"
    private const val API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
    
    // Check if device is online
    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
    
    // Get the current language code from system
    private fun getCurrentLanguage(context: Context): String {
        return context.resources.configuration.locales.get(0).language
    }
    
    // Generate recommendation based on transaction statistics
    suspend fun getRecommendation(
        context: Context,
        totalExpense: Double,
        totalIncome: Double,
        expenseCategories: List<CategoryData>,
        apiKey: String? = null
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                // Check if online
                if (!isOnline(context)) {
                    return@withContext getOfflineMessage(context)
                }
                
                // Log the API key length without revealing the key itself
                apiKey?.let {
                    Log.d(TAG, "API key provided: ${if (it.isBlank()) "blank" else "${it.length} characters"}")
                } ?: Log.d(TAG, "No API key provided")
                
                // If no API key provided, use local recommendations
                if (apiKey.isNullOrBlank()) {
                    Log.d(TAG, "Using local recommendations due to missing API key")
                    return@withContext generateLocalRecommendation(context, totalExpense, totalIncome, expenseCategories)
                }
                
                // Prepare data for API call
                val currentLanguage = getCurrentLanguage(context)
                val prompt = preparePrompt(context, currentLanguage, totalExpense, totalIncome, expenseCategories)
                Log.d(TAG, "Prepared prompt for API call in language: $currentLanguage")
                
                try {
                    // Make API call
                    val response = makeApiCall(prompt, apiKey, currentLanguage)
                    Log.d(TAG, "API call successful")
                    
                    // Parse and return the recommendation
                    return@withContext parseApiResponse(response)
                } catch (e: Exception) {
                    Log.e(TAG, "Error during API call: ${e.message}", e)
                    return@withContext getApiErrorMessage(context, e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting recommendation: ${e.message}", e)
                return@withContext getErrorMessage(context)
            }
        }
    }
    
    private fun getOfflineMessage(context: Context): String {
        val language = getCurrentLanguage(context)
        return when (language) {
            "es" -> "Actualmente estás sin conexión. Por favor, conéctate a internet para obtener recomendaciones personalizadas."
            "fr" -> "Vous êtes actuellement hors ligne. Veuillez vous connecter à internet pour obtenir des recommandations personnalisées."
            "de" -> "Sie sind derzeit offline. Bitte verbinden Sie sich mit dem Internet, um personalisierte Empfehlungen zu erhalten."
            "hi" -> "आप वर्तमान में ऑफलाइन हैं। कृपया व्यक्तिगत बजट सिफारिशों के लिए इंटरनेट से कनेक्ट करें।"
            else -> "You're currently offline. Please connect to the internet to get personalized budget recommendations."
        }
    }
    
    private fun getApiErrorMessage(context: Context, e: Exception): String {
        val language = getCurrentLanguage(context)
        return when (language) {
            "es" -> "No se pudieron generar recomendaciones: ${e.message}. Intente actualizar su clave API o verifique su conexión a internet."
            "fr" -> "Impossible de générer des recommandations: ${e.message}. Essayez de mettre à jour votre clé API ou vérifiez votre connexion internet."
            "de" -> "Empfehlungen konnten nicht generiert werden: ${e.message}. Versuchen Sie, Ihren API-Schlüssel zu aktualisieren oder überprüfen Sie Ihre Internetverbindung."
            "hi" -> "सिफारिशें नहीं मिल सकीं: ${e.message}। कृपया अपनी API कुंजी अपडेट करें या अपने इंटरनेट कनेक्शन की जांच करें।"
            else -> "Unable to generate AI recommendations: ${e.message}. Try updating your API key or check your internet connection."
        }
    }
    
    private fun getErrorMessage(context: Context): String {
        val language = getCurrentLanguage(context)
        return when (language) {
            "es" -> "No se pueden generar recomendaciones en este momento. Por favor, inténtelo de nuevo más tarde."
            "fr" -> "Impossible de générer des recommandations pour le moment. Veuillez réessayer plus tard."
            "de" -> "Empfehlungen können derzeit nicht generiert werden. Bitte versuchen Sie es später erneut."
            "hi" -> "इस समय सिफारिशें नहीं मिल सकतीं। कृपया बाद में पुनः प्रयास करें।"
            else -> "Unable to generate recommendations at this time. Please try again later."
        }
    }
    
    private fun preparePrompt(
        context: Context,
        languageCode: String,
        totalExpense: Double,
        totalIncome: Double,
        expenseCategories: List<CategoryData>
    ): String {
        val categoriesText = expenseCategories.joinToString(separator = ", ") { 
            "${it.name}: ₹${it.amount} (${it.percentage.toInt()}%)" 
        }
        
        val currencySymbol = "₹" // Can be made dynamic if needed
        
        return when (languageCode) {
            "es" -> """
                Necesito recomendaciones de presupuesto basadas en los siguientes datos financieros:
                Ingreso Mensual Total: $currencySymbol$totalIncome
                Gastos Mensuales Totales: $currencySymbol$totalExpense
                Desglose de gastos por categoría: $categoriesText
                
                Por favor, proporciona 3 recomendaciones específicas y prácticas para ayudar a mejorar la gestión del presupuesto.
                Limita cada recomendación a 1-2 frases cortas solamente.
                No incluyas introducciones ni conclusiones, solo los 3 puntos numerados.
                Responde en español.
            """.trimIndent()
            
            "fr" -> """
                J'ai besoin de recommandations budgétaires basées sur les données financières suivantes:
                Revenu mensuel total: $currencySymbol$totalIncome
                Dépenses mensuelles totales: $currencySymbol$totalExpense
                Répartition des dépenses par catégorie: $categoriesText
                
                Veuillez fournir 3 recommandations spécifiques et réalisables pour aider à améliorer la gestion du budget.
                Limitez chaque recommandation à 1-2 phrases courtes seulement.
                N'incluez aucune introduction ou conclusion, juste les 3 points numérotés.
                Répondez en français.
            """.trimIndent()
            
            "de" -> """
                Ich benötige Budgetempfehlungen basierend auf den folgenden Finanzdaten:
                Monatliches Gesamteinkommen: $currencySymbol$totalIncome
                Monatliche Gesamtausgaben: $currencySymbol$totalExpense
                Aufschlüsselung der Ausgaben nach Kategorien: $categoriesText
                
                Bitte geben Sie 3 spezifische, umsetzbare Empfehlungen zur Verbesserung des Budgetmanagements.
                Beschränken Sie jede Empfehlung auf nur 1-2 kurze Sätze.
                Fügen Sie keine Einleitung oder Schlussfolgerungen hinzu, nur die 3 nummerierten Punkte.
                Antworten Sie auf Deutsch.
            """.trimIndent()
            
            "hi" -> """
                मुझे निम्नलिखित वित्तीय डेटा के आधार पर बजट सिफारिशों की आवश्यकता है:
                कुल मासिक आय: $currencySymbol$totalIncome
                कुल मासिक खर्च: $currencySymbol$totalExpense
                श्रेणी के अनुसार खर्च का विवरण: $categoriesText
                
                कृपया बजट प्रबंधन में सुधार करने के लिए 3 विशिष्ट, कार्य योग्य सिफारिशें प्रदान करें।
                प्रत्येक सिफारिश को केवल 1-2 छोटे वाक्यों तक सीमित रखें।
                कोई परिचय या निष्कर्ष शामिल न करें, केवल 3 क्रमांकित बिंदु।
                हिंदी में उत्तर दें।
            """.trimIndent()
            
            else -> """
                I need budget recommendations based on the following financial data:
                Total Monthly Income: $currencySymbol$totalIncome
                Total Monthly Expenses: $currencySymbol$totalExpense
                Expense breakdown by category: $categoriesText
                
                Please provide 3 specific, actionable recommendations to help improve budget management. 
                Keep each recommendation to 1-2 short sentences only.
                Don't include any introduction or conclusions, just the 3 numbered points.
            """.trimIndent()
        }
    }
    
    private suspend fun makeApiCall(prompt: String, apiKey: String, languageCode: String): String {
        // Log request preparation without including the full API key
        Log.d(TAG, "Preparing API request to Gemini API")
        
        // Use try-catch to provide better error messages
        try {
            val urlWithKey = "$API_ENDPOINT?key=$apiKey"
            val url = URL(urlWithKey)
            val connection = withContext(Dispatchers.IO) {
                (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 15000
                    readTimeout = 15000
                }
            }
            
            // Create request body according to Gemini API requirements
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 256)
                    put("topP", 0.95)
                    put("topK", 40)
                })
            }.toString()
            
            Log.d(TAG, "Request body created, sending to API")
            
            return withContext(Dispatchers.IO) {
                try {
                    OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
                        writer.write(requestBody)
                        writer.flush()
                    }
                    
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                        Log.d(TAG, "Received successful response from Gemini API")
                        responseBody
                    } else {
                        val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                        Log.e(TAG, "API error: $responseCode - $errorResponse")
                        throw Exception("API call failed with response code: $responseCode. Error: $errorResponse")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Network error: ${e.message}", e)
                    throw Exception("Network error: ${e.message}")
                } finally {
                    connection.disconnect()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing API call: ${e.message}", e)
            throw Exception("Could not connect to Gemini API: ${e.message}")
        }
    }
    
    private fun parseApiResponse(response: String): String {
        try {
            Log.d(TAG, "Parsing API response")
            val jsonResponse = JSONObject(response)
            
            // Check for error message in response
            if (jsonResponse.has("error")) {
                val error = jsonResponse.getJSONObject("error")
                val message = error.optString("message", "Unknown error")
                val code = error.optInt("code", 0)
                Log.e(TAG, "API error in response: $code - $message")
                return "Error from Gemini API: $message. Please check your API key and try again."
            }
            
            // Parse the candidates array
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                if (content != null) {
                    val parts = content.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val firstPart = parts.getJSONObject(0)
                        var text = firstPart.optString("text", "").trim()
                        
                        // Clean up markdown formatting
                        text = text.replace("\\*\\*(.*?)\\*\\*".toRegex(), "$1") // Remove **bold**
                        text = text.replace("\\*(.*?)\\*".toRegex(), "$1")      // Remove *italic*
                        text = text.replace("__(.*?)__".toRegex(), "$1")        // Remove __underline__
                        text = text.replace("~~(.*?)~~".toRegex(), "$1")        // Remove ~~strikethrough~~
                        text = text.replace("`(.*?)`".toRegex(), "$1")          // Remove `code`
                        
                        if (text.isNotEmpty()) {
                            Log.d(TAG, "Successfully parsed recommendation text")
                            return text
                        }
                    }
                }
            }
            
            Log.e(TAG, "Could not find recommendation text in response structure")
            return "Couldn't extract recommendation from API response. Structure: ${JSONObject(response).toString(2)}"
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing API response: ${e.message}", e)
            return "Error parsing Gemini API response: ${e.message}. Please try again later."
        }
    }
    
    // Generate local recommendations without API call
    private fun generateLocalRecommendation(
        context: Context,
        totalExpense: Double, 
        totalIncome: Double, 
        expenseCategories: List<CategoryData>
    ): String {
        val recommendations = mutableListOf<String>()
        val language = getCurrentLanguage(context)
        
        // Check expense to income ratio
        val expenseToIncomeRatio = if (totalIncome > 0) totalExpense / totalIncome else 0.0
        
        // Find highest expense category
        val highestExpenseCategory = expenseCategories.maxByOrNull { it.amount }
        
        // Check uncategorized transactions
        val hasUncategorizedTransactions = expenseCategories.any { it.name == "Uncategorized" || it.name.contains("uncategorized", true) }
        
        // Get localized currency symbol
        val currencySymbol = "₹" // Can be made dynamic based on locale if needed
        
        // Recommendation 1: Based on expense-to-income ratio
        if (totalIncome > 0) {
            when (language) {
                "es" -> {
                    when {
                        expenseToIncomeRatio > 0.9 -> {
                            recommendations.add("1. Tus gastos ($currencySymbol$totalExpense) están demasiado cerca de tus ingresos ($currencySymbol$totalIncome). Intenta reducir los gastos en al menos un 20% para generar ahorros.")
                        }
                        expenseToIncomeRatio > 0.7 -> {
                            recommendations.add("1. Estás gastando ${(expenseToIncomeRatio * 100).toInt()}% de tus ingresos. Considera seguir la regla 50/30/20: 50% necesidades, 30% deseos, 20% ahorros.")
                        }
                        expenseToIncomeRatio < 0.4 -> {
                            recommendations.add("1. Solo estás gastando ${(expenseToIncomeRatio * 100).toInt()}% de tus ingresos, lo que es excelente para ahorrar, pero considera si estás satisfaciendo todas tus necesidades.")
                        }
                        else -> {
                            recommendations.add("1. Tu relación gastos-ingresos es buena en un ${(expenseToIncomeRatio * 100).toInt()}%. Intenta mantener este enfoque equilibrado del gasto.")
                        }
                    }
                }
                "fr" -> {
                    when {
                        expenseToIncomeRatio > 0.9 -> {
                            recommendations.add("1. Vos dépenses ($currencySymbol$totalExpense) sont trop proches de vos revenus ($currencySymbol$totalIncome). Essayez de réduire les dépenses d'au moins 20% pour constituer une épargne.")
                        }
                        expenseToIncomeRatio > 0.7 -> {
                            recommendations.add("1. Vous dépensez ${(expenseToIncomeRatio * 100).toInt()}% de vos revenus. Envisagez de suivre la règle 50/30/20 : 50% pour les besoins, 30% pour les envies, 20% pour l'épargne.")
                        }
                        expenseToIncomeRatio < 0.4 -> {
                            recommendations.add("1. Vous ne dépensez que ${(expenseToIncomeRatio * 100).toInt()}% de vos revenus, ce qui est excellent pour l'épargne, mais vérifiez si vous répondez à tous vos besoins.")
                        }
                        else -> {
                            recommendations.add("1. Votre ratio dépenses/revenus est bon à ${(expenseToIncomeRatio * 100).toInt()}%. Essayez de maintenir cette approche équilibrée des dépenses.")
                        }
                    }
                }
                "de" -> {
                    when {
                        expenseToIncomeRatio > 0.9 -> {
                            recommendations.add("1. Ihre Ausgaben ($currencySymbol$totalExpense) liegen zu nahe an Ihrem Einkommen ($currencySymbol$totalIncome). Versuchen Sie, die Ausgaben um mindestens 20% zu reduzieren, um Ersparnisse aufzubauen.")
                        }
                        expenseToIncomeRatio > 0.7 -> {
                            recommendations.add("1. Sie geben ${(expenseToIncomeRatio * 100).toInt()}% Ihres Einkommens aus. Erwägen Sie die 50/30/20-Regel: 50% für Bedürfnisse, 30% für Wünsche, 20% für Ersparnisse.")
                        }
                        expenseToIncomeRatio < 0.4 -> {
                            recommendations.add("1. Sie geben nur ${(expenseToIncomeRatio * 100).toInt()}% Ihres Einkommens aus, was hervorragend zum Sparen ist, aber überprüfen Sie, ob Sie alle Ihre Bedürfnisse erfüllen.")
                        }
                        else -> {
                            recommendations.add("1. Ihr Ausgaben-Einkommens-Verhältnis ist mit ${(expenseToIncomeRatio * 100).toInt()}% gut. Versuchen Sie, diesen ausgewogenen Ansatz bei den Ausgaben beizubehalten.")
                        }
                    }
                }
                "hi" -> {
                    when {
                        expenseToIncomeRatio > 0.9 -> {
                            recommendations.add("1. आपके खर्च ($currencySymbol$totalExpense) आपकी आय ($currencySymbol$totalIncome) के बहुत करीब हैं। बचत बनाने के लिए खर्चों को कम से कम 20% कम करने का प्रयास करें।")
                        }
                        expenseToIncomeRatio > 0.7 -> {
                            recommendations.add("1. आप अपनी आय का ${(expenseToIncomeRatio * 100).toInt()}% खर्च कर रहे हैं। 50/30/20 नियम पर विचार करें: 50% जरूरतें, 30% इच्छाएं, 20% बचत।")
                        }
                        expenseToIncomeRatio < 0.4 -> {
                            recommendations.add("1. आप अपनी आय का केवल ${(expenseToIncomeRatio * 100).toInt()}% खर्च कर रहे हैं, जो बचत के लिए उत्कृष्ट है, लेकिन विचार करें कि क्या आप अपनी सभी जरूरतों को पूरा कर रहे हैं।")
                        }
                        else -> {
                            recommendations.add("1. आपका खर्च-आय अनुपात ${(expenseToIncomeRatio * 100).toInt()}% पर अच्छा है। खर्च के इस संतुलित दृष्टिकोण को बनाए रखने का प्रयास करें।")
                        }
                    }
                }
                else -> {
                    when {
                        expenseToIncomeRatio > 0.9 -> {
                            recommendations.add("1. Your expenses ($currencySymbol$totalExpense) are too close to your income ($currencySymbol$totalIncome). Try to reduce expenses by at least 20% to build savings.")
                        }
                        expenseToIncomeRatio > 0.7 -> {
                            recommendations.add("1. You're spending ${(expenseToIncomeRatio * 100).toInt()}% of your income. Consider following the 50/30/20 rule: 50% needs, 30% wants, 20% savings.")
                        }
                        expenseToIncomeRatio < 0.4 -> {
                            recommendations.add("1. You're only spending ${(expenseToIncomeRatio * 100).toInt()}% of your income, which is excellent for saving but consider if you're meeting all your needs.")
                        }
                        else -> {
                            recommendations.add("1. Your expense-to-income ratio is good at ${(expenseToIncomeRatio * 100).toInt()}%. Try to maintain this balanced approach to spending.")
                        }
                    }
                }
            }
        } else {
            when (language) {
                "es" -> recommendations.add("1. Agrega tus fuentes de ingresos para obtener una imagen completa de tu salud financiera.")
                "fr" -> recommendations.add("1. Ajoutez vos sources de revenus pour avoir une image complète de votre santé financière.")
                "de" -> recommendations.add("1. Fügen Sie Ihre Einkommensquellen hinzu, um ein vollständiges Bild Ihrer finanziellen Gesundheit zu erhalten.")
                "hi" -> recommendations.add("1. अपनी वित्तीय स्थिति का पूरा चित्र प्राप्त करने के लिए अपने आय स्रोतों को जोड़ें।")
                else -> recommendations.add("1. Add your income sources to get a complete picture of your financial health.")
            }
        }
        
        // Recommendation 2: Based on highest expense category
        highestExpenseCategory?.let {
            when (language) {
                "es" -> {
                    if (it.percentage > 40) {
                        recommendations.add("2. Tus gastos de ${it.name} representan el ${it.percentage.toInt()}% de tu gasto total. Busca formas de reducir esta categoría en un 10-15%.")
                    } else {
                        recommendations.add("2. Tu categoría de gastos más alta es ${it.name} con un ${it.percentage.toInt()}%. Esto parece razonable, pero busca pequeñas optimizaciones.")
                    }
                }
                "fr" -> {
                    if (it.percentage > 40) {
                        recommendations.add("2. Vos dépenses en ${it.name} représentent ${it.percentage.toInt()}% de vos dépenses totales. Cherchez des moyens de réduire cette catégorie de 10-15%.")
                    } else {
                        recommendations.add("2. Votre catégorie de dépenses la plus élevée est ${it.name} à ${it.percentage.toInt()}%. Cela semble raisonnable, mais recherchez de petites optimisations.")
                    }
                }
                "de" -> {
                    if (it.percentage > 40) {
                        recommendations.add("2. Ihre ${it.name}-Ausgaben machen ${it.percentage.toInt()}% Ihrer Gesamtausgaben aus. Suchen Sie nach Möglichkeiten, diese Kategorie um 10-15% zu reduzieren.")
                    } else {
                        recommendations.add("2. Ihre höchste Ausgabenkategorie ist ${it.name} mit ${it.percentage.toInt()}%. Dies scheint angemessen zu sein, aber suchen Sie nach kleinen Optimierungen.")
                    }
                }
                "hi" -> {
                    if (it.percentage > 40) {
                        recommendations.add("2. आपके ${it.name} खर्च आपके कुल खर्च का ${it.percentage.toInt()}% हैं। इस श्रेणी को 10-15% कम करने के तरीके खोजें।")
                    } else {
                        recommendations.add("2. आपकी सबसे अधिक खर्च की श्रेणी ${it.name} है, जो ${it.percentage.toInt()}% है। यह उचित लगता है, लेकिन छोटे अनुकूलन खोजें।")
                    }
                }
                else -> {
                    if (it.percentage > 40) {
                        recommendations.add("2. Your ${it.name} expenses account for ${it.percentage.toInt()}% of your total spending. Look for ways to reduce this category by 10-15%.")
                    } else {
                        recommendations.add("2. Your highest expense category is ${it.name} at ${it.percentage.toInt()}%. This seems reasonable, but look for small optimizations.")
                    }
                }
            }
        } ?: when (language) {
            "es" -> recommendations.add("2. Agrega más datos de transacciones para obtener recomendaciones específicas por categoría.")
            "fr" -> recommendations.add("2. Ajoutez plus de données de transactions pour obtenir des recommandations spécifiques à chaque catégorie.")
            "de" -> recommendations.add("2. Fügen Sie weitere Transaktionsdaten hinzu, um kategoriespezifische Empfehlungen zu erhalten.")
            "hi" -> recommendations.add("2. श्रेणी-विशिष्ट सिफारिशें प्राप्त करने के लिए अधिक लेनदेन डेटा जोड़ें।")
            else -> recommendations.add("2. Add more transaction data to get category-specific recommendations.")
        }
        
        // Recommendation 3: Based on uncategorized transactions or savings
        if (hasUncategorizedTransactions) {
            when (language) {
                "es" -> recommendations.add("3. Categoriza tus transacciones no categorizadas para obtener mejores perspectivas sobre tus patrones de gasto.")
                "fr" -> recommendations.add("3. Catégorisez vos transactions non catégorisées pour mieux comprendre vos habitudes de dépenses.")
                "de" -> recommendations.add("3. Kategorisieren Sie Ihre nicht kategorisierten Transaktionen, um bessere Einblicke in Ihre Ausgabenmuster zu erhalten.")
                "hi" -> recommendations.add("3. अपने खर्च पैटर्न में बेहतर अंतर्दृष्टि प्राप्त करने के लिए अपने अवर्गीकृत लेनदेन को वर्गीकृत करें।")
                else -> recommendations.add("3. Categorize your uncategorized transactions to get better insights into your spending patterns.")
            }
        } else if (totalIncome > 0) {
            val savingsAmount = totalIncome - totalExpense
            val savingsPercentage = (savingsAmount / totalIncome) * 100
            
            when (language) {
                "es" -> {
                    if (savingsPercentage < 10) {
                        recommendations.add("3. Estás ahorrando solo el ${savingsPercentage.toInt()}% de tus ingresos. Trata de ahorrar al menos el 20% para emergencias y metas futuras.")
                    } else {
                        recommendations.add("3. Estás ahorrando el ${savingsPercentage.toInt()}% de tus ingresos, lo cual es bueno. Considera invertir parte de estos ahorros para un crecimiento a largo plazo.")
                    }
                }
                "fr" -> {
                    if (savingsPercentage < 10) {
                        recommendations.add("3. Vous n'économisez que ${savingsPercentage.toInt()}% de vos revenus. Visez à économiser au moins 20% pour les urgences et les objectifs futurs.")
                    } else {
                        recommendations.add("3. Vous économisez ${savingsPercentage.toInt()}% de vos revenus, ce qui est bien. Envisagez d'investir une partie de ces économies pour une croissance à long terme.")
                    }
                }
                "de" -> {
                    if (savingsPercentage < 10) {
                        recommendations.add("3. Sie sparen nur ${savingsPercentage.toInt()}% Ihres Einkommens. Streben Sie an, mindestens 20% für Notfälle und zukünftige Ziele zu sparen.")
                    } else {
                        recommendations.add("3. Sie sparen ${savingsPercentage.toInt()}% Ihres Einkommens, was gut ist. Erwägen Sie, einen Teil dieser Ersparnisse für langfristiges Wachstum zu investieren.")
                    }
                }
                "hi" -> {
                    if (savingsPercentage < 10) {
                        recommendations.add("3. आप अपनी आय का केवल ${savingsPercentage.toInt()}% बचा रहे हैं। आपातकाल और भविष्य के लक्ष्यों के लिए कम से कम 20% बचाने का लक्ष्य रखें।")
                    } else {
                        recommendations.add("3. आप अपनी आय का ${savingsPercentage.toInt()}% बचा रहे हैं, जो अच्छा है। दीर्घकालिक विकास के लिए इन बचतों का कुछ हिस्सा निवेश करने पर विचार करें।")
                    }
                }
                else -> {
                    if (savingsPercentage < 10) {
                        recommendations.add("3. You're saving only ${savingsPercentage.toInt()}% of your income. Aim to save at least 20% for emergencies and future goals.")
                    } else {
                        recommendations.add("3. You're saving ${savingsPercentage.toInt()}% of your income, which is good. Consider investing some of these savings for long-term growth.")
                    }
                }
            }
        } else {
            when (language) {
                "es" -> recommendations.add("3. Registra tanto gastos como ingresos para recibir recomendaciones de ahorro.")
                "fr" -> recommendations.add("3. Enregistrez à la fois les dépenses et les revenus pour recevoir des recommandations d'épargne.")
                "de" -> recommendations.add("3. Erfassen Sie sowohl Ausgaben als auch Einkommen, um Sparempfehlungen zu erhalten.")
                "hi" -> recommendations.add("3. बचत संबंधी सिफारिशें प्राप्त करने के लिए खर्च और आय दोनों का रिकॉर्ड रखें।")
                else -> recommendations.add("3. Record both expenses and income to receive savings recommendations.")
            }
        }
        
        return recommendations.joinToString("\n\n")
    }
} 