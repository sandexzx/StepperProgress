package com.example.stepperprogress.ui.navigation

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object Calibration : Screen("calibration")
    object Workout : Screen("workout")
    object Settings : Screen("settings")
}

sealed class NavigationEvent {
    object NavigateToMainMenu : NavigationEvent()
    object NavigateToCalibration : NavigationEvent()
    object NavigateToWorkout : NavigationEvent()
    object NavigateToSettings : NavigationEvent()
    object Exit : NavigationEvent()
} 