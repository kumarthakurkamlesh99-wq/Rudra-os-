package com.example.data.model

enum class QuestionType(val displayName: String, val defaultMarks: Double) {
    MCQ("Multiple Choice (MCQ)", 1.0),
    ASSERTION_REASON("Assertion - Reason", 1.0),
    VERY_SHORT_ANSWER("Very Short Answer (VSA)", 1.0),
    SHORT_ANSWER("Short Answer (SA)", 2.0),
    LONG_ANSWER("Long Answer / Derivation (LA)", 5.0),
    NUMERICAL("Numerical Problem", 3.0),
    DIAGRAM_BASED("Diagram Based Question", 3.0),
    CASE_BASED("Case Based Question", 4.0)
}

data class TestQuestion(
    val id: Int,
    val type: QuestionType,
    val questionText: String,
    val marks: Double = 1.0,
    val options: List<String> = emptyList(), // For MCQ / Assertion-Reason
    val correctAnswer: String = "",
    val stepByStepSolution: String = "",
    val markingScheme: String = "",
    val importantConcepts: List<String> = emptyList(),
    val explanation: String = "",
    var userAnswer: String = "",
    var obtainedMarks: Double = 0.0,
    var accuracy: String = "",
    var keyPointsCovered: String = "",
    var missingConcepts: String = "",
    var boardQualityFeedback: String = "",
    var aiEvaluation: String = ""
)

data class GeneratedTest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val mode: String, // Single Chapter, Multiple Chapters, Full Subject, Full PCB Mock Test
    val difficulty: String, // Bihar Board Level, Bihar Board Advanced, CBSE Level, CBSE Advanced
    val chapters: List<String>,
    val totalMarks: Double,
    val timeLimitMinutes: Int,
    val generalInstructions: List<String> = emptyList(),
    val questions: List<TestQuestion>,
    val createdAt: Long = System.currentTimeMillis(),
    var isSubmitted: Boolean = false,
    var totalObtainedMarks: Double = 0.0,
    var feedback: String = "",
    var recommendedNextDifficulty: String = "",
    var adaptiveSuggestion: String = ""
)

data class VivaQuestion(
    val id: Int,
    val question: String,
    val expectedAnswer: String,
    val keyPoints: List<String>,
    var userResponse: String = "",
    var correctnessScore: Int = 0, // 0 to 5
    var missingPoints: String = "",
    var boardExamQuality: String = "",
    var feedback: String = "",
    var isAnswered: Boolean = false
)

data class VivaSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val subject: String,
    val chapter: String,
    val questions: List<VivaQuestion>,
    var currentQuestionIndex: Int = 0,
    var isCompleted: Boolean = false,
    var overallScore: Int = 0,
    var maxScore: Int = 0,
    var overallFeedback: String = ""
)

data class WeaknessReport(
    val weakTopics: List<String>,
    val strongTopics: List<String>,
    val recommendedChapters: List<String>,
    val studyTimeHours: Double,
    val mockAveragePercent: Double,
    val adaptiveTier: String, // Level 1 (Foundation), Level 2 (Board Standard), Level 3 (Topper Drill)
    val adaptiveAdvice: String,
    val actionPlan: String
)

data class AiDoubtResult(
    val subject: String,
    val query: String,
    val concept: String,
    val formula: String,
    val stepByStep: String,
    val commonMistakes: String,
    val hasImage: Boolean = false
)

data class AiDailyPlan(
    val todayChapters: List<String>,
    val revisionTasks: List<String>,
    val mockTestTask: String,
    val prioritySubject: String,
    val motivationalTip: String,
    val dateString: String
)

data class AiBoardPrediction(
    val syllabusCompletionPercent: Double,
    val revisionReadinessPercent: Double,
    val predictedBoardPercent: Double,
    val physicsPredicted: Double,
    val chemistryPredicted: Double,
    val biologyPredicted: Double,
    val warnings: List<String>,
    val recommendations: List<String>
)
