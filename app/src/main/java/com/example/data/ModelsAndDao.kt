package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "motorcycle_profile")
data class MotorcycleProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val brand: String,
    val model: String,
    val plateNumber: String,
    val currentOdometer: Int,
    val isSetupComplete: Boolean = false,
    val type: String, // "Matic", "Bebek" (Manual), "Sport"
    val averageDailyKm: Int = 20,
    val lastOdometerUpdateDate: Long = 0L,
    val lastOdometerValue: Int = 0,
    val odometerUpdateStreak: Int = 0,
    val lastOdometerUpdateStreakDate: Long = 0L
)

@Entity(tableName = "service_schedules")
data class ServiceSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val componentName: String,
    val intervalKm: Int,
    val intervalMonths: Int,
    val lastServicedKm: Int,
    val lastServicedDate: Long, // timestamp
    val isReminderEnabled: Boolean = true
)

enum class ComponentStatus {
    OK, WARNING, CRITICAL
}

@Entity(tableName = "service_history")
data class ServiceHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val componentName: String,
    val servicedKm: Int,
    val servicedDate: Long, // timestamp
    val cost: Double,
    val notes: String
)

@Dao
interface MotorcycleDao {
    @Query("SELECT * FROM motorcycle_profile WHERE id = 1")
    fun getProfileFlow(): Flow<MotorcycleProfile?>

    @Query("SELECT * FROM motorcycle_profile WHERE id = 1")
    suspend fun getProfile(): MotorcycleProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: MotorcycleProfile)

    @Update
    suspend fun updateProfile(profile: MotorcycleProfile)

    @Query("SELECT * FROM service_schedules ORDER BY id ASC")
    fun getSchedulesFlow(): Flow<List<ServiceSchedule>>

    @Query("SELECT * FROM service_schedules ORDER BY id ASC")
    suspend fun getSchedules(): List<ServiceSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ServiceSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ServiceSchedule>)

    @Update
    suspend fun updateSchedule(schedule: ServiceSchedule)

    @Query("SELECT * FROM service_history ORDER BY servicedDate DESC")
    fun getHistoryFlow(): Flow<List<ServiceHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ServiceHistory)

    @Delete
    suspend fun deleteHistory(history: ServiceHistory)

    @Query("DELETE FROM service_schedules")
    suspend fun clearSchedules()

    @Query("DELETE FROM service_history")
    suspend fun clearHistory()

    @Query("DELETE FROM motorcycle_profile")
    suspend fun clearProfile()
}
