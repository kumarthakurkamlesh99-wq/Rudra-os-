package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.TrendingUp
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

    // Score breakdown
    val greenDays = allScorecards.count { it.totalScore >= 5 }
    val yellowDays = allScorecards.count { it.totalScore in 3..4 }
    val redDays = allScorecards.count { it.totalScore <= 2 }

    // Total study stats
    val totalStudyHours = remember(allSessions) {
        String.format(Locale.getDefault(), "%.1f", allSessions.sumOf { it.durationMinutes } / 60.0)
    }

    val totalChaptersRevised = remember(allChapters) {
        allChapters.count { it.revisionCount > 0 }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // KPI Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = AccentNavy.copy(alpha = 0.25f)
                ) {
                    Text("Total Deep Study", style = MaterialTheme.typography.labelSmall, color = AccentElectricBlue)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${totalStudyHours}h", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Class 12 prep", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = ScoreGreen.copy(alpha = 0.12f)
                ) {
                    Text("Chapters Revised", style = MaterialTheme.typography.labelSmall, color = ScoreGreen)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("$totalChaptersRevised", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ScoreGreen)
                    Text("Spaced repeated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 7-Day Study Hours Bar Chart
        item {
            GlassCard {
                Text(
                    text = "PAST 7-DAYS STUDY HOURS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentElectricBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Daily Target: 5.0 - 6.0 Hours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Bar Chart Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .testTag("study_hours_bar_chart")
                ) {
                    val maxHours = 7f
                    val barWidth = size.width / (past7DaysStudy.size * 2)
                    val spacing = size.width / past7DaysStudy.size

                    // Draw 5h Target Line
                    val targetY = size.height * (1f - (5f / maxHours))
                    drawLine(
                        color = Color(0xFFF59E0B).copy(alpha = 0.5f),
                        start = Offset(0f, targetY),
                        end = Offset(size.width, targetY),
                        strokeWidth = 2f
                    )

                    past7DaysStudy.forEachIndexed { index, pair ->
                        val (_, hours) = pair
                        val barHeight = (hours / maxHours).coerceIn(0f, 1f) * size.height
                        val startX = (index * spacing) + (spacing - barWidth) / 2
                        val startY = size.height - barHeight

                        val barColor = if (hours >= 4.5f) ScoreGreen else if (hours >= 2.5f) AccentElectricBlue else ScoreRed

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(startX, startY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    past7DaysStudy.forEach { (label, hours) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.getDefault(), "%.1fh", hours),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Discipline Consistency Breakdown (Green / Yellow / Red)
        item {
            GlassCard {
                Text(
                    text = "DISCIPLINE CONSISTENCY SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentElectricBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConsistencyStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Green Days",
                        count = greenDays,
                        subtitle = "5-7 Points",
                        color = ScoreGreen
                    )
                    ConsistencyStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Yellow Days",
                        count = yellowDays,
                        subtitle = "3-4 Points",
                        color = ScoreYellow
                    )
                    ConsistencyStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Red Days",
                        count = redDays,
                        subtitle = "0-2 Points",
                        color = ScoreRed
                    )
                }
            }
        }

        // Daily KPIs Target Table (from PDF Section 21)
        item {
            GlassCard {
                Text(
                    text = "DAILY SYSTEM KPIS (TARGET VS MINIMUM)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentElectricBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                KpiRow(metric = "Deep Study Time", target = "5.5 - 6 Hours", minViable = "20 min (MVD)")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                KpiRow(metric = "Wake-Up Time", target = "05:45 AM", minViable = "06:30 AM hard cap")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                KpiRow(metric = "Physical Fitness", target = "45 min (8km cycle)", minViable = "15 min stretch")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                KpiRow(metric = "Daily Scorecard", target = "6 - 7 / 7", minViable = "3 / 7 (Yellow)")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                KpiRow(metric = "Evening Shutdown", target = "09:15 PM Plan", minViable = "3-Line Journal")
            }
        }
    }
}

@Composable
fun ConsistencyStatCard(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    subtitle: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$count", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = color)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun KpiRow(
    metric: String,
    target: String,
    minViable: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = metric, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(text = target, style = MaterialTheme.typography.labelLarge, color = AccentElectricBlue, fontWeight = FontWeight.Bold)
            Text(text = "Min: $minViable", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
