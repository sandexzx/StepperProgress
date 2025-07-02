package com.example.stepperprogress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepperprogress.data.CalibrationDataStore
import com.example.stepperprogress.data.WorkoutHistoryDataStore
import com.example.stepperprogress.service.StepCounterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val calibrationDataStore = CalibrationDataStore(application)
    private val workoutHistoryDataStore = WorkoutHistoryDataStore(application)
    private var stepCounterService: StepCounterService? = null

    private val _currentCalories = MutableStateFlow(0.0)
    val currentCalories: StateFlow<Double> = _currentCalories.asStateFlow()

    val dailyCaloriesGoal: StateFlow<Double> = calibrationDataStore.calibrationData.map { it.dailyCaloriesGoal }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 300.0)

    val progressPercentage: StateFlow<Float> = combine(_currentCalories, dailyCaloriesGoal) { calories, goal ->
        if (goal > 0) {
            (calories / goal).toFloat()
        } else {
            0f
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    init {
        loadTodayCalories()
    }

    private fun loadTodayCalories() {
        viewModelScope.launch {
            val workoutRecords = workoutHistoryDataStore.loadWorkoutRecords()
            val totalCalories = workoutRecords.sumOf { it.workoutSession.currentCalories }
            _currentCalories.value = totalCalories
        }
    }

    fun setStepCounterService(service: StepCounterService?) {
        stepCounterService = service
    }

    fun refreshCalories() {
        loadTodayCalories()
    }
    
    fun refreshData() {
        loadTodayCalories()
        calibrationDataStore.refreshData()
    }
}
