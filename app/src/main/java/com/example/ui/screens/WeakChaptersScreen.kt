package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

@Composable
fun WeakChaptersScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allChapters by viewModel.allChapters.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()

    val weakChaptersList = remember(allChapters) {
        allChapters
            .filter { it.isWeak || it.weaknessScore >= 30 }
            .sortedByDescending { it.weaknessScore }
    }

    var selectedChapterForAi by remember { mutableStateOf<ChapterEntity?>(null) }
    var showAiModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        // Weak Chapter Radar Overview Banner
        item {
            GlassCard(
                backgroundColor = ScoreRedBg.copy(alpha = 0.35f),
                borderColor = ScoreRed.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = ScoreRed)
                            Text(
                                text = "Weak Chapter Detector",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Identified ${weakChaptersList.size} chapters needing urgent intervention (low confidence, high difficulty, or overdue revisions).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ScoreRed.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, ScoreRed.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "${weakChaptersList.size} Weak",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ScoreRed,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Action Recommendation
        item {
            SectionHeader(
                title = "Priority Attack List (${weakChaptersList.size})",
                actionText = "AI Study Coach",
                onActionClick = { viewModel.navigateTo(Screen.AiCoach) }
            )
        }

        if (weakChaptersList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ScoreGreen, modifier = Modifier.size(40.dp))
                        Text(
                            text = "No Critical Weak Areas Detected!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your confidence and revision indicators across Physics, Chemistry, and Biology are on target. Keep up the consistency!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(weakChaptersList, key = { it.id }) { chapter ->
                val subject = allSubjects.find { it.id == chapter.subjectId }
                val subjectName = subject?.name ?: "PCB Subject"

                WeakChapterCard(
                    chapter = chapter,
                    subjectName = subjectName,
                    onFixToday = {
                        viewModel.addChapterToTodayTasks(chapter, subjectName)
                        viewModel.navigateTo(Screen.Tasks)
                    },
                    onReviseNow = {
                        viewModel.startTimer(subject = subjectName, chapter = chapter.title, targetMinutes = 45)
                        viewModel.navigateTo(Screen.StudySession)
                    },
                    onTriggerAi = {
                        selectedChapterForAi = chapter
                        showAiModal = true
                    }
                )
            }
        }
    }

    // AI Study Assistant Dialog
    if (showAiModal && selectedChapterForAi != null) {
        val ch = selectedChapterForAi!!
        val subject = allSubjects.find { it.id == ch.subjectId }?.name ?: "PCB"
        val aiResult by viewModel.aiResult.collectAsState()
        val isAiLoading by viewModel.isAiLoading.collectAsState()

        AlertDialog(
            onDismissRequest = {
                showAiModal = false
                viewModel.clearAiResult()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentElectricBlue)
                    Text("AI Emergency Boost: ${ch.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.requestAiSummary(subject, ch.title) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Summary", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.requestAiQuiz(subject, ch.title) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("PYQ Quiz", fontSize = 11.sp)
                        }
                    }

                    if (isAiLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AccentElectricBlue)
                        }
                    } else if (aiResult != null) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .padding(6.dp)
                        ) {
                            item {
                                Text(
                                    text = aiResult ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Generate a 1-minute breakdown or practice 5 PYQs to eliminate weaknesses in ${ch.title}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAiModal = false
                        viewModel.clearAiResult()
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun WeakChapterCard(
    chapter: ChapterEntity,
    subjectName: String,
    onFixToday: () -> Unit,
    onReviseNow: () -> Unit,
    onTriggerAi: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ScoreRed.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$subjectName • Ch #${chapter.chapterNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentElectricBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ScoreRed.copy(alpha = 0.18f),
                    border = BorderStroke(0.5.dp, ScoreRed.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Weakness Score: ${chapter.weaknessScore}/100",
                        style = MaterialTheme.typography.labelSmall,
                        color = ScoreRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Reason Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (chapter.confidenceRating <= 2) {
                    WeaknessReasonTag("Low Confidence (${chapter.confidenceRating}/5)")
                }
                if (chapter.difficultyRating >= 4) {
                    WeaknessReasonTag("High Difficulty (${chapter.difficultyRating}/5)")
                }
                if (!chapter.revision1Done) {
                    WeaknessReasonTag("Pending R1")
                }
                if (chapter.pyqStatus == ChapterEntity.PYQ_PENDING) {
                    WeaknessReasonTag("PYQs Pending")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onFixToday,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ScoreRedBg, contentColor = Color.White),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Fix Today", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onReviseNow,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Revise Now", fontSize = 11.sp)
                }

                Button(
                    onClick = onTriggerAi,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Booster", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun WeaknessReasonTag(label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = "• $label",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
