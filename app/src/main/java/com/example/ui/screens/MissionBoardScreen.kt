package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun MissionBoardScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val targetBoard by viewModel.targetBoard.collectAsState()
    val targetScore by viewModel.targetScore.collectAsState()
    val boardExamDate by viewModel.boardExamDate.collectAsState()
    val physicsExamDate by viewModel.physicsExamDate.collectAsState()
    val chemistryExamDate by viewModel.chemistryExamDate.collectAsState()
    val biologyExamDate by viewModel.biologyExamDate.collectAsState()
    val weeklyChapterTarget by viewModel.weeklyChapterTarget.collectAsState()
    val weeklyLectureTarget by viewModel.weeklyLectureTarget.collectAsState()
    val weeklyMockTarget by viewModel.weeklyMockTarget.collectAsState()

    val allChapters by viewModel.allChapters.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val allMockTests by viewModel.allMockTests.collectAsState()

    var showEditGoalsDialog by remember { mutableStateOf(false) }

    // Progress calculations
    val totalChapters = allChapters.size.coerceAtLeast(1)
    val completedChapters = allChapters.count { it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED }
    val overallProgressFraction = completedChapters.toFloat() / totalChapters

    val physicsChapters = allChapters.filter { it.subjectId == 1L }
    val chemistryChapters = allChapters.filter { it.subjectId == 2L }
    val biologyChapters = allChapters.filter { it.subjectId == 3L }

    val phyDone = physicsChapters.count { it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED }
    val chemDone = chemistryChapters.count { it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED }
    val bioDone = biologyChapters.count { it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED }

    val boardCountdown = calculateDaysRemaining(boardExamDate)
    val phyCountdown = calculateDaysRemaining(physicsExamDate)
    val chemCountdown = calculateDaysRemaining(chemistryExamDate)
    val bioCountdown = calculateDaysRemaining(biologyExamDate)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        // Mission Hero Card
        item {
            GlassCard(
                backgroundColor = AccentNavy.copy(alpha = 0.4f),
                borderColor = AccentElectricBlue.copy(alpha = 0.5f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Mission 2027",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = targetBoard,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ScoreGreen.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, ScoreGreen.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "Target: $targetScore",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ScoreGreen,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Progress Arc / Bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Syllabus Completed: $completedChapters/$totalChapters Chapters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(overallProgressFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentElectricBlue
                            )
                        }

                        LinearProgressIndicator(
                            progress = { overallProgressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = AccentElectricBlue,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showEditGoalsDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Configure Targets & Dates", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Exam Countdown Grid
        item {
            SectionHeader(title = "Live Exam Countdowns")
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CountdownCard("Main Board", "$boardCountdown Days", "Feb 2027", ScoreRed, Modifier.weight(1f))
                CountdownCard("Physics", "$phyCountdown Days", physicsExamDate, AccentElectricBlue, Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CountdownCard("Chemistry", "$chemCountdown Days", chemistryExamDate, ScoreGreen, Modifier.weight(1f))
                CountdownCard("Biology", "$bioCountdown Days", biologyExamDate, Color(0xFFAB47BC), Modifier.weight(1f))
            }
        }

        // Subject Breakdown Progress
        item {
            SectionHeader(title = "PCB Subject Readiness")
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SubjectProgressRow("Physics (15 Chapters)", phyDone, physicsChapters.size, AccentElectricBlue)
                    SubjectProgressRow("Chemistry (15 Chapters)", chemDone, chemistryChapters.size, ScoreGreen)
                    SubjectProgressRow("Biology (16 Chapters)", bioDone, biologyChapters.size, Color(0xFFAB47BC))
                }
            }
        }

        // Weekly Target Benchmarks
        item {
            SectionHeader(title = "Weekly Execution Targets")
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WeeklyTargetItem("Complete Full Chapters", "$weeklyChapterTarget Chapters/week", Icons.Default.MenuBook, AccentCyan)
                    WeeklyTargetItem("PW Video Lectures", "$weeklyLectureTarget Lectures/week", Icons.Default.PlayCircle, ScoreGreen)
                    WeeklyTargetItem("Mock Tests & PYQs", "$weeklyMockTarget Mocks/week", Icons.Default.Quiz, ScoreYellow)
                }
            }
        }
    }

    if (showEditGoalsDialog) {
        EditGoalsDialog(
            currentBoard = targetBoard,
            currentScore = targetScore,
            boardDate = boardExamDate,
            phyDate = physicsExamDate,
            chemDate = chemistryExamDate,
            bioDate = biologyExamDate,
            onDismiss = { showEditGoalsDialog = false },
            onSave = { b, s, bd, pd, cd, bid ->
                viewModel.saveMissionGoals(b, s, bd, pd, cd, bid)
                showEditGoalsDialog = false
            }
        )
    }
}

@Composable
fun CountdownCard(title: String, days: String, date: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text(days, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(date, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SubjectProgressRow(name: String, done: Int, total: Int, color: Color) {
    val t = total.coerceAtLeast(1)
    val fraction = done.toFloat() / t

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("$done/$total (${(fraction * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun WeeklyTargetItem(title: String, target: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
        Text(target, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

fun calculateDaysRemaining(targetDateStr: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val targetDate = sdf.parse(targetDateStr) ?: return 0L
        val today = Date()
        val diff = targetDate.time - today.time
        (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0L)
    } catch (e: Exception) {
        0L
    }
}

@Composable
fun EditGoalsDialog(
    currentBoard: String,
    currentScore: String,
    boardDate: String,
    phyDate: String,
    chemDate: String,
    bioDate: String,
    onDismiss: () -> Unit,
    onSave: (board: String, score: String, bd: String, pd: String, cd: String, bid: String) -> Unit
) {
    var board by remember { mutableStateOf(currentBoard) }
    var score by remember { mutableStateOf(currentScore) }
    var bd by remember { mutableStateOf(boardDate) }
    var pd by remember { mutableStateOf(phyDate) }
    var cd by remember { mutableStateOf(chemDate) }
    var bid by remember { mutableStateOf(bioDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Mission Goals & Dates") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = board,
                        onValueChange = { board = it },
                        label = { Text("Target Board") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = score,
                        onValueChange = { score = it },
                        label = { Text("Target Score % (e.g. 85%+)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = bd,
                        onValueChange = { bd = it },
                        label = { Text("Board Exam Start Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = pd,
                        onValueChange = { pd = it },
                        label = { Text("Physics Exam Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = cd,
                        onValueChange = { cd = it },
                        label = { Text("Chemistry Exam Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = bid,
                        onValueChange = { bid = it },
                        label = { Text("Biology Exam Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(board, score, bd, pd, cd, bid)
                }
            ) {
                Text("Save Targets")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
