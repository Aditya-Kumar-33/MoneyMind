package com.example.moneymind.data

import android.content.Context // Import context
import android.util.Log // Add log import
import androidx.room.Database
import androidx.room.Room // Import Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // Import TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.RoomDatabase.JournalMode

// Define the single database for the app
@Database(
  // List ALL entities for this database
  entities = [
    Transaction::class, 
    TransactionCategory::class,
    SavingsRecord::class,
    Note::class
  ],
  version = 4, // Increment version to 4 for the new userId column in notes
  exportSchema = false // Disable schema export for now
)
// Tell Room to use the Converters class
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

  // Abstract getters for ALL DAOs
  abstract fun transactionDao(): TransactionDao
  abstract fun categoryDao(): CategoryDao
  abstract fun savingsDao(): SavingsDao // Add SavingsDao getter
  abstract fun noteDao(): NoteDao // Add NoteDao getter

  // Companion object for singleton instance
  companion object {
    private const val TAG = "AppDatabase" // Tag for logging
    @Volatile // Visible across threads
    private var INSTANCE: AppDatabase? = null
    
    // Define migration from version 1 to 2
    private val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) {
        try {
          // Check if the column exists before adding it
          val cursor = database.query("PRAGMA table_info(transactions)")
          val columnNames = mutableListOf<String>()
          
          while (cursor.moveToNext()) {
            val columnName = cursor.getString(cursor.getColumnIndex("name"))
            columnNames.add(columnName)
          }
          cursor.close()
          
          // Only add the column if it doesn't exist
          if (!columnNames.contains("category")) {
            database.execSQL("ALTER TABLE transactions ADD COLUMN category TEXT NOT NULL DEFAULT '${TransactionCategories.UNCATEGORIZED}'")
            Log.d(TAG, "Migration 1-2: Added category column to transactions table")
          } else {
            Log.d(TAG, "Migration 1-2: Category column already exists in transactions table")
          }
        } catch (e: Exception) {
          Log.e(TAG, "Error in migration 1-2", e)
          // We don't rethrow as this would crash the app
          // Instead, we'll let the fallback migration strategy handle it
        }
      }
    }
    
    // Define migration from version 2 to 3 to add notes table and ensure savings_records table
    private val MIGRATION_2_3 = object : Migration(2, 3) {
      override fun migrate(database: SupportSQLiteDatabase) {
        try {
          // Create notes table if it doesn't exist
          handleNotesTable(database)
          
          // Handle the savings_records table
          handleSavingsRecordsTable(database)
          
          Log.d(TAG, "Migration 2-3 completed successfully")
        } catch (e: Exception) {
          Log.e(TAG, "Error in migration 2-3", e)
          // We don't rethrow as this would crash the app
          // Instead, we'll let the fallback migration strategy handle it
        }
      }
      
      // Helper method to create or update notes table
      private fun handleNotesTable(database: SupportSQLiteDatabase) {
        // First check if the table exists
        val tableExists = tableExists(database, "notes")
        
        if (!tableExists) {
          // Create notes table
          database.execSQL(
            "CREATE TABLE IF NOT EXISTS notes (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "text TEXT NOT NULL, " +
            "createdAt INTEGER NOT NULL, " +
            "updatedAt INTEGER NOT NULL)"
          )
          Log.d(TAG, "Created notes table")
        } else {
          Log.d(TAG, "Notes table already exists")
        }
      }
      
      // Helper method to create or update savings_records table
      private fun handleSavingsRecordsTable(database: SupportSQLiteDatabase) {
        val tableExists = tableExists(database, "savings_records")
        
        if (!tableExists) {
          // Table doesn't exist, create it with all required columns
          createSavingsRecordsTable(database)
        } else {
          // Table exists but might not have all columns
          ensureSavingsRecordsTableColumns(database)
        }
      }
      
      // Helper to check if a table exists
      private fun tableExists(database: SupportSQLiteDatabase, tableName: String): Boolean {
        val cursor = database.query(
          "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
          arrayOf(tableName)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
      }
      
      // Helper to create the savings_records table
      private fun createSavingsRecordsTable(database: SupportSQLiteDatabase) {
        try {
          // Drop the table if it exists but is invalid
          database.execSQL("DROP TABLE IF EXISTS savings_records")
          
          // Create a new table with all required columns
          database.execSQL(
            "CREATE TABLE savings_records (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "userId TEXT NOT NULL, " +
            "amount REAL NOT NULL, " +
            "type TEXT NOT NULL, " +
            "category TEXT NOT NULL, " +
            "payment TEXT NOT NULL, " +
            "subCategory TEXT, " +
            "description TEXT, " +
            "date TEXT NOT NULL, " +
            "time TEXT NOT NULL)"
          )
          Log.d(TAG, "Created savings_records table from scratch")
        } catch (e: Exception) {
          Log.e(TAG, "Error creating savings_records table", e)
          throw e // Re-throw to be handled by the parent try-catch
        }
      }
      
      // Helper to ensure all columns exist in the savings_records table
      private fun ensureSavingsRecordsTableColumns(database: SupportSQLiteDatabase) {
        try {
          // Check existing columns
          val columnsQuery = database.query("PRAGMA table_info(savings_records)")
          val existingColumns = mutableListOf<String>()
          
          while (columnsQuery.moveToNext()) {
            val columnName = columnsQuery.getString(columnsQuery.getColumnIndex("name"))
            existingColumns.add(columnName)
          }
          columnsQuery.close()
          
          // If table exists but has no columns (which shouldn't happen normally),
          // recreate it completely
          if (existingColumns.isEmpty()) {
            Log.w(TAG, "savings_records table exists but has no columns, recreating it")
            createSavingsRecordsTable(database)
            return
          }
          
          // Otherwise add any missing columns one by one
          addColumnIfMissing(database, existingColumns, "userId", "TEXT NOT NULL DEFAULT ''")
          addColumnIfMissing(database, existingColumns, "amount", "REAL NOT NULL DEFAULT 0.0")
          addColumnIfMissing(database, existingColumns, "type", "TEXT NOT NULL DEFAULT 'Expense'")
          addColumnIfMissing(database, existingColumns, "category", "TEXT NOT NULL DEFAULT '${TransactionCategories.UNCATEGORIZED}'")
          addColumnIfMissing(database, existingColumns, "payment", "TEXT NOT NULL DEFAULT 'Cash'")
          addColumnIfMissing(database, existingColumns, "subCategory", "TEXT")
          addColumnIfMissing(database, existingColumns, "description", "TEXT")
          addColumnIfMissing(database, existingColumns, "date", "TEXT NOT NULL DEFAULT '2023-01-01'")
          addColumnIfMissing(database, existingColumns, "time", "TEXT NOT NULL DEFAULT '00:00'")
          
          Log.d(TAG, "Ensured all columns exist in savings_records table")
        } catch (e: Exception) {
          Log.e(TAG, "Error ensuring savings_records columns", e)
          throw e // Re-throw to be handled by the parent try-catch
        }
      }
      
      // Helper to add a column if it's missing
      private fun addColumnIfMissing(
        database: SupportSQLiteDatabase, 
        existingColumns: List<String>,
        columnName: String,
        columnDef: String
      ) {
        if (!existingColumns.contains(columnName)) {
          try {
            database.execSQL("ALTER TABLE savings_records ADD COLUMN $columnName $columnDef")
            Log.d(TAG, "Added missing column $columnName to savings_records")
          } catch (e: Exception) {
            Log.e(TAG, "Error adding column $columnName", e)
            // Continue with other columns even if one fails
          }
        }
      }
    }
    
    // Define migration from version 3 to 4 to add userId column to notes table
    private val MIGRATION_3_4 = object : Migration(3, 4) {
      override fun migrate(database: SupportSQLiteDatabase) {
        try {
          // Check if notes table exists
          val tableExists = tableExists(database, "notes")
          
          if (tableExists) {
            // Check if userId column exists in notes table
            val columnsQuery = database.query("PRAGMA table_info(notes)")
            val existingColumns = mutableListOf<String>()
            
            while (columnsQuery.moveToNext()) {
              val columnName = columnsQuery.getString(columnsQuery.getColumnIndex("name"))
              existingColumns.add(columnName)
            }
            columnsQuery.close()
            
            // Add userId column if it doesn't exist
            if (!existingColumns.contains("userId")) {
              // Add userId column with default value for existing notes
              database.execSQL("ALTER TABLE notes ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
              
              // Create index for the new userId column
              database.execSQL("CREATE INDEX index_notes_userId ON notes (userId)")
              
              Log.d(TAG, "Migration 3-4: Added userId column to notes table")
            } else {
              Log.d(TAG, "Migration 3-4: userId column already exists in notes table")
            }
          } else {
            Log.w(TAG, "Migration 3-4: notes table doesn't exist, creating it with userId column")
            
            // Create notes table with userId column
            database.execSQL(
              "CREATE TABLE IF NOT EXISTS notes (" +
              "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
              "userId TEXT NOT NULL, " +
              "text TEXT NOT NULL, " +
              "createdAt INTEGER NOT NULL, " +
              "updatedAt INTEGER NOT NULL)"
            )
            
            // Create index for userId column
            database.execSQL("CREATE INDEX index_notes_userId ON notes (userId)")
          }
          
          Log.d(TAG, "Migration 3-4 completed successfully")
        } catch (e: Exception) {
          Log.e(TAG, "Error in migration 3-4", e)
          // We don't rethrow as this would crash the app
          // Instead, we'll let the fallback migration strategy handle it
        }
      }
      
      // Helper to check if a table exists
      private fun tableExists(database: SupportSQLiteDatabase, tableName: String): Boolean {
        val cursor = database.query(
          "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
          arrayOf(tableName)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
      }
    }

    // Get the singleton database instance
    fun getDatabase(context: Context): AppDatabase {
      val tempInstance = INSTANCE
      if (tempInstance != null) {
        return tempInstance
      }
      synchronized(this) {
        // Build a more robust database with better migration handling
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          // Use a consistent database name
          "money_mind_database"
        )
          // Add migrations in order
          .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
          // If migrations fail, fallback to destructive migration as last resort
          .fallbackToDestructiveMigration()
          // Enable logging for debugging (can be removed in production)
          .setJournalMode(JournalMode.AUTOMATIC)
          .build()
        
        INSTANCE = instance // Store the instance
        return instance // Return the instance
      }
    }
  }
}
