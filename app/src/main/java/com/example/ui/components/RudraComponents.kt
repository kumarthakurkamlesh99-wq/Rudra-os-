package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun ScoreBadge(
    score: Int,
    maxScore: Int = 7,
    modifier: Modifier = Modifier
) {
    val (bg, textColor, label) = when {
        score >= 5 -> Triple(ScoreGreen.copy(alpha = 0.18f), ScoreGreen, "Green Day")
        score in 3..4 -> Triple(ScoreYellow.copy(alpha = 0.18f), ScoreYellow, "Yellow Day")
        else -> Triple(ScoreRed.copy(alpha = 0.18f), ScoreRed, "Red Day")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = "$score/$maxScore • $label",
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.testTag("section_action_${title.lowercase().replace(" ", "_")}")
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RudraTopAppBar(
    currentScreen: Screen,
    onOpenDrawer: () -> Unit,
    onEmergencyClick: () -> Unit,
    isLowEnergy: Boolean,
    onToggleLowEnergy: () -> Unit,
    onLetsStudyClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Rudra Life OS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentElectricBlue
                )
                Text(
                    text = currentScreen.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("hamburger_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            // Low Energy Indicator
            FilterChip(
                selected = isLowEnergy,
                onClick = onToggleLowEnergy,
                label = { Text(if (isLowEnergy) "Low Energy" else "Normal", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = if (isLowEnergy) Icons.Default.BatteryChargingFull else Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isLowEnergy) ScoreYellow else AccentElectricBlue
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ScoreYellowBg.copy(alpha = 0.4f),
                    selectedLabelColor = ScoreYellow
                ),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("low_energy_toggle_chip")
            )

            // Emergency Recovery button
            IconButton(
                onClick = onEmergencyClick,
                modifier = Modifier.testTag("emergency_recovery_top_action")
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Emergency Recovery (I am falling behind)",
                    tint = ScoreRed
                )
            }

            // Quick Study Fast Action
            IconButton(
                onClick = onLetsStudyClick,
                modifier = Modifier.testTag("quick_study_top_action")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleOutline,
                    contentDescription = "Let's Study (PW Thor)",
                    tint = AccentCyan
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
fun RudraDrawerContent(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onCloseDrawer: () -> Unit,
    isLowEnergy: Boolean,
    onToggleLowEnergy: () -> Unit,
    onEmergencyClick: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(310.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentNavy,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "R",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Rudra Life OS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Class 12 Science (BSEB)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "“Consistency > Intensity • Har din SHOWING UP chahiye”",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentElectricBlue,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val drawerItems = listOf(
                DrawerItem(Screen.Dashboard, Icons.Default.Dashboard, Icons.Outlined.Dashboard),
                DrawerItem(Screen.Subjects, Icons.Default.AutoStories, Icons.Outlined.AutoStories),
                DrawerItem(Screen.WeakChapters, Icons.Default.WarningAmber, Icons.Outlined.WarningAmber),
                DrawerItem(Screen.MockTests, Icons.Default.Quiz, Icons.Outlined.Quiz),
                DrawerItem(Screen.LectureTracker, Icons.Default.PlayCircle, Icons.Outlined.PlayCircleOutline),
                DrawerItem(Screen.AiCoach, Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome),
                DrawerItem(Screen.MissionBoard, Icons.Default.Flag, Icons.Outlined.OutlinedFlag),
                DrawerItem(Screen.Streaks, Icons.Default.LocalFireDepartment, Icons.Outlined.LocalFireDepartment),
                DrawerItem(Screen.Timeline, Icons.Default.Schedule, Icons.Outlined.Schedule),
                DrawerItem(Screen.Revision, Icons.Default.Update, Icons.Outlined.Update),
                DrawerItem(Screen.Tasks, Icons.Default.CheckCircle, Icons.Outlined.CheckCircleOutline),
                DrawerItem(Screen.LetsStudy, Icons.Default.PlayLesson, Icons.Outlined.PlayLesson),
                DrawerItem(Screen.StudySession, Icons.Default.Timer, Icons.Outlined.Timer),
                DrawerItem(Screen.Journal, Icons.Default.EditNote, Icons.Outlined.EditNote),
                DrawerItem(Screen.BrainDump, Icons.Default.TipsAndUpdates, Icons.Outlined.TipsAndUpdates),
                DrawerItem(Screen.Resources, Icons.Default.FolderSpecial, Icons.Outlined.Folder),
                DrawerItem(Screen.Analytics, Icons.Default.BarChart, Icons.Outlined.BarChart),
                DrawerItem(Screen.Scorecard, Icons.Default.FactCheck, Icons.Outlined.FactCheck),
                DrawerItem(Screen.Settings, Icons.Default.Settings, Icons.Outlined.Settings)
            )

            drawerItems.forEach { item ->
                val isSelected = currentScreen == item.screen
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.screen.title,
                            tint = if (isSelected) AccentElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            text = item.screen.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AccentElectricBlue else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        onNavigate(item.screen)
                        onCloseDrawer()
                    },
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .testTag("drawer_item_${item.screen.route}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = AccentNavy.copy(alpha = 0.16f),
                        unselectedContainerColor = Color.Transparent
                    )
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Quick bottom actions inside drawer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onEmergencyClick()
                    onCloseDrawer()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("drawer_emergency_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ScoreRedBg, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(Icons.Default.WarningAmber, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Falling Behind?", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class DrawerItem(
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
