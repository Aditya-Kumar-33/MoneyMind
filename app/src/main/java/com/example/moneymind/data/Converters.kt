package com.example.moneymind.data // Use your correct package name

import android.os.Build // Keep for RequiresApi
import androidx.annotation.RequiresApi // Keep for RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Note: java.time types require minSdk 26+ or library desugaring enabled.

object Converters {

  // --- LocalDate <-> String ---
  @RequiresApi(Build.VERSION_CODES.O) // Required for java.time
  @TypeConverter
  fun fromLocalDateString(value: String?): LocalDate? {
    // String from DB to LocalDate
    return value?.let { LocalDate.parse(it) }
  }

  @RequiresApi(Build.VERSION_CODES.O) // Required for java.time
  @TypeConverter
  fun localDateToString(date: LocalDate?): String? {
    // LocalDate from app to String
    return date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
  }

  // --- LocalTime <-> String ---
  @RequiresApi(Build.VERSION_CODES.O) // Required for java.time
  @TypeConverter
  fun fromLocalTimeString(value: String?): LocalTime? {
    // String from DB to LocalTime
    return value?.let { LocalTime.parse(it) }
  }

  @RequiresApi(Build.VERSION_CODES.O) // Required for java.time
  @TypeConverter
  fun localTimeToString(time: LocalTime?): String? {
    // LocalTime from app to String
    return time?.format(DateTimeFormatter.ISO_LOCAL_TIME)
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
