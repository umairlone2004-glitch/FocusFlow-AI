package com.focusflow.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.focusflow.ai.domain.analytics.SeriesPoint
import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.ui.theme.PriorityHigh
import com.focusflow.ai.ui.theme.PriorityLow
import com.focusflow.ai.ui.theme.PriorityMedium
import com.focusflow.ai.ui.theme.PriorityUrgent
import com.focusflow.ai.util.DateUtils
import java.time.LocalDate

fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.LOW -> PriorityLow
    Priority.MEDIUM -> PriorityMedium
    Priority.HIGH -> PriorityHigh
    Priority.URGENT -> PriorityUrgent
}

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val color = priorityColor(priority)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.14f)
    ) {
        Text(
            text = priority.label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TagChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = "#$text",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Rounded.CheckCircle
                    else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "Mark incomplete" else "Mark complete",
                    tint = if (task.isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PriorityBadge(task.priority)
                    task.dueDate?.let { due ->
                        val overdue = task.isOverdue
                        Text(
                            text = DateUtils.relativeDueLabel(due, LocalDate.now()),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (overdue) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    size: Int = 120
) {
    Box(modifier = modifier.size(size.dp), contentAlignment = Alignment.Center) {
        androidx.compose.material3.CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.size(size.dp),
            strokeWidth = 8.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        if (label != null) {
            Text(text = label, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun BarChart(
    points: List<SeriesPoint>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val max = (points.maxOfOrNull { it.value } ?: 0).coerceAtLeast(1)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { point ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = point.value.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((72 * point.value / max).coerceAtLeast(2).dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(barColor)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusFlowTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

val ScreenPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
