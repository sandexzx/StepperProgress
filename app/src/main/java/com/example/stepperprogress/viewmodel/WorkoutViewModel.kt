package com.example.stepperprogress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stepperprogress.data.CalibrationData
import com.example.stepperprogress.data.CalibrationDataStore
import com.example.stepperprogress.data.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Duration

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val calibrationDataStore = CalibrationDataStore(application)
    
    private val _calibrationData = MutableStateFlow(calibrationDataStore.loadCalibrationData())
    val calibrationData: StateFlow<CalibrationData> = _calibrationData.asStateFlow()

    private val _workoutSession = MutableStateFlow(WorkoutSession())
    val workoutSession: StateFlow<WorkoutSession> = _workoutSession.asStateFlow()

    private var lastStepTime: Long = 0
    private var accumulatedFractionalCalories: Double = 0.0

    fun startCalibration() {
        resetWorkoutSession()
        _workoutSession.update { it.copy(isCalibrationMode = true, startTime = System.currentTimeMillis()) }
        lastStepTime = System.currentTimeMillis()
    }

    fun endCalibration(totalCalories: Int) {
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
    }

    fun startWorkout(targetCalories: Int) {
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
    }

    fun recordStep() {
        val currentTime = System.currentTimeMillis()
        val timePerStep = currentTime - lastStepTime
        lastStepTime = currentTime

        _workoutSession.update { session ->
            if (session.isCalibrationMode) {
                session.copy(steps = session.steps + 1)
            } else {
                val caloriesPerStep = _calibrationData.value.caloriesPerStep
                session.copy(
                    steps = session.steps + 1,
                    currentCalories = session.currentCalories + caloriesPerStep
                )
            }
        }
    }

    fun togglePause() {
        _workoutSession.update { it.copy(isPaused = !it.isPaused) }
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
        _workoutSession.value = WorkoutSession()
        accumulatedFractionalCalories = 0.0
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