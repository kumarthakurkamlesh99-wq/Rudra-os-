package com.example.notification

object NotificationConstants {
    // Notification Channels
    const val CHANNEL_ID_STUDY_BLOCKS = "rudra_channel_study_blocks"
    const val CHANNEL_NAME_STUDY_BLOCKS = "Study Routine Reminders"
    const val CHANNEL_DESC_STUDY_BLOCKS = "Notifications for Study Blocks 1, 3, and 5 start times"

    const val CHANNEL_ID_REVISIONS = "rudra_channel_revisions"
    const val CHANNEL_NAME_REVISIONS = "Revision Due Alerts"
    const val CHANNEL_DESC_REVISIONS = "Spaced repetition revision schedule alerts"

    const val CHANNEL_ID_TASKS = "rudra_channel_tasks"
    const val CHANNEL_NAME_TASKS = "Task Due Reminders"
    const val CHANNEL_DESC_TASKS = "Reminders before high and normal priority tasks are due"

    const val CHANNEL_ID_SHUTDOWN = "rudra_channel_shutdown"
    const val CHANNEL_NAME_SHUTDOWN = "Evening Shutdown Ritual"
    const val CHANNEL_DESC_SHUTDOWN = "Daily evening scorecard, reflection, and tomorrow preparation"

    const val CHANNEL_ID_RECOVERY = "rudra_channel_recovery"
    const val CHANNEL_NAME_RECOVERY = "Emergency Recovery Alerts"
    const val CHANNEL_DESC_RECOVERY = "Alerts suggesting Recovery Mode when falling behind"

    const val CHANNEL_ID_WEEKLY = "rudra_channel_weekly"
    const val CHANNEL_NAME_WEEKLY = "Weekly Review"
    const val CHANNEL_DESC_WEEKLY = "Sunday weekly system evaluation and planning reminder"

    // Channel aliases
    const val CHANNEL_STUDY_BLOCKS = CHANNEL_ID_STUDY_BLOCKS
    const val CHANNEL_REVISION_ALERTS = CHANNEL_ID_REVISIONS
    const val CHANNEL_TASK_REMINDERS = CHANNEL_ID_TASKS
    const val CHANNEL_RITUALS_REVIEWS = CHANNEL_ID_SHUTDOWN
    const val CHANNEL_RECOVERY_MODE = CHANNEL_ID_RECOVERY

    // Actions
    const val ACTION_START_STUDY = "com.example.rudra.ACTION_START_STUDY"
    const val ACTION_SNOOZE_15_MIN = "com.example.rudra.ACTION_SNOOZE_15_MIN"
    const val ACTION_SKIP_BLOCK = "com.example.rudra.ACTION_SKIP_BLOCK"
    const val ACTION_ALARM_TRIGGER = "com.example.rudra.ACTION_ALARM_TRIGGER"
    const val ACTION_TEST_NOTIFICATION = "com.example.rudra.ACTION_TEST_NOTIFICATION"

    // Extras
    const val EXTRA_ALARM_TYPE = "extra_alarm_type"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_BLOCK_NUMBER = "extra_block_number"
    const val EXTRA_BLOCK_TITLE = "extra_block_title"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_TARGET_SCREEN = "extra_target_screen"

    // Alarm Types
    const val TYPE_BLOCK_1 = "TYPE_BLOCK_1"
    const val TYPE_BLOCK_3 = "TYPE_BLOCK_3"
    const val TYPE_BLOCK_5 = "TYPE_BLOCK_5"
    const val TYPE_REVISION = "TYPE_REVISION"
    const val TYPE_TASK = "TYPE_TASK"
    const val TYPE_SHUTDOWN = "TYPE_SHUTDOWN"
    const val TYPE_RECOVERY = "TYPE_RECOVERY"
    const val TYPE_WEEKLY = "TYPE_WEEKLY"
    const val TYPE_SNOOZE = "TYPE_SNOOZE"

    // Notification IDs
    const val NOTIF_ID_BLOCK_1 = 1001
    const val NOTIF_ID_BLOCK_3 = 1003
    const val NOTIF_ID_BLOCK_5 = 1005
    const val NOTIF_ID_REVISION = 2001
    const val NOTIF_ID_SHUTDOWN = 3001
    const val NOTIF_ID_RECOVERY = 4001
    const val NOTIF_ID_WEEKLY = 5001
    const val NOTIF_ID_TEST = 9999
    const val NOTIF_ID_TASK_BASE = 6000

    // Target Screen Identifiers for Deep Linking
    const val SCREEN_DASHBOARD = "DASHBOARD"
    const val SCREEN_LETS_STUDY = "LETS_STUDY"
    const val SCREEN_REVISION = "REVISION"
    const val SCREEN_TASKS = "TASKS"
    const val SCREEN_JOURNAL = "JOURNAL"
    const val SCREEN_SCORECARD = "SCORECARD"
    const val SCREEN_EMERGENCY_RECOVERY = "EMERGENCY_RECOVERY"
    const val SCREEN_SETTINGS = "SETTINGS"
    const val SCREEN_TIMELINE = "TIMELINE"
}
