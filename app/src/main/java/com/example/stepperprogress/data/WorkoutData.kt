package com.example.stepperprogress.data

import java.time.Duration

data class CalibrationData(
    val steps: Int = 0,
    val calories: Int = 0,
    val caloriesPerStep: Double = 0.0,
    val timePerStep: Duration = Duration.ZERO
)

data class WorkoutSession(
    val targetCalories: Double = 0.0,
    val currentCalories: Double = 0.0,
    val steps: Int = 0,
    val startTime: Long = 0,
    val isPaused: Boolean = false,
    val isCalibrationMode: Boolean = false
) {
    val progressPercentage: Double
        get() = if (targetCalories > 0) {
            (currentCalories / targetCalories) * 100
        } else 0.0

    val remainingCalories: Double
        get() = targetCalories - currentCalories

    val isGoalAchieved: Boolean
        get() = targetCalories > 0 && currentCalories >= targetCalories
} 