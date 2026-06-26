package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MotorcycleViewModel(private val repository: MotorcycleRepository) : ViewModel() {
    private val _isLoaded = MutableStateFlow(false)
    val isLoaded = _isLoaded.asStateFlow()

    private val _profile = MutableStateFlow<MotorcycleProfile?>(null)
    val profile = _profile.asStateFlow()

    val schedules = repository.schedulesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history = repository.historyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.profileFlow.collect { updatedProfile ->
                _profile.value = updatedProfile
                _isLoaded.value = true
            }
        }
    }

    // Health state: Calculates percentage depending on the wear of all components
    val healthScore = combine(profile, schedules) { prof, schedList ->
        if (prof == null || schedList.isEmpty()) {
            100
        } else {
            var sumHealth = 0f
            schedList.forEach { sched ->
                val usedKm = (prof.currentOdometer - sched.lastServicedKm).coerceAtLeast(0)
                val ratio = usedKm.toFloat() / sched.intervalKm.toFloat()
                val componentHealth = (1.0f - ratio).coerceIn(0f, 1f)
                sumHealth += componentHealth
            }
            ((sumHealth / schedList.size) * 100).toInt()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    // Outstanding Reminders: Automatik notifications when a component is equal to or over 85% of its lifespan
    val activeReminders = combine(profile, schedules) { prof, schedList ->
        if (prof == null) {
            emptyList()
        } else {
            schedList.filter { sched ->
                val usedKm = (prof.currentOdometer - sched.lastServicedKm).coerceAtLeast(0)
                // Flag if used km is 80% or more of the interval, or overdue
                val isDue = usedKm >= (sched.intervalKm * 0.8)
                isDue && sched.isReminderEnabled
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Onboarding Form States
    private val _onboardingName = MutableStateFlow("")
    val onboardingName = _onboardingName.asStateFlow()

    private val _onboardingBrand = MutableStateFlow("")
    val onboardingBrand = _onboardingBrand.asStateFlow()

    private val _onboardingModel = MutableStateFlow("")
    val onboardingModel = _onboardingModel.asStateFlow()

    private val _onboardingPlate = MutableStateFlow("")
    val onboardingPlate = _onboardingPlate.asStateFlow()

    private val _onboardingOdometer = MutableStateFlow("0")
    val onboardingOdometer = _onboardingOdometer.asStateFlow()

    private val _onboardingType = MutableStateFlow("Matic") // Matic, Bebek, Sport
    val onboardingType = _onboardingType.asStateFlow()

    private val _onboardingAverageKm = MutableStateFlow("20")
    val onboardingAverageKm = _onboardingAverageKm.asStateFlow()

    fun updateOnboardingFields(
        name: String? = null,
        brand: String? = null,
        model: String? = null,
        plate: String? = null,
        odometer: String? = null,
        type: String? = null,
        averageKm: String? = null
    ) {
        if (name != null) _onboardingName.value = name
        if (brand != null) _onboardingBrand.value = brand
        if (model != null) _onboardingModel.value = model
        if (plate != null) _onboardingPlate.value = plate
        if (odometer != null) _onboardingOdometer.value = odometer
        if (type != null) _onboardingType.value = type
        if (averageKm != null) _onboardingAverageKm.value = averageKm
    }

    fun completeOnboarding() {
        val odo = _onboardingOdometer.value.toIntOrNull() ?: 0
        val avgKm = _onboardingAverageKm.value.toIntOrNull() ?: 20
        val prof = MotorcycleProfile(
            name = _onboardingName.value.ifBlank { "Motorku" },
            brand = _onboardingBrand.value.ifBlank { "Honda" },
            model = _onboardingModel.value.ifBlank { "Vario 150" },
            plateNumber = _onboardingPlate.value.ifBlank { "B 1234 ABC" },
            currentOdometer = odo,
            isSetupComplete = true,
            type = _onboardingType.value,
            averageDailyKm = avgKm,
            lastOdometerUpdateDate = System.currentTimeMillis(),
            lastOdometerValue = odo,
            odometerUpdateStreak = 1,
            lastOdometerUpdateStreakDate = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.clearAllData() // CRITICAL: Clear all old data to prevent mixed-up/duplicated schedules or profiles
            repository.saveProfile(prof)
            repository.initializeDefaultSchedules(prof.type, odo)
        }
    }

    private fun calculateNewStreak(currentProf: MotorcycleProfile, currentTime: Long): Pair<Int, Long> {
        val lastStreakDate = currentProf.lastOdometerUpdateStreakDate
        var newStreak = currentProf.odometerUpdateStreak
        var newStreakDate = currentProf.lastOdometerUpdateStreakDate

        if (lastStreakDate == 0L) {
            newStreak = 1
            newStreakDate = currentTime
        } else {
            val diffMs = currentTime - lastStreakDate
            val diffHours = diffMs.toFloat() / (1000f * 60f * 60f)
            if (diffHours in 12.0..36.0) {
                newStreak += 1
                newStreakDate = currentTime
            } else if (diffHours > 36.0) {
                newStreak = 1
                newStreakDate = currentTime
            }
        }
        return Pair(newStreak, newStreakDate)
    }

    fun updateOdometer(newOdoo: Int) {
        val currentProf = profile.value ?: return
        val currentTime = System.currentTimeMillis()
        val oldOdo = currentProf.currentOdometer
        val oldDate = currentProf.lastOdometerUpdateDate
        
        var calculatedAvg = currentProf.averageDailyKm
        if (oldDate > 0L && newOdoo > oldOdo) {
            val timeDiffMs = currentTime - oldDate
            val timeDiffHours = timeDiffMs.toFloat() / (1000f * 60f * 60f)
            if (timeDiffHours >= 0.016f) { // set at least 1 minute elapsed
                val daysFraction = timeDiffHours / 24f
                val kmDiff = newOdoo - oldOdo
                val calculated = (kmDiff / daysFraction).toInt()
                if (calculated in 1..300) {
                    calculatedAvg = calculated
                }
            }
        }
        
        val streakPair = calculateNewStreak(currentProf, currentTime)
        
        viewModelScope.launch {
            repository.updateProfile(currentProf.copy(
                currentOdometer = newOdoo,
                lastOdometerValue = newOdoo,
                lastOdometerUpdateDate = currentTime,
                averageDailyKm = calculatedAvg,
                odometerUpdateStreak = streakPair.first,
                lastOdometerUpdateStreakDate = streakPair.second
            ))
        }
    }

    fun updateAverageDailyKm(newAvgKm: Int) {
        val currentProf = profile.value ?: return
        viewModelScope.launch {
            repository.updateProfile(currentProf.copy(averageDailyKm = newAvgKm))
        }
    }

    fun updateFullProfile(
        name: String,
        brand: String,
        model: String,
        plateNumber: String,
        type: String,
        averageDailyKm: Int
    ) {
        val currentProf = profile.value ?: return
        val typeChanged = currentProf.type != type
        viewModelScope.launch {
            val updatedProf = currentProf.copy(
                name = name,
                brand = brand,
                model = model,
                plateNumber = plateNumber,
                type = type,
                averageDailyKm = averageDailyKm
            )
            repository.updateProfile(updatedProf)
            if (typeChanged) {
                repository.clearSchedules()
                repository.initializeDefaultSchedules(type, updatedProf.currentOdometer)
            }
        }
    }

    fun toggleReminder(scheduleId: Int, isEnabled: Boolean) {
        val currentSchedules = schedules.value
        val schedule = currentSchedules.find { it.id == scheduleId } ?: return
        viewModelScope.launch {
            repository.updateSchedule(schedule.copy(isReminderEnabled = isEnabled))
        }
    }

    // Record / Log a service routine replacement completed
    fun logServiceCompleted(scheduleId: Int, cost: Double, notes: String, customOdoo: Int? = null) {
        val currentProf = profile.value ?: return
        val currentSchedules = schedules.value
        val schedule = currentSchedules.find { it.id == scheduleId } ?: return
        val odo = customOdoo ?: currentProf.currentOdometer
        val currentTime = System.currentTimeMillis()

        viewModelScope.launch {
            // 1. Insert history
            val historyItem = ServiceHistory(
                componentName = schedule.componentName,
                servicedKm = odo,
                servicedDate = currentTime,
                cost = cost,
                notes = notes.ifBlank { "Penggantian berkala ${schedule.componentName}" }
            )
            repository.insertHistory(historyItem)

            // 2. Update schedule's lastServiced values
            val updatedSchedule = schedule.copy(
                lastServicedKm = odo,
                lastServicedDate = currentTime
            )
            repository.updateSchedule(updatedSchedule)

            // 3. If the custom odometer is higher than current, update motorcycle's current odometer too
            if (odo > currentProf.currentOdometer) {
                val oldOdo = currentProf.currentOdometer
                val oldDate = currentProf.lastOdometerUpdateDate
                var calculatedAvg = currentProf.averageDailyKm
                if (oldDate > 0L) {
                    val timeDiffMs = currentTime - oldDate
                    val timeDiffHours = timeDiffMs.toFloat() / (1000f * 60f * 60f)
                    if (timeDiffHours >= 0.016f) {
                        val daysFraction = timeDiffHours / 24f
                        val kmDiff = odo - oldOdo
                        val calculated = (kmDiff / daysFraction).toInt()
                        if (calculated in 1..300) {
                            calculatedAvg = calculated
                        }
                    }
                }
                val streakPair = calculateNewStreak(currentProf, currentTime)
                repository.updateProfile(currentProf.copy(
                    currentOdometer = odo,
                    lastOdometerValue = odo,
                    lastOdometerUpdateDate = currentTime,
                    averageDailyKm = calculatedAvg,
                    odometerUpdateStreak = streakPair.first,
                    lastOdometerUpdateStreakDate = streakPair.second
                ))
            }
        }
    }

    // Add a custom schedule (e.g., replacement of customized components like "Kampas Rem Belakang" or "Ban Belakang")
    fun addCustomSchedule(componentName: String, intervalKm: Int, intervalMonths: Int, lastServicedKmCheck: Int) {
        val currentProf = profile.value ?: return
        val currentTime = System.currentTimeMillis()
        viewModelScope.launch {
            val customSched = ServiceSchedule(
                componentName = componentName,
                intervalKm = intervalKm,
                intervalMonths = intervalMonths,
                lastServicedKm = lastServicedKmCheck,
                lastServicedDate = currentTime
            )
            repository.updateSchedule(customSched)
        }
    }

    fun deleteHistory(historyItem: ServiceHistory) {
        viewModelScope.launch {
            repository.deleteHistory(historyItem)
        }
    }

    // Reset setup logic (to allow clearing database easily or re-onboarding)
    fun resetApp() {
        viewModelScope.launch {
            repository.clearAllData()
            _onboardingName.value = ""
            _onboardingBrand.value = ""
            _onboardingModel.value = ""
            _onboardingPlate.value = ""
            _onboardingOdometer.value = "0"
            _onboardingType.value = "Matic"
            _onboardingAverageKm.value = "20"
            
            // Overwrite/insert incomplete profile so that flow emits
            repository.saveProfile(MotorcycleProfile(id = 1, name = "", brand = "", model = "", plateNumber = "", currentOdometer = 0, isSetupComplete = false, type = "Matic"))
        }
    }
}

class MotorcycleViewModelFactory(private val repository: MotorcycleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MotorcycleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MotorcycleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
