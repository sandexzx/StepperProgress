package com.example.stepperprogress.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector // Добавляем этот импорт
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stepperprogress.data.WorkoutRecord
import com.example.stepperprogress.ui.navigation.NavigationEvent
import com.example.stepperprogress.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    viewModel: WorkoutViewModel,
    onNavigationEvent: (NavigationEvent) -> Unit
) {
    val workoutHistory by viewModel.workoutHistory.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadWorkoutHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История тренировок") },
                navigationIcon = {
                    IconButton(onClick = { onNavigationEvent(NavigationEvent.NavigateToMainMenu) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (workoutHistory.isEmpty()) {
                Text(
                    text = "Пока нет завершенных тренировок за сегодня.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(workoutHistory) { record ->
                        WorkoutRecordCard(record = record)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutRecordCard(record: WorkoutRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(record.date)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Divider()
            Spacer(modifier = Modifier.height(4.dp))

            StatRow(
                icon = Icons.Rounded.LocalFireDepartment,
                label = "Сожжено калорий",
                value = "${formatCalories(record.workoutSession.currentCalories)} ккал"
            )
            StatRow(
                icon = Icons.Rounded.DirectionsWalk,
                label = "Шагов",
                value = record.workoutSession.steps.toString()
            )
            StatRow(
                icon = Icons.Rounded.Timer,
                label = "Длительность",
                value = formatDuration(record.workoutSession.let { session ->
                    val activeDurationMillis = (record.date - session.startTime) - session.pausedDuration
                    Duration.ofMillis(activeDurationMillis.coerceAtLeast(0L))
                })
            )
            StatRow(
                icon = Icons.Rounded.FitnessCenter,
                label = "Цель",
                value = "${formatCalories(record.workoutSession.targetCalories)} ккал"
            )
        }
    }
}

@Composable
private fun StatRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
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
