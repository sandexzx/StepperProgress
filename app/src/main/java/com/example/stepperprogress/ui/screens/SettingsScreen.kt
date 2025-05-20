package com.example.stepperprogress.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stepperprogress.ui.navigation.NavigationEvent
import com.example.stepperprogress.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigationEvent: (NavigationEvent) -> Unit
) {
    val caloriesPerSteps by viewModel.caloriesPerSteps.collectAsState()
    val stepsPerCalorie by viewModel.stepsPerCalorie.collectAsState()
    
    var caloriesPerStepsText by remember { mutableStateOf(caloriesPerSteps.toString()) }
    var stepsPerCalorieText by remember { mutableStateOf(stepsPerCalorie.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = caloriesPerStepsText,
            onValueChange = { caloriesPerStepsText = it },
            label = { Text("Калории на шаг") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = stepsPerCalorieText,
            onValueChange = { stepsPerCalorieText = it },
            label = { Text("Шагов на калорию") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                caloriesPerStepsText.toFloatOrNull()?.let { calories ->
                    stepsPerCalorieText.toFloatOrNull()?.let { steps ->
                        viewModel.updateCaloriesPerSteps(calories, steps)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }

        Button(
            onClick = { onNavigationEvent(NavigationEvent.NavigateToMainMenu) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Назад")
        }
    }
} 