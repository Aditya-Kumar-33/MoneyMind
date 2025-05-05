package com.example.moneymind.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Note: java.time types require minSdk 26+ or library desugaring enabled.

/**
 * Type converters for Room database to handle non-primitive types
 */
class Converters {
  private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
  private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

  // LocalDate Converters
  @RequiresApi(Build.VERSION_CODES.O) // Required for java.time
  @TypeConverter
  fun fromLocalDate(date: LocalDate?): String? {
    return date?.format(dateFormatter)
  }

  @RequiresApi(Build.VERSION_CODES.O) // Required for java.time
  @TypeConverter
  fun toLocalDate(dateString: String?): LocalDate? {
    return dateString?.let { LocalDate.parse(it, dateFormatter) }
  }

  // LocalTime Converters
  @RequiresApi(Build.VERSION_CODES.O) // Required for java.time
  @TypeConverter
  fun fromLocalTime(time: LocalTime?): String? {
    return time?.format(timeFormatter)
  }

  @RequiresApi(Build.VERSION_CODES.O) // Required for java.time
  @TypeConverter
  fun toLocalTime(timeString: String?): LocalTime? {
    return timeString?.let { LocalTime.parse(it, timeFormatter) }
  }

  // --- TransactionType Enum <-> String --- ADDED THIS
  @TypeConverter
  fun fromTransactionTypeString(value: String?): TransactionType? {
    // String from DB to Enum
    return value?.let { enumValueOf<TransactionType>(it) }
  }

  @TypeConverter
  fun transactionTypeToString(type: TransactionType?): String? {
    // Enum from app to String
    return type?.name // Store enum by name
  }
}
