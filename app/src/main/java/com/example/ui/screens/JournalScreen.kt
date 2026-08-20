package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.entities.JournalEntryEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import kotlinx.coroutines.launch

@Composable
fun JournalScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val todayJournal by viewModel.todayJournal.collectAsState()
    val allJournalEntries by viewModel.allJournalEntries.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Daily 3-Line, 1: Sunday Weekly Review, 2: History

    // Daily 3-Line fields
    var mood by remember(todayJournal) { mutableStateOf(todayJournal?.mood ?: "Normal") }
    var winsDone by remember(todayJournal) { mutableStateOf(todayJournal?.winsDone ?: "") }
    var missedWhat by remember(todayJournal) { mutableStateOf(todayJournal?.missedWhat ?: "") }
    var tomorrowFocus by remember(todayJournal) { mutableStateOf(todayJournal?.tomorrowFocusAndBlock1 ?: "") }
    var reflection by remember(todayJournal) { mutableStateOf(todayJournal?.generalReflection ?: "") }

    // Sunday Review fields
    var strongDay by remember(todayJournal) { mutableStateOf(todayJournal?.weeklyReviewStrongDay ?: "") }
    var weakDayAndTrigger by remember(todayJournal) { mutableStateOf(todayJournal?.weeklyReviewWeakDayAndTrigger ?: "") }
    var neglectedSubject by remember(todayJournal) { mutableStateOf(todayJournal?.weeklyReviewNeglectedSubject ?: "") }
    var oneAdjustment by remember(todayJournal) { mutableStateOf(todayJournal?.weeklyReviewOneAdjustment ?: "") }

    val moods = listOf("Great", "Normal", "Tired", "Stressed", "Low Energy")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Tab selector
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = AccentElectricBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Daily 3-Line Shutdown", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Sunday Weekly Review", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Past Entries (${allJournalEntries.size})", fontSize = 13.sp) }
                )
            }
        }

        if (selectedTab == 0) {
            // Daily 3-Line Shutdown (PDF Section 5)
            item {
                GlassCard {
                    Text(
                        text = "EVENING SHUTDOWN RITUAL (9:15 PM)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentElectricBlue,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "3-Line Journal har raat fill karo: Aaj kya kiya, kya miss hua, kal ka ek focus & Block 1 topic.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Today's State / Mood", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        moods.forEach { m ->
                            FilterChip(
                                selected = mood == m,
                                onClick = { mood = m },
                                label = { Text(m, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = winsDone,
                        onValueChange = { winsDone = it },
                        label = { Text("1. Aaj kya kiya (Wins / What was accomplished)") },
                        placeholder = { Text("e.g. Completed 10 Physics numericals, cycled 8km, 5h deep study.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_wins_input"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = missedWhat,
                        onValueChange = { missedWhat = it },
                        label = { Text("2. Kya miss hua (Missed / Obstacles)") },
                        placeholder = { Text("e.g. Afternoon phone distraction of 20 min, delayed Block 3.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_missed_input"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tomorrowFocus,
                        onValueChange = { tomorrowFocus = it },
                        label = { Text("3. Kal ka ek focus & Block 1 topic (Decide TONIGHT)") },
                        placeholder = { Text("e.g. Block 1: Electrochemistry Kohlrausch's law numericals. Books on table.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_tomorrow_focus_input"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reflection,
                        onValueChange = { reflection = it },
                        label = { Text("General Reflection / Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.saveEveningJournal(mood, winsDone, missedWhat, tomorrowFocus, reflection)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_evening_journal_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Complete Evening Shutdown")
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Sunday Weekly Review (PDF Section 15)
            item {
                GlassCard {
                    Text(
                        text = "SUNDAY SYSTEM REVIEW (20 MIN PROTOCOL)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentElectricBlue,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“Pichle 7 din ka honest post-mortem. Guilt nahi, system adjustment.”",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = strongDay,
                        onValueChange = { strongDay = it },
                        label = { Text("1. Strongest Day of the Week & Why it worked") },
                        placeholder = { Text("e.g. Wednesday was 7/7 because morning Block 1 started on time.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("weekly_strong_day_input"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = weakDayAndTrigger,
                        onValueChange = { weakDayAndTrigger = it },
                        label = { Text("2. Weakest Day & Exact Trigger (phone, late sleep, tiredness?)") },
                        placeholder = { Text("e.g. Friday slipped because stayed up till 11:30 PM on Thursday.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("weekly_weak_day_input"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = neglectedSubject,
                        onValueChange = { neglectedSubject = it },
                        label = { Text("3. Most Neglected Subject this week") },
                        placeholder = { Text("e.g. Biology Genetics - need extra 1-hour block on Sunday.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = oneAdjustment,
                        onValueChange = { oneAdjustment = it },
                        label = { Text("4. ONE Small System Adjustment for next week (NOT 10 things)") },
                        placeholder = { Text("e.g. Charge phone strictly outside room at 9:45 PM.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.saveSundayWeeklyReview(strongDay, weakDayAndTrigger, neglectedSubject, oneAdjustment)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ChecklistRtl, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Sunday Weekly Review")
                    }
                }
            }
        } else {
            // Past entries list
            if (allJournalEntries.isEmpty()) {
                item {
                    GlassCard {
                        Text(
                            text = "No past journal entries yet. Complete your first Evening Shutdown or Weekly Review above.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(allJournalEntries) { entry ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.dateString,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentNavy.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = entry.mood,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentElectricBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (entry.winsDone.isNotBlank()) {
                            Text(
                                text = "✅ Wins: ${entry.winsDone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (entry.missedWhat.isNotBlank()) {
                            Text(
                                text = "⚠️ Missed: ${entry.missedWhat}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ScoreYellow
                            )
                        }
                        if (entry.tomorrowFocusAndBlock1.isNotBlank()) {
                            Text(
                                text = "🎯 Tomorrow Focus: ${entry.tomorrowFocusAndBlock1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentCyan
                            )
                        }
                        if (entry.isWeeklyReview) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "📊 Weekly Adjustment: ${entry.weeklyReviewOneAdjustment}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
