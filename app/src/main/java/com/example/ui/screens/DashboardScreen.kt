package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.entities.ScorecardEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.ScoreBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val todayScorecard by viewModel.todayScorecard.collectAsState()
    val last7DaysScorecards by viewModel.last7DaysScorecards.collectAsState()
    val pendingTasks by viewModel.pendingTasks.collectAsState()
    val dueRevisions by viewModel.dueRevisions.collectAsState()
    val currentTimelineBlocks by viewModel.currentTimelineBlocks.collectAsState()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsState()
    val overdueTasks by viewModel.overdueTasks.collectAsState()

    var showQuickScorecardDialog by remember { mutableStateOf(false) }
    var quickBrainDumpText by remember { mutableStateOf("") }

    val avgScore = remember(last7DaysScorecards) {
        if (last7DaysScorecards.isNotEmpty()) {
            val total = last7DaysScorecards.sumOf { it.totalScore }
            String.format(Locale.getDefault(), "%.1f", total.toDouble() / last7DaysScorecards.size)
        } else "0.0"
    }

    val todayDateFormatted = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. Header greeting & Date
        item {
            Column {
                Text(
                    text = "Welcome back, Rudra",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = todayDateFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Notification Permission Banner
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            val permissionManager = remember { com.example.notification.PermissionManager(context) }
            var hasNotifPermission by remember { mutableStateOf(permissionManager.hasNotificationPermission()) }
            val promptDismissed by viewModel.permissionPromptShown.collectAsState()

            val permLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasNotifPermission = isGranted
                if (isGranted) {
                    viewModel.setNotificationsEnabled(true)
                }
            }

            if (!hasNotifPermission && !promptDismissed) {
                com.example.ui.components.NotificationPermissionBanner(
                    hasPermission = false,
                    onRequestPermission = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            permLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            permissionManager.openNotificationSettings()
                        }
                    },
                    onDismissBanner = {
                        viewModel.setPermissionPromptShown(true)
                    }
                )
            }
        }

        // 2. Emergency Falling Behind or Low Energy Banner (if active or overdue items exist)
        if (isLowEnergy) {
            item {
                GlassCard(
                    backgroundColor = ScoreYellowBg.copy(alpha = 0.25f),
                    borderColor = ScoreYellow.copy(alpha = 0.5f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = ScoreYellow)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Low Energy Mode Active",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ScoreYellow
                            )
                            Text(
                                text = "Target: 2-3/7. Planned battery saver mode. Zero guilt today.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        } else if (overdueTasks.isNotEmpty()) {
            item {
                GlassCard(
                    backgroundColor = ScoreRedBg.copy(alpha = 0.2f),
                    borderColor = ScoreRed.copy(alpha = 0.5f),
                    onClick = { viewModel.navigateTo(Screen.EmergencyRecovery) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = ScoreRed)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Falling Behind? ${overdueTasks.size} Overdue Task(s)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ScoreRed
                            )
                            Text(
                                text = "Tap for 3-Step Reset Protocol & Minimum Viable Day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ScoreRed)
                    }
                }
            }
        }

        // 3. Discipline Scorecard Overview Card
        item {
            val score = todayScorecard?.totalScore ?: 0
            GlassCard(
                onClick = { viewModel.navigateTo(Screen.Scorecard) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TODAY'S DISCIPLINE SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentElectricBlue,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$score",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "/ 7 Points",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        ScoreBadge(score = score)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "7-Day Avg: $avgScore/7",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scorecard roz bharoge — accha ho ya bura, honestly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { showQuickScorecardDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("quick_scorecard_log_button")
                    ) {
                        Text("Log Score", fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. Quick Action Grid
        item {
            SectionHeader(title = "Quick Actions")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Action 1: Let's Study PW
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(Screen.LetsStudy) }
                        .testTag("quick_action_lets_study"),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.PlayLesson, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                        Text("Let's Study", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("PW Thor Live", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Action 2: Study Timer
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(Screen.StudySession) }
                        .testTag("quick_action_study_timer"),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = AccentElectricBlue, modifier = Modifier.size(24.dp))
                        Text("Study Timer", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("Deep Focus", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Action 3: Evening Journal
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(Screen.Journal) }
                        .testTag("quick_action_journal"),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = ScoreGreen, modifier = Modifier.size(24.dp))
                        Text("Shutdown", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("3-Line Journal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 5. Today's Timeline Anchor
        item {
            SectionHeader(
                title = "Today's Study Blocks",
                actionText = "View All",
                onActionClick = { viewModel.navigateTo(Screen.Timeline) }
            )

            val studyBlocks = currentTimelineBlocks.filter { it.category == "Study" || it.category == "Shutdown" }.take(4)
            if (studyBlocks.isEmpty()) {
                GlassCard {
                    Text("No timeline active. Tap to choose a routine.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    studyBlocks.forEach { block ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Checkbox(
                                    checked = block.isCompleted,
                                    onCheckedChange = { checked ->
                                        coroutineScope.launch {
                                            viewModel.repository.updateBlockCompletion(block.id, checked)
                                        }
                                    },
                                    modifier = Modifier.testTag("block_check_${block.id}")
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = block.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "${block.startTime}–${block.endTime}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (block.triggerAction.isNotBlank()) {
                                        Text(
                                            text = "⚡ Trigger: ${block.triggerAction}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AccentElectricBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Spaced Repetition Due Today
        item {
            SectionHeader(
                title = "Due Revisions Today (${dueRevisions.size})",
                actionText = "Revision Hub",
                onActionClick = { viewModel.navigateTo(Screen.Revision) }
            )

            if (dueRevisions.isEmpty()) {
                GlassCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ScoreGreen)
                        Text(
                            text = "All revisions up to date for today!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dueRevisions.take(3).forEach { rev ->
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
                                            color = AccentNavy.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = rev.intervalLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AccentElectricBlue,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = rev.subjectName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = rev.chapterTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Button(
                                    onClick = { viewModel.markRevisionDone(rev.id, rev.chapterId, rev.intervalLabel) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ScoreGreen),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("mark_rev_done_${rev.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Revised", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Pending Tasks
        item {
            SectionHeader(
                title = "Pending Tasks (${pendingTasks.size})",
                actionText = "Task Manager",
                onActionClick = { viewModel.navigateTo(Screen.Tasks) }
            )

            if (pendingTasks.isEmpty()) {
                GlassCard {
                    Text(
                        text = "No pending tasks. You're on track!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingTasks.take(3).forEach { task ->
                        GlassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { checked ->
                                        coroutineScope.launch {
                                            viewModel.repository.updateTaskCompletion(task.id, checked)
                                        }
                                    },
                                    modifier = Modifier.testTag("task_dashboard_check_${task.id}")
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (task.subjectName.isNotBlank() || task.dueDate != null) {
                                        Text(
                                            text = "${task.subjectName} • Due: ${task.dueDate ?: "Today"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Instant Brain Dump / Parking Lot quick input
        item {
            SectionHeader(title = "Quick Capture / Parking Lot")
            GlassCard {
                Text(
                    text = "Jot down random distraction thoughts so your mind stays clear for study.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quickBrainDumpText,
                        onValueChange = { quickBrainDumpText = it },
                        placeholder = { Text("Write thought, doubt, or reminder...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_brain_dump_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Button(
                        onClick = {
                            if (quickBrainDumpText.isNotBlank()) {
                                viewModel.saveBrainDump(quickBrainDumpText, "Parking Lot (Study Distraction)")
                                quickBrainDumpText = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_quick_brain_dump")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Save Note", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // 9. Core Operating Principles Card (from PDF page 1)
        item {
            GlassCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Text(
                    text = "CORE PHILOSOPHY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentElectricBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "1. Consistency > Intensity: 40% effort daily beats 100% for 2 days then zero.\n" +
                           "2. Identity > Goals: Tum wo insaan ho jo roz apna kaam karta hai.\n" +
                           "3. System > Willpower: Agar Plan A fail ho, backup turant activate hota hai. Never Zero Day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }
    }

    // Quick Scorecard Check-in Modal Dialog
    if (showQuickScorecardDialog) {
        QuickScorecardDialog(
            todayScorecard = todayScorecard,
            onDismiss = { showQuickScorecardDialog = false },
            onSave = { w630, b1, b3, fit, b5, shut, noPhone, notes ->
                viewModel.saveScorecard(w630, b1, b3, fit, b5, shut, noPhone, notes)
                showQuickScorecardDialog = false
            }
        )
    }
}

@Composable
fun QuickScorecardDialog(
    todayScorecard: ScorecardEntity?,
    onDismiss: () -> Unit,
    onSave: (Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, String) -> Unit
) {
    var woke630 by remember { mutableStateOf(todayScorecard?.wokeUpBy630 ?: false) }
    var block1 by remember { mutableStateOf(todayScorecard?.completedBlock1 ?: false) }
    var block3 by remember { mutableStateOf(todayScorecard?.completedBlock3 ?: false) }
    var fitness by remember { mutableStateOf(todayScorecard?.completedFitness ?: false) }
    var block5 by remember { mutableStateOf(todayScorecard?.completedBlock5 ?: false) }
    var shutdown by remember { mutableStateOf(todayScorecard?.didShutdownRitual ?: false) }
    var noPhone by remember { mutableStateOf(todayScorecard?.noPhoneBlocked ?: false) }
    var notes by remember { mutableStateOf(todayScorecard?.notes ?: "") }

    val currentTotal = listOf(woke630, block1, block3, fitness, block5, shutdown, noPhone).count { it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daily Discipline Checklist", style = MaterialTheme.typography.titleLarge)
                ScoreBadge(score = currentTotal)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreItemRow(label = "Woke up by 6:30 AM (Hard Cap)", checked = woke630, onToggle = { woke630 = it })
                ScoreItemRow(label = "Completed Study Block 1 (Deep Focus)", checked = block1, onToggle = { block1 = it })
                ScoreItemRow(label = "Completed Study Block 3 (Main Theory)", checked = block3, onToggle = { block3 = it })
                ScoreItemRow(label = "Completed Fitness Block (Min 15m)", checked = fitness, onToggle = { fitness = it })
                ScoreItemRow(label = "Completed Study Block 5 (Revision)", checked = block5, onToggle = { block5 = it })
                ScoreItemRow(label = "Did Shutdown Ritual (9:15 PM)", checked = shutdown, onToggle = { shutdown = it })
                ScoreItemRow(label = "No Phone during blocked study hours", checked = noPhone, onToggle = { noPhone = it })

                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Brief reflection / note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(woke630, block1, block3, fitness, block5, shutdown, noPhone, notes) },
                modifier = Modifier.testTag("save_scorecard_dialog_button")
            ) {
                Text("Save Score ($currentTotal/7)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ScoreItemRow(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) ScoreGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { onToggle(!checked) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (checked) ScoreGreen else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ScoreGreen,
                checkedTrackColor = ScoreGreenBg
            )
        )
    }
}
