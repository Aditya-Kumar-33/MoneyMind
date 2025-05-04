package com.example.moneymind.data

import android.content.Context // Import context
import androidx.room.Database
import androidx.room.Room // Import Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // Import TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Define the single database for the app
@Database(
  // List ALL entities for this database
  entities = [
    Transaction::class, 
    TransactionCategory::class,
    SavingsRecord::class
  ],
  version = 1, // Increment version to 1
  exportSchema = false // Disable schema export for now
)
// Tell Room to use the Converters class
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

  // Abstract getters for ALL DAOs
  abstract fun transactionDao(): TransactionDao
  abstract fun categoryDao(): CategoryDao
  abstract fun savingsDao(): SavingsDao // Add SavingsDao getter

  // Companion object for singleton instance
  companion object {
    @Volatile // Visible across threads
    private var INSTANCE: AppDatabase? = null
    
    // Define migration from version 1 to 2
    private val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) {
        // Add the category column to the transactions table with default value
        database.execSQL("ALTER TABLE transactions ADD COLUMN category TEXT NOT NULL DEFAULT '${TransactionCategories.UNCATEGORIZED}'")
      }
    }

    // Get the singleton database instance
    fun getDatabase(context: Context): AppDatabase {
      val tempInstance = INSTANCE
      if (tempInstance != null) {
        return tempInstance
      }
      synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          // Use a consistent database name
          "money_mind_database"
        )
          // Add migrations here
          .addMigrations(MIGRATION_1_2)
          // Fallback strategy if migrations fail (OPTIONAL - use with caution)
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance // Store the instance
        return instance // Return the instance
      }
    }
  }
}
