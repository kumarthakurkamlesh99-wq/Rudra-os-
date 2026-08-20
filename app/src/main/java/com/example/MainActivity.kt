package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.notification.NotificationConstants
import com.example.ui.components.RudraDrawerContent
import com.example.ui.components.RudraTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.RudraTheme
import com.example.ui.viewmodel.RudraViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: RudraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationDeepLink(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            // Request Notification Permission on Android 13+ (API 33+)
            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    viewModel.setNotificationsEnabled(true)
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            RudraTheme(darkTheme = isDarkTheme) {
                RudraLifeOsApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationDeepLink(intent)
    }

    private fun handleNotificationDeepLink(intent: Intent?) {
        val target = intent?.getStringExtra(NotificationConstants.EXTRA_TARGET_SCREEN) ?: return
        when (target) {
            NotificationConstants.SCREEN_LETS_STUDY -> viewModel.navigateTo(Screen.LetsStudy)
            NotificationConstants.SCREEN_REVISION -> viewModel.navigateTo(Screen.Revision)
            NotificationConstants.SCREEN_TASKS -> viewModel.navigateTo(Screen.Tasks)
            NotificationConstants.SCREEN_JOURNAL -> viewModel.navigateTo(Screen.Journal)
            NotificationConstants.SCREEN_SCORECARD -> viewModel.navigateTo(Screen.Scorecard)
            NotificationConstants.SCREEN_EMERGENCY_RECOVERY -> viewModel.navigateTo(Screen.EmergencyRecovery)
            NotificationConstants.SCREEN_SETTINGS -> viewModel.navigateTo(Screen.Settings)
            NotificationConstants.SCREEN_TIMELINE -> viewModel.navigateTo(Screen.Timeline)
            NotificationConstants.SCREEN_DASHBOARD -> viewModel.navigateTo(Screen.Dashboard)
        }
    }
}

@Composable
fun RudraLifeOsApp(viewModel: RudraViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isLowEnergy by viewModel.isLowEnergyMode.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RudraDrawerContent(
                currentScreen = currentScreen,
                onNavigate = { screen -> viewModel.navigateTo(screen) },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                },
                isLowEnergy = isLowEnergy,
                onToggleLowEnergy = { viewModel.toggleLowEnergyMode() },
                onEmergencyClick = { viewModel.navigateTo(Screen.EmergencyRecovery) }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                RudraTopAppBar(
                    currentScreen = currentScreen,
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onEmergencyClick = {
                        viewModel.navigateTo(Screen.EmergencyRecovery)
                    },
                    isLowEnergy = isLowEnergy,
                    onToggleLowEnergy = {
                        viewModel.toggleLowEnergyMode()
                    },
                    onLetsStudyClick = {
                        viewModel.navigateTo(Screen.LetsStudy)
                    }
                )
            }
        ) { innerPadding ->
            val modifier = Modifier.padding(innerPadding)
            when (currentScreen) {
                is Screen.Dashboard -> DashboardScreen(viewModel = viewModel, modifier = modifier)
                is Screen.Timeline -> TimelineScreen(viewModel = viewModel, modifier = modifier)
                is Screen.LetsStudy -> LetsStudyScreen(viewModel = viewModel, modifier = modifier)
                is Screen.Subjects -> SubjectsScreen(viewModel = viewModel, modifier = modifier)
                is Screen.Revision -> RevisionScreen(viewModel = viewModel, modifier = modifier)
                is Screen.Tasks -> TasksScreen(viewModel = viewModel, modifier = modifier)
                is Screen.StudySession -> StudySessionScreen(viewModel = viewModel, modifier = modifier)
                is Screen.Journal -> JournalScreen(viewModel = viewModel, modifier = modifier)
                is Screen.BrainDump -> BrainDumpScreen(viewModel = viewModel, modifier = modifier)
                is Screen.Resources -> ResourceVaultScreen(viewModel = viewModel, modifier = modifier)
                is Screen.Analytics -> AnalyticsScreen(viewModel = viewModel, modifier = modifier)
                is Screen.Scorecard -> ScorecardScreen(viewModel = viewModel, modifier = modifier)
                is Screen.EmergencyRecovery -> EmergencyRecoveryScreen(viewModel = viewModel, modifier = modifier)
                is Screen.Settings -> SettingsScreen(viewModel = viewModel, modifier = modifier)
            }
        }
    }
}

