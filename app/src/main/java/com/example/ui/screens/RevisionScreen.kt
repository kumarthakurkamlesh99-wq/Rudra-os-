package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.RevisionLogEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RevisionScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val dueRevisions by viewModel.dueRevisions.collectAsState()
    val allLogs by viewModel.repository.allRevisionLogs.collectAsState(initial = emptyList())
    val completedRevisions by viewModel.completedRevisions.collectAsState()

    var showScheduleDialog by remember { mutableStateOf(false) }

    val upcomingRevisions = remember(allLogs) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        allLogs.filter { !it.isCompleted && it.scheduledDate > today }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Revision Engine Philosophy Card (PDF Section 7 & 8)
        item {
            GlassCard(
                backgroundColor = AccentNavy.copy(alpha = 0.2f),
                borderColor = AccentElectricBlue.copy(alpha = 0.4f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Update, contentDescription = null, tint = AccentElectricBlue, modifier = Modifier.size(28.dp))
                    Column {
                        Text(
                            text = "SPACED REPETITION ENGINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentElectricBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "“Brute force nahi, smart repeat. Har revision ke baad interval badhta hai: Same Day → +1d → +3d → +7d → +15d → +30d.”",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Action header
        item {
            SectionHeader(
                title = "Due Today (${dueRevisions.size})",
                actionText = "+ Schedule Revision",
                onActionClick = { showScheduleDialog = true }
            )
        }

        if (dueRevisions.isEmpty()) {
            item {
                GlassCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ScoreGreen)
                        Text("No revisions overdue! All topics up to date.", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        } else {
            items(dueRevisions) { rev ->
                RevisionCard(
                    log = rev,
                    onMarkCompleted = {
                        viewModel.markRevisionDone(rev.id, rev.chapterId, rev.intervalLabel)
                    }
                )
            }
        }

        // Upcoming Revisions
        if (upcomingRevisions.isNotEmpty()) {
            item {
                SectionHeader(title = "Upcoming Scheduled (${upcomingRevisions.size})")
            }

            items(upcomingRevisions.take(5)) { rev ->
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
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = rev.intervalLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = rev.subjectName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = rev.chapterTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Due: ${rev.scheduledDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentElectricBlue
                        )
                    }
                }
            }
        }

        // Completed Logs History
        if (completedRevisions.isNotEmpty()) {
            item {
                SectionHeader(title = "Recently Mastered (${completedRevisions.size})")
            }

            items(completedRevisions.take(5)) { rev ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${rev.subjectName} • ${rev.chapterTitle}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Completed on ${rev.completedDate ?: "Recently"} (${rev.intervalLabel})",
                                style = MaterialTheme.typography.labelSmall,
                                color = ScoreGreen
                            )
                        }
                        Icon(Icons.Default.Check, contentDescription = null, tint = ScoreGreen, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

    if (showScheduleDialog) {
        ScheduleRevisionDialog(
            onDismiss = { showScheduleDialog = false },
            onSave = { subject, chapter, interval, days ->
                coroutineScope.launch {
                    viewModel.repository.scheduleNewRevision(
                        chapterId = 1L,
                        subjectName = subject,
                        chapterTitle = chapter,
                        intervalLabel = interval,
                        daysToAdd = days
                    )
                }
                showScheduleDialog = false
            }
        )
    }
}

@Composable
fun RevisionCard(
    log: RevisionLogEntity,
    onMarkCompleted: () -> Unit
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AccentNavy.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = log.intervalLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentElectricBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = log.subjectName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = log.chapterTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (log.notes.isNotBlank()) {
                    Text(
                        text = log.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onMarkCompleted,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScoreGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("mark_revision_button_${log.id}")
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mark Reviewed", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ScheduleRevisionDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int) -> Unit
) {
    var subject by remember { mutableStateOf("Physics") }
    var chapter by remember { mutableStateOf("") }
    var interval by remember { mutableStateOf("Same Day") }
    var days by remember { mutableIntStateOf(0) }

    val intervals = listOf(
        "Same Day" to 0,
        "+1 Day" to 1,
        "+3 Days" to 3,
        "+7 Days" to 7,
        "+15 Days" to 15,
        "+30 Days" to 30
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Spaced Revision") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter / Formula Topic") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Spaced Interval", style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    intervals.forEach { (label, d) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = interval == label,
                                onClick = {
                                    interval = label
                                    days = d
                                }
                            )
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (chapter.isNotBlank()) {
                        onSave(subject, chapter, interval, days)
                    }
                }
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
