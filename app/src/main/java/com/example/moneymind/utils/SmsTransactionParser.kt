package com.example.moneymind.util // Or com.example.moneymind.parser

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.moneymind.data.Transaction
import com.example.moneymind.data.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

// Class dedicated to SMS parsing
class SmsTransactionParser {

  // Improved regex patterns for more flexible transaction detection
  
  // Pattern to find amount in various formats (Rs/INR followed by a number with 2 decimals)
  private val amountPattern = Pattern.compile(
    """(?:(?:rs|inr|₹)\.?\s*(\d+(?:,\d+)*\.?\d{0,2}))|((?:\d+(?:,\d+)*\.?\d{0,2})\s*(?:rs|inr|₹))""",
    Pattern.CASE_INSENSITIVE
  )
  
  // Pattern to detect transaction type (credited or debited)
  private val typePattern = Pattern.compile(
    """(credit(?:ed)?|debit(?:ed)?)""",
    Pattern.CASE_INSENSITIVE
  )
  
  // Common date formats in transaction messages
  private val datePatterns = listOf(
    Pattern.compile("""(\d{2})[/-](\d{2})[/-](\d{2,4})"""), // DD-MM-YY or DD/MM/YYYY
    Pattern.compile("""(\d{2})(?:\s+)?(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)(?:\s+)?(\d{2,4})""", Pattern.CASE_INSENSITIVE), // DD MMM YYYY
    Pattern.compile("""(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)(?:\s+)?(\d{2})(?:\s+)?(\d{2,4})""", Pattern.CASE_INSENSITIVE)  // MMM DD YYYY
  )
  
  // Pattern to find account number references (common formats)
  private val accountPattern = Pattern.compile(
    """(?:a/c|account|ac|acct)(?:\s+)?(?:\*+|x+|xx+)?(\d{4,})""",
    Pattern.CASE_INSENSITIVE
  )

  // Parsers for different date formats
  @RequiresApi(Build.VERSION_CODES.O)
  private val dateParser1 = DateTimeFormatter.ofPattern("dd-MM-yy", Locale.ENGLISH)
  @RequiresApi(Build.VERSION_CODES.O)
  private val dateParser2 = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  // Output formatter (ISO standard)
  @RequiresApi(Build.VERSION_CODES.O)
  private val outputDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // YYYY-MM-DD

  // Main function to read and parse
  @RequiresApi(Build.VERSION_CODES.O)
  @SuppressLint("Range") // Suppress range check lint
  suspend fun readTransactionsFromSms(contentResolver: ContentResolver): List<Transaction> {
    // Run on IO dispatcher
    return withContext(Dispatchers.IO) {
      val transactions = mutableListOf<Transaction>()
      val projection = arrayOf(
        Telephony.Sms._ID,
        Telephony.Sms.BODY,
        Telephony.Sms.DATE // SMS arrival timestamp
        // Telephony.Sms.ADDRESS // Sender (optional)
      )
      val cursor: Cursor? = contentResolver.query(
        Telephony.Sms.CONTENT_URI, // Content URI for SMS
        projection,
        null, // No selection filter initially
        null,
        Telephony.Sms.DEFAULT_SORT_ORDER // Sort by date desc
      )

      cursor?.use { // Ensure cursor is closed
        if (it.moveToFirst()) {
          do {
            val msgBody = it.getString(it.getColumnIndex(Telephony.Sms.BODY))
            val msgTimestamp = it.getLong(it.getColumnIndex(Telephony.Sms.DATE))
            // Try parsing the message
            parseSmsMessage(msgBody, msgTimestamp)?.let { transaction ->
              transactions.add(transaction)
              Log.d("SmsTransactionParser", "Found transaction: $transaction")
            }
          } while (it.moveToNext())
        }
      }
      Log.d("SmsTransactionParser", "Found ${transactions.size} potential transactions in SMS.")
      transactions // Return list of parsed transactions
    }
  }

  // Parse a single SMS message body using more flexible approach
  @RequiresApi(Build.VERSION_CODES.O)
  private fun parseSmsMessage(body: String, timestamp: Long): Transaction? {
    try {
      Log.d("SmsTransactionParser", "Parsing message: ${body.take(50)}...")
      
      // Find amount
      val amount = extractAmount(body) ?: return null
      Log.d("SmsTransactionParser", "Extracted amount: $amount")
      
      // Find transaction type
      val type = extractTransactionType(body) ?: return null
      Log.d("SmsTransactionParser", "Extracted type: $type")
      
      // Try to find the TRANSACTION date from the SMS content
      // This is the date when the actual transaction occurred (not when SMS was received)
      val parsedDate = extractDate(body)
      
      // If we can't determine the transaction date from the SMS content, skip this transaction
      if (parsedDate == null) {
        Log.d("SmsTransactionParser", "Could not determine transaction date from SMS, skipping transaction")
        return null
      }
      
      // Format the transaction date
      val transactionDate = parsedDate.format(outputDateFormatter)
      Log.d("SmsTransactionParser", "Using transaction date: $transactionDate")
      
      // Try to extract account identifier
      val accountId = extractAccountIdentifier(body)
      
      // Extract a basic description based on keywords
      val description = generateDescription(body, type, accountId)
      
      return Transaction(
        amount = amount,
        type = type,
        transactionDate = transactionDate,
        smsTimestamp = timestamp,
        description = description,
        accountIdentifier = accountId
      )
    } catch (e: Exception) {
      Log.e("SmsTransactionParser", "Error parsing message: ${e.message}")
      return null
    }
  }
  
  // Extract amount from message text
  private fun extractAmount(text: String): Double? {
    val matcher = amountPattern.matcher(text.lowercase(Locale.ROOT))
    while (matcher.find()) {
      // Get the matched group - either format Rs. XXX.XX or XXX.XX Rs
      val amountStr = matcher.group(1) ?: matcher.group(2) ?: continue
      
      // Clean up the amount string (remove commas, spaces, etc)
      val cleanAmount = amountStr.replace(",", "").replace(" ", "").trim()
      
      return try {
        cleanAmount.toDoubleOrNull()
      } catch (e: NumberFormatException) {
        Log.w("SmsTransactionParser", "Failed to parse amount: $amountStr")
        null
      }
    }
    return null
  }
  
  // Extract transaction type (CREDIT or DEBIT)
  private fun extractTransactionType(text: String): TransactionType? {
    val matcher = typePattern.matcher(text.lowercase(Locale.ROOT))
    if (matcher.find()) {
      val typeStr = matcher.group(1)
      return if (typeStr.startsWith("credit")) {
        TransactionType.CREDIT
      } else {
        TransactionType.DEBIT
      }
    }
    
    // If explicit credit/debit not found, look for other indicators
    if (text.lowercase(Locale.ROOT).contains("received") || 
        text.lowercase(Locale.ROOT).contains("added") ||
        text.lowercase(Locale.ROOT).contains("deposited")) {
      return TransactionType.CREDIT
    }
    
    if (text.lowercase(Locale.ROOT).contains("spent") || 
        text.lowercase(Locale.ROOT).contains("paid") ||
        text.lowercase(Locale.ROOT).contains("withdrawn")) {
      return TransactionType.DEBIT
    }
    
    // Default to null if can't determine
    return null
  }
  
