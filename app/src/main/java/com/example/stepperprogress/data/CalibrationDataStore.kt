package com.example.stepperprogress.data

import android.content.Context
import android.content.SharedPreferences
import java.time.Duration

class CalibrationDataStore(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    fun saveCalibrationData(data: CalibrationData) {
        sharedPreferences.edit().apply {
            putInt(KEY_STEPS, data.steps)
            putInt(KEY_CALORIES, data.calories)
            putFloat(KEY_CALORIES_PER_STEP, data.caloriesPerStep.toFloat())
            putLong(KEY_TIME_PER_STEP, data.timePerStep.toMillis())
            apply()
        }
    }

    fun loadCalibrationData(): CalibrationData {
        return CalibrationData(
            steps = sharedPreferences.getInt(KEY_STEPS, 0),
            calories = sharedPreferences.getInt(KEY_CALORIES, 0),
            caloriesPerStep = sharedPreferences.getFloat(KEY_CALORIES_PER_STEP, 0f).toDouble(),
            timePerStep = Duration.ofMillis(sharedPreferences.getLong(KEY_TIME_PER_STEP, 0))
        )
    }

    companion object {
        private const val PREFS_NAME = "calibration_prefs"
        private const val KEY_STEPS = "steps"
        private const val KEY_CALORIES = "calories"
        private const val KEY_CALORIES_PER_STEP = "calories_per_step"
        private const val KEY_TIME_PER_STEP = "time_per_step"
    }
} 