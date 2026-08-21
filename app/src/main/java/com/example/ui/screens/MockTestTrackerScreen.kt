package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MockTestEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MockTestTrackerScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val allMockTests by viewModel.allMockTests.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSubjectFilter by remember { mutableStateOf("All") }

    val filteredTests = remember(allMockTests, selectedSubjectFilter) {
        if (selectedSubjectFilter == "All") allMockTests else allMockTests.filter { it.subject.equals(selectedSubjectFilter, ignoreCase = true) }
    }

    // Calculations
    val totalTests = allMockTests.size
    val avgScore = if (allMockTests.isNotEmpty()) allMockTests.map { it.percentage }.average() else 0.0
    val physicsAvg = allMockTests.filter { it.subject == "Physics" }.map { it.percentage }.let { if (it.isNotEmpty()) it.average() else 0.0 }
    val chemistryAvg = allMockTests.filter { it.subject == "Chemistry" }.map { it.percentage }.let { if (it.isNotEmpty()) it.average() else 0.0 }
    val biologyAvg = allMockTests.filter { it.subject == "Biology" }.map { it.percentage }.let { if (it.isNotEmpty()) it.average() else 0.0 }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        // Overview Card with Score Analytics
        item {
            GlassCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Mock Test Performance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$totalTests Tests Attempted • Target: 85%+",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                avgScore >= 75 -> ScoreGreen.copy(alpha = 0.2f)
                                avgScore >= 50 -> ScoreYellow.copy(alpha = 0.2f)
                                else -> ScoreRed.copy(alpha = 0.2f)
                            },
                            border = BorderStroke(1.dp, if (avgScore >= 75) ScoreGreen else if (avgScore >= 50) ScoreYellow else ScoreRed)
                        ) {
                            Text(
                                text = "${avgScore.toInt()}% Avg",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (avgScore >= 75) ScoreGreen else if (avgScore >= 50) ScoreYellow else ScoreRed,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Subject Breakdown Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubjectAverageBadge("Physics", physicsAvg, AccentElectricBlue, Modifier.weight(1f))
                        SubjectAverageBadge("Chemistry", chemistryAvg, ScoreGreen, Modifier.weight(1f))
                        SubjectAverageBadge("Biology", biologyAvg, Color(0xFFAB47BC), Modifier.weight(1f))
                    }
                }
            }
        }

        // Performance Trend Chart
        if (allMockTests.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Score Trend Over Time (%)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        MockPerformanceChart(
                            tests = allMockTests.reversed().takeLast(10),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }
                }
            }
        }

        // Filter and Add Test Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(listOf("All", "Physics", "Chemistry", "Biology")) { subject ->
                        FilterChip(
                            selected = selectedSubjectFilter == subject,
                            onClick = { selectedSubjectFilter = subject },
                            label = { Text(subject, fontSize = 11.sp) }
                        )
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Test", fontSize = 12.sp)
                }
            }
        }

        // Mock Tests History List
        if (filteredTests.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(36.dp))
                        Text("No Mock Tests Logged Yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Tap 'Log Test' to record Allen, PW, or Board sample paper scores with mistake analysis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredTests, key = { it.id }) { test ->
                MockTestCard(
                    test = test,
                    onDelete = { viewModel.deleteMockTest(test.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddMockTestDialog(
            onDismiss = { showAddDialog = false },
            onSave = { subject, chapter, name, marks, totalMarks, date, notes ->
                viewModel.addMockTest(subject, chapter, name, marks, totalMarks, date, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SubjectAverageBadge(subject: String, avg: Double, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(subject, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text("${avg.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun MockTestCard(
    test: MockTestEntity,
    onDelete: () -> Unit
) {
    val (badgeColor, badgeLabel) = when (test.performanceBadge) {
        "GREEN" -> Pair(ScoreGreen, "Strong (>75%)")
        "YELLOW" -> Pair(ScoreYellow, "Average (50-75%)")
        else -> Pair(ScoreRed, "Critical (<50%)")
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (test.subject) {
                                "Physics" -> AccentElectricBlue.copy(alpha = 0.2f)
                                "Chemistry" -> ScoreGreen.copy(alpha = 0.2f)
                                "Biology" -> Color(0xFFAB47BC).copy(alpha = 0.2f)
                                else -> AccentCyan.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = test.subject,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (test.subject) {
                                    "Physics" -> AccentElectricBlue
                                    "Chemistry" -> ScoreGreen
                                    "Biology" -> Color(0xFFAB47BC)
                                    else -> AccentCyan
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = test.testDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = test.testName + if (test.chapter.isNotBlank()) " • ${test.chapter}" else "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${test.marksObtained.toInt()}/${test.totalMarks.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                        Text(
                            text = "${test.percentage.toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Test", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            if (test.notes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mistake Analysis: ${test.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MockPerformanceChart(
    tests: List<MockTestEntity>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.padding(vertical = 8.dp)) {
        if (tests.isEmpty()) return@Canvas
        val width = size.width
        val height = size.height
        val spacing = width / (tests.size + 1).coerceAtLeast(1)

        val path = Path()
        val points = mutableListOf<Offset>()

        tests.forEachIndexed { index, test ->
            val x = spacing * (index + 1)
            val y = height - (test.percentage.toFloat() / 100f * height).coerceIn(0f, height)
            val point = Offset(x, y)
            points.add(point)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw connecting line
        drawPath(
            path = path,
            color = AccentElectricBlue,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw points and target threshold (85%)
        val targetY = height - (0.85f * height)
        drawLine(
            color = ScoreGreen.copy(alpha = 0.4f),
            start = Offset(0f, targetY),
            end = Offset(width, targetY),
            strokeWidth = 1.dp.toPx()
        )

        points.forEachIndexed { idx, point ->
            val pct = tests[idx].percentage
            val ptColor = if (pct >= 75) ScoreGreen else if (pct >= 50) ScoreYellow else ScoreRed
            drawCircle(
                color = ptColor,
                radius = 5.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun AddMockTestDialog(
    onDismiss: () -> Unit,
    onSave: (subject: String, chapter: String, name: String, marks: Double, totalMarks: Double, date: String, notes: String) -> Unit
) {
    var subject by remember { mutableStateOf("Physics") }
    var chapter by remember { mutableStateOf("") }
    var testName by remember { mutableStateOf("") }
    var marks by remember { mutableStateOf("") }
    var totalMarks by remember { mutableStateOf("70") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Mock Test Result") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Physics", "Chemistry", "Biology", "Full PCB").forEach { s ->
                        FilterChip(
                            selected = subject == s,
                            onClick = { subject = s },
                            label = { Text(s, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = testName,
                    onValueChange = { testName = it },
                    label = { Text("Test Name (e.g. PW Yakeen Minor 1 / Allen)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter / Syllabus Coverage") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = marks,
                        onValueChange = { marks = it },
                        label = { Text("Marks Scored") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalMarks,
                        onValueChange = { totalMarks = it },
                        label = { Text("Total Marks") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Mistake Analysis / Silly errors") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = marks.toDoubleOrNull() ?: 0.0
                    val tm = totalMarks.toDoubleOrNull() ?: 70.0
                    if (testName.isNotBlank()) {
                        onSave(subject, chapter, testName, m, tm, date, notes)
                    }
                }
            ) {
                Text("Save Mock Test")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
