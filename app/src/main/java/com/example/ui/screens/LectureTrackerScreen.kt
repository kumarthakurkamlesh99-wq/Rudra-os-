package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.ui.viewmodel.Screen

@Composable
fun LectureTrackerScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val allChapters by viewModel.allChapters.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }

    val currentSubjectId = selectedSubjectId ?: allSubjects.firstOrNull()?.id ?: 1L
    val currentSubject = allSubjects.find { it.id == currentSubjectId }
    val subjectChapters = allChapters.filter { it.subjectId == currentSubjectId }

    val totalLectures = subjectChapters.sumOf { it.totalLectures }
    val totalWatched = subjectChapters.sumOf { it.watchedLectures }
    val totalRemaining = (totalLectures - totalWatched).coerceAtLeast(0)
    val overallLecturePct = if (totalLectures > 0) (totalWatched.toFloat() / totalLectures) * 100 else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        // PCB Subject Selector
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
                            selectedContainerColor = AccentNavy.copy(alpha = 0.35f),
                            selectedLabelColor = AccentElectricBlue
                        )
                    )
                }
            }
        }

        // Summary Card
        item {
            GlassCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${currentSubject?.name ?: "PCB"} Lecture Tracker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Source: PW / NCERT Video Series",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AccentNavy.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "${overallLecturePct.toInt()}% Done",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentElectricBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { overallLecturePct / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AccentCyan,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LectureStatPill("Watched", "$totalWatched", ScoreGreen)
                        LectureStatPill("Remaining", "$totalRemaining", ScoreYellow)
                        LectureStatPill("Total", "$totalLectures", AccentElectricBlue)
                    }
                }
            }
        }

        // Action button to open PW Thor / Batches
        item {
            Button(
                onClick = { viewModel.navigateTo(Screen.LetsStudy) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayCircleOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open PW Thor / Let's Study Portal")
            }
        }

        // Chapters Lecture List
        item {
            SectionHeader(
                title = "Chapter-wise Lectures (${subjectChapters.size} Chapters)"
            )
        }

        items(subjectChapters, key = { it.id }) { chapter ->
            LectureChapterCard(
                chapter = chapter,
                onIncrement = { viewModel.incrementWatchedLecture(chapter.id) },
                onStartStudy = {
                    viewModel.startTimer(
                        subject = currentSubject?.name ?: "PCB",
                        chapter = chapter.title,
                        targetMinutes = 45
                    )
                    viewModel.navigateTo(Screen.StudySession)
                }
            )
        }
    }
}

@Composable
fun LectureStatPill(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun LectureChapterCard(
    chapter: ChapterEntity,
    onIncrement: () -> Unit,
    onStartStudy: () -> Unit
) {
    val pct = chapter.lectureProgressPercent

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
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
                            Text("${chapter.chapterNumber}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Column {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${chapter.watchedLectures} of ${chapter.totalLectures} watched • ${chapter.remainingLectures} remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (pct >= 100) ScoreGreen.copy(alpha = 0.15f) else AccentNavy.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (pct >= 100) ScoreGreen else AccentElectricBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { pct / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (pct >= 100) ScoreGreen else AccentCyan,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onIncrement,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    enabled = chapter.watchedLectures < chapter.totalLectures
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (chapter.watchedLectures < chapter.totalLectures) "+1 Lecture Done" else "All Done", fontSize = 11.sp)
                }

                Button(
                    onClick = onStartStudy,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Study Session", fontSize = 11.sp)
                }
            }
        }
    }
}
