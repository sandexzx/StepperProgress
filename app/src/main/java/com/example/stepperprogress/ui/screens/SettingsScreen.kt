package com.example.stepperprogress.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stepperprogress.ui.navigation.NavigationEvent
import com.example.stepperprogress.viewmodel.SettingsViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
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

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.background
        )
    )

    LaunchedEffect(caloriesPerSteps) {
        caloriesPerStepsText = caloriesPerSteps.toString()
    }
    LaunchedEffect(stepsPerCalorie) {
        stepsPerCalorieText = stepsPerCalorie.toString()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 72.dp,
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Настройки",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            SettingsTextField(
                value = caloriesPerStepsText,
                onValueChange = { 
                    caloriesPerStepsText = it
                    it.toFloatOrNull()?.let { value ->
                        viewModel.updateCaloriesPerSteps(value)
                    }
                },
                label = "Калории на шаг",
                icon = Icons.Default.Info,
                delay = 100
            )

            SettingsTextField(
                value = stepsPerCalorieText,
                onValueChange = { 
                    stepsPerCalorieText = it
                    it.toFloatOrNull()?.let { value ->
                        viewModel.updateStepsPerCalorie(value)
                    }
                },
                label = "Шагов на калорию",
                icon = Icons.Default.Info,
                delay = 200
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сохранить")
                }

                OutlinedButton(
                    onClick = { onNavigationEvent(NavigationEvent.NavigateToMainMenu) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Назад")
                }
            }

            AnimatedVisibility(
                visible = saveSuccess,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Настройки успешно сохранены",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    delay: Int
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(delayMillis = delay)) + slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )
            }
        }
    }
} 