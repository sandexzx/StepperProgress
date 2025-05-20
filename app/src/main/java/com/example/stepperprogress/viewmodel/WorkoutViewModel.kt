package com.example.stepperprogress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepperprogress.data.CalibrationData
import com.example.stepperprogress.data.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Duration

class WorkoutViewModel : ViewModel() {
    private val _calibrationData = MutableStateFlow(CalibrationData())
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
        
        _calibrationData.update {
            it.copy(
                steps = steps,
                calories = totalCalories,
                caloriesPerStep = totalCalories.toDouble() / steps,
                timePerStep = duration.dividedBy(steps.toLong())
            )
        }
        resetWorkoutSession()
    }

    fun startWorkout(targetCalories: Int) {
        resetWorkoutSession()
        _workoutSession.update {
            it.copy(
                targetCalories = targetCalories,
                currentCalories = 0,
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
                accumulatedFractionalCalories += caloriesPerStep
                val wholeCalories = accumulatedFractionalCalories.toInt()
                accumulatedFractionalCalories -= wholeCalories
                
                session.copy(
                    steps = session.steps + 1,
                    currentCalories = session.currentCalories + wholeCalories
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
} 