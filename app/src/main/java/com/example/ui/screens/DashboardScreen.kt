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
import androidx.compose.material.icons.outlined.*
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
import com.example.data.local.entities.ChapterEntity
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
import java.util.concurrent.TimeUnit

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
    val allChapters by viewModel.allChapters.collectAsState()
    val weakChapters by viewModel.weakChapters.collectAsState()
    val allStreaks by viewModel.allStreaks.collectAsState()

    val targetBoard by viewModel.targetBoard.collectAsState()
    val targetScore by viewModel.targetScore.collectAsState()
    val boardExamDate by viewModel.boardExamDate.collectAsState()

    var showQuickScorecardDialog by remember { mutableStateOf(false) }
    var quickBrainDumpText by remember { mutableStateOf("") }

    val daysRemaining = remember(boardExamDate) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val exam = sdf.parse(boardExamDate)?.time ?: 0L
            val today = System.currentTimeMillis()
            val diff = exam - today
            (TimeUnit.MILLISECONDS.toDays(diff)).coerceAtLeast(0)
        } catch (e: Exception) {
            365L
        }
    }

    val avgScore = remember(last7DaysScorecards) {
        if (last7DaysScorecards.isNotEmpty()) {
            val total = last7DaysScorecards.sumOf { it.totalScore }
            String.format(Locale.getDefault(), "%.1f", total.toDouble() / last7DaysScorecards.size)
        } else "0.0"
    }

    val todayDateFormatted = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    val completedChapters = remember(allChapters) {
        allChapters.count { it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED }
    }

    val phyCount = remember(allChapters) { allChapters.count { it.subjectId == 1L && (it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED) } }
    val chemCount = remember(allChapters) { allChapters.count { it.subjectId == 2L && (it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED) } }
    val bioCount = remember(allChapters) { allChapters.count { it.subjectId == 3L && (it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED) } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        // 1. Mission & Exam Countdown Header Banner
        item {
            GlassCard(
                backgroundColor = AccentNavy.copy(alpha = 0.35f),
                borderColor = AccentElectricBlue.copy(alpha = 0.5f),
                onClick = { viewModel.navigateTo(Screen.MissionBoard) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentElectricBlue.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = targetBoard,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentElectricBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ScoreGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Target: $targetScore",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ScoreGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rudra's PCB Battle Station",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = todayDateFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentElectricBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "$daysRemaining",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentElectricBlue
                        )
                        Text(
                            text = "DAYS LEFT",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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

        // 2. PCB Syllabus Progress Summary Card
        item {
            GlassCard(
                onClick = { viewModel.navigateTo(Screen.Subjects) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PCB SYLLABUS READINESS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentElectricBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$completedChapters / 46 Chapters Mastered",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${((completedChapters / 46.0) * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ScoreGreen
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (completedChapters / 46f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AccentElectricBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SubjectProgressPill("Physics", "$phyCount/15", AccentElectricBlue)
                    SubjectProgressPill("Chemistry", "$chemCount/15", AccentCyan)
                    SubjectProgressPill("Biology", "$bioCount/16", ScoreGreen)
                }
            }
        }

        // 3. Weak Chapters Alert Banner (if any)
        if (weakChapters.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ScoreRedBg.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, ScoreRed.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(Screen.WeakChapters) }
                        .testTag("weak_chapters_dashboard_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = ScoreRed, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Weak Area Radar: ${weakChapters.size} Chapters Need Fixing",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ScoreRed
                            )
                            Text(
                                text = "Top: ${weakChapters.first().title} • Tap for 1-click PYQ action plan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ScoreRed)
                    }
                }
            }
        }

        // 4. Quick Action Grid
        item {
            SectionHeader(title = "Fast Launch Actions")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.PlayLesson, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(22.dp))
                        Text("Let's Study", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("PW Thor Live", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = AccentElectricBlue, modifier = Modifier.size(22.dp))
                        Text("Deep Timer", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("Stopwatch", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Action 3: AI Study Coach
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(Screen.AiCoach) }
                        .testTag("quick_action_ai_coach"),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ScoreYellow, modifier = Modifier.size(22.dp))
                        Text("AI Coach", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("Doubt & Quiz", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Action 4: Evening Journal
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
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = ScoreGreen, modifier = Modifier.size(22.dp))
                        Text("Shutdown", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("3-Line Log", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 5. 5-Pillar Discipline Streaks Quick Overview
        item {
            SectionHeader(
                title = "5-Pillar Discipline Streaks",
                actionText = "Full Tracker",
                onActionClick = { viewModel.navigateTo(Screen.Streaks) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allStreaks.forEach { streak ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.toggleStreakCheckIn(streak.streakKey) }
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔥 ${streak.currentStreak}d",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (streak.currentStreak > 0) ScoreGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = streak.title.split(" ").firstOrNull() ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 6. Discipline Scorecard Overview Card
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

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scorecard roz bharoge — honestly.",
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

        // 7. Spaced Repetition Due Today
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
                            text = "All PCB revisions up to date for today!",
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
                                    onClick = { viewModel.markRevisionCompleted(rev, 5, "Completed from dashboard") },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ScoreGreen),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("mark_rev_done_${rev.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Done", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Pending Tasks
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
                                    onCheckedChange = { viewModel.toggleTaskDone(task) },
                                    modifier = Modifier.testTag("task_dashboard_check_${task.id}")
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (task.dueDate != null) {
                                        Text(
                                            text = "Due: ${task.dueDate}",
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

        // 9. Instant Brain Dump quick input
        item {
            SectionHeader(title = "Parking Lot / Distraction Capture")
            GlassCard {
                Text(
                    text = "Jot down random distraction thoughts so your mind stays clear for PCB study.",
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
                                viewModel.insertBrainDump(quickBrainDumpText, "Parking Lot")
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
    }

    // Quick Scorecard Check-in Modal Dialog
    if (showQuickScorecardDialog) {
        QuickScorecardDialog(
            todayScorecard = todayScorecard,
            onDismiss = { showQuickScorecardDialog = false },
            onSave = { b1, b3, b5, shut, noPorn, running, rev, notes ->
                viewModel.updateTodayScorecard(b1, b3, b5, shut, noPorn, running, rev, notes)
                showQuickScorecardDialog = false
            }
        )
    }
}

@Composable
fun SubjectProgressPill(subject: String, count: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
            Text(subject, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(count, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        }
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
                ScoreItemRow(label = "1. Woke up by 6:30 AM (Hard Cap)", checked = woke630, onToggle = { woke630 = it })
                ScoreItemRow(label = "2. Completed Block 1 (Deep Morning Study)", checked = block1, onToggle = { block1 = it })
                ScoreItemRow(label = "3. Completed Block 3 (Afternoon Deep Work)", checked = block3, onToggle = { block3 = it })
                ScoreItemRow(label = "4. Completed Fitness (Min 15 min workout)", checked = fitness, onToggle = { fitness = it })
                ScoreItemRow(label = "5. Completed Block 5 (Evening PCB Revision)", checked = block5, onToggle = { block5 = it })
                ScoreItemRow(label = "6. Completed Shutdown Ritual (9:15 PM)", checked = shutdown, onToggle = { shutdown = it })
                ScoreItemRow(label = "7. No Phone / Clean Focus during study", checked = noPhone, onToggle = { noPhone = it })

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
