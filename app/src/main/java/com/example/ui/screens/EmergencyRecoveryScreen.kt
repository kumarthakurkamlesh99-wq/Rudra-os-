package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import kotlinx.coroutines.launch

@Composable
fun EmergencyRecoveryScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allPresets by viewModel.allPresets.collectAsState()

    var mvdWakeup by remember { mutableStateOf(false) }
    var mvd20MinStudy by remember { mutableStateOf(false) }
    var mvd5MinStretch by remember { mutableStateOf(false) }
    var mvdShutdownDone by remember { mutableStateOf(false) }

    val mvdProgress = listOf(mvdWakeup, mvd20MinStudy, mvd5MinStretch, mvdShutdownDone).count { it }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Red Emergency Alert Banner
        item {
            GlassCard(
                backgroundColor = ScoreRedBg.copy(alpha = 0.2f),
                borderColor = ScoreRed.copy(alpha = 0.5f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = ScoreRed, modifier = Modifier.size(32.dp))
                    Column {
                        Text(
                            text = "EMERGENCY RECOVERY PROTOCOL",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ScoreRed
                        )
                        Text(
                            text = "Feeling overwhelmed or broke a streak? Do NOT spiral into guilt. Activate the 3-step reset right now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // The Never-Miss-Twice Rule Card
        item {
            GlassCard {
                Text(
                    text = "THE NEVER-MISS-TWICE RULE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentElectricBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "“1 din miss hona accident hai — human ho. 2 din miss hona ek naya habit shuru karta hai. Agar kal bilkul nahi padha ya schedule toot gaya, aaj sirf Minimum Viable Day complete karo.”",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }

        // 3-Step Reset Protocol
        item {
            SectionHeader(title = "3-Step Streak Break Reset")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("STEP 1: NO-GUILT AUDIT", style = MaterialTheme.typography.labelLarge, color = AccentElectricBlue, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("No self-blame or self-hatred. Schedule tuta? Note karo: Exactly kya hua? (Phone trigger, sleep shortage, outside work). Bass. Audit complete.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("STEP 2: RUN MINIMUM VIABLE DAY (MVD)", style = MaterialTheme.typography.labelLarge, color = ScoreYellow, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("6 ghante padhne ki koshish mat karo. Sirf 20 minute book kholo aur 1 numerical solve karo. Zero Day toot gaya.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("STEP 3: NEXT DAY NORMAL (NO OVERCOMPENSATION)", style = MaterialTheme.typography.labelLarge, color = ScoreGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Kal ka loss cover karne ke liye 12 ghante padhne ka plan mat banao (wo fir fail hoga). Kal standard School Day routine pe normal start karo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Interactive Minimum Viable Day Checklist
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MINIMUM VIABLE DAY CHECKLIST ($mvdProgress/4)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentElectricBlue
                    )
                    if (mvdProgress == 4) {
                        Text("MVD Complete! Zero Broken", style = MaterialTheme.typography.labelSmall, color = ScoreGreen, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                MvdCheckRow(label = "1. Wake up by 6:30 AM (Hard Cap)", checked = mvdWakeup, onToggle = { mvdWakeup = it })
                MvdCheckRow(label = "2. One 20-min Study Block (1 formula / 1 passage)", checked = mvd20MinStudy, onToggle = { mvd20MinStudy = it })
                MvdCheckRow(label = "3. 5-min Light Movement / Stretches", checked = mvd5MinStretch, onToggle = { mvd5MinStretch = it })
                MvdCheckRow(label = "4. Evening Shutdown (1-line journal + tomorrow Block 1)", checked = mvdShutdownDone, onToggle = { mvdShutdownDone = it })

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val emergencyPreset = allPresets.find { it.name.contains("Emergency", ignoreCase = true) }
                            if (emergencyPreset != null) {
                                viewModel.repository.activatePreset(emergencyPreset.id)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activate_emergency_mvd_routine_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ScoreRedBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Activate MVD Emergency Routine Now")
                }
            }
        }
    }
}

@Composable
fun MvdCheckRow(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (checked) ScoreGreen else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Checkbox(checked = checked, onCheckedChange = onToggle)
    }
}
