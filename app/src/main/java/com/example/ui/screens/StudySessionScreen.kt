package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.StudySessionEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudySessionScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val timerSubject by viewModel.timerSubject.collectAsState()
    val timerTopic by viewModel.timerTopic.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    var subjectInput by remember { mutableStateOf(timerSubject) }
    var topicInput by remember { mutableStateOf(timerTopic) }
    var sessionNotes by remember { mutableStateOf("") }
    var showManualAddDialog by remember { mutableStateOf(false) }

    val subjects = listOf("Physics", "Chemistry", "Biology", "Hindi", "English")

    val formattedTimer = remember(timerSeconds) {
        val hrs = timerSeconds / 3600
        val mins = (timerSeconds % 3600) / 60
        val secs = timerSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
    }

    val todayTotalMinutes = remember(allSessions) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        allSessions.filter { it.dateString == todayStr }.sumOf { it.durationMinutes }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. Deep Work Stopwatch Hero
        item {
            GlassCard(
                backgroundColor = AccentNavy.copy(alpha = 0.25f),
                borderColor = AccentElectricBlue.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DEEP WORK STUDY TRACKER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentElectricBlue,
                        letterSpacing = 1.sp
                    )

                    // Big Circular Timer display
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(3.dp, if (isTimerRunning) ScoreGreen else AccentElectricBlue),
                        modifier = Modifier.size(190.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formattedTimer,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = subjectInput,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentElectricBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Subject Selector Pills
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(subjects) { sub ->
                            FilterChip(
                                selected = subjectInput == sub,
                                onClick = { subjectInput = sub },
                                label = { Text(sub, fontSize = 12.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        placeholder = { Text("What topic are you studying? (e.g. Gauss's Law)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Timer Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isTimerRunning) {
                            Button(
                                onClick = { viewModel.startTimer(subjectInput, topicInput.ifBlank { "Deep Study" }) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("start_deep_work_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ScoreGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start Session", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.pauseTimer() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ScoreYellow),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pause")
                            }
                        }

                        if (timerSeconds > 0) {
                            Button(
                                onClick = {
                                    viewModel.stopAndSaveTimer(sessionNotes)
                                    sessionNotes = ""
                                    topicInput = ""
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("stop_save_session_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ScoreRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log & Save")
                            }
                        }
                    }
                }
            }
        }

        // 2. Today's Total Study Time vs Target (4.5 - 6 Hours)
        item {
            val hrs = todayTotalMinutes / 60
            val mins = todayTotalMinutes % 60
            val targetHours = 5.0f
            val progressFrac = (todayTotalMinutes.toFloat() / (targetHours * 60f)).coerceIn(0f, 1f)

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S DEEP STUDY TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentElectricBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${hrs}h ${mins}m / 5h Target",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (todayTotalMinutes >= 300) ScoreGreen.copy(alpha = 0.2f) else AccentNavy.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${(progressFrac * 100).toInt()}% Target Met",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (todayTotalMinutes >= 300) ScoreGreen else AccentElectricBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressFrac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = AccentElectricBlue
                )
            }
        }

        // 3. Past Sessions Log History
        item {
            SectionHeader(
                title = "Study Session History (${allSessions.size})",
                actionText = "+ Log Past Study",
                onActionClick = { showManualAddDialog = true }
            )
        }

        if (allSessions.isEmpty()) {
            item {
                GlassCard {
                    Text(
                        text = "No study sessions recorded yet. Start the Deep Work stopwatch above or tap '+ Log Past Study'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(allSessions.take(10)) { session ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = session.subjectName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentElectricBlue
                                )
                                Text(
                                    text = "• ${session.durationMinutes} min",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ScoreGreen
                                )
                            }
                            Text(
                                text = session.topic,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (session.notes.isNotBlank()) {
                                Text(
                                    text = session.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = session.dateString,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showManualAddDialog) {
        ManualAddSessionDialog(
            onDismiss = { showManualAddDialog = false },
            onSave = { newSession ->
                coroutineScope.launch {
                    viewModel.repository.insertSession(newSession)
                }
                showManualAddDialog = false
            }
        )
    }
}

@Composable
fun ManualAddSessionDialog(
    onDismiss: () -> Unit,
    onSave: (StudySessionEntity) -> Unit
) {
    var subject by remember { mutableStateOf("Physics") }
    var topic by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("60") }
    var notes by remember { mutableStateOf("") }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Past Study Session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic Studied") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    label = { Text("Duration (Minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Formulas Mastered") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    val duration = durationMinutes.toIntOrNull() ?: 30
                    onSave(
                        StudySessionEntity(
                            subjectName = subject,
                            topic = topic.ifBlank { "Self Study" },
                            durationMinutes = duration,
                            startTimeMs = now - (duration * 60000L),
                            endTimeMs = now,
                            notes = notes,
                            dateString = todayStr
                        )
                    )
                }
            ) {
                Text("Log Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
