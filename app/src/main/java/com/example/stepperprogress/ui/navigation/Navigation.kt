package com.example.stepperprogress.ui.navigation

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object Calibration : Screen("calibration")
    object Workout : Screen("workout")
}

sealed class NavigationEvent {
    object NavigateToMainMenu : NavigationEvent()
    object NavigateToCalibration : NavigationEvent()
    object NavigateToWorkout : NavigationEvent()
    object Exit : NavigationEvent()
} 