package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.SubjectEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import kotlinx.coroutines.launch

@Composable
fun SubjectsScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val allChapters by viewModel.allChapters.collectAsState()
    val aiResult by viewModel.aiResult.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
    val currentSubjectId = selectedSubjectId ?: allSubjects.firstOrNull()?.id ?: 1L
    val currentSubject = allSubjects.find { it.id == currentSubjectId }
    val subjectChapters = allChapters.filter { it.subjectId == currentSubjectId }

    var selectedChapterForAi by remember { mutableStateOf<ChapterEntity?>(null) }
    var showAiModal by remember { mutableStateOf(false) }
    var showAddChapterDialog by remember { mutableStateOf(false) }

    // Progress calculations
    val totalChapters = subjectChapters.size
    val completedCount = subjectChapters.count { it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED }
    val progressFraction = if (totalChapters > 0) completedCount.toFloat() / totalChapters else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Subject Selector Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allSubjects) { subject ->
                    val isSelected = subject.id == currentSubjectId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubjectId = subject.id },
                        label = {
                            Text(
                                text = subject.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentNavy.copy(alpha = 0.3f),
                            selectedLabelColor = AccentElectricBlue
                        ),
                        modifier = Modifier.testTag("subject_tab_${subject.name.lowercase()}")
                    )
                }
            }
        }

        // Subject Header Banner & Progress
        if (currentSubject != null) {
            item {
                GlassCard(
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentSubject.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentSubject.code + " • " + currentSubject.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AccentNavy.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "$completedCount/$totalChapters Completed",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentElectricBlue,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = AccentElectricBlue,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }
        }

        // Chapter List
        item {
            SectionHeader(
                title = "Syllabus Chapters (${subjectChapters.size})",
                actionText = "+ Add Chapter",
                onActionClick = { showAddChapterDialog = true }
            )
        }

        items(subjectChapters) { chapter ->
            ChapterItemCard(
                chapter = chapter,
                subjectName = currentSubject?.name ?: "Subject",
                onStatusChange = { newStatus ->
                    val newProgress = when (newStatus) {
                        ChapterEntity.STATUS_COMPLETED -> 100
                        ChapterEntity.STATUS_REVISED -> 100
                        ChapterEntity.STATUS_IN_PROGRESS -> 50
                        else -> 0
                    }
                    coroutineScope.launch {
                        viewModel.repository.updateChapterStatus(chapter.id, newStatus, newProgress)
                    }
                },
                onTriggerAi = {
                    selectedChapterForAi = chapter
                    showAiModal = true
                },
                onQuickScheduleRevision = {
                    coroutineScope.launch {
                        viewModel.repository.scheduleNewRevision(
                            chapterId = chapter.id,
                            subjectName = currentSubject?.name ?: "Subject",
                            chapterTitle = chapter.title,
                            intervalLabel = "Same Day",
                            daysToAdd = 0,
                            notes = "Spaced repetition initiated from Subject Hub"
                        )
                    }
                }
            )
        }
    }

    // AI Study Assistant Dialog
    if (showAiModal && selectedChapterForAi != null) {
        val ch = selectedChapterForAi!!
        AlertDialog(
            onDismissRequest = {
                showAiModal = false
                viewModel.clearAiResult()
            },
            title = {
                Text(
                    text = "AI Study Hub: ${ch.title}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.requestAiSummary(currentSubject?.name ?: "Physics", ch.title) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Summary", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.requestAiQuiz(currentSubject?.name ?: "Physics", ch.title) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Quiz", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.requestAiFlashcards(currentSubject?.name ?: "Physics", ch.title) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Cards", fontSize = 11.sp)
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
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
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
                            text = "Tap Summary, Quiz, or Cards above to generate high-yield Class 12 board study materials on demand.",
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

    if (showAddChapterDialog) {
        AddChapterDialog(
            subjectId = currentSubjectId,
            onDismiss = { showAddChapterDialog = false },
            onSave = { newChapter ->
                coroutineScope.launch {
                    viewModel.repository.insertChapter(newChapter)
                }
                showAddChapterDialog = false
            }
        )
    }
}

@Composable
fun ChapterItemCard(
    chapter: ChapterEntity,
    subjectName: String,
    onStatusChange: (String) -> Unit,
    onTriggerAi: () -> Unit,
    onQuickScheduleRevision: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (chapter.status) {
        ChapterEntity.STATUS_COMPLETED -> ScoreGreen
        ChapterEntity.STATUS_REVISED -> AccentElectricBlue
        ChapterEntity.STATUS_IN_PROGRESS -> ScoreYellow
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${chapter.chapterNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = chapter.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (chapter.priority == "Weak Area") {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = ScoreRedBg.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Weak Area",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ScoreRed,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            if (chapter.revisionCount > 0) {
                                Text(
                                    text = "• ${chapter.revisionCount} revisions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Options"
                    )
                }
            }

            if (chapter.notes.isNotBlank()) {
                Text(
                    text = chapter.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val statuses = listOf(
                        ChapterEntity.STATUS_NOT_STARTED,
                        ChapterEntity.STATUS_IN_PROGRESS,
                        ChapterEntity.STATUS_COMPLETED,
                        ChapterEntity.STATUS_REVISED
                    )

                    statuses.forEach { s ->
                        FilterChip(
                            selected = chapter.status == s,
                            onClick = { onStatusChange(s) },
                            label = { Text(s, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onQuickScheduleRevision,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Spaced Repeat", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onTriggerAi,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Study Hub", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddChapterDialog(
    subjectId: Long,
    onDismiss: () -> Unit,
    onSave: (ChapterEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var chapterNumber by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Normal") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Chapter") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = chapterNumber,
                    onValueChange = { chapterNumber = it },
                    label = { Text("Chapter #") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Chapter Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Key formulas / focus topics") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            ChapterEntity(
                                subjectId = subjectId,
                                chapterNumber = chapterNumber.toIntOrNull() ?: 1,
                                title = title,
                                notes = notes,
                                priority = priority
                            )
                        )
                    }
                }
            ) {
                Text("Add Chapter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
