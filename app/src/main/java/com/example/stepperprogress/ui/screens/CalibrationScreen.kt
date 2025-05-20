package com.example.stepperprogress.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stepperprogress.ui.navigation.NavigationEvent
import com.example.stepperprogress.viewmodel.WorkoutViewModel

@Composable
fun CalibrationScreen(
    viewModel: WorkoutViewModel,
    onNavigationEvent: (NavigationEvent) -> Unit
) {
    var showCaloriesDialog by remember { mutableStateOf(false) }
    val workoutSession by viewModel.workoutSession.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Калибровка",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Шагов: ${workoutSession.steps}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { viewModel.recordStep() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Шаг")
        }

        Button(
            onClick = { showCaloriesDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Завершить калибровку")
        }

        Button(
            onClick = { onNavigationEvent(NavigationEvent.NavigateToMainMenu) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Отмена")
        }
    }

    if (showCaloriesDialog) {
        var caloriesInput by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCaloriesDialog = false },
            title = { Text("Введите количество сожженных калорий") },
            text = {
                OutlinedTextField(
                    value = caloriesInput,
                    onValueChange = { caloriesInput = it },
                    label = { Text("Калории") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        caloriesInput.toIntOrNull()?.let { calories ->
                            viewModel.endCalibration(calories)
                            showCaloriesDialog = false
                            onNavigationEvent(NavigationEvent.NavigateToMainMenu)
                        }
                    }
                ) {
                    Text("Подтвердить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCaloriesDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
} 