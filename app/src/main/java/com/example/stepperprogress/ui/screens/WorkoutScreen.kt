package com.example.stepperprogress.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stepperprogress.ui.components.WorkoutProgressBar
import com.example.stepperprogress.ui.navigation.NavigationEvent
import com.example.stepperprogress.viewmodel.WorkoutViewModel
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import kotlin.math.abs

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    onNavigationEvent: (NavigationEvent) -> Unit
) {
    var showTargetCaloriesDialog by remember { mutableStateOf(true) }
    val workoutSession by viewModel.workoutSession.collectAsState()
    val calibrationData by viewModel.calibrationData.collectAsState()

    LaunchedEffect(Unit) {
        if (calibrationData.caloriesPerStep == 0.0) {
            onNavigationEvent(NavigationEvent.NavigateToCalibration)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Тренировка",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Progress Section
        Text(
            text = "${workoutSession.progressPercentage.toInt()}%",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        WorkoutProgressBar(
            progress = (workoutSession.progressPercentage / 100f).toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Text(
            text = "${formatCalories(workoutSession.currentCalories)} ккал",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Stats Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatRow(
                    "Сожжено калорий", 
                    "${formatCalories(workoutSession.currentCalories)}/${formatCalories(workoutSession.targetCalories)}"
                )
                StatRow("Шагов", workoutSession.steps.toString())
                StatRow("Время тренировки", formatDuration(viewModel.getWorkoutDuration()))
                if (!workoutSession.isGoalAchieved) {
                    StatRow("Осталось времени", formatDuration(viewModel.getEstimatedTimeToGoal()))
                }
            }
        }

        // Buttons Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.togglePause() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (workoutSession.isPaused) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(if (workoutSession.isPaused) "Продолжить" else "Пауза")
            }

            Button(
                onClick = { onNavigationEvent(NavigationEvent.NavigateToMainMenu) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Завершить")
            }

            Button(
                onClick = { viewModel.recordStep() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                enabled = !workoutSession.isPaused,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Шаг",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }

    // Target Calories Dialog
    if (showTargetCaloriesDialog) {
        var targetCaloriesInput by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { /* Dialog cannot be dismissed */ },
            title = { Text("Установите цель по калориям") },
            text = {
                OutlinedTextField(
                    value = targetCaloriesInput,
                    onValueChange = { targetCaloriesInput = it },
                    label = { Text("Целевые калории") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        targetCaloriesInput.toIntOrNull()?.let { calories ->
                            viewModel.startWorkout(calories)
                            showTargetCaloriesDialog = false
                        }
                    }
                ) {
                    Text("Начать")
                }
            }
        )
    }

    // Goal Achieved Dialog
    if (workoutSession.isGoalAchieved) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Поздравляем!") },
            text = {
                Text(
                    "Вы достигли своей цели!\n" +
                    "Сожжено калорий: ${workoutSession.currentCalories}\n" +
                    "Выполнено шагов: ${workoutSession.steps}",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { onNavigationEvent(NavigationEvent.NavigateToMainMenu) }
                ) {
                    Text("Завершить")
                }
            }
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Text(text = value)
    }
}

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()
    val seconds = duration.toSecondsPart()
    
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
}

private fun formatCalories(calories: Double): String {
    return if (calories == calories.toInt().toDouble()) {
        calories.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", calories).trimEnd('0').trimEnd('.')
    }
} 