package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LetsStudyScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val letsStudyMode by viewModel.letsStudyMode.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val timerSubject by viewModel.timerSubject.collectAsState()
    val timerTopic by viewModel.timerTopic.collectAsState()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoadingWeb by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var currentUrl by remember { mutableStateOf("https://pwthor.live") }

    var showParkingLotDialog by remember { mutableStateOf(false) }
    var parkingLotNote by remember { mutableStateOf("") }
    var showTimerSaveDialog by remember { mutableStateOf(false) }
    var timerNotes by remember { mutableStateOf("") }

    val formattedTime = remember(timerSeconds) {
        val hours = timerSeconds / 3600
        val mins = (timerSeconds % 3600) / 60
        val secs = timerSeconds % 60
        if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, mins, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Focus Study Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isTimerRunning) ScoreGreen else ScoreRed)
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isTimerRunning) AccentElectricBlue else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• $timerSubject",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Start/Pause Button
                    IconButton(
                        onClick = {
                            if (isTimerRunning) viewModel.pauseTimer()
                            else viewModel.startTimer(timerSubject, timerTopic.ifBlank { "PW Lecture & Practice" })
                        },
                        modifier = Modifier.testTag("study_timer_toggle")
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Toggle Timer",
                            tint = if (isTimerRunning) ScoreYellow else ScoreGreen
                        )
                    }

                    // Stop & Save
                    if (timerSeconds > 0) {
                        IconButton(
                            onClick = { showTimerSaveDialog = true },
                            modifier = Modifier.testTag("study_timer_stop")
                        ) {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "Stop & Save Session",
                                tint = ScoreRed
                            )
                        }
                    }

                    // Quick Distraction Parking Lot Jotter
                    IconButton(
                        onClick = { showParkingLotDialog = true },
                        modifier = Modifier.testTag("study_parking_lot_quick_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.StickyNote2,
                            contentDescription = "Distraction Parking Lot",
                            tint = AccentCyan
                        )
                    }

                    // External Browser Fallback
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pwthor.live")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("open_external_browser_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Open in External Browser",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Web Loading Linear Progress
        if (isLoadingWeb && loadProgress < 100) {
            LinearProgressIndicator(
                progress = { loadProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = AccentElectricBlue
            )
        }

        // 2. Embedded Web View of PW Thor
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        settings.allowFileAccess = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoadingWeb = true
                                if (url != null) currentUrl = url
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoadingWeb = false
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString() ?: return false
                                return if (url.startsWith("http://") || url.startsWith("https://")) {
                                    false
                                } else {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        ctx.startActivity(intent)
                                    } catch (_: Exception) {
                                    }
                                    true
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                loadProgress = newProgress
                            }
                        }

                        loadUrl("https://pwthor.live")
                        webViewInstance = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Web Navigation controls overlay at bottom right
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        if (webViewInstance?.canGoBack() == true) {
                            webViewInstance?.goBack()
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("webview_back_button"),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
                }

                FloatingActionButton(
                    onClick = {
                        webViewInstance?.reload()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("webview_reload_button"),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload", modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    // 3. Quick Distraction Parking Lot Dialog (Section 12: Anti-Distraction Engine)
    if (showParkingLotDialog) {
        AlertDialog(
            onDismissRequest = { showParkingLotDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.StickyNote2, contentDescription = null, tint = AccentCyan)
                    Text("Distraction Parking Lot", style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Padhte waqt aane wale random thoughts, doubts, ya ideas yahan likh do. Brain unhe bhool jaayega aur focus lecture pe bana rahega.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = parkingLotNote,
                        onValueChange = { parkingLotNote = it },
                        placeholder = { Text("e.g. Check physics derivation later / buy pen / whatsapp friend...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parking_lot_dialog_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (parkingLotNote.isNotBlank()) {
                            viewModel.saveBrainDump(parkingLotNote, "Parking Lot (Study Distraction)")
                            parkingLotNote = ""
                        }
                        showParkingLotDialog = false
                    },
                    modifier = Modifier.testTag("save_parking_lot_dialog_button")
                ) {
                    Text("Park Thought")
                }
            },
            dismissButton = {
                TextButton(onClick = { showParkingLotDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4. Stop Study Session Dialog
    if (showTimerSaveDialog) {
        val durationMins = (timerSeconds / 60).coerceAtLeast(1)
        AlertDialog(
            onDismissRequest = { showTimerSaveDialog = false },
            title = { Text("Log Completed Study Session", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Session Duration: $durationMins minutes ($timerSubject)", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = timerNotes,
                        onValueChange = { timerNotes = it },
                        label = { Text("What did you master? (Numericals solved / topics)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.stopAndSaveTimer(timerNotes)
                        timerNotes = ""
                        showTimerSaveDialog = false
                    },
                    modifier = Modifier.testTag("confirm_save_study_session_button")
                ) {
                    Text("Save to Study Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimerSaveDialog = false }) {
                    Text("Discard / Keep Timing")
                }
            }
        )
    }
}
