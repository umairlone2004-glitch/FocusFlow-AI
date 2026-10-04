package com.focusflow.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.focusflow.ai.domain.model.ThemeMode

@Composable
fun OnboardingScreen(
    onComplete: (name: String, goalMinutes: Int, themeMode: ThemeMode) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var goal by remember { mutableIntStateOf(120) }
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Icon(
                imageVector = Icons.Rounded.Timer,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "FocusFlow AI",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Plan tasks, protect your focus, and watch your progress build.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            when (step) {
                0 -> StepName(name = name, onNameChange = { name = it })
                1 -> StepGoal(goal = goal, onGoalChange = { goal = it })
                2 -> StepTheme(themeMode = themeMode, onThemeChange = { themeMode = it })
            }

            Spacer(Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier.weight(1f)
                    ) { Text("Back") }
                }
                Button(
                    onClick = {
                        if (step < 2) step++ else onComplete(name, goal, themeMode)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(if (step < 2) "Continue" else "Get started") }
            }
        }
    }
}

@Composable
private fun StepName(name: String, onNameChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text("What should we call you?", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StepGoal(goal: Int, onGoalChange: (Int) -> Unit) {
    val options = listOf(60, 120, 180, 240)
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text("Daily focus goal", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "How much focused time would you like to hit each day?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        options.forEach { minutes ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = goal == minutes, onClick = { onGoalChange(minutes) })
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = goal == minutes, onClick = { onGoalChange(minutes) })
                Text("${minutes / 60} hours", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun StepTheme(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text("Choose a theme", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        ThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = themeMode == mode, onClick = { onThemeChange(mode) })
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = themeMode == mode, onClick = { onThemeChange(mode) })
                Icon(
                    imageVector = when (mode) {
                        ThemeMode.LIGHT -> Icons.Rounded.LightMode
                        ThemeMode.DARK -> Icons.Rounded.DarkMode
                        ThemeMode.SYSTEM -> Icons.Rounded.Settings
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}
