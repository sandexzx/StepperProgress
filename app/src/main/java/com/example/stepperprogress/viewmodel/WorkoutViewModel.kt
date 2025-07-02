package com.example.stepperprogress.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stepperprogress.data.CalibrationData
import com.example.stepperprogress.data.CalibrationDataStore
import com.example.stepperprogress.data.WorkoutHistoryDataStore
import com.example.stepperprogress.data.WorkoutRecord
import com.example.stepperprogress.data.WorkoutSession
import com.example.stepperprogress.service.StepCounterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.Duration
import java.util.UUID

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "WorkoutViewModel"
    private val calibrationDataStore = CalibrationDataStore(application)
    private val workoutHistoryDataStore = WorkoutHistoryDataStore(application)
    
    private val _calibrationData = MutableStateFlow(calibrationDataStore.loadCalibrationData())
    val calibrationData: StateFlow<CalibrationData> = _calibrationData.asStateFlow()

    private val _workoutSession = MutableStateFlow(WorkoutSession())
    val workoutSession: StateFlow<WorkoutSession> = _workoutSession.asStateFlow()

    private val _workoutHistory = MutableStateFlow<List<WorkoutRecord>>(emptyList())
    val workoutHistory: StateFlow<List<WorkoutRecord>> = _workoutHistory.asStateFlow()

    private var lastStepTime: Long = 0
    private var accumulatedFractionalCalories: Double = 0.0
    private var lastStepCount: Int = 0
    private var stepCounterService: StepCounterService? = null
    
    // Автоматическая пауза
    private val AUTO_PAUSE_DELAY = 2000L // 3 секунды без шагов
    private var autoPauseJob: kotlinx.coroutines.Job? = null
    private var wasAutoPaused = false

    fun setStepCounterService(service: StepCounterService?) {
        stepCounterService = service
    }



    fun startWorkout(targetCalories: Int) {
        Log.d(TAG, "Starting workout with target $targetCalories calories")
        resetWorkoutSession()
        _workoutSession.update {
            it.copy(
                targetCalories = targetCalories.toDouble(),
                currentCalories = 0.0,
                steps = 0,
                startTime = System.currentTimeMillis(),
                isPaused = false,
                pauseStartTime = 0L,
                pausedDuration = 0L
            )
        }
        lastStepTime = System.currentTimeMillis()
        startStepCounter()
    }

    private fun startStepCounter() {
        Log.d(TAG, "Starting step counter service")
        val intent = Intent(getApplication(), StepCounterService::class.java)
        getApplication<Application>().startService(intent)
    }

    private fun stopStepCounter() {
        Log.d(TAG, "Stopping step counter service")
        val intent = Intent(getApplication(), StepCounterService::class.java)
        getApplication<Application>().stopService(intent)
        stepCounterService?.resetStepCounter()
    }

    fun updateSteps(newStepCount: Int) {
        Log.d(TAG, "Updating steps: new=$newStepCount, last=$lastStepCount")
        if (newStepCount > lastStepCount) {
            val stepsToAdd = newStepCount - lastStepCount
            lastStepCount = newStepCount
            
            repeat(stepsToAdd) {
                recordStep()
            }
        }
    }

    fun recordStep() {
        val currentTime = System.currentTimeMillis()
        val timePerStep = currentTime - lastStepTime
        lastStepTime = currentTime
        
        // Отменяем автопаузу при новом шаге
        cancelAutoPause()
        
        // Если была автопауза, снимаем её
        if (wasAutoPaused && _workoutSession.value.isPaused) {
            resumeFromAutoPause()
        }
        
        // Планируем автопаузу через заданное время
        scheduleAutoPause()

        _workoutSession.update { session ->
            val baseCaloriesPerStep = _calibrationData.value.caloriesPerStep
            val caloriesPerStep = if (session.isMovingUp) {
                baseCaloriesPerStep
            } else {
                baseCaloriesPerStep * 0.35 // 35% от базового значения при движении вниз
            }
            val newCalories = session.currentCalories + caloriesPerStep
            Log.d(TAG, "Recording step in workout mode: calories=$newCalories, direction=${if (session.isMovingUp) "up" else "down"}")
            session.copy(
                steps = session.steps + 1,
                currentCalories = newCalories
            )
        }
    }
    
    private fun scheduleAutoPause() {
        autoPauseJob = viewModelScope.launch {
            delay(AUTO_PAUSE_DELAY)
            if (!_workoutSession.value.isPaused) {
                Log.d(TAG, "Auto-pausing workout due to inactivity")
                wasAutoPaused = true
                _workoutSession.update { currentSession ->
                    currentSession.copy(isPaused = true, pauseStartTime = System.currentTimeMillis())
                }
            }
        }
    }
    
    private fun cancelAutoPause() {
        autoPauseJob?.cancel()
        autoPauseJob = null
    }
    
    private fun resumeFromAutoPause() {
        Log.d(TAG, "Resuming from auto-pause")
        val currentSession = _workoutSession.value
        val pauseDuration = System.currentTimeMillis() - currentSession.pauseStartTime
        _workoutSession.update {
            it.copy(
                isPaused = false,
                pausedDuration = it.pausedDuration + pauseDuration,
                pauseStartTime = 0
            )
        }
        wasAutoPaused = false
    }

    fun togglePause() {
        _workoutSession.update { currentSession ->
            val newPausedState = !currentSession.isPaused
            Log.d(TAG, "Toggling pause state to: $newPausedState")

            if (newPausedState) { // Переходим в состояние паузы
                cancelAutoPause() // Отменяем автопаузу при ручной паузе
                wasAutoPaused = false
                currentSession.copy(isPaused = true, pauseStartTime = System.currentTimeMillis())
            } else { // Выходим из состояния паузы
                val pauseDuration = System.currentTimeMillis() - currentSession.pauseStartTime
                scheduleAutoPause() // Планируем автопаузу при возобновлении
                currentSession.copy(
                    isPaused = false,
                    pausedDuration = currentSession.pausedDuration + pauseDuration,
                    pauseStartTime = 0 // Сбрасываем время начала паузы
                )
            }
        }
    }

    fun toggleDirection() {
        val newDirection = !_workoutSession.value.isMovingUp
        Log.d(TAG, "Toggling direction to: ${if (newDirection) "up" else "down"}")
        _workoutSession.update { it.copy(isMovingUp = newDirection) }
    }

    fun createWorkoutRecord(): WorkoutRecord? {
        val currentSession = _workoutSession.value
        return if (currentSession.steps > 0) {
            WorkoutRecord(
                id = UUID.randomUUID().toString(),
                date = System.currentTimeMillis(),
                workoutSession = currentSession,
                calibrationData = _calibrationData.value,
                timestamp = java.time.LocalDateTime.ofEpochSecond(currentSession.startTime / 1000, 0, java.time.ZoneOffset.UTC),
                duration = getWorkoutDuration()
            )
        } else {
            null
        }
    }

    fun endWorkout() {
        Log.d(TAG, "Ending workout")
        cancelAutoPause()
        wasAutoPaused = false

        // Сохраняем завершенную тренировку, если она имела шаги
        val currentSession = _workoutSession.value
        if (currentSession.steps > 0) {
            val record = WorkoutRecord(
                id = UUID.randomUUID().toString(),
                date = System.currentTimeMillis(),
                workoutSession = currentSession,
                calibrationData = _calibrationData.value,
                timestamp = java.time.LocalDateTime.ofEpochSecond(currentSession.startTime / 1000, 0, java.time.ZoneOffset.UTC),
                duration = getWorkoutDuration()
            )
            viewModelScope.launch {
                workoutHistoryDataStore.saveWorkoutRecord(record)
                Log.d(TAG, "Workout record saved: $record")
            }
        }

        resetWorkoutSession()
        stopStepCounter()
    }

    fun getEstimatedTimeToGoal(): Duration {
        val session = _workoutSession.value
        if (session.isPaused || session.targetCalories <= 0 || session.currentCalories <= 0) return Duration.ZERO

        val baseCaloriesPerStep = _calibrationData.value.caloriesPerStep
        val currentCaloriesPerStep = if (session.isMovingUp) {
            baseCaloriesPerStep
        } else {
            baseCaloriesPerStep * 0.35 // 35% от базового значения при движении вниз
        }
        
        // Рассчитываем время на основе текущего темпа тренировки
        val workoutDuration = getWorkoutDuration()
        val currentCaloriesPerSecond = session.currentCalories / workoutDuration.seconds.coerceAtLeast(1)
        val remainingSeconds = (session.remainingCalories / currentCaloriesPerSecond).toLong()

        return Duration.ofSeconds(remainingSeconds)
    }

    fun getWorkoutDuration(): Duration {
        val session = _workoutSession.value
        val currentTime = System.currentTimeMillis()

        val activeDurationMillis = if (session.isPaused) {
            // Если на паузе, то текущая длительность - это время до паузы
            // минус накопленная длительность паузы
            (session.pauseStartTime - session.startTime) - session.pausedDuration
        } else {
            // Если не на паузе, то текущая длительность - это общее время
            // минус накопленная длительность паузы
            (currentTime - session.startTime) - session.pausedDuration
        }
        return Duration.ofMillis(activeDurationMillis.coerceAtLeast(0L))
    }

    fun loadWorkoutHistory() {
        viewModelScope.launch {
            _workoutHistory.value = workoutHistoryDataStore.loadWorkoutRecords()
            Log.d(TAG, "Workout history loaded: ${_workoutHistory.value.size} records")
        }
    }

    private fun resetWorkoutSession() {
        Log.d(TAG, "Resetting workout session")
        cancelAutoPause()
        wasAutoPaused = false
        _workoutSession.value = WorkoutSession()
        accumulatedFractionalCalories = 0.0
        lastStepCount = 0
        // Убедимся, что эти поля тоже сбрасываются
        _workoutSession.update { it.copy(pausedDuration = 0L, pauseStartTime = 0L) }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
        stopStepCounter()
    }

    class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
                return WorkoutViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
