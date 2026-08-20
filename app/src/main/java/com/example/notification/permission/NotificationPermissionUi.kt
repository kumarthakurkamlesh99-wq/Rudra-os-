package com.example.notification.permission

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.ui.theme.AccentElectricBlue
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.ScoreYellow
import com.example.ui.theme.ScoreGreen

@Composable
fun NotificationPermissionRationaleCard(
    permissionManager: NotificationPermissionManager,
    onPermissionGranted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(permissionManager.hasNotificationPermission()) }
    var showRationaleDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            onPermissionGranted()
        }
    }

    // Periodically or on resume check permission state
    DisposableEffect(Unit) {
        hasPermission = permissionManager.hasNotificationPermission()
        onDispose { }
    }

    AnimatedVisibility(
        visible = !hasPermission,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ScoreYellow.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, ScoreYellow.copy(alpha = 0.4f)),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("notification_permission_banner")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = ScoreYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Notifications Disabled",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ScoreYellow
                    )
                }

                Text(
                    text = "Enable notifications to receive exact study block alerts, spaced revision reminders, and task deadlines.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showRationaleDialog = true },
                        modifier = Modifier.testTag("notification_why_button")
                    ) {
                        Text("Why needed?", fontSize = 12.sp, color = AccentElectricBlue)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val activity = context as? Activity
                                val shouldShowRationale = activity != null &&
                                        ActivityCompat.shouldShowRequestPermissionRationale(
                                            activity,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )

                                if (shouldShowRationale) {
                                    showRationaleDialog = true
                                } else {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                permissionManager.openNotificationSettings()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ScoreYellow),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("enable_notifications_button")
                    ) {
                        Text(
                            text = "Enable Notifications",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }

    if (showRationaleDialog) {
        NotificationRationaleDialog(
            onDismiss = { showRationaleDialog = false },
            onOpenSettings = {
                showRationaleDialog = false
                permissionManager.openNotificationSettings()
            },
            onRequestPermission = {
                showRationaleDialog = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    permissionManager.openNotificationSettings()
                }
            }
        )
    }
}

@Composable
fun NotificationRationaleDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = AccentElectricBlue,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Rudra Life OS Notification System",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Rudra Life OS uses Android exact alarms and notification channels for the following key features:",
                    style = MaterialTheme.typography.bodyMedium
                )

                RationaleItem(
                    icon = Icons.Default.Timer,
                    title = "Study Block Reminders",
                    description = "Exact wake-up alerts for Block 1, 3, and 5 with Start Now, Snooze, and Skip actions."
                )

                RationaleItem(
                    icon = Icons.Default.Autorenew,
                    title = "Spaced Repetition Alerts",
                    description = "Never miss active recall intervals and retention milestone checkpoints."
                )

                RationaleItem(
                    icon = Icons.Default.CheckCircle,
                    title = "Task Deadlines & Shutdown Ritual",
                    description = "Prioritized reminders before tasks are due and evening reflection prompts."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlue)
            ) {
                Text("Allow Notifications", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onOpenSettings) {
                    Text("App Settings")
                }
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        }
    )
}

@Composable
private fun RationaleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentElectricBlue,
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
