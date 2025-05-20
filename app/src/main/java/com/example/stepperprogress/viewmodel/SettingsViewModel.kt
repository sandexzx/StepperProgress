package com.example.stepperprogress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _caloriesPerSteps = MutableStateFlow(0.0f)
    val caloriesPerSteps: StateFlow<Float> = _caloriesPerSteps

    private val _stepsPerCalorie = MutableStateFlow(0.0f)
    val stepsPerCalorie: StateFlow<Float> = _stepsPerCalorie

    fun updateCaloriesPerSteps(calories: Float, steps: Float) {
        viewModelScope.launch {
            _caloriesPerSteps.value = calories
            _stepsPerCalorie.value = steps
            // TODO: Save to SharedPreferences
        }
    }
} 