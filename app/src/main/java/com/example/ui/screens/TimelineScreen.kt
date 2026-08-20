package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.entities.TimelineBlockEntity
import com.example.data.local.entities.TimelinePresetEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import kotlinx.coroutines.launch

@Composable
fun TimelineScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allPresets by viewModel.allPresets.collectAsState()
    val activePreset by viewModel.activePreset.collectAsState()
    val selectedPresetId by viewModel.selectedPresetId.collectAsState()
    val blocks by viewModel.currentTimelineBlocks.collectAsState()

    val currentActivePresetId = selectedPresetId ?: activePreset?.id ?: 1L

    var showAddBlockDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Preset selector chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ROUTINE PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentElectricBlue,
                    letterSpacing = 1.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allPresets) { preset ->
                        val isSelected = preset.id == currentActivePresetId
                        val isActive = preset.isActive
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedPresetId(preset.id) },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(preset.name, fontSize = 13.sp)
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(ScoreGreen)
                                        )
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentNavy.copy(alpha = 0.3f),
                                selectedLabelColor = AccentElectricBlue
                            ),
                            modifier = Modifier.testTag("preset_chip_${preset.id}")
                        )
                    }
                }
            }
        }

        // Active Preset Details & Activate button
        item {
            val preset = allPresets.find { it.id == currentActivePresetId } ?: activePreset
            if (preset != null) {
                GlassCard(
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (preset.isActive) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ScoreGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Active Daily",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ScoreGreen,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!preset.isActive) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.repository.activatePreset(preset.id)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("activate_preset_button")
                            ) {
                                Text("Set Active", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Master Timeline Blocks list
        item {
            SectionHeader(
                title = "Timeline Blocks (${blocks.size})",
                actionText = "+ Add Block",
                onActionClick = { showAddBlockDialog = true }
            )
        }

        items(blocks) { block ->
            TimelineBlockCard(
                block = block,
                onToggleComplete = { completed ->
                    coroutineScope.launch {
                        viewModel.repository.updateBlockCompletion(block.id, completed)
                    }
                }
            )
        }
    }

    if (showAddBlockDialog) {
        AddTimelineBlockDialog(
            presetId = currentActivePresetId,
            onDismiss = { showAddBlockDialog = false },
            onSave = { newBlock ->
                coroutineScope.launch {
                    viewModel.repository.insertBlock(newBlock)
                }
                showAddBlockDialog = false
            }
        )
    }
}

@Composable
fun TimelineBlockCard(
    block: TimelineBlockEntity,
    onToggleComplete: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val categoryColor = when (block.category) {
        "Study" -> AccentElectricBlue
        "Fitness" -> ScoreGreen
        "Shutdown" -> ScoreRed
        "School" -> Color(0xFF8B5CF6)
        "Rest" -> ScoreYellow
        else -> Color(0xFF64748B)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Checkbox(
                    checked = block.isCompleted,
                    onCheckedChange = onToggleComplete,
                    modifier = Modifier.testTag("timeline_block_check_${block.id}")
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = categoryColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = block.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = categoryColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${block.startTime} – ${block.endTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = block.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (block.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (block.description.isNotBlank()) {
                Text(
                    text = block.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Always show physical trigger if present
            if (block.triggerAction.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentNavy.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, AccentElectricBlue.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = AccentElectricBlue, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Physical Trigger: ${block.triggerAction}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentElectricBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Expanded details: Backup Plan & Failure Recovery
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (block.backupPlan.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ScoreYellowBg.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "⚡ Backup Version: ${block.backupPlan}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ScoreYellow,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    if (block.failureRecovery.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ScoreRedBg.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🛡️ Failure Recovery: ${block.failureRecovery}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ScoreRed,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTimelineBlockDialog(
    presetId: Long,
    onDismiss: () -> Unit,
    onSave: (TimelineBlockEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("06:00 AM") }
    var endTime by remember { mutableStateOf("07:15 AM") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Study") }
    var triggerAction by remember { mutableStateOf("") }
    var backupPlan by remember { mutableStateOf("") }
    var failureRecovery by remember { mutableStateOf("") }

    val categories = listOf("Study", "Routine", "Fitness", "School", "Rest", "Shutdown")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Timeline Block", style = MaterialTheme.typography.titleLarge) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Block Title (e.g. Study Block 1)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Text("Category", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 12.sp) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = triggerAction,
                        onValueChange = { triggerAction = it },
                        label = { Text("Physical Habit-Stack Trigger") },
                        placeholder = { Text("e.g. Water peene ke turant baad") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = backupPlan,
                        onValueChange = { backupPlan = it },
                        label = { Text("Backup Plan (If running late)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = failureRecovery,
                        onValueChange = { failureRecovery = it },
                        label = { Text("Failure Recovery Instruction") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            TimelineBlockEntity(
                                presetId = presetId,
                                title = title,
                                startTime = startTime,
                                endTime = endTime,
                                description = description,
                                category = category,
                                triggerAction = triggerAction,
                                backupPlan = backupPlan,
                                failureRecovery = failureRecovery
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_timeline_block_button")
            ) {
                Text("Save Block")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
