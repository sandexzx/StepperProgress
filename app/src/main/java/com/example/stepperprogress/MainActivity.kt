package com.example.stepperprogress

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.stepperprogress.ui.navigation.NavigationEvent
import com.example.stepperprogress.ui.navigation.Screen
import com.example.stepperprogress.ui.screens.CalibrationScreen
import com.example.stepperprogress.ui.screens.MainMenuScreen
import com.example.stepperprogress.ui.screens.SettingsScreen
import com.example.stepperprogress.ui.screens.WorkoutScreen
import com.example.stepperprogress.viewmodel.SettingsViewModel
import com.example.stepperprogress.viewmodel.WorkoutViewModel

class MainActivity : ComponentActivity() {
    private val workoutViewModel: WorkoutViewModel by viewModels { 
        WorkoutViewModel.ViewModelFactory(application) 
    }
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent(workoutViewModel, settingsViewModel, this)
        }
    }
}

@Composable
fun AppContent(
    workoutViewModel: WorkoutViewModel,
    settingsViewModel: SettingsViewModel,
    activity: ComponentActivity
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }

    fun handleNavigation(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.NavigateToMainMenu -> currentScreen = Screen.MainMenu
            is NavigationEvent.NavigateToCalibration -> {
                workoutViewModel.startCalibration()
                currentScreen = Screen.Calibration
            }
            is NavigationEvent.NavigateToWorkout -> currentScreen = Screen.Workout
            is NavigationEvent.NavigateToSettings -> currentScreen = Screen.Settings
            is NavigationEvent.Exit -> {
                activity.finish()
                currentScreen = Screen.MainMenu
            }
        }
    }

    when (currentScreen) {
        Screen.MainMenu -> MainMenuScreen(onNavigationEvent = ::handleNavigation)
        Screen.Calibration -> CalibrationScreen(
            viewModel = workoutViewModel,
            onNavigationEvent = ::handleNavigation
        )
        Screen.Workout -> WorkoutScreen(
            viewModel = workoutViewModel,
            onNavigationEvent = ::handleNavigation
        )
        Screen.Settings -> SettingsScreen(
            viewModel = settingsViewModel,
            onNavigationEvent = ::handleNavigation
        )
    }
}