package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.StreakRecordEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StreakTrackerScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val allStreaks by viewModel.allStreaks.collectAsState()
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val totalCurrentStreakSum = allStreaks.sumOf { it.currentStreak }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        // Streaks Header Card
        item {
            GlassCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = ScoreRed)
                            Text(
                                text = "Discipline Streaks",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "“Consistency > Intensity • Har din SHOWING UP chahiye”",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentElectricBlue
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ScoreRed.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, ScoreRed.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Total Power", style = MaterialTheme.typography.labelSmall, color = ScoreRed)
                            Text("$totalCurrentStreakSum Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ScoreRed)
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "5 Core Pillars of Success")
        }

        // Streak Cards
        items(allStreaks, key = { it.streakKey }) { streak ->
            val isCheckedToday = streak.historyLog.contains(todayStr)
            StreakItemCard(
                streak = streak,
                isCheckedToday = isCheckedToday,
                onToggleCheckIn = { viewModel.toggleStreakCheckIn(streak.streakKey) }
            )
        }
    }
}

@Composable
fun StreakItemCard(
    streak: StreakRecordEntity,
    isCheckedToday: Boolean,
    onToggleCheckIn: () -> Unit
) {
    val (icon, color) = when (streak.streakKey) {
        StreakRecordEntity.KEY_STUDY -> Pair(Icons.Default.MenuBook, AccentElectricBlue)
        StreakRecordEntity.KEY_RUNNING -> Pair(Icons.Default.DirectionsRun, ScoreGreen)
        StreakRecordEntity.KEY_NO_PORN -> Pair(Icons.Default.Shield, Color(0xFFAB47BC))
        StreakRecordEntity.KEY_NO_PROCRASTINATION -> Pair(Icons.Default.Bolt, ScoreYellow)
        StreakRecordEntity.KEY_REVISION -> Pair(Icons.Default.HistoryEdu, AccentCyan)
        else -> Pair(Icons.Default.LocalFireDepartment, ScoreRed)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isCheckedToday) color.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                        shape = RoundedCornerShape(12.dp),
                        color = color.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                        }
                    }

                    Column {
                        Text(
                            text = streak.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = streak.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${streak.currentStreak}d",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    Text(
                        text = "Best: ${streak.bestStreak}d",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isCheckedToday) "✓ Done for Today" else "Not yet marked for today",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCheckedToday) ScoreGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isCheckedToday) FontWeight.Bold else FontWeight.Normal
                )

                Button(
                    onClick = onToggleCheckIn,
                    shape = RoundedCornerShape(10.dp),
                    colors = if (isCheckedToday) {
                        ButtonDefaults.buttonColors(containerColor = ScoreGreen.copy(alpha = 0.2f), contentColor = ScoreGreen)
                    } else {
                        ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White)
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isCheckedToday) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCheckedToday) "Completed" else "Check In Today",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
