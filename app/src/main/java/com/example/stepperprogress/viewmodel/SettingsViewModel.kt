package com.example.stepperprogress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepperprogress.data.CalibrationDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val calibrationDataStore = CalibrationDataStore(application)
    
    private val _caloriesPerSteps = MutableStateFlow(0.0f)
    val caloriesPerSteps: StateFlow<Float> = _caloriesPerSteps

    private val _stepsPerCalorie = MutableStateFlow(0.0f)
    val stepsPerCalorie: StateFlow<Float> = _stepsPerCalorie

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    init {
        loadCalibrationData()
    }

    private fun loadCalibrationData() {
        val calibrationData = calibrationDataStore.loadCalibrationData()
        val caloriesPerStep = calibrationData.caloriesPerStep
        _caloriesPerSteps.value = caloriesPerStep.toFloat()
        _stepsPerCalorie.value = if (caloriesPerStep > 0.0) {
            (1.0 / caloriesPerStep).toFloat()
        } else {
            0.0f
        }
    }

    fun updateCaloriesPerSteps(calories: Float) {
        viewModelScope.launch {
            _caloriesPerSteps.value = calories
            _stepsPerCalorie.value = if (calories > 0f) {
                1f / calories
            } else {
                0f
            }
        }
    }

    fun updateStepsPerCalorie(steps: Float) {
        viewModelScope.launch {
            _stepsPerCalorie.value = steps
            _caloriesPerSteps.value = if (steps > 0f) {
                1f / steps
            } else {
                0f
            }
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            try {
                val calibrationData = calibrationDataStore.loadCalibrationData()
                calibrationDataStore.saveCalibrationData(
                    calibrationData.copy(
                        caloriesPerStep = _caloriesPerSteps.value.toDouble()
                    )
                )
                _saveSuccess.value = true
                // Reset success indicator after 2 seconds
                kotlinx.coroutines.delay(2000)
                _saveSuccess.value = false
            } catch (e: Exception) {
                _saveSuccess.value = false
            }
        }
    }
} 