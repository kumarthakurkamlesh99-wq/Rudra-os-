package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val allSessions by viewModel.allSessions.collectAsState()
    val allScorecards by viewModel.allScorecards.collectAsState()
    val allChapters by viewModel.allChapters.collectAsState()
    val allMockTests by viewModel.allMockTests.collectAsState()
    val allStreaks by viewModel.allStreaks.collectAsState()

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Today's Study Hours
    val todayStudyMinutes = remember(allSessions) {
        allSessions.filter { it.dateString == todayStr }.sumOf { it.durationMinutes }
    }
    val todayStudyHours = String.format(Locale.getDefault(), "%.1f", todayStudyMinutes / 60.0)

    // Aggregate past 7 days study hours
    val past7DaysStudy = remember(allSessions) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayLabelFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val result = mutableListOf<Pair<String, Float>>()

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormat.format(cal.time)
            val dayLabel = dayLabelFormat.format(cal.time)
            val totalMins = allSessions.filter { it.dateString == dateStr }.sumOf { it.durationMinutes }
            val hours = totalMins / 60f
            result.add(Pair(dayLabel, hours))
        }
        result
    }

    val past7DaysTotalHours = past7DaysStudy.sumOf { it.second.toDouble() }
    val dailyAverageHours = String.format(Locale.getDefault(), "%.1f", past7DaysTotalHours / 7.0)

    // Total study stats
    val totalStudyHours = remember(allSessions) {
        String.format(Locale.getDefault(), "%.1f", allSessions.sumOf { it.durationMinutes } / 60.0)
    }

    // PCB Chapters Metrics
    val totalChapters = allChapters.size.coerceAtLeast(1)
    val completedChapters = allChapters.count { it.status == ChapterEntity.STATUS_COMPLETED || it.status == ChapterEntity.STATUS_REVISED }
    val learningChapters = allChapters.count { it.status == ChapterEntity.STATUS_LEARNING }
    val weakChaptersCount = allChapters.count { it.isWeak }
    val strongChaptersCount = allChapters.count { it.confidenceRating >= 4 }

    val mockAverage = if (allMockTests.isNotEmpty()) allMockTests.map { it.percentage }.average() else 0.0

    // Consistency score (Percentage of green/yellow days out of logged scorecards)
    val totalScorecardLogs = allScorecards.size.coerceAtLeast(1)
    val highPerformanceDays = allScorecards.count { it.totalScore >= 4 }
    val consistencyScore = ((highPerformanceDays.toDouble() / totalScorecardLogs) * 100).toInt()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        // Reality Dashboard Banner
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
                                text = "Reality Dashboard",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Uncompromising Truth & PCB Readiness Metrics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ScoreGreen.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, ScoreGreen.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "$consistencyScore% Consistency",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ScoreGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBox("Today", "${todayStudyHours}h", AccentElectricBlue, Modifier.weight(1f))
                        MetricBox("7-Day Avg", "${dailyAverageHours}h/d", ScoreGreen, Modifier.weight(1f))
                        MetricBox("Total Study", "${totalStudyHours}h", AccentCyan, Modifier.weight(1f))
                    }
                }
            }
        }

        // 7-Day Study Hours Bar Chart
        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PAST 7-DAYS STUDY HOURS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentElectricBlue,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Target: 5.0 - 6.0 Hours / Day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("study_hours_bar_chart")
                    ) {
                        val maxHours = 8f
                        val count = past7DaysStudy.size
                        val barWidth = size.width / (count * 2.2f)
                        val step = size.width / count

                        // Draw 5h Target Line
                        val targetY = size.height * (1f - (5f / maxHours))
                        drawLine(
                            color = Color(0xFFF59E0B).copy(alpha = 0.5f),
                            start = Offset(0f, targetY),
                            end = Offset(size.width, targetY),
                            strokeWidth = 2.dp.toPx()
                        )

                        past7DaysStudy.forEachIndexed { index, pair ->
                            val hours = pair.second
                            val barHeight = (hours / maxHours).coerceIn(0f, 1f) * size.height
                            val x = (index * step) + (step - barWidth) / 2
                            val y = size.height - barHeight

                            val barColor = when {
                                hours >= 5f -> ScoreGreen
                                hours >= 3f -> ScoreYellow
                                else -> ScoreRed
                            }

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx())
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        past7DaysStudy.forEach { pair ->
                            Text(
                                text = "${pair.first}\n${String.format(Locale.getDefault(), "%.1f", pair.second)}h",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // PCB Chapter Preparedness Matrix
        item {
            SectionHeader(title = "PCB Syllabus Health Matrix")
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ChapterHealthPill("Completed", "$completedChapters", ScoreGreen, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        ChapterHealthPill("Learning", "$learningChapters", AccentCyan, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        ChapterHealthPill("Weak Radar", "$weakChaptersCount", ScoreRed, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        ChapterHealthPill("Strong (4-5★)", "$strongChaptersCount", ScoreGreen, Modifier.weight(1f))
                    }
                }
            }
        }

        // Mock Tests & Revision Accuracy
        item {
            SectionHeader(title = "Exam Readiness Indicators")
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = AccentNavy.copy(alpha = 0.25f)
                ) {
                    Text("Mock Test Average", style = MaterialTheme.typography.labelSmall, color = AccentElectricBlue)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${mockAverage.toInt()}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Target: 85%+", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = ScoreGreen.copy(alpha = 0.12f)
                ) {
                    Text("Active Streaks", style = MaterialTheme.typography.labelSmall, color = ScoreGreen)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${allStreaks.count { it.currentStreak > 0 }}/5", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ScoreGreen)
                    Text("Pillars active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun MetricBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ChapterHealthPill(label: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
