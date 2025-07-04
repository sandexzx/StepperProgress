package com.example.stepperprogress.data

import java.time.Duration
import java.time.LocalDateTime

data class WorkoutSession(
    val targetCalories: Double = 0.0,
    val currentCalories: Double = 0.0,
    val steps: Int = 0,
    val startTime: Long = 0,
    val isPaused: Boolean = false,
    val pauseStartTime: Long = 0, // Время начала паузы
    val pausedDuration: Long = 0, // Общая продолжительность паузы
    val isMovingUp: Boolean = true
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

data class WorkoutRecord(
    val id: String, // Уникальный ID для записи
    val date: Long, // Дата и время завершения тренировки в миллисекундах
    val workoutSession: WorkoutSession,
    val calibrationData: CalibrationData,
    val timestamp: LocalDateTime, // Время начала тренировки
    val duration: Duration // Продолжительность тренировки
)
