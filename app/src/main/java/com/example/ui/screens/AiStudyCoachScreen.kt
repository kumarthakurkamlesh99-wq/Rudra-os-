package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ChapterEntity
import com.example.data.model.*
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.PdfExamExporter
import kotlinx.coroutines.launch

@Composable
fun AiStudyCoachScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Test Generator", "Oral Viva", "Doubt Solver", "Weakness Radar", "Board Predictor")
    val tabIcons = listOf(
        Icons.Default.Quiz,
        Icons.Default.RecordVoiceOver,
        Icons.Default.HelpOutline,
        Icons.Default.Radar,
        Icons.Default.AutoAwesome
    )

    val apiWarning by viewModel.apiStatusWarning.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = AccentElectricBlue,
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
                    icon = { Icon(tabIcons[index], contentDescription = title, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("ai_coach_tab_$index")
                )
            }
        }

        // Persistent API Status Warning Strip
        if (apiWarning != null && apiWarning?.isWarning == true) {
            Surface(
                color = when (apiWarning?.severity) {
                    ApiWarningSeverity.ERROR -> ScoreRed.copy(alpha = 0.15f)
                    ApiWarningSeverity.WARNING -> WarningOrange.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.Settings) }
                    .testTag("ai_coach_api_warning_strip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = when (apiWarning?.severity) {
                            ApiWarningSeverity.ERROR -> Icons.Default.Error
                            ApiWarningSeverity.WARNING -> Icons.Default.Warning
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (apiWarning?.severity) {
                            ApiWarningSeverity.ERROR -> ScoreRed
                            ApiWarningSeverity.WARNING -> WarningOrange
                            else -> AccentElectricBlue
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${apiWarning?.title}: ${apiWarning?.message}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Settings →",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentElectricBlue,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> AiTestGeneratorTab(viewModel = viewModel, onNavigateToWeakness = { selectedTab = 3 })
                1 -> AiOralVivaTab(viewModel = viewModel)
                2 -> AiDoubtSolverTab(viewModel = viewModel)
                3 -> AiWeaknessRadarTab(viewModel = viewModel, onStartAdaptiveTest = { selectedTab = 0 })
                4 -> AiBoardPredictorTab(viewModel = viewModel)
            }
        }
    }
}

// ===================================================================
// TAB 1: AI TEST GENERATOR & INTERACTIVE EXAM ENGINE
// ===================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiTestGeneratorTab(
    viewModel: RudraViewModel,
    onNavigateToWeakness: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val generatedTest by viewModel.generatedTest.collectAsState()
    val isTestGenerating by viewModel.isTestGenerating.collectAsState()
    val isTestSubmitting by viewModel.isTestSubmitting.collectAsState()
    val userAnswers by viewModel.testUserAnswers.collectAsState()
    val allChapters by viewModel.allChapters.collectAsState()
    val geminiApiStatus by viewModel.geminiApiStatus.collectAsState()

    var selectedSubject by remember { mutableStateOf("Physics") }
    var selectedScope by remember { mutableStateOf("Single Chapter") }
    var selectedDifficulty by remember { mutableStateOf("Bihar Board Level") }
    var selectedChapter by remember { mutableStateOf("") }
    var selectedChaptersMulti by remember { mutableStateOf<Set<String>>(emptySet()) }
    var questionCount by remember { mutableIntStateOf(20) }
    var saveStatusMsg by remember { mutableStateOf<String?>(null) }
    var outputFormatTab by remember { mutableIntStateOf(0) } // 0: Interactive, 1: PDF, 2: Text

    val subjectChapters = remember(selectedSubject, allChapters) {
        val subId = when (selectedSubject) {
            "Physics" -> 1L
            "Chemistry" -> 2L
            "Biology" -> 3L
            else -> 1L
        }
        allChapters.filter { it.subjectId == subId }
    }

    LaunchedEffect(subjectChapters) {
        if (selectedChapter.isBlank() && subjectChapters.isNotEmpty()) {
            selectedChapter = subjectChapters.first().title
        }
        if (selectedChaptersMulti.isEmpty() && subjectChapters.size >= 2) {
            selectedChaptersMulti = setOf(subjectChapters[0].title, subjectChapters[1].title)
        }
    }

    if (generatedTest == null) {
        // Test Configuration Form
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                GlassCard(
                    backgroundColor = AccentNavy.copy(alpha = 0.45f),
                    borderColor = AccentElectricBlue.copy(alpha = 0.5f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = AccentElectricBlue)
                                Text(
                                    "AI Examination Engine",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (geminiApiStatus == "CONNECTED") ScoreGreen.copy(alpha = 0.2f) else ScoreYellow.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (geminiApiStatus == "CONNECTED") "GEMINI 2.5 ACTIVE" else "STANDARD TEMPLATES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (geminiApiStatus == "CONNECTED") ScoreGreen else ScoreYellow,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Text(
                            "Generates balanced Board Examination papers with MCQs, Assertion-Reason, Short & Long Answers, Derivations, Numericals, Diagrams, and Case Studies.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 1. Select Subject
            item {
                Text("1. Select Subject", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Physics", "Chemistry", "Biology").forEach { sub ->
                        FilterChip(
                            selected = selectedSubject == sub,
                            onClick = {
                                selectedSubject = sub
                                val subId = if (sub == "Physics") 1L else if (sub == "Chemistry") 2L else 3L
                                val chs = allChapters.filter { it.subjectId == subId }
                                selectedChapter = chs.firstOrNull()?.title ?: ""
                                selectedChaptersMulti = chs.take(2).map { it.title }.toSet()
                            },
                            label = { Text(sub, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Select Scope
            item {
                Text("2. Exam Scope", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Single Chapter", "Multiple Chapters", "Full Subject", "Full PCB Mock Test").forEach { sc ->
                        FilterChip(
                            selected = selectedScope == sc,
                            onClick = { selectedScope = sc },
                            label = { Text(sc, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // 3. Select Target Chapter(s) based on Scope
            if (selectedScope == "Single Chapter" && subjectChapters.isNotEmpty()) {
                item {
                    Text("3. Target Chapter", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedChapter.ifBlank { "Select Chapter" }, maxLines = 1, fontSize = 12.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            subjectChapters.forEach { ch ->
                                DropdownMenuItem(
                                    text = { Text("${ch.chapterNumber}. ${ch.title}", fontSize = 12.sp) },
                                    onClick = {
                                        selectedChapter = ch.title
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            } else if (selectedScope == "Multiple Chapters" && subjectChapters.isNotEmpty()) {
                item {
                    Text("3. Choose Multiple Chapters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        subjectChapters.forEach { ch ->
                            val isSelected = selectedChaptersMulti.contains(ch.title)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AccentElectricBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, if (isSelected) AccentElectricBlue else Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val mutable = selectedChaptersMulti.toMutableSet()
                                        if (isSelected) mutable.remove(ch.title) else mutable.add(ch.title)
                                        selectedChaptersMulti = mutable
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            val mutable = selectedChaptersMulti.toMutableSet()
                                            if (checked) mutable.add(ch.title) else mutable.remove(ch.title)
                                            selectedChaptersMulti = mutable
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${ch.chapterNumber}. ${ch.title}", fontSize = 12.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Difficulty Level
            item {
                Text("4. Board & Difficulty Level", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val difficulties = listOf(
                        "Bihar Board Level" to ScoreGreen,
                        "Bihar Board Advanced" to AccentElectricBlue,
                        "CBSE Level" to ScoreYellow,
                        "CBSE Advanced" to ScoreRed
                    )
                    difficulties.forEach { (diff, _) ->
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = diff },
                            label = { Text(diff, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            // 5. Number of Questions (10 to 70)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("5. Number of Questions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("$questionCount Questions (${questionCount * 3} Mins)", fontWeight = FontWeight.Bold, color = AccentElectricBlue, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(10, 20, 30, 40, 50, 60, 70).forEach { count ->
                        FilterChip(
                            selected = questionCount == count,
                            onClick = { questionCount = count },
                            label = { Text("$count", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Mixed Question Pattern Indicator
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Text("Mixed Question Paper Blueprint", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentCyan)
                        }
                        Text(
                            "Includes: MCQs • Assertion-Reason • Very Short (1M) • Short Answers (2M) • Long Derivations (5M) • Numericals • Diagrams • Case-Based Studies.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Generate Button
            item {
                Button(
                    onClick = {
                        val chaptersToInclude = when (selectedScope) {
                            "Single Chapter" -> if (selectedChapter.isNotBlank()) listOf(selectedChapter) else subjectChapters.take(1).map { it.title }
                            "Multiple Chapters" -> if (selectedChaptersMulti.isNotEmpty()) selectedChaptersMulti.toList() else subjectChapters.take(3).map { it.title }
                            else -> emptyList()
                        }

                        viewModel.generateTestPaper(
                            subject = selectedSubject,
                            chapters = chaptersToInclude,
                            mode = selectedScope,
                            difficulty = selectedDifficulty,
                            questionCount = questionCount
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_generate_test"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isTestGenerating
                ) {
                    if (isTestGenerating) {
                        CircularProgressIndicator(color = DarkNavyBg, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Drafting Board Exam Paper ($selectedDifficulty)...", color = DarkNavyBg, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = DarkNavyBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("⚡ Generate $questionCount-Question AI Paper", fontWeight = FontWeight.Bold, color = DarkNavyBg)
                    }
                }
            }
        }
    } else {
        // Active or Evaluated Test View with 3 Output Formats
        val test = generatedTest!!
        var currentQuestionIndex by remember { mutableIntStateOf(0) }
        val currentQ = test.questions.getOrNull(currentQuestionIndex) ?: test.questions.first()
        val paperText = remember(test) { PdfExamExporter.generateQuestionPaperText(test) }
        val answerText = remember(test) { PdfExamExporter.generateAnswerKeyText(test) }
        var showAnswersInText by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Test Header
            item {
                GlassCard(
                    backgroundColor = if (test.isSubmitted) ScoreGreen.copy(alpha = 0.15f) else AccentNavy.copy(alpha = 0.35f),
                    borderColor = if (test.isSubmitted) ScoreGreen.copy(alpha = 0.5f) else AccentElectricBlue.copy(alpha = 0.4f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    test.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Total Marks: ${test.totalMarks.toInt()} • Questions: ${test.questions.size} • Time: ${test.timeLimitMinutes} mins • ${test.difficulty}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            TextButton(
                                onClick = { viewModel.clearActiveTest() },
                                colors = ButtonDefaults.textButtonColors(contentColor = ScoreRed)
                            ) {
                                Text("New Test", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (test.isSubmitted) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Score Awarded", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            "${"%.1f".format(test.totalObtainedMarks)} / ${"%.1f".format(test.totalMarks)} (${"%.0f".format(if (test.totalMarks > 0) (test.totalObtainedMarks / test.totalMarks) * 100 else 0.0)}%)",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (test.totalObtainedMarks / test.totalMarks >= 0.75) ScoreGreen else if (test.totalObtainedMarks / test.totalMarks >= 0.5) ScoreYellow else ScoreRed
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.saveTestResultToMockHistory()
                                            saveStatusMsg = "Score Logged!"
                                            Toast.makeText(context, "Saved to Mock Test History!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(saveStatusMsg ?: "Save to Mocks", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Output Format Switcher (Format 1: Interactive, Format 2: PDF, Format 3: Text)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val formats = listOf("📱 Interactive", "📄 PDF Paper", "📝 Text Version")
                    formats.forEachIndexed { idx, label ->
                        FilterChip(
                            selected = outputFormatTab == idx,
                            onClick = { outputFormatTab = idx },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Render Output Format based on selection
            when (outputFormatTab) {
                0 -> {
                    // FORMAT 1: INTERACTIVE TEST
                    // Question Navigator Chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(test.questions) { q ->
                                val isSelected = test.questions.indexOf(q) == currentQuestionIndex
                                val hasAnswered = userAnswers[q.id]?.isNotBlank() == true || q.userAnswer.isNotBlank()
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) AccentElectricBlue else if (hasAnswered) ScoreGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (isSelected) AccentElectricBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clickable { currentQuestionIndex = test.questions.indexOf(q) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "${test.questions.indexOf(q) + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) DarkNavyBg else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Question Detail Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AccentCyan.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            "Q${currentQuestionIndex + 1} • ${currentQ.type.displayName}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentCyan,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text("${currentQ.marks.toInt()} Mark(s)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ScoreYellow)
                                }

                                Text(
                                    currentQ.questionText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 22.sp
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                                if (!test.isSubmitted) {
                                    // User Answer Input Area
                                    if (currentQ.options.isNotEmpty()) {
                                        // MCQ or Assertion-Reason Radio Options
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            currentQ.options.forEach { opt ->
                                                val optLetter = opt.take(2).trim()
                                                val isChosen = userAnswers[currentQ.id]?.contains(optLetter) == true
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isChosen) AccentElectricBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                    border = BorderStroke(1.dp, if (isChosen) AccentElectricBlue else Color.Transparent),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { viewModel.updateTestAnswer(currentQ.id, opt) }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        RadioButton(
                                                            selected = isChosen,
                                                            onClick = { viewModel.updateTestAnswer(currentQ.id, opt) }
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(opt, style = MaterialTheme.typography.bodyMedium)
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Written Response Field
                                        OutlinedTextField(
                                            value = userAnswers[currentQ.id] ?: "",
                                            onValueChange = { viewModel.updateTestAnswer(currentQ.id, it) },
                                            label = { Text("Write your answer / derivation / numerical calculation") },
                                            placeholder = { Text("Enter your solution steps here...") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 120.dp),
                                            maxLines = 8
                                        )
                                    }
                                } else {
                                    // Evaluated View
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (currentQ.obtainedMarks >= currentQ.marks) ScoreGreen.copy(alpha = 0.15f) else ScoreRed.copy(alpha = 0.15f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Score Awarded: ${String.format("%.1f", currentQ.obtainedMarks)} / ${currentQ.marks.toInt()}", fontWeight = FontWeight.Bold)
                                                Text(if (currentQ.obtainedMarks >= currentQ.marks) "FULL MARKS" else "NEEDS REVISION", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }

                                        Text("Your Answer:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(currentQ.userAnswer.ifBlank { "[Not Attempted]" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                        Text("Model Board Answer:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ScoreGreen)
                                        Text(currentQ.correctAnswer, style = MaterialTheme.typography.bodySmall)

                                        if (currentQ.stepByStepSolution.isNotBlank()) {
                                            Text("Step-by-Step Solution:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentCyan)
                                            Text(currentQ.stepByStepSolution, style = MaterialTheme.typography.bodySmall)
                                        }

                                        if (currentQ.markingScheme.isNotBlank()) {
                                            Text("Marking Scheme Breakdown:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ScoreYellow)
                                            Text(currentQ.markingScheme, style = MaterialTheme.typography.bodySmall)
                                        }

                                        if (currentQ.aiEvaluation.isNotBlank()) {
                                            Text("AI Examiner Evaluation:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentElectricBlue)
                                            Text(currentQ.aiEvaluation, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Navigation & Submit Bar
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentQuestionIndex > 0) {
                                OutlinedButton(
                                    onClick = { currentQuestionIndex -= 1 },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Previous")
                                }
                            }

                            if (currentQuestionIndex < test.questions.size - 1) {
                                Button(
                                    onClick = { currentQuestionIndex += 1 },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Next Question")
                                }
                            } else if (!test.isSubmitted) {
                                Button(
                                    onClick = { viewModel.submitTestPaper() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ScoreGreen),
                                    enabled = !isTestSubmitting
                                ) {
                                    if (isTestSubmitting) {
                                        CircularProgressIndicator(color = DarkNavyBg, modifier = Modifier.size(18.dp))
                                    } else {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DarkNavyBg)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Submit & Evaluate", color = DarkNavyBg, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // FORMAT 2: PDF QUESTION PAPER & ANSWER KEY
                    item {
                        GlassCard {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = ScoreRed, modifier = Modifier.size(24.dp))
                                    Column {
                                        Text("Downloadable PDF Question Paper", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("Formatted for print with Board header, marks, time limit, and instructions.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                                // Paper Metadata Summary
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("• Test Name: ${test.title}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text("• Subject: ${test.subject} | Scope: ${test.mode}", fontSize = 12.sp)
                                        Text("• Difficulty: ${test.difficulty}", fontSize = 12.sp)
                                        Text("• Total Questions: ${test.questions.size} | Max Marks: ${test.totalMarks.toInt()}", fontSize = 12.sp)
                                        Text("• Time Limit: ${test.timeLimitMinutes} Minutes", fontSize = 12.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        try {
                                            val pdfFile = PdfExamExporter.generateQuestionPaperPdf(context, test)
                                            PdfExamExporter.sharePdf(context, pdfFile, "${test.title} - Question Paper")
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "PDF export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = DarkNavyBg)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download / Share Question Paper PDF", color = DarkNavyBg, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val pdfFile = PdfExamExporter.generateAnswerKeyPdf(context, test)
                                            PdfExamExporter.sharePdf(context, pdfFile, "${test.title} - Model Answer Key")
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "PDF export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download Model Answer Key PDF")
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // FORMAT 3: TEXT VERSION
                    item {
                        GlassCard {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Notes, contentDescription = null, tint = AccentCyan)
                                        Text("Complete Question Paper Text", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = {
                                            val textToCopy = if (showAnswersInText) answerText else paperText
                                            clipboardManager.setText(AnnotatedString(textToCopy))
                                            Toast.makeText(context, "Question Paper copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Text", modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(onClick = {
                                            val textToShare = if (showAnswersInText) answerText else paperText
                                            val sendIntent = android.content.Intent().apply {
                                                action = android.content.Intent.ACTION_SEND
                                                putExtra(android.content.Intent.EXTRA_TEXT, textToShare)
                                                type = "text/plain"
                                            }
                                            context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Question Paper Text"))
                                        }) {
                                            Icon(Icons.Default.Share, contentDescription = "Share Text", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = !showAnswersInText,
                                        onClick = { showAnswersInText = false },
                                        label = { Text("Questions Only", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = showAnswersInText,
                                        onClick = { showAnswersInText = true },
                                        label = { Text("Questions + Answer Key", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (showAnswersInText) answerText else paperText,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===================================================================
// TAB 2: AI ORAL VIVA MODE
// ===================================================================
@Composable
fun AiOralVivaTab(viewModel: RudraViewModel) {
    val vivaSession by viewModel.vivaSession.collectAsState()
    val isVivaLoading by viewModel.isVivaLoading.collectAsState()
    val allChapters by viewModel.allChapters.collectAsState()

    var selectedSubject by remember { mutableStateOf("Physics") }
    var selectedChapter by remember { mutableStateOf("") }
    var userAnswerInput by remember { mutableStateOf("") }

    val subjectChapters = remember(selectedSubject, allChapters) {
        val subId = if (selectedSubject == "Physics") 1L else if (selectedSubject == "Chemistry") 2L else 3L
        allChapters.filter { it.subjectId == subId }
    }

    LaunchedEffect(subjectChapters) {
        if (selectedChapter.isBlank() && subjectChapters.isNotEmpty()) {
            selectedChapter = subjectChapters.first().title
        }
    }

    if (vivaSession == null) {
        // Start Viva Screen
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                GlassCard(
                    backgroundColor = AccentNavy.copy(alpha = 0.35f),
                    borderColor = AccentElectricBlue.copy(alpha = 0.5f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = AccentElectricBlue)
                            Text(
                                "AI Oral Viva Examiner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "Simulates strict Board Practical / Viva Voce examination. AI asks conceptual questions 1-by-1 and grades your oral presentation on Correctness, Missing Points, and Board Quality.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text("Select Subject", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Physics", "Chemistry", "Biology").forEach { sub ->
                        FilterChip(
                            selected = selectedSubject == sub,
                            onClick = {
                                selectedSubject = sub
                                val subId = if (sub == "Physics") 1L else if (sub == "Chemistry") 2L else 3L
                                selectedChapter = allChapters.firstOrNull { it.subjectId == subId }?.title ?: ""
                            },
                            label = { Text(sub) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Text("Select Chapter for Viva", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedChapter.ifBlank { "Select Topic" }, maxLines = 1, fontSize = 12.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        subjectChapters.forEach { ch ->
                            DropdownMenuItem(
                                text = { Text("${ch.chapterNumber}. ${ch.title}", fontSize = 12.sp) },
                                onClick = {
                                    selectedChapter = ch.title
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.startVivaSession(selectedSubject, selectedChapter.ifBlank { "Core Concepts" })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_start_viva"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isVivaLoading
                ) {
                    if (isVivaLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connecting with Board Examiner...")
                    } else {
                        Icon(Icons.Default.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🎙️ Begin Oral Viva Session", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        // Active Viva Flow
        val session = vivaSession!!
        val currentQ = session.questions.getOrNull(session.currentQuestionIndex) ?: session.questions.first()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${session.subject} Viva Voce", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(session.chapter, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    TextButton(onClick = { viewModel.resetVivaSession() }) {
                        Text("End Session", color = ScoreRed)
                    }
                }
            }

            if (!session.isCompleted) {
                // Examiner Question Box
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, AccentElectricBlue.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Question ${session.currentQuestionIndex + 1} of 5",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentElectricBlue
                                )
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = AccentCyan)
                            }

                            Text(
                                currentQ.question,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                if (!currentQ.isAnswered) {
                    // Answer Input
                    item {
                        OutlinedTextField(
                            value = userAnswerInput,
                            onValueChange = { userAnswerInput = it },
                            label = { Text("Speak or type your viva answer here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            maxLines = 6
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.submitVivaAnswer(userAnswerInput)
                                userAnswerInput = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isVivaLoading && userAnswerInput.isNotBlank()
                        ) {
                            if (isVivaLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit Answer to Examiner")
                            }
                        }
                    }
                } else {
                    // Instant 3-Point Evaluation Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, ScoreGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Examiner Evaluation", fontWeight = FontWeight.Bold, color = ScoreGreen)
                                    Text("Score: ${currentQ.correctnessScore} / 5", fontWeight = FontWeight.Bold, color = ScoreYellow)
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                Text("🎯 Correctness: ${currentQ.feedback}", style = MaterialTheme.typography.bodySmall)
                                Text("⚠️ Missing Points: ${currentQ.missingPoints}", style = MaterialTheme.typography.bodySmall, color = ScoreRed)
                                Text("📋 Board Quality: ${currentQ.boardExamQuality}", style = MaterialTheme.typography.bodySmall, color = AccentCyan)

                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { viewModel.nextVivaQuestion() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = !isVivaLoading
                                ) {
                                    if (isVivaLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text("Proceed to Next Question")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Viva Session Summary
                item {
                    GlassCard(
                        backgroundColor = ScoreGreen.copy(alpha = 0.15f),
                        borderColor = ScoreGreen.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = ScoreYellow, modifier = Modifier.size(48.dp))
                            Text("Viva Voce Completed!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "Final Score: ${session.overallScore} / ${session.maxScore}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = ScoreGreen
                            )
                            Text(
                                session.overallFeedback,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { viewModel.resetVivaSession() },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Start Another Viva Session")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===================================================================
// TAB 3: AI MULTIMODAL DOUBT SOLVER
// ===================================================================
@Composable
fun AiDoubtSolverTab(viewModel: RudraViewModel) {
    val doubtResult by viewModel.doubtResult.collectAsState()
    val isDoubtLoading by viewModel.isDoubtLoading.collectAsState()
    val selectedBitmap by viewModel.selectedDoubtBitmap.collectAsState()
    val context = LocalContext.current

    var selectedSubject by remember { mutableStateOf("Physics") }
    var doubtQuery by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val service = com.example.ai.GeminiAiService()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                val bmp = service.decodeUriToBitmap(context, uri)
                viewModel.setSelectedDoubtBitmap(bmp)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            GlassCard(
                backgroundColor = AccentNavy.copy(alpha = 0.35f),
                borderColor = AccentElectricBlue.copy(alpha = 0.5f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = AccentElectricBlue)
                        Text(
                            "AI Multimodal Doubt Solver",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Type a doubt or upload an image of a textbook question / handwritten notes. AI explains Concept, Formula, Step-by-step solution, and Common Exam Traps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Physics", "Chemistry", "Biology").forEach { sub ->
                    FilterChip(
                        selected = selectedSubject == sub,
                        onClick = { selectedSubject = sub },
                        label = { Text(sub) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = doubtQuery,
                onValueChange = { doubtQuery = it },
                label = { Text("Type doubt, formula derivation, or question here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp),
                maxLines = 4
            )
        }

        // Image Attachment Bar
        item {
            if (selectedBitmap != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Attached question image",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Text("Question Image Attached", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = { viewModel.setSelectedDoubtBitmap(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Image", tint = ScoreRed)
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Attach Photo of Question / Notes")
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (doubtQuery.isNotBlank() || selectedBitmap != null) {
                        viewModel.solveDoubt(selectedSubject, doubtQuery)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_solve_doubt"),
                shape = RoundedCornerShape(10.dp),
                enabled = !isDoubtLoading && (doubtQuery.isNotBlank() || selectedBitmap != null)
            ) {
                if (isDoubtLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing Concept & Equations...")
                } else {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("⚡ Solve Doubt with AI Coach", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Doubt Solution Result 4-Pillars
        if (doubtResult != null) {
            val res = doubtResult!!

            item {
                SectionHeader(title = "AI Board Solution Breakdown")
            }

            item {
                DoubtPillarCard(
                    title = "1. Concept & Core Principle",
                    content = res.concept,
                    icon = Icons.Default.Lightbulb,
                    color = AccentCyan
                )
            }

            item {
                DoubtPillarCard(
                    title = "2. Relevant Formulas & Equations",
                    content = res.formula,
                    icon = Icons.Default.Calculate,
                    color = ScoreYellow
                )
            }

            item {
                DoubtPillarCard(
                    title = "3. Step-by-Step Solution / Derivation",
                    content = res.stepByStep,
                    icon = Icons.Default.FormatListNumbered,
                    color = ScoreGreen
                )
            }

            item {
                DoubtPillarCard(
                    title = "4. Common Exam Pitfalls & Traps",
                    content = res.commonMistakes,
                    icon = Icons.Default.WarningAmber,
                    color = ScoreRed
                )
            }
        }
    }
}

@Composable
fun DoubtPillarCard(title: String, content: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Text(title, fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp)
            }
            Text(
                content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ===================================================================
// TAB 4: AI WEAKNESS RADAR & ADAPTIVE TESTING
// ===================================================================
@Composable
fun AiWeaknessRadarTab(
    viewModel: RudraViewModel,
    onStartAdaptiveTest: () -> Unit
) {
    val weaknessReport by viewModel.weaknessReport.collectAsState()
    val isWeaknessLoading by viewModel.isWeaknessLoading.collectAsState()
    val weakChapters by viewModel.weakChapters.collectAsState()
    val allChapters by viewModel.allChapters.collectAsState()
    val allMockTests by viewModel.allMockTests.collectAsState()

    LaunchedEffect(Unit) {
        if (weaknessReport == null) {
            viewModel.generateWeaknessAnalysis()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            GlassCard(
                backgroundColor = AccentNavy.copy(alpha = 0.35f),
                borderColor = AccentElectricBlue.copy(alpha = 0.5f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Radar, contentDescription = null, tint = AccentElectricBlue)
                        Text(
                            "AI Weakness Radar & Adaptive Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Continuously evaluates your Room database records (confidence stars, mock test percentages, study duration) to dynamically adapt test difficulty.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Live Performance Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val mockAvg = if (allMockTests.isNotEmpty()) allMockTests.map { it.percentage }.average() else 72.0
                ContextChip("Weak Chapters", "${weakChapters.size}", ScoreRed, Modifier.weight(1f))
                ContextChip("Mock Avg", "${"%.0f".format(mockAvg)}%", if (mockAvg >= 75) ScoreGreen else ScoreYellow, Modifier.weight(1f))
                ContextChip("Completed", "${allChapters.count { it.status == ChapterEntity.STATUS_COMPLETED }} / 46", AccentCyan, Modifier.weight(1f))
            }
        }

        // Adaptive Tier Banner
        val report = weaknessReport
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, AccentElectricBlue.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = AccentElectricBlue)
                            Text("Current Adaptive Tier", fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentElectricBlue.copy(alpha = 0.15f)
                        ) {
                            Text(
                                report?.adaptiveTier ?: "Level 2: Board Standard",
                                color = AccentElectricBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        report?.adaptiveAdvice ?: "Focus on completing 10 PYQs per weak chapter and master high-yield derivations before increasing difficulty.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            viewModel.generateAdaptiveRemediationTest()
                            onStartAdaptiveTest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("⚡ Generate Adaptive Remediation Test")
                    }
                }
            }
        }

        // Weak vs Strong Topics
        item {
            SectionHeader(title = "Weak Topics Radar (Needs Fix)")
        }

        item {
            val weakList = report?.weakTopics ?: listOf("Electric Potential Derivations", "Electrochemistry Nernst Numericals", "Biotechnology Vectors")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                weakList.forEach { topic ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ScoreRed.copy(alpha = 0.1f),
                        border = BorderStroke(0.5.dp, ScoreRed.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ScoreRed, modifier = Modifier.size(16.dp))
                            Text(topic, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Strong Topics (Mastered)")
        }

        item {
            val strongList = report?.strongTopics ?: listOf("Solutions & Raoult's Law", "Sexual Reproduction in Plants", "Electrostatics Coulomb's Law")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                strongList.forEach { topic ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ScoreGreen.copy(alpha = 0.1f),
                        border = BorderStroke(0.5.dp, ScoreGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ScoreGreen, modifier = Modifier.size(16.dp))
                            Text(topic, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ===================================================================
// TAB 5: AI BOARD PREDICTOR & DAILY STUDY PLANNER
// ===================================================================
@Composable
fun AiBoardPredictorTab(viewModel: RudraViewModel) {
    val boardPrediction by viewModel.boardPrediction.collectAsState()
    val aiDailyPlan by viewModel.aiDailyPlan.collectAsState()
    val isDailyPlanLoading by viewModel.isDailyPlanLoading.collectAsState()
    var planAddedMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshBoardPrediction()
        if (aiDailyPlan == null) {
            viewModel.generateAiDailyPlan()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Board Prediction Hero Card
        val pred = boardPrediction
        if (pred != null) {
            item {
                GlassCard(
                    backgroundColor = AccentNavy.copy(alpha = 0.35f),
                    borderColor = AccentElectricBlue.copy(alpha = 0.5f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Timeline, contentDescription = null, tint = AccentElectricBlue)
                                Text("AI Board Readiness Predictor", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                            IconButton(onClick = { viewModel.refreshBoardPrediction() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh Prediction", tint = AccentElectricBlue)
                            }
                        }

                        // Readiness Metric
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Predicted Board Score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${"%.1f".format(pred.predictedBoardPercent)}%",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pred.predictedBoardPercent >= 85) ScoreGreen else ScoreYellow
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Target: 85%+", fontWeight = FontWeight.Bold, color = AccentCyan)
                                Text("Syllabus Done: ${"%.0f".format(pred.syllabusCompletionPercent)}%", fontSize = 12.sp)
                                Text("Revision Ready: ${"%.0f".format(pred.revisionReadinessPercent)}%", fontSize = 12.sp)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Subject Predictions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ContextChip("Physics", "${"%.0f".format(pred.physicsPredicted)}/100", AccentElectricBlue, Modifier.weight(1f))
                            ContextChip("Chemistry", "${"%.0f".format(pred.chemistryPredicted)}/100", ScoreYellow, Modifier.weight(1f))
                            ContextChip("Biology", "${"%.0f".format(pred.biologyPredicted)}/100", ScoreGreen, Modifier.weight(1f))
                        }

                        // Warning Alerts
                        if (pred.warnings.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                pred.warnings.forEach { warn ->
                                    Text(warn, fontSize = 11.sp, color = ScoreRed, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Study Planner
        item {
            SectionHeader(title = "AI Daily Study Planner")
        }

        val plan = aiDailyPlan
        if (plan != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Today's High-Yield Plan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            IconButton(onClick = { viewModel.generateAiDailyPlan() }, enabled = !isDailyPlanLoading) {
                                if (isDailyPlanLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                else Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = AccentElectricBlue)
                            }
                        }

                        Text("Priority Subject: ${plan.prioritySubject}", fontWeight = FontWeight.Bold, color = AccentElectricBlue, fontSize = 12.sp)

                        Text("📖 Focus Chapters:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        plan.todayChapters.forEach { ch ->
                            Text("• $ch", fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
                        }

                        Text("🔄 Revision Targets:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ScoreYellow)
                        plan.revisionTasks.forEach { rev ->
                            Text("• $rev", fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
                        }

                        Text("🧪 Mock Target: ${plan.mockTestTask}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ScoreGreen)

                        Text("💡 ${plan.motivationalTip}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Button(
                            onClick = {
                                viewModel.addDailyPlanTasksToTaskManager()
                                planAddedMsg = "Tasks Added to Task Board!"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlaylistAddCheck, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(planAddedMsg ?: "Apply All to Task Manager")
                        }
                    }
                }
            }
        } else if (isDailyPlanLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentElectricBlue)
                }
            }
        }
    }
}

@Composable
fun ContextChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
        }
    }
}
