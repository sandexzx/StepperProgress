package com.example.stepperprogress.data

import java.time.Duration

data class CalibrationData(
    val steps: Int = 0,
    val calories: Int = 0,
    val caloriesPerStep: Double = 0.0,
    val timePerStep: Duration = Duration.ZERO
)

data class WorkoutSession(
    val targetCalories: Int = 0,
    val currentCalories: Int = 0,
    val steps: Int = 0,
    val startTime: Long = 0,
    val isPaused: Boolean = false,
    val isCalibrationMode: Boolean = false
) {
    val progressPercentage: Float
        get() = if (targetCalories > 0) {
            (currentCalories.toFloat() / targetCalories.toFloat()) * 100
        } else 0f

    val remainingCalories: Int
        get() = targetCalories - currentCalories

    val isGoalAchieved: Boolean
        get() = currentCalories >= targetCalories
} 