package com.example.stepperprogress.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration

data class CalibrationData(
    val steps: Int = 0,
    val calories: Int = 0,
    val caloriesPerStep: Double = 0.0,
    val timePerStep: Duration = Duration.ZERO,
    val dailyCaloriesGoal: Double = 300.0 // Default to 300 calories
)

class CalibrationDataStore(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val _calibrationData = MutableStateFlow(loadCalibrationData())
    val calibrationData: StateFlow<CalibrationData> = _calibrationData.asStateFlow()

    fun saveCalibrationData(data: CalibrationData) {
        sharedPreferences.edit().apply {
            putInt(KEY_STEPS, data.steps)
            putInt(KEY_CALORIES, data.calories)
            putFloat(KEY_CALORIES_PER_STEP, data.caloriesPerStep.toFloat())
            putLong(KEY_TIME_PER_STEP, data.timePerStep.toMillis())
            putFloat(KEY_DAILY_CALORIES_GOAL, data.dailyCaloriesGoal.toFloat()) // Save daily calories goal
            apply()
        }
        _calibrationData.value = data
    }

    fun loadCalibrationData(): CalibrationData {
        return CalibrationData(
            steps = sharedPreferences.getInt(KEY_STEPS, 0),
            calories = sharedPreferences.getInt(KEY_CALORIES, 0),
            caloriesPerStep = sharedPreferences.getFloat(KEY_CALORIES_PER_STEP, 0f).toDouble(),
            timePerStep = Duration.ofMillis(sharedPreferences.getLong(KEY_TIME_PER_STEP, 0)),
            dailyCaloriesGoal = sharedPreferences.getFloat(KEY_DAILY_CALORIES_GOAL, 300f).toDouble() // Load daily calories goal
        )
    }
    
    fun refreshData() {
        _calibrationData.value = loadCalibrationData()
    }

    companion object {
        private const val PREFS_NAME = "calibration_prefs"
        private const val KEY_STEPS = "steps"
        private const val KEY_CALORIES = "calories"
        private const val KEY_CALORIES_PER_STEP = "calories_per_step"
        private const val KEY_TIME_PER_STEP = "time_per_step"
        private const val KEY_DAILY_CALORIES_GOAL = "daily_calories_goal" // New key for daily calories goal
    }
}
