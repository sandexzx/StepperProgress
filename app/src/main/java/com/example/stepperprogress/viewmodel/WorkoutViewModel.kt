package com.example.stepperprogress.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stepperprogress.data.CalibrationData
import com.example.stepperprogress.data.CalibrationDataStore
import com.example.stepperprogress.data.WorkoutSession
import com.example.stepperprogress.service.StepCounterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Duration

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "WorkoutViewModel"
    private val calibrationDataStore = CalibrationDataStore(application)
    
    private val _calibrationData = MutableStateFlow(calibrationDataStore.loadCalibrationData())
    val calibrationData: StateFlow<CalibrationData> = _calibrationData.asStateFlow()

    private val _workoutSession = MutableStateFlow(WorkoutSession())
    val workoutSession: StateFlow<WorkoutSession> = _workoutSession.asStateFlow()

    private var lastStepTime: Long = 0
    private var accumulatedFractionalCalories: Double = 0.0
    private var lastStepCount: Int = 0
    private var stepCounterService: StepCounterService? = null

    fun setStepCounterService(service: StepCounterService?) {
        stepCounterService = service
    }

    fun startCalibration() {
        Log.d(TAG, "Starting calibration")
        resetWorkoutSession()
        _workoutSession.update { it.copy(isCalibrationMode = true, startTime = System.currentTimeMillis()) }
        lastStepTime = System.currentTimeMillis()
        startStepCounter()
    }

    fun endCalibration(totalCalories: Int) {
        Log.d(TAG, "Ending calibration with $totalCalories calories")
        val session = _workoutSession.value
        val steps = session.steps
        val duration = Duration.ofMillis(System.currentTimeMillis() - session.startTime)
        
        val newCalibrationData = CalibrationData(
            steps = steps,
            calories = totalCalories,
            caloriesPerStep = totalCalories.toDouble() / steps,
            timePerStep = duration.dividedBy(steps.toLong())
        )
        
        _calibrationData.update { newCalibrationData }
        calibrationDataStore.saveCalibrationData(newCalibrationData)
        resetWorkoutSession()
        stopStepCounter()
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
                isCalibrationMode = false
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

        _workoutSession.update { session ->
            if (session.isCalibrationMode) {
                Log.d(TAG, "Recording step in calibration mode")
                session.copy(steps = session.steps + 1)
            } else {
                val caloriesPerStep = _calibrationData.value.caloriesPerStep
                val newCalories = session.currentCalories + caloriesPerStep
                Log.d(TAG, "Recording step in workout mode: calories=$newCalories")
                session.copy(
                    steps = session.steps + 1,
                    currentCalories = newCalories
                )
            }
        }
    }

    fun togglePause() {
        val newPausedState = !_workoutSession.value.isPaused
        Log.d(TAG, "Toggling pause state to: $newPausedState")
        _workoutSession.update { it.copy(isPaused = newPausedState) }
    }

    fun endWorkout() {
        Log.d(TAG, "Ending workout")
        resetWorkoutSession()
        stopStepCounter()
    }

    fun getEstimatedTimeToGoal(): Duration {
        val session = _workoutSession.value
        if (session.isPaused || session.targetCalories <= 0) return Duration.ZERO

        val caloriesPerStep = _calibrationData.value.caloriesPerStep
        val timePerStep = _calibrationData.value.timePerStep
        val remainingSteps = (session.remainingCalories / caloriesPerStep).toLong()

        return timePerStep.multipliedBy(remainingSteps)
    }

    fun getWorkoutDuration(): Duration {
        val session = _workoutSession.value
        return Duration.ofMillis(System.currentTimeMillis() - session.startTime)
    }

    private fun resetWorkoutSession() {
        Log.d(TAG, "Resetting workout session")
        _workoutSession.value = WorkoutSession()
        accumulatedFractionalCalories = 0.0
        lastStepCount = 0
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