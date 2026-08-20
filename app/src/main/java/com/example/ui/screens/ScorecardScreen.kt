package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.util.Locale

@Composable
fun ScorecardScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val todayScorecard by viewModel.todayScorecard.collectAsState()
    val last7DaysScorecards by viewModel.last7DaysScorecards.collectAsState()
    val last30DaysScorecards by viewModel.last30DaysScorecards.collectAsState()

    var woke630 by remember(todayScorecard) { mutableStateOf(todayScorecard?.wokeUpBy630 ?: false) }
    var block1 by remember(todayScorecard) { mutableStateOf(todayScorecard?.completedBlock1 ?: false) }
    var block3 by remember(todayScorecard) { mutableStateOf(todayScorecard?.completedBlock3 ?: false) }
    var fitness by remember(todayScorecard) { mutableStateOf(todayScorecard?.completedFitness ?: false) }
    var block5 by remember(todayScorecard) { mutableStateOf(todayScorecard?.completedBlock5 ?: false) }
    var shutdown by remember(todayScorecard) { mutableStateOf(todayScorecard?.didShutdownRitual ?: false) }
    var noPhone by remember(todayScorecard) { mutableStateOf(todayScorecard?.noPhoneBlocked ?: false) }
    var notes by remember(todayScorecard) { mutableStateOf(todayScorecard?.notes ?: "") }

    val currentTotal = listOf(woke630, block1, block3, fitness, block5, shutdown, noPhone).count { it }

    val avg7Day = remember(last7DaysScorecards) {
        if (last7DaysScorecards.isNotEmpty()) {
            val total = last7DaysScorecards.sumOf { it.totalScore }
            String.format(Locale.getDefault(), "%.1f", total.toDouble() / last7DaysScorecards.size)
        } else "0.0"
    }

    val avg30Day = remember(last30DaysScorecards) {
        if (last30DaysScorecards.isNotEmpty()) {
            val total = last30DaysScorecards.sumOf { it.totalScore }
            String.format(Locale.getDefault(), "%.1f", total.toDouble() / last30DaysScorecards.size)
        } else "0.0"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. Philosophy Banner (PDF Section 14)
        item {
            GlassCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = AccentElectricBlue)
                    Column {
                        Text(
                            text = "DAILY DISCIPLINE SCORECARD (7 PTS)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentElectricBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "“Scorecard roz bharoge — accha ho ya bura. Green (5-7): System Working • Yellow (3-4): Survived & Logged • Red (0-2): Emergency Protocol.”",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 2. Interactive Today's Scorecard Form
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S AUDIT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Total Score: $currentTotal / 7",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    ScoreBadge(score = currentTotal)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScoreCheckRow(label = "1. Woke up by 6:30 AM (Hard Cap)", checked = woke630, onToggle = { woke630 = it })
                    ScoreCheckRow(label = "2. Completed Study Block 1 (Deep Focus Physics/Chem)", checked = block1, onToggle = { block1 = it })
                    ScoreCheckRow(label = "3. Completed Study Block 3 (Main Theory)", checked = block3, onToggle = { block3 = it })
                    ScoreCheckRow(label = "4. Completed Fitness Block (Min 15 min workout/cycle)", checked = fitness, onToggle = { fitness = it })
                    ScoreCheckRow(label = "5. Completed Study Block 5 (Revision only, no new topics)", checked = block5, onToggle = { block5 = it })
                    ScoreCheckRow(label = "6. Did Shutdown Ritual (9:15 PM plan tomorrow)", checked = shutdown, onToggle = { shutdown = it })
                    ScoreCheckRow(label = "7. No Phone during blocked study hours", checked = noPhone, onToggle = { noPhone = it })

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Daily notes / reflection") },
                        placeholder = { Text("What worked well today or what triggered distraction?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            viewModel.saveScorecard(woke630, block1, block3, fitness, block5, shutdown, noPhone, notes)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_scorecard_main_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Today's Scorecard ($currentTotal/7)")
                    }
                }
            }
        }

        // 3. Scorecard Moving Averages
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("7-Day Moving Avg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$avg7Day / 7", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AccentElectricBlue)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("30-Day Moving Avg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$avg30Day / 7", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AccentCyan)
                    }
                }
            }
        }

        // 4. Past 7-Days History Matrix
        item {
            SectionHeader(title = "Recent Discipline History")
        }

        items(last7DaysScorecards) { sc ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sc.dateString,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (sc.notes.isNotBlank()) {
                            Text(
                                text = sc.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    ScoreBadge(score = sc.totalScore)
                }
            }
        }
    }
}

@Composable
fun ScoreCheckRow(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) ScoreGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
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
