package org.mccmarion.radio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.mccmarion.radio.data.ScheduleDay
import org.mccmarion.radio.data.ScheduleProgram
import org.mccmarion.radio.ui.theme.Primary
import org.mccmarion.radio.ui.theme.Error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    schedule: List<ScheduleDay>,
    isLoading: Boolean,
    error: String?,
    currentDayIndex: Int,
    onRefresh: () -> Unit
) {
    var selectedDayIndex by remember { mutableIntStateOf(currentDayIndex) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Schedule") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Day Selector
            DaySelector(
                schedule = schedule,
                selectedDayIndex = selectedDayIndex,
                currentDayIndex = currentDayIndex,
                onDaySelected = { selectedDayIndex = it }
            )

            // Content
            when {
                isLoading && schedule.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null && schedule.isEmpty() -> {
                    ErrorView(message = error, onRetry = onRefresh)
                }
                else -> {
                    ScheduleList(
                        schedule = schedule,
                        selectedDayIndex = selectedDayIndex,
                        currentDayIndex = currentDayIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun DaySelector(
    schedule: List<ScheduleDay>,
    selectedDayIndex: Int,
    currentDayIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        LazyRow(
            modifier = Modifier.padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(schedule) { day ->
                val isSelected = day.id == selectedDayIndex
                val isToday = day.id == currentDayIndex

                Button(
                    onClick = { onDaySelected(day.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(50.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            day.shortName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                        if (isToday) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (isSelected) Color.White else Primary,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleList(
    schedule: List<ScheduleDay>,
    selectedDayIndex: Int,
    currentDayIndex: Int
) {
    val selectedDay = schedule.find { it.id == selectedDayIndex }
    val isToday = selectedDayIndex == currentDayIndex

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(selectedDay?.programs ?: emptyList()) { program ->
            ProgramCard(
                program = program,
                isCurrentlyPlaying = isToday && program.isCurrentlyPlaying()
            )
        }
    }
}

@Composable
private fun ProgramCard(
    program: ScheduleProgram,
    isCurrentlyPlaying: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        border = if (isCurrentlyPlaying) {
            androidx.compose.foundation.BorderStroke(2.dp, Primary)
        } else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Time Column
            Column(
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    program.startTime,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    program.endTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(IntrinsicSize.Max)
                    .defaultMinSize(minHeight = 40.dp)
                    .background(
                        if (isCurrentlyPlaying) Primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(1.5.dp)
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Program Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        program.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )

                    if (isCurrentlyPlaying) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "NOW",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                if (program.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        program.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Try Again")
            }
        }
    }
}
