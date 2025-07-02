package com.example.stepperprogress.ui.navigation

import com.example.stepperprogress.data.WorkoutRecord

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object Workout : Screen("workout")
    object Settings : Screen("settings")
    object WorkoutHistory : Screen("workout_history")
    data class WorkoutSummary(val workoutRecord: WorkoutRecord) : Screen("workout_summary")
}

sealed class NavigationEvent {
    object NavigateToMainMenu : NavigationEvent()
    object NavigateToWorkout : NavigationEvent()
    object NavigateToSettings : NavigationEvent()
    object NavigateToWorkoutHistory : NavigationEvent()
    data class NavigateToWorkoutSummary(val workoutRecord: WorkoutRecord) : NavigationEvent()
    object Exit : NavigationEvent()
}
