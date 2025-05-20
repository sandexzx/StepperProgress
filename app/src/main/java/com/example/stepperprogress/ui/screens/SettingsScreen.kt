package com.example.stepperprogress.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stepperprogress.ui.navigation.NavigationEvent
import com.example.stepperprogress.viewmodel.SettingsViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigationEvent: (NavigationEvent) -> Unit
) {
    val caloriesPerSteps by viewModel.caloriesPerSteps.collectAsState()
    val stepsPerCalorie by viewModel.stepsPerCalorie.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    
    var caloriesPerStepsText by remember { mutableStateOf(caloriesPerSteps.toString()) }
    var stepsPerCalorieText by remember { mutableStateOf(stepsPerCalorie.toString()) }

    // Обновляем текстовые поля при изменении значений из ViewModel
    LaunchedEffect(caloriesPerSteps) {
        caloriesPerStepsText = caloriesPerSteps.toString()
    }
    LaunchedEffect(stepsPerCalorie) {
        stepsPerCalorieText = stepsPerCalorie.toString()
    }

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
            onValueChange = { 
                caloriesPerStepsText = it
                it.toFloatOrNull()?.let { value ->
                    viewModel.updateCaloriesPerSteps(value)
                }
            },
            label = { Text("Калории на шаг") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        OutlinedTextField(
            value = stepsPerCalorieText,
            onValueChange = { 
                stepsPerCalorieText = it
                it.toFloatOrNull()?.let { value ->
                    viewModel.updateStepsPerCalorie(value)
                }
            },
            label = { Text("Шагов на калорию") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Сохранить")
            }

            Button(
                onClick = { onNavigationEvent(NavigationEvent.NavigateToMainMenu) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Назад")
            }
        }

        // Success indicator
        AnimatedVisibility(
            visible = saveSuccess,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Настройки успешно сохранены",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
} 