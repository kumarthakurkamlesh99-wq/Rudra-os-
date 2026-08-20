package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notification.PermissionManager
import com.example.ui.theme.AccentElectricBlue
import com.example.ui.theme.DarkNavyBg

@Composable
fun NotificationRationaleDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current
    val permissionManager = PermissionManager(context)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = AccentElectricBlue,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Study Reminders & Exact Alarms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Rudra Life OS is an execution engine designed around strict time-blocked study sessions.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Notifications are required for:\n• Study Block start alarms with Start/Snooze/Skip\n• Daily spaced repetition due alerts\n• Pre-due task countdown alerts\n• Evening shutdown rituals at 21:15\n• Emergency recovery alerts when behind",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onRequestPermission()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlue),
                modifier = Modifier.testTag("rationale_grant_button")
            ) {
                Text("Grant Permission", color = DarkNavyBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    permissionManager.openNotificationSettings()
                },
                modifier = Modifier.testTag("rationale_settings_button")
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open Settings")
            }
        }
    )
}
