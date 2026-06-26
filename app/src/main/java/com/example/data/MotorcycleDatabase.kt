package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MotorcycleProfile::class, ServiceSchedule::class, ServiceHistory::class],
    version = 4,
    exportSchema = false
)
abstract class MotorcycleDatabase : RoomDatabase() {
    abstract fun dao(): MotorcycleDao

    companion object {
        @Volatile
        private var INSTANCE: MotorcycleDatabase? = null

        fun getDatabase(context: Context): MotorcycleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MotorcycleDatabase::class.java,
                    "motorcycle_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
