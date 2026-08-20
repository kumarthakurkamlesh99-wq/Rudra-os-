package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notification.permission.NotificationPermissionManager
import com.example.notification.permission.NotificationPermissionRationaleCard
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val themeMode by viewModel.themeMode.collectAsState()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsState()
    val lastBackupDate by viewModel.repository.lastBackupDate.collectAsState(initial = "Never")

    // Notification State
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val studyRemindersEnabled by viewModel.studyRemindersEnabled.collectAsState()
    val revisionRemindersEnabled by viewModel.revisionRemindersEnabled.collectAsState()
    val taskRemindersEnabled by viewModel.taskRemindersEnabled.collectAsState()
    val shutdownRemindersEnabled by viewModel.shutdownRemindersEnabled.collectAsState()
    val recoveryRemindersEnabled by viewModel.recoveryRemindersEnabled.collectAsState()
    val weeklyReviewEnabled by viewModel.weeklyReviewEnabled.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val quietHoursEnabled by viewModel.quietHoursEnabled.collectAsState()
    val quietHoursStart by viewModel.quietHoursStart.collectAsState()
    val quietHoursEnd by viewModel.quietHoursEnd.collectAsState()
    val taskReminderOffset by viewModel.taskReminderOffset.collectAsState()
    val block1Time by viewModel.block1Time.collectAsState()
    val block3Time by viewModel.block3Time.collectAsState()
    val block5Time by viewModel.block5Time.collectAsState()
    val shutdownTime by viewModel.shutdownTime.collectAsState()

    var showEditTimesDialog by remember { mutableStateOf(false) }
    var showEditQuietHoursDialog by remember { mutableStateOf(false) }

    var importJsonText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportResultJson by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    val permissionManager = remember { NotificationPermissionManager(context) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Notification Permission Rationale Banner (if permission missing)
        item {
            NotificationPermissionRationaleCard(
                permissionManager = permissionManager,
                onPermissionGranted = {
                    viewModel.setNotificationsEnabled(true)
                }
            )
        }

        // Master Notification Settings Card
        item {
            GlassCard {
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
                                text = "SYSTEM NOTIFICATIONS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentElectricBlue,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (notificationsEnabled) ScoreGreen.copy(alpha = 0.2f) else ScoreRed.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (notificationsEnabled) "ACTIVE" else "MUTED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (notificationsEnabled) ScoreGreen else ScoreRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Real Android local notifications using exact AlarmManager and background WorkManager.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                        modifier = Modifier.testTag("master_notifications_switch")
                    )
                }

                if (notificationsEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Study Block Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("1. Study Block Reminders", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Block 1 ($block1Time) • Block 3 ($block3Time) • Block 5 ($block5Time)\nActions: Start Now, Snooze 15m, Skip",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                        Switch(
                            checked = studyRemindersEnabled,
                            onCheckedChange = { viewModel.setStudyRemindersEnabled(it) },
                            modifier = Modifier.testTag("study_reminders_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Revision Due Reminder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("2. Revision Due Alerts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Triggers daily when spaced repetition revisions become due.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = revisionRemindersEnabled,
                            onCheckedChange = { viewModel.setRevisionRemindersEnabled(it) },
                            modifier = Modifier.testTag("revision_reminders_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Task Reminder & Offset Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("3. Task Due Reminders", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "Notify before high and normal priority tasks are due.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = taskRemindersEnabled,
                                onCheckedChange = { viewModel.setTaskRemindersEnabled(it) },
                                modifier = Modifier.testTag("task_reminders_switch")
                            )
                        }

                        if (taskRemindersEnabled) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Notify Before Due Time:", style = MaterialTheme.typography.labelSmall, color = AccentElectricBlue)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val offsets = listOf(
                                    "10_MIN" to "10 min",
                                    "30_MIN" to "30 min",
                                    "1_HOUR" to "1 hour",
                                    "1_DAY" to "1 day"
                                )
                                offsets.forEach { (key, label) ->
                                    FilterChip(
                                        selected = taskReminderOffset == key,
                                        onClick = { viewModel.setTaskReminderOffset(key) },
                                        label = { Text(label, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Shutdown Ritual Reminder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("4. Shutdown Ritual Reminder", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Evening shutdown at $shutdownTime: Review today & prepare tomorrow.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = shutdownRemindersEnabled,
                            onCheckedChange = { viewModel.setShutdownRemindersEnabled(it) },
                            modifier = Modifier.testTag("shutdown_reminders_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Recovery Mode Suggestion
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("5. Recovery Mode Alerts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Triggered when multiple tasks or revisions are overdue.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = recoveryRemindersEnabled,
                            onCheckedChange = { viewModel.setRecoveryRemindersEnabled(it) },
                            modifier = Modifier.testTag("recovery_reminders_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Weekly Sunday Review
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("6. Sunday Weekly Review", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Every Sunday at 15:00: Review week and set target adjustments.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = weeklyReviewEnabled,
                            onCheckedChange = { viewModel.setWeeklyReviewEnabled(it) },
                            modifier = Modifier.testTag("weekly_review_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Sound, Vibration, Quiet Hours
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sound", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Switch(checked = soundEnabled, onCheckedChange = { viewModel.setSoundEnabled(it) })
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Vibrate", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Switch(checked = vibrationEnabled, onCheckedChange = { viewModel.setVibrationEnabled(it) })
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Quiet Hours (DND)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (quietHoursEnabled) "Active: $quietHoursStart to $quietHoursEnd" else "Disabled (24h alerts)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (quietHoursEnabled) {
                                TextButton(onClick = { showEditQuietHoursDialog = true }) {
                                    Text("Edit", fontSize = 12.sp)
                                }
                            }
                            Switch(checked = quietHoursEnabled, onCheckedChange = { viewModel.setQuietHoursEnabled(it) })
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditTimesDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Custom Times", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.triggerTestNotification()
                                Toast.makeText(context, "System Notification Sent!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("test_notification_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlue)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp), tint = DarkNavyBg)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Alert", fontSize = 12.sp, color = DarkNavyBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        // App Theme Selector
        item {
            GlassCard {
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentElectricBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf("DARK" to "Dark Navy", "LIGHT" to "Light", "SYSTEM" to "System")
                    themes.forEach { (mode, label) ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.repository.setThemeMode(mode)
                                }
                            },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Low Energy Mode Master Switch
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Low Energy Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Reduces targets to minimum viable (2-3/7), activates planned degraded routine.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isLowEnergy,
                        onCheckedChange = { viewModel.toggleLowEnergyMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ScoreYellow,
                            checkedTrackColor = ScoreYellowBg
                        )
                    )
                }
            }
        }

        // Complete Offline Backup & Export Engine (Long-term retention 5+ years)
        item {
            GlassCard {
                Text(
                    text = "DATA RETENTION & BACKUP (OFFLINE-FIRST)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentElectricBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rudra Life OS stores 100% of data locally on your device. Export complete JSON backups to keep data safe for years.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isExporting = true
                                val json = viewModel.backupManager.exportToJsonString()
                                exportResultJson = json
                                isExporting = false
                                Toast.makeText(context, "Full Backup Export Generated!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_backup_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export JSON", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("import_backup_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import JSON", fontSize = 12.sp)
                    }
                }

                if (exportResultJson != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("JSON Backup Ready", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ScoreGreen)
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(exportResultJson ?: ""))
                                        Toast.makeText(context, "JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 12.sp)
                                }
                            }
                            Text(
                                text = exportResultJson?.take(200) + "...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // About Rudra Life OS System Info
        item {
            GlassCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "Rudra Life OS v1.0.0",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Engineered for Class 12 Science (BSEB / CBSE).\n" +
                           "Personal offline-first Life Operating System.\n" +
                           "Zero social feeds • Zero leaderboards • System over Motivation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import JSON Backup", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paste the JSON backup string below to restore/merge your records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("Paste JSON here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 8,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            coroutineScope.launch {
                                val res = viewModel.backupManager.importFromJsonString(importJsonText)
                                if (res.isSuccess) {
                                    Toast.makeText(context, res.getOrNull(), Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to import JSON: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                                showImportDialog = false
                            }
                        }
                    }
                ) {
                    Text("Restore Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditTimesDialog) {
        var editB1 by remember { mutableStateOf(block1Time) }
        var editB3 by remember { mutableStateOf(block3Time) }
        var editB5 by remember { mutableStateOf(block5Time) }
        var editShutdown by remember { mutableStateOf(shutdownTime) }

        AlertDialog(
            onDismissRequest = { showEditTimesDialog = false },
            title = { Text("Customize Routine Times (24h format)", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editB1,
                        onValueChange = { editB1 = it },
                        label = { Text("Block 1 Time (HH:mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editB3,
                        onValueChange = { editB3 = it },
                        label = { Text("Block 3 Time (HH:mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editB5,
                        onValueChange = { editB5 = it },
                        label = { Text("Block 5 Time (HH:mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editShutdown,
                        onValueChange = { editShutdown = it },
                        label = { Text("Shutdown Ritual Time (HH:mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setBlockTimes(editB1, editB3, editB5, editShutdown)
                        showEditTimesDialog = false
                        Toast.makeText(context, "Routine times updated and alarms rescheduled.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTimesDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditQuietHoursDialog) {
        var editStart by remember { mutableStateOf(quietHoursStart) }
        var editEnd by remember { mutableStateOf(quietHoursEnd) }

        AlertDialog(
            onDismissRequest = { showEditQuietHoursDialog = false },
            title = { Text("Quiet Hours Window (DND)", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("All non-emergency reminders are silenced during this period.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = editStart,
                        onValueChange = { editStart = it },
                        label = { Text("Start Time (HH:mm, e.g. 22:00)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editEnd,
                        onValueChange = { editEnd = it },
                        label = { Text("End Time (HH:mm, e.g. 05:45)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setQuietHoursRange(editStart, editEnd)
                        showEditQuietHoursDialog = false
                        Toast.makeText(context, "Quiet hours updated.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditQuietHoursDialog = false }) { Text("Cancel") }
            }
        )
    }
}