  // Extract date using various patterns
  @RequiresApi(Build.VERSION_CODES.O)
  private fun extractDate(text: String): LocalDate? {
    // Try all date patterns
    for (pattern in datePatterns) {
      val matcher = pattern.matcher(text)
      if (matcher.find()) {
        try {
          // Depending on the pattern, construct the date differently
          when (pattern) {
            datePatterns[0] -> {
              // DD-MM-YYYY or DD/MM/YYYY
              val day = matcher.group(1).toInt()
              val month = matcher.group(2).toInt()
              var year = matcher.group(3).toInt()
              
              // Adjust for 2-digit years
              if (year < 100) {
                year += 2000
              }
              
              return LocalDate.of(year, month, day)
            }
            datePatterns[1] -> {
              // DD MMM YYYY
              val day = matcher.group(1).toInt()
              val monthStr = matcher.group(2)
              var year = matcher.group(3).toInt()
              
              // Adjust for 2-digit years
              if (year < 100) {
                year += 2000
              }
              
              val month = parseMonthName(monthStr)
              return LocalDate.of(year, month, day)
            }
            datePatterns[2] -> {
              // MMM DD YYYY
              val day = matcher.group(1).toInt()
              var year = matcher.group(2).toInt()
              
              // Adjust for 2-digit years
              if (year < 100) {
                year += 2000
              }
              
              val monthStr = matcher.group(0).substring(0, 3)
              val month = parseMonthName(monthStr)
              return LocalDate.of(year, month, day)
            }
          }
        } catch (e: Exception) {
          Log.e("SmsTransactionParser", "Error parsing date: ${e.message}")
        }
      }
    }
    
    // Fallback: look for words like 'today', 'yesterday'
    val lowerText = text.lowercase(Locale.ROOT)
    if (lowerText.contains("today")) {
      return LocalDate.now()
    } else if (lowerText.contains("yesterday")) {
      return LocalDate.now().minusDays(1)
    }
    
    return null
  }
  
  // Helper to parse month name to number
  private fun parseMonthName(monthStr: String): Int {
    return when (monthStr.lowercase(Locale.ROOT).take(3)) {
      "jan" -> 1
      "feb" -> 2
      "mar" -> 3
      "apr" -> 4
      "may" -> 5
      "jun" -> 6
      "jul" -> 7
      "aug" -> 8
      "sep" -> 9
      "oct" -> 10
      "nov" -> 11
      "dec" -> 12
      else -> 1 // Default to January if can't parse
    }
  }
  
  // Extract account identifier
  private fun extractAccountIdentifier(text: String): String? {
    val matcher = accountPattern.matcher(text)
    if (matcher.find()) {
      return matcher.group(1)
    }
    
    // Additional check for common patterns like XXXX1234
    val lastFourPattern = Pattern.compile("""(?:x+|X+)(\d{4})""")
    val lastFourMatcher = lastFourPattern.matcher(text)
    if (lastFourMatcher.find()) {
      return lastFourMatcher.group(1)
    }
    
    return null
  }
  
  // Generate a descriptive label for the transaction
  private fun generateDescription(body: String, type: TransactionType, accountId: String?): String {
    // Look for common transaction sources/destinations
    val lowerText = body.lowercase(Locale.ROOT)
    
    // Common merchants and transaction types
    val keywords = listOf(
      "upi", "neft", "imps", "rtgs", "atm", "cash", "salary", "zomato", "swiggy", "amazon", "flipkart", 
      "paytm", "phonepe", "gpay", "google pay", "card", "grocery", "electricity", "bill", "payment"
    )
    
    for (keyword in keywords) {
      if (lowerText.contains(keyword)) {
        val typeStr = if (type == TransactionType.CREDIT) "Credit" else "Debit"
        return "$typeStr via $keyword".capitalize(Locale.ROOT)
      }
    }
    
    // If no specific keyword, use a generic description with account if available
    return if (accountId != null) {
      if (type == TransactionType.CREDIT) "Credit to A/c XX$accountId" else "Debit from A/c XX$accountId"
    } else {
      if (type == TransactionType.CREDIT) "Credit Transaction" else "Debit Transaction"
    }
  }
  
  // Extension function to capitalize strings
  private fun String.capitalize(locale: Locale): String {
    return this.replaceFirstChar { 
      if (it.isLowerCase()) it.titlecase(locale) else it.toString() 
    }
  }
}