package com.example.kenyanfoodrecognitionsystem.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Food::class, MealHistory::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun mealHistoryDao(): MealHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to version 2 (adding MealHistory table)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the meal_history table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `meal_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `imageData` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `dateString` TEXT NOT NULL,
                        `classifiedFoodName` TEXT NOT NULL,
                        `foodItems` TEXT NOT NULL,
                        `totalEnergy` REAL,
                        `totalProtein` REAL,
                        `totalFat` REAL,
                        `totalCarbohydrates` REAL,
                        `totalFibre` REAL,
                        `mealType` TEXT,
                        `notes` TEXT
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kenyan_foods.db"
                )
                    .createFromAsset("kenyan_foods.db")
                    .addMigrations(MIGRATION_1_2) // Add the migration
                    // REMOVED: .fallbackToDestructiveMigrationFrom(1) - This was the conflict!
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Optional: Method to clear instance for testing
        fun clearInstance() {
            INSTANCE = null
        }
    }
}