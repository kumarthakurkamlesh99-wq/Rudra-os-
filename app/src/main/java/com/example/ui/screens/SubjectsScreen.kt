package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.entities.SubjectEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import com.example.ui.viewmodel.Screen
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
    var showEditChapterModal by remember { mutableStateOf<ChapterEntity?>(null) }
    var filterStatus by remember { mutableStateOf("All") }

    // Progress calculations
    val totalChapters = subjectChapters.size
    val completedCount = subjectChapters.count { it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED }
    val progressFraction = if (totalChapters > 0) completedCount.toFloat() / totalChapters else 0f

    val filteredChapters = when (filterStatus) {
        "Completed" -> subjectChapters.filter { it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED }
        "Learning" -> subjectChapters.filter { it.status == ChapterEntity.STATUS_LEARNING }
        "Not Started" -> subjectChapters.filter { it.status == ChapterEntity.STATUS_NOT_STARTED }
        "Weak" -> subjectChapters.filter { it.isWeak }
        else -> subjectChapters
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        // PCB Subject Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allSubjects) { subject ->
                    val isSelected = subject.id == currentSubjectId
                    val count = allChapters.count { it.subjectId == subject.id }
                    val done = allChapters.count { it.subjectId == subject.id && (it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED) }
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubjectId = subject.id },
                        label = {
                            Text(
                                text = "${subject.name} ($done/$count)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (subject.name) {
                                            "Physics" -> AccentElectricBlue
                                            "Chemistry" -> ScoreGreen
                                            "Biology" -> Color(0xFFAB47BC)
                                            else -> AccentCyan
                                        }
                                    )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentNavy.copy(alpha = 0.35f),
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
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${currentSubject.name} Syllabus",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${currentSubject.description} • Class 12 Board Prep",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AccentNavy.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "$completedCount/$totalChapters Completed (${(progressFraction * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentElectricBlue,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when (currentSubject.name) {
                                "Physics" -> AccentElectricBlue
                                "Chemistry" -> ScoreGreen
                                "Biology" -> Color(0xFFAB47BC)
                                else -> AccentCyan
                            },
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        // Quick Filter Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("All", "Learning", "Completed", "Weak").forEach { status ->
                                FilterChip(
                                    selected = filterStatus == status,
                                    onClick = { filterStatus = status },
                                    label = { Text(status, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Chapters List Header
        item {
            SectionHeader(
                title = "${currentSubject?.name ?: "Subject"} Chapters (${filteredChapters.size})",
                actionText = "Weak Area Radar",
                onActionClick = { viewModel.navigateTo(Screen.WeakChapters) }
            )
        }

        // Chapters List
        items(filteredChapters, key = { it.id }) { chapter ->
            DetailedChapterCard(
                chapter = chapter,
                subjectName = currentSubject?.name ?: "Subject",
                onIncrementLecture = { viewModel.incrementWatchedLecture(chapter.id) },
                onEditDetails = { showEditChapterModal = chapter },
                onTriggerAi = {
                    selectedChapterForAi = chapter
                    showAiModal = true
                },
                onQuickFixToday = {
                    viewModel.addChapterToTodayTasks(chapter, currentSubject?.name ?: "PCB")
                },
                onQuickScheduleRevision = {
                    coroutineScope.launch {
                        viewModel.repository.scheduleNewRevision(
                            chapterId = chapter.id,
                            subjectName = currentSubject?.name ?: "Subject",
                            chapterTitle = chapter.title,
                            intervalLabel = "Revision 1 (Same Day)",
                            daysToAdd = 0,
                            notes = "PCB Spaced Revision cycle"
                        )
                    }
                }
            )
        }
    }

    // Edit Chapter Progress Modal
    if (showEditChapterModal != null) {
        val ch = showEditChapterModal!!
        EditChapterProgressDialog(
            chapter = ch,
            onDismiss = { showEditChapterModal = null },
            onSave = { updated ->
                viewModel.updateChapter(updated)
                showEditChapterModal = null
            }
        )
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentElectricBlue)
                    Text(
                        text = "AI Study Hub: ${ch.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                            Text("PYQ Quiz", fontSize = 11.sp)
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
                            text = "Tap Summary, PYQ Quiz, or Cards above to generate high-yield Class 12 board study materials on demand with Gemini 2.5 Flash.",
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
fun DetailedChapterCard(
    chapter: ChapterEntity,
    subjectName: String,
    onIncrementLecture: () -> Unit,
    onEditDetails: () -> Unit,
    onTriggerAi: () -> Unit,
    onQuickFixToday: () -> Unit,
    onQuickScheduleRevision: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val (heatColor, heatLabel) = when (chapter.heatmapColorType) {
        "GREEN" -> Pair(ScoreGreen, "Strong")
        "YELLOW" -> Pair(ScoreYellow, "Moderate")
        else -> Pair(ScoreRed, "Weak")
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row with Chapter Number, Title, and Heatmap Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${chapter.chapterNumber}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
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
                            // Status Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = heatColor.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, heatColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "${chapter.status} • $heatLabel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = heatColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Confidence Rating Stars
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = if (star <= chapter.confidenceRating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                        contentDescription = null,
                                        tint = if (star <= chapter.confidenceRating) ScoreYellow else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Chapter Details"
                    )
                }
            }

            // Quick Progress & Checklist Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lecture progress
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentCyan)
                    Text(
                        text = "Lec: ${chapter.watchedLectures}/${chapter.totalLectures}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )

                    if (chapter.watchedLectures < chapter.totalLectures) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AccentNavy.copy(alpha = 0.2f),
                            modifier = Modifier.clickable { onIncrementLecture() }
                        ) {
                            Text(
                                text = "+1 Done",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentElectricBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Checkpoint pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniCheckPill(label = "NCERT", isDone = chapter.ncertRead)
                    MiniCheckPill(label = "Notes", isDone = chapter.notesStatus == ChapterEntity.NOTES_COMPLETED)
                    MiniCheckPill(label = "PYQ", isDone = chapter.pyqStatus == ChapterEntity.PYQ_COMPLETED)
                    MiniCheckPill(label = "R1", isDone = chapter.revision1Done)
                }
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { (chapter.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = heatColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Expanded Full Controls
            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Detailed Tracking Matrix
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("NCERT Read: ${if (chapter.ncertRead) "Yes" else "Pending"}", style = MaterialTheme.typography.bodySmall)
                            Text("NCERT Revised: ${if (chapter.ncertRevised) "Yes" else "Pending"}", style = MaterialTheme.typography.bodySmall)
                            Text("Difficulty: ${chapter.difficultyRating}/5", style = MaterialTheme.typography.bodySmall)
                        }
                        Column {
                            Text("Notes: ${chapter.notesStatus}", style = MaterialTheme.typography.bodySmall)
                            Text("Mock Test: ${chapter.mockTestStatus}", style = MaterialTheme.typography.bodySmall)
                            Text("Study Hours: ${chapter.totalStudyHours}h", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Revision Milestone Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RevisionMilestoneBadge("R1 (Day 1)", chapter.revision1Done, Modifier.weight(1f))
                        RevisionMilestoneBadge("R2 (Day 7)", chapter.revision2Done, Modifier.weight(1f))
                        RevisionMilestoneBadge("R3 (Day 30)", chapter.revision3Done, Modifier.weight(1f))
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEditDetails,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Status", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onQuickFixToday,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ScoreRedBg, contentColor = Color.White),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fix Today", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onTriggerAi,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Hub", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniCheckPill(label: String, isDone: Boolean) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isDone) ScoreGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(0.5.dp, if (isDone) ScoreGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = if (isDone) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isDone) ScoreGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                color = if (isDone) ScoreGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RevisionMilestoneBadge(label: String, isDone: Boolean, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isDone) ScoreGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isDone) ScoreGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint = if (isDone) ScoreGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                color = if (isDone) ScoreGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EditChapterProgressDialog(
    chapter: ChapterEntity,
    onDismiss: () -> Unit,
    onSave: (ChapterEntity) -> Unit
) {
    var status by remember { mutableStateOf(chapter.status) }
    var progressPercent by remember { mutableStateOf(chapter.progressPercent.toString()) }
    var totalLectures by remember { mutableStateOf(chapter.totalLectures.toString()) }
    var watchedLectures by remember { mutableStateOf(chapter.watchedLectures.toString()) }
    var ncertRead by remember { mutableStateOf(chapter.ncertRead) }
    var ncertRevised by remember { mutableStateOf(chapter.ncertRevised) }
    var notesStatus by remember { mutableStateOf(chapter.notesStatus) }
    var pyqStatus by remember { mutableStateOf(chapter.pyqStatus) }
    var mockTestStatus by remember { mutableStateOf(chapter.mockTestStatus) }
    var r1Done by remember { mutableStateOf(chapter.revision1Done) }
    var r2Done by remember { mutableStateOf(chapter.revision2Done) }
    var r3Done by remember { mutableStateOf(chapter.revision3Done) }
    var confidence by remember { mutableStateOf(chapter.confidenceRating) }
    var difficulty by remember { mutableStateOf(chapter.difficultyRating) }
    var studyHours by remember { mutableStateOf(chapter.totalStudyHours.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit: ${chapter.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Status", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(ChapterEntity.STATUS_NOT_STARTED, ChapterEntity.STATUS_LEARNING, ChapterEntity.STATUS_COMPLETED).forEach { s ->
                            FilterChip(
                                selected = status == s,
                                onClick = {
                                    status = s
                                    if (s == ChapterEntity.STATUS_COMPLETED) progressPercent = "100"
                                    if (s == ChapterEntity.STATUS_NOT_STARTED) progressPercent = "0"
                                },
                                label = { Text(s, fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = watchedLectures,
                            onValueChange = { watchedLectures = it },
                            label = { Text("Watched Lec") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = totalLectures,
                            onValueChange = { totalLectures = it },
                            label = { Text("Total Lec") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = progressPercent,
                            onValueChange = { progressPercent = it },
                            label = { Text("Progress %") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = studyHours,
                            onValueChange = { studyHours = it },
                            label = { Text("Study Hours") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Text("Checkpoints", style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = ncertRead, onCheckedChange = { ncertRead = it })
                        Text("NCERT Read", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(12.dp))
                        Checkbox(checked = ncertRevised, onCheckedChange = { ncertRevised = it })
                        Text("NCERT Revised", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notes Status", style = MaterialTheme.typography.labelSmall)
                            listOf(ChapterEntity.NOTES_NOT_STARTED, ChapterEntity.NOTES_IN_PROGRESS, ChapterEntity.NOTES_COMPLETED).forEach { ns ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = notesStatus == ns, onClick = { notesStatus = ns })
                                    Text(ns, fontSize = 11.sp)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("PYQ Status", style = MaterialTheme.typography.labelSmall)
                            listOf(ChapterEntity.PYQ_PENDING, ChapterEntity.PYQ_COMPLETED).forEach { ps ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = pyqStatus == ps, onClick = { pyqStatus = ps })
                                    Text(ps, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Spaced Revisions", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = r1Done,
                            onClick = { r1Done = !r1Done },
                            label = { Text("R1 Done", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = r2Done,
                            onClick = { r2Done = !r2Done },
                            label = { Text("R2 Done", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = r3Done,
                            onClick = { r3Done = !r3Done },
                            label = { Text("R3 Done", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Text("Confidence Rating (1 to 5)", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { confidence = star }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = if (star <= confidence) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = if (star <= confidence) ScoreYellow else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Difficulty Rating (1 to 5)", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { diff ->
                            IconButton(onClick = { difficulty = diff }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = if (diff <= difficulty) Icons.Default.Warning else Icons.Outlined.WarningAmber,
                                    contentDescription = null,
                                    tint = if (diff <= difficulty) ScoreRed else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val watched = watchedLectures.toIntOrNull() ?: chapter.watchedLectures
                    val total = totalLectures.toIntOrNull() ?: chapter.totalLectures
                    val prog = progressPercent.toIntOrNull() ?: chapter.progressPercent
                    val hrs = studyHours.toDoubleOrNull() ?: chapter.totalStudyHours

                    onSave(
                        chapter.copy(
                            status = status,
                            progressPercent = prog.coerceIn(0, 100),
                            watchedLectures = watched,
                            totalLectures = total,
                            ncertRead = ncertRead,
                            ncertRevised = ncertRevised,
                            notesStatus = notesStatus,
                            pyqStatus = pyqStatus,
                            mockTestStatus = mockTestStatus,
                            revision1Done = r1Done,
                            revision2Done = r2Done,
                            revision3Done = r3Done,
                            confidenceRating = confidence,
                            difficultyRating = difficulty,
                            totalStudyHours = hrs
                        )
                    )
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
