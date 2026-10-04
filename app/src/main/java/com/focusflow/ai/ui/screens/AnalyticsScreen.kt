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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.ui.components.BarChart
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.components.SectionHeader
import com.focusflow.ai.ui.components.StatTile
import com.focusflow.ai.ui.viewmodel.AnalyticsViewModel
import com.focusflow.ai.util.DateUtils

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = state.summary

    Scaffold(topBar = { FocusFlowTopBar(title = "Analytics") }) { padding ->
        if (summary == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatTile(
                            label = "Completed",
                            value = summary.tasksCompleted.toString(),
                            icon = Icons.Rounded.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = "Created",
                            value = summary.tasksCreated.toString(),
                            icon = Icons.Rounded.TaskAlt,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = "Focus time",
                            value = DateUtils.formatMinutes(summary.focusMinutes),
                            icon = Icons.Rounded.Timer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatTile(
                            label = "Completion rate",
                            value = "${(summary.completionRate * 100).toInt()}%",
                            icon = Icons.Rounded.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = "Study streak",
                            value = "${summary.currentStreak}d",
                            icon = Icons.Rounded.LocalFireDepartment,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item { ChartCard(title = "Last 7 days", points = summary.daily) }
                item { ChartCard(title = "Last 6 weeks", points = summary.weekly) }
                item { ChartCard(title = "Last 6 months", points = summary.monthly) }
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, points: List<com.focusflow.ai.domain.analytics.SeriesPoint>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = title)
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            BarChart(
                points = points,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
