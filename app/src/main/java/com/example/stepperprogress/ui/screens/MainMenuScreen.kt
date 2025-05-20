package com.example.stepperprogress.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stepperprogress.ui.navigation.NavigationEvent

@Composable
fun MainMenuScreen(
    onNavigationEvent: (NavigationEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Тренировка калорий",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { onNavigationEvent(NavigationEvent.NavigateToCalibration) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Калибровка")
        }

        Button(
            onClick = { onNavigationEvent(NavigationEvent.NavigateToWorkout) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Тренировка")
        }

        Button(
            onClick = { onNavigationEvent(NavigationEvent.NavigateToSettings) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Настройки")
        }

        Button(
            onClick = { onNavigationEvent(NavigationEvent.Exit) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Выход")
        }
    }
} 