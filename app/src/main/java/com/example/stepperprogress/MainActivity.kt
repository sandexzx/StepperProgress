package com.example.stepperprogress

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.stepperprogress.service.StepCounterService
import com.example.stepperprogress.ui.navigation.NavigationEvent
import com.example.stepperprogress.ui.navigation.Screen
import com.example.stepperprogress.ui.screens.CalibrationScreen
import com.example.stepperprogress.ui.screens.MainMenuScreen
import com.example.stepperprogress.ui.screens.SettingsScreen
import com.example.stepperprogress.ui.screens.WorkoutHistoryScreen
import com.example.stepperprogress.ui.screens.WorkoutScreen
import com.example.stepperprogress.viewmodel.MainScreenViewModel
import com.example.stepperprogress.viewmodel.SettingsViewModel
import com.example.stepperprogress.viewmodel.WorkoutViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val workoutViewModel: WorkoutViewModel by viewModels {
        WorkoutViewModel.ViewModelFactory(application)
    }
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val mainScreenViewModel: MainScreenViewModel by viewModels() // New ViewModel for MainScreen

    private var stepCounterService: StepCounterService? = null
    private var isServiceBound = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (hasStepCounterSensor()) {
                bindStepCounterService()
            } else {
                Toast.makeText(
                    this,
                    "This device doesn't have a step counter sensor",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                "Activity recognition permission is required for step counting",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected")
            stepCounterService = (service as? StepCounterService.LocalBinder)?.getService()
            workoutViewModel.setStepCounterService(stepCounterService)
            mainScreenViewModel.setStepCounterService(stepCounterService) // Pass service to MainScreenViewModel
            isServiceBound = true
            collectStepUpdates()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            stepCounterService = null
            workoutViewModel.setStepCounterService(null)
            mainScreenViewModel.setStepCounterService(null) // Clear service from MainScreenViewModel
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        setContent {
            AppContent(workoutViewModel, settingsViewModel, mainScreenViewModel, this) // Pass MainScreenViewModel
        }
    }

    private fun hasStepCounterSensor(): Boolean {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val hasSensor = stepSensor != null
        Log.d(TAG, "Device has step counter sensor: $hasSensor")
        return hasSensor
    }

    private fun bindStepCounterService() {
        Log.d(TAG, "Binding step counter service")
        Intent(this, StepCounterService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun collectStepUpdates() {
        Log.d(TAG, "Starting to collect step updates")
        lifecycleScope.launch {
            try {
                stepCounterService?.stepFlow?.collectLatest { steps ->
                    Log.d(TAG, "Received step update: $steps")
                    workoutViewModel.updateSteps(steps)
                    // MainScreenViewModel уже подписан на stepFlow через setStepCounterService
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting step updates", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            Log.d(TAG, "Unbinding service")
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}

@Composable
fun AppContent(
    workoutViewModel: WorkoutViewModel,
    settingsViewModel: SettingsViewModel,
    mainScreenViewModel: MainScreenViewModel, // New parameter
    activity: ComponentActivity
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }

    fun handleNavigation(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.NavigateToMainMenu -> {
                workoutViewModel.endWorkout()
                mainScreenViewModel.refreshData() // Refresh all data when returning to main menu
                currentScreen = Screen.MainMenu
            }
            is NavigationEvent.NavigateToCalibration -> {
                workoutViewModel.startCalibration()
                currentScreen = Screen.Calibration
            }
            is NavigationEvent.NavigateToWorkout -> currentScreen = Screen.Workout
            is NavigationEvent.NavigateToSettings -> currentScreen = Screen.Settings
            is NavigationEvent.NavigateToWorkoutHistory -> currentScreen = Screen.WorkoutHistory
            is NavigationEvent.Exit -> {
                activity.finish()
                currentScreen = Screen.MainMenu
            }
        }
    }

    when (currentScreen) {
        Screen.MainMenu -> MainMenuScreen(
            onNavigationEvent = ::handleNavigation,
            viewModel = mainScreenViewModel // Pass MainScreenViewModel
        )
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
        Screen.WorkoutHistory -> WorkoutHistoryScreen(
            viewModel = workoutViewModel,
            onNavigationEvent = ::handleNavigation
        )
    }
}
