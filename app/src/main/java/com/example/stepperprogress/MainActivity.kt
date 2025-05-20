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
import com.example.stepperprogress.ui.screens.WorkoutScreen
import com.example.stepperprogress.viewmodel.WorkoutViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: WorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent(viewModel, this)
        }
    }
}

@Composable
fun AppContent(viewModel: WorkoutViewModel, activity: ComponentActivity) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }

    fun handleNavigation(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.NavigateToMainMenu -> currentScreen = Screen.MainMenu
            is NavigationEvent.NavigateToCalibration -> {
                viewModel.startCalibration()
                currentScreen = Screen.Calibration
            }
            is NavigationEvent.NavigateToWorkout -> currentScreen = Screen.Workout
            is NavigationEvent.Exit -> {
                activity.finish()
                currentScreen = Screen.MainMenu
            }
        }
    }

    when (currentScreen) {
        Screen.MainMenu -> MainMenuScreen(onNavigationEvent = ::handleNavigation)
        Screen.Calibration -> CalibrationScreen(
            viewModel = viewModel,
            onNavigationEvent = ::handleNavigation
        )
        Screen.Workout -> WorkoutScreen(
            viewModel = viewModel,
            onNavigationEvent = ::handleNavigation
        )
    }
}