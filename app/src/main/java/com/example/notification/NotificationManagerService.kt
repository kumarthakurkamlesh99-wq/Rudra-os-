package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import java.util.Calendar

class NotificationManagerService(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // 1. Study Blocks Channel (High Priority)
            val studyChannel = NotificationChannel(
                NotificationConstants.CHANNEL_ID_STUDY_BLOCKS,
                NotificationConstants.CHANNEL_NAME_STUDY_BLOCKS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NotificationConstants.CHANNEL_DESC_STUDY_BLOCKS
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // 2. Revision Alerts Channel (High Priority)
            val revisionChannel = NotificationChannel(
                NotificationConstants.CHANNEL_ID_REVISIONS,
                NotificationConstants.CHANNEL_NAME_REVISIONS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NotificationConstants.CHANNEL_DESC_REVISIONS
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // 3. Task Reminders Channel (High Priority)
            val taskChannel = NotificationChannel(
                NotificationConstants.CHANNEL_ID_TASKS,
                NotificationConstants.CHANNEL_NAME_TASKS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NotificationConstants.CHANNEL_DESC_TASKS
                enableLights(true)
                lightColor = Color.YELLOW
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // 4. Shutdown Channel (High Priority)
            val shutdownChannel = NotificationChannel(
                NotificationConstants.CHANNEL_ID_SHUTDOWN,
                NotificationConstants.CHANNEL_NAME_SHUTDOWN,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NotificationConstants.CHANNEL_DESC_SHUTDOWN
                enableLights(true)
                lightColor = Color.MAGENTA
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // 5. Recovery Channel (High Priority)
            val recoveryChannel = NotificationChannel(
                NotificationConstants.CHANNEL_ID_RECOVERY,
                NotificationConstants.CHANNEL_NAME_RECOVERY,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NotificationConstants.CHANNEL_DESC_RECOVERY
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // 6. Weekly Review Channel (High Priority)
            val weeklyChannel = NotificationChannel(
                NotificationConstants.CHANNEL_ID_WEEKLY,
                NotificationConstants.CHANNEL_NAME_WEEKLY,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NotificationConstants.CHANNEL_DESC_WEEKLY
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 300, 300)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannels(
                listOf(studyChannel, revisionChannel, taskChannel, shutdownChannel, recoveryChannel, weeklyChannel)
            )
        }
    }

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun isInQuietHours(quietHoursEnabled: Boolean, startStr: String, endStr: String): Boolean {
        if (!quietHoursEnabled) return false
        try {
            val cal = Calendar.getInstance()
            val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

            val startParts = startStr.split(":").map { it.trim().toInt() }
            val endParts = endStr.split(":").map { it.trim().toInt() }

            val startMinutes = startParts[0] * 60 + startParts[1]
            val endMinutes = endParts[0] * 60 + endParts[1]

            return if (startMinutes <= endMinutes) {
                currentMinutes in startMinutes..endMinutes
            } else {
                currentMinutes >= startMinutes || currentMinutes <= endMinutes
            }
        } catch (e: Exception) {
            return false
        }
    }

    private fun getDeepLinkIntent(targetScreen: String, taskId: Long = 0L, blockNumber: Int = 0): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationConstants.EXTRA_TARGET_SCREEN, targetScreen)
            if (taskId > 0L) {
                putExtra(NotificationConstants.EXTRA_TASK_ID, taskId)
            }
            if (blockNumber > 0) {
                putExtra(NotificationConstants.EXTRA_BLOCK_NUMBER, blockNumber)
            }
        }
        val requestCode = targetScreen.hashCode() + taskId.toInt() + blockNumber
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // 1. Study Block Notification
    fun showStudyBlockNotification(
        blockNumber: Int,
        title: String = "Study Block $blockNumber",
        message: String = "Time to begin your planned study session."
    ) {
        if (!hasPermission()) return

        val notifId = when (blockNumber) {
            1 -> NotificationConstants.NOTIF_ID_BLOCK_1
            3 -> NotificationConstants.NOTIF_ID_BLOCK_3
            else -> NotificationConstants.NOTIF_ID_BLOCK_5
        }

        val contentPendingIntent = getDeepLinkIntent(NotificationConstants.SCREEN_LETS_STUDY, blockNumber = blockNumber)

        // Action: Start Now
        val startIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationConstants.EXTRA_TARGET_SCREEN, NotificationConstants.SCREEN_LETS_STUDY)
            putExtra(NotificationConstants.EXTRA_BLOCK_NUMBER, blockNumber)
        }
        val startPendingIntent = PendingIntent.getActivity(
            context,
            notifId + 100,
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze 15 Min
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationConstants.ACTION_SNOOZE_15_MIN
            putExtra(NotificationConstants.EXTRA_NOTIFICATION_ID, notifId)
            putExtra(NotificationConstants.EXTRA_BLOCK_NUMBER, blockNumber)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 200,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Skip
        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationConstants.ACTION_SKIP_BLOCK
            putExtra(NotificationConstants.EXTRA_NOTIFICATION_ID, notifId)
            putExtra(NotificationConstants.EXTRA_BLOCK_NUMBER, blockNumber)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 300,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID_STUDY_BLOCKS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_media_play, "Start Now", startPendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 15 Min", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Skip", skipPendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notifId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // 2. Revision Due Notification
    fun showRevisionDueNotification(
        message: String = "You have revisions scheduled today."
    ) {
        if (!hasPermission()) return

        val contentPendingIntent = getDeepLinkIntent(NotificationConstants.SCREEN_REVISION)

        val builder = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID_REVISIONS)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("Revision Due")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NotificationConstants.NOTIF_ID_REVISION, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // 3. Task Reminder Notification
    fun showTaskReminderNotification(
        taskId: Long,
        title: String,
        message: String
    ) {
        if (!hasPermission()) return

        val notifId = (NotificationConstants.NOTIF_ID_TASK_BASE + (taskId % 1000)).toInt()
        val contentPendingIntent = getDeepLinkIntent(NotificationConstants.SCREEN_TASKS, taskId = taskId)

        val builder = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID_TASKS)
            .setSmallIcon(android.R.drawable.ic_input_add)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notifId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // 4. Shutdown Ritual Notification
    fun showShutdownRitualNotification(
        title: String = "Shutdown Ritual",
        message: String = "Review today and prepare tomorrow."
    ) {
        if (!hasPermission()) return

        val contentPendingIntent = getDeepLinkIntent(NotificationConstants.SCREEN_JOURNAL)

        val builder = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID_SHUTDOWN)
            .setSmallIcon(android.R.drawable.ic_lock_power_off)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NotificationConstants.NOTIF_ID_SHUTDOWN, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // 5. Recovery Mode Notification
    fun showRecoveryModeNotification(
        title: String = "Recovery Mode Suggested",
        message: String = "You are falling behind. Open Recovery Mode."
    ) {
        if (!hasPermission()) return

        val contentPendingIntent = getDeepLinkIntent(NotificationConstants.SCREEN_EMERGENCY_RECOVERY)

        val builder = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID_RECOVERY)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NotificationConstants.NOTIF_ID_RECOVERY, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // 6. Weekly Review Notification
    fun showWeeklyReviewNotification(
        title: String = "Weekly Review",
        message: String = "Review your week and plan next week."
    ) {
        if (!hasPermission()) return

        val contentPendingIntent = getDeepLinkIntent(NotificationConstants.SCREEN_JOURNAL)

        val builder = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID_WEEKLY)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NotificationConstants.NOTIF_ID_WEEKLY, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // Test Notification
    fun showTestNotification() {
        if (!hasPermission()) return

        val contentPendingIntent = getDeepLinkIntent(NotificationConstants.SCREEN_SETTINGS)

        val builder = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID_STUDY_BLOCKS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Rudra Life OS Notification System")
            .setContentText("Production-grade Android notification channels and alarm triggers active.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Rudra Life OS production-grade Android notifications, channels, exact alarms, and background synchronization are fully operational."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NotificationConstants.NOTIF_ID_TEST, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun cancelAll() {
        notificationManager.cancelAll()
    }
}
