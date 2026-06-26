package com.example.data

import kotlinx.coroutines.flow.Flow

class MotorcycleRepository(private val dao: MotorcycleDao) {
    val profileFlow: Flow<MotorcycleProfile?> = dao.getProfileFlow()
    val schedulesFlow: Flow<List<ServiceSchedule>> = dao.getSchedulesFlow()
    val historyFlow: Flow<List<ServiceHistory>> = dao.getHistoryFlow()

    suspend fun getProfile(): MotorcycleProfile? = dao.getProfile()
    
    suspend fun saveProfile(profile: MotorcycleProfile) {
        dao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: MotorcycleProfile) {
        dao.updateProfile(profile)
    }

    suspend fun updateSchedule(schedule: ServiceSchedule) {
        dao.updateSchedule(schedule)
    }

    suspend fun insertHistory(history: ServiceHistory) {
        dao.insertHistory(history)
    }

    suspend fun deleteHistory(history: ServiceHistory) {
        dao.deleteHistory(history)
    }

    suspend fun clearAllData() {
        dao.clearSchedules()
        dao.clearHistory()
        dao.clearProfile()
    }

    suspend fun clearSchedules() {
        dao.clearSchedules()
    }

    suspend fun initializeDefaultSchedules(type: String, currentOdometer: Int) {
        val currentTime = System.currentTimeMillis()
        val schedules = mutableListOf(
            ServiceSchedule(
                componentName = "Oli Mesin",
                intervalKm = 3000,
                intervalMonths = 3,
                lastServicedKm = currentOdometer,
                lastServicedDate = currentTime
            ),
            ServiceSchedule(
                componentName = "Busi",
                intervalKm = 8000,
                intervalMonths = 8,
                lastServicedKm = currentOdometer,
                lastServicedDate = currentTime
            ),
            ServiceSchedule(
                componentName = "Saringan Udara",
                intervalKm = 12000,
                intervalMonths = 12,
                lastServicedKm = currentOdometer,
                lastServicedDate = currentTime
            ),
            ServiceSchedule(
                componentName = "Cairan Rem",
                intervalKm = 20000,
                intervalMonths = 24,
                lastServicedKm = currentOdometer,
                lastServicedDate = currentTime
            )
        )

        if (type == "Matic") {
            schedules.add(
                ServiceSchedule(
                    componentName = "Oli Gardan",
                    intervalKm = 8000,
                    intervalMonths = 6,
                    lastServicedKm = currentOdometer,
                    lastServicedDate = currentTime
                )
            )
            schedules.add(
                ServiceSchedule(
                    componentName = "V-Belt / CVT",
                    intervalKm = 12000,
                    intervalMonths = 12,
                    lastServicedKm = currentOdometer,
                    lastServicedDate = currentTime
                )
            )
        } else {
            schedules.add(
                ServiceSchedule(
                    componentName = "Setel & Lumas Rantai",
                    intervalKm = 1000,
                    intervalMonths = 1,
                    lastServicedKm = currentOdometer,
                    lastServicedDate = currentTime
                )
            )
            schedules.add(
                ServiceSchedule(
                    componentName = "Sprocket & Rantai",
                    intervalKm = 15000,
                    intervalMonths = 18,
                    lastServicedKm = currentOdometer,
                    lastServicedDate = currentTime
                )
            )
        }

        dao.insertSchedules(schedules)
    }
}
