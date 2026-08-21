package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
    val generatedTest by viewModel.generatedTest.collectAsState()
    val isTestGenerating by viewModel.isTestGenerating.collectAsState()
    val isTestSubmitting by viewModel.isTestSubmitting.collectAsState()
    val userAnswers by viewModel.testUserAnswers.collectAsState()
    val allChapters by viewModel.allChapters.collectAsState()

    var selectedSubject by remember { mutableStateOf("Physics") }
    var selectedMode by remember { mutableStateOf("Chapter-wise Test") }
    var selectedDifficulty by remember { mutableStateOf("Board Level") }
    var selectedChapter by remember { mutableStateOf("") }
    var questionCount by remember { mutableIntStateOf(5) }
    var saveStatusMsg by remember { mutableStateOf<String?>(null) }

    val subjectChapters = remember(selectedSubject, allChapters) {
        val subName = if (selectedSubject == "Biology") "Biology" else if (selectedSubject == "Chemistry") "Chemistry" else "Physics"
        allChapters.filter { ch ->
            when (subName) {
                "Physics" -> ch.subjectId == 1L
                "Chemistry" -> ch.subjectId == 2L
                else -> ch.subjectId == 3L
            }
        }
    }

    LaunchedEffect(subjectChapters) {
        if (selectedChapter.isBlank() && subjectChapters.isNotEmpty()) {
            selectedChapter = subjectChapters.first().title
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
                    backgroundColor = AccentNavy.copy(alpha = 0.35f),
                    borderColor = AccentElectricBlue.copy(alpha = 0.5f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, tint = AccentElectricBlue)
                            Text(
                                "AI Question Paper Setter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            "Generates custom board exam question papers with MCQs, Assertion-Reason, Short/Long Derivations, and Numericals.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 1. Select Subject
            item {
                Text("1. Select Subject", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Physics", "Chemistry", "Biology", "Full PCB").forEach { sub ->
                        FilterChip(
                            selected = selectedSubject == sub,
                            onClick = {
                                selectedSubject = sub
                                val subName = if (sub == "Biology") "Biology" else if (sub == "Chemistry") "Chemistry" else "Physics"
                                val chs = allChapters.filter {
                                    if (subName == "Physics") it.subjectId == 1L else if (subName == "Chemistry") it.subjectId == 2L else it.subjectId == 3L
                                }
                                selectedChapter = chs.firstOrNull()?.title ?: ""
                            },
                            label = { Text(sub, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Select Test Mode
            item {
                Text("2. Test Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Chapter-wise Test", "Multiple Chapter Test", "Full Subject Test", "Full PCB Mock Test").forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(mode, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // 3. Select Chapter (if Chapter-wise)
            if (selectedMode == "Chapter-wise Test" && subjectChapters.isNotEmpty()) {
                item {
                    Text("3. Target Chapter", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
            }

            // 4. Difficulty Level
            item {
                Text("4. Difficulty Level", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Easy", "Medium", "Hard", "Board Level").forEach { diff ->
                        val color = when (diff) {
                            "Easy" -> ScoreGreen
                            "Medium" -> ScoreYellow
                            "Hard" -> ScoreRed
                            else -> AccentElectricBlue
                        }
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = diff },
                            label = { Text(diff, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Question Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("5. Number of Questions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("$questionCount Questions", fontWeight = FontWeight.Bold, color = AccentElectricBlue)
                }
                Slider(
                    value = questionCount.toFloat(),
                    onValueChange = { questionCount = it.toInt() },
                    valueRange = 3f..10f,
                    steps = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Generate Button
            item {
                Button(
                    onClick = {
                        val chaptersToInclude = if (selectedMode == "Chapter-wise Test" && selectedChapter.isNotBlank()) {
                            listOf(selectedChapter)
                        } else if (selectedMode == "Multiple Chapter Test") {
                            subjectChapters.take(3).map { it.title }
                        } else {
                            emptyList()
                        }

                        val qTypes = listOf(
                            QuestionType.MCQ,
                            QuestionType.ASSERTION_REASON,
                            QuestionType.SHORT_ANSWER,
                            QuestionType.NUMERICAL,
                            QuestionType.LONG_ANSWER
                        )

                        viewModel.generateTestPaper(
                            subject = selectedSubject,
                            chapters = chaptersToInclude,
                            mode = selectedMode,
                            difficulty = selectedDifficulty,
                            questionTypes = qTypes,
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
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Drafting Board Level Questions...")
                    } else {
                        Icon(Icons.Default.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("⚡ Generate AI Test Paper", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        // Active or Evaluated Test View
        val test = generatedTest!!
        var currentQuestionIndex by remember { mutableIntStateOf(0) }
        val currentQ = test.questions.getOrNull(currentQuestionIndex) ?: test.questions.first()

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
                                    "Total Marks: ${test.totalMarks.toInt()} • Time: ${test.timeLimitMinutes} mins • ${test.difficulty}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            TextButton(
                                onClick = { viewModel.clearActiveTest() },
                                colors = ButtonDefaults.textButtonColors(contentColor = ScoreRed)
                            ) {
                                Text("Exit", fontSize = 12.sp)
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
                                            "${"%.1f".format(test.totalObtainedMarks)} / ${"%.1f".format(test.totalMarks)} (${"%.0f".format((test.totalObtainedMarks / test.totalMarks) * 100)}%)",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (test.totalObtainedMarks / test.totalMarks >= 0.75) ScoreGreen else if (test.totalObtainedMarks / test.totalMarks >= 0.5) ScoreYellow else ScoreRed
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.saveTestResultToMockHistory()
                                            saveStatusMsg = "Score logged to Mock Tests!"
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
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
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
                                    label = { Text("Write your step-by-step solution / definition / derivation") },
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
                                        Text("Score: ${currentQ.obtainedMarks} / ${currentQ.marks}", fontWeight = FontWeight.Bold)
                                        Text(if (currentQ.obtainedMarks >= currentQ.marks) "CORRECT" else "NEEDS REVISION", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }

                                Text("Your Answer:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(currentQ.userAnswer.ifBlank { "[Not Attempted]" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Text("Model Board Answer:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ScoreGreen)
                                Text(currentQ.correctAnswer, style = MaterialTheme.typography.bodySmall)

                                if (currentQ.explanation.isNotBlank()) {
                                    Text("Explanation & Marking Scheme:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentCyan)
                                    Text("${currentQ.explanation}\n${currentQ.markingScheme}", style = MaterialTheme.typography.bodySmall)
                                }

                                if (currentQ.aiEvaluation.isNotBlank()) {
                                    Text("AI Examiner Feedback:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ScoreYellow)
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
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit & Evaluate")
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
