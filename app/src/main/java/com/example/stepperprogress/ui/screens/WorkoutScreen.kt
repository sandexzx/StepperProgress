package com.example.stepperprogress.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stepperprogress.ui.components.WorkoutProgressBar
import com.example.stepperprogress.ui.navigation.NavigationEvent
import com.example.stepperprogress.viewmodel.WorkoutViewModel
import java.time.Duration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke

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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Rounded.FitnessCenter,
                contentDescription = "Тренировка",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Тренировка",
                style = MaterialTheme.typography.headlineLarge,
            )
        }

        // Progress Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${workoutSession.progressPercentage.toInt()}%",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            WorkoutProgressBar(
                progress = (workoutSession.progressPercentage / 100f).toFloat(),
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Слегка сузим для визуального акцента
                    .height(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${formatCalories(workoutSession.currentCalories)} из ${formatCalories(workoutSession.targetCalories)} ккал",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Direction indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (workoutSession.isMovingUp) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                contentDescription = "Направление движения",
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (workoutSession.isMovingUp) "Движение вверх" else "Движение вниз",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Stats Section
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp), // Скругляем углы для более мягкого вида
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Используем цвет из темы
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start, // Выравнивание контента внутри карточки
                verticalArrangement = Arrangement.spacedBy(12.dp) // Отступы между строками статистики
            ) {
                StatRow(
                    icon = Icons.Rounded.LocalFireDepartment,
                    label = "Сожжено калорий",
                    value = "${formatCalories(workoutSession.currentCalories)} / ${formatCalories(workoutSession.targetCalories)}"
                )
                StatRow(
                    icon = Icons.Rounded.DirectionsWalk,
                    label = "Шагов",
                    value = workoutSession.steps.toString()
                )
                StatRow(
                    icon = Icons.Rounded.Bolt,
                    label = "Калорий на шаг",
                    value = String.format("%.2f", if (workoutSession.isMovingUp) 
                        calibrationData.caloriesPerStep 
                    else 
                        calibrationData.caloriesPerStep * 0.35
                    )
                )
                StatRow(
                    icon = Icons.Rounded.Timer,
                    label = "Время тренировки",
                    value = formatDuration(viewModel.getWorkoutDuration())
                )
                if (!workoutSession.isGoalAchieved) {
                    StatRow(
                        icon = Icons.Rounded.Timer,
                        label = "Осталось времени",
                        value = formatDuration(viewModel.getEstimatedTimeToGoal()),
                        valueColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
        
        Spacer(Modifier.weight(1f)) // Этот Spacer отодвинет кнопки вниз экрана

        // Buttons Section
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Увеличиваем отступы между кнопками
        ) {
            // Вспомогательные кнопки в строке
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.togglePause() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (workoutSession.isPaused)
                            MaterialTheme.colorScheme.secondaryContainer // Используем цвета контейнеров для лучшего контраста
                        else
                            MaterialTheme.colorScheme.secondary,
                        contentColor = if (workoutSession.isPaused)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(
                        imageVector = if (workoutSession.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                        contentDescription = if (workoutSession.isPaused) "Продолжить" else "Пауза"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (workoutSession.isPaused) "Старт" else "Пауза") // Более короткие и активные названия
                }

                Button(
                    onClick = { viewModel.toggleDirection() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Icon(Icons.Rounded.SwapVert, contentDescription = "Сменить направление")
                    Spacer(Modifier.width(8.dp))
                    Text(if (workoutSession.isMovingUp) "Спуск" else "Подъем") // Короткие названия
                }
            }

            // Кнопка "Завершить" - менее акцентная
            OutlinedButton(
                onClick = { onNavigationEvent(NavigationEvent.NavigateToMainMenu) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Rounded.Stop, contentDescription = "Завершить")
                Spacer(Modifier.width(8.dp))
                Text("Завершить")
            }

        }
    }

    // Target Calories Dialog (UI без существенных изменений, стандартный вид подходит)
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

    // Goal Achieved Dialog (небольшие улучшения текста)
    if (workoutSession.isGoalAchieved) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Поздравляем!") },
            text = {
                Text(
                    "Вы достигли своей цели!\n\n" + // Добавляем пустую строку для лучшего разделения
                    "Сожжено калорий: ${formatCalories(workoutSession.currentCalories)}\n" + // Используем форматирование калорий
                    "Выполнено шагов: ${workoutSession.steps}\n\n" +
                    "Отличная работа!",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { onNavigationEvent(NavigationEvent.NavigateToMainMenu) }
                ) {
                    Text("Отлично!") // Более позитивный текст кнопки
                }
            }
        )
    }
}

@Composable
private fun StatRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {    
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End
        )
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
