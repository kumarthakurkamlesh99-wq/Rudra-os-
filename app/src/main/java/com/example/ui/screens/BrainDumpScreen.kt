package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import com.example.data.local.entities.BrainDumpEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import kotlinx.coroutines.launch

@Composable
fun BrainDumpScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val unprocessedNotes by viewModel.unprocessedBrainDumps.collectAsState()
    val allNotes by viewModel.repository.allBrainDumps.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Unprocessed, 1: All
    var newContent by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(BrainDumpEntity.CATEGORY_PARKING_LOT) }

    val categories = listOf(
        BrainDumpEntity.CATEGORY_PARKING_LOT,
        BrainDumpEntity.CATEGORY_IDEA,
        BrainDumpEntity.CATEGORY_THOUGHT,
        BrainDumpEntity.CATEGORY_LINK,
        BrainDumpEntity.CATEGORY_NOTE
    )

    val displayedNotes = if (selectedTab == 0) unprocessedNotes else allNotes

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Zero Friction Capture Card
        item {
            GlassCard {
                Text(
                    text = "RAPID CAPTURE / PARKING LOT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentElectricBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dimag mein koi bhi thought ya distraction aaye, turant yahan likh kar dimag khaali karo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.split(" (").first(), fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newContent,
                    onValueChange = { newContent = it },
                    placeholder = { Text("Quick dump: thought, formula doubt, to-do item...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("brain_dump_main_input"),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newContent.isNotBlank()) {
                            viewModel.saveBrainDump(newContent, selectedCategory)
                            newContent = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_brain_dump_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Capture Note")
                }
            }
        }

        // Tab Selector
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = AccentElectricBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Inbox / Unprocessed (${unprocessedNotes.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("All Notes (${allNotes.size})") }
                )
            }
        }

        // Notes List
        if (displayedNotes.isEmpty()) {
            item {
                GlassCard {
                    Text(
                        text = "Inbox Zero! No unorganized thoughts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(displayedNotes) { note ->
                BrainDumpItemCard(
                    note = note,
                    onConvertToTask = {
                        viewModel.convertBrainDumpToTask(note)
                    },
                    onMarkProcessed = {
                        coroutineScope.launch {
                            viewModel.repository.markBrainDumpProcessed(note.id)
                        }
                    },
                    onDelete = {
                        coroutineScope.launch {
                            viewModel.repository.deleteBrainDump(note.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BrainDumpItemCard(
    note: BrainDumpEntity,
    onConvertToTask: () -> Unit,
    onMarkProcessed: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AccentNavy.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = note.category.split(" (").first(),
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentElectricBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (note.isProcessed) {
                    Text("Processed", style = MaterialTheme.typography.labelSmall, color = ScoreGreen)
                }
            }

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!note.isProcessed) {
                    TextButton(onClick = onConvertToTask) {
                        Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Make Task", fontSize = 12.sp)
                    }

                    TextButton(onClick = onMarkProcessed) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Done", fontSize = 12.sp)
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
