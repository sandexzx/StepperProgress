package com.example.stepperprogress.ui.navigation

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object Workout : Screen("workout")
    object Settings : Screen("settings")
    object WorkoutHistory : Screen("workout_history")
}

sealed class NavigationEvent {
    object NavigateToMainMenu : NavigationEvent()
    object NavigateToWorkout : NavigationEvent()
    object NavigateToSettings : NavigationEvent()
    object NavigateToWorkoutHistory : NavigationEvent()
    object Exit : NavigationEvent()
}
