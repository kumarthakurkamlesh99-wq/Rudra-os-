package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val modelName = "gemini-2.5-flash"
    private val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    suspend fun generateChapterSummary(subject: String, chapterTitle: String): Result<String> {
        val prompt = """
            You are an expert Class 12 Science tutor specializing in BSEB and CBSE board curriculum.
            Provide a crisp, high-yield summary for the chapter: "$chapterTitle" in subject: "$subject".
            Include:
            1. Core concepts & key definitions
            2. Most important formulas / chemical equations / biological diagrams to remember
            3. Top 3 frequently asked exam questions (PYQ style)
            4. 1-minute quick recall bullet points.
            Keep formatting clean with bullet points and bold headers.
        """.trimIndent()
        return executePrompt(prompt)
    }

    suspend fun generatePracticeQuiz(subject: String, chapterTitle: String): Result<String> {
        val prompt = """
            Create a 5-question high-yield practice quiz for Class 12 BSEB Science:
            Subject: $subject
            Chapter: $chapterTitle
            
            Provide:
            - 3 Multiple Choice Questions (with options A, B, C, D and answer with 1-line explanation)
            - 2 Short Answer/Numerical questions with step-by-step solutions.
        """.trimIndent()
        return executePrompt(prompt)
    }

    suspend fun solveDoubt(subject: String, doubtText: String): Result<String> {
        val prompt = """
            You are a helpful Class 12 Science mentor for Rudra.
            Subject: $subject
            Student Question/Doubt: "$doubtText"
            
            Explain the concept in simple, lucid language.
            If numerical, show step-by-step calculation.
            If theory, provide intuitive physical reasoning or diagram explanation.
            End with a 1-sentence memorable takeaway.
        """.trimIndent()
        return executePrompt(prompt)
    }

    suspend fun generateFlashcards(subject: String, chapterTitle: String): Result<String> {
        val prompt = """
            Generate 6 high-impact flashcards for Class 12 BSEB $subject: "$chapterTitle".
            Format:
            Card 1:
            Q: [Question / Formula / Reaction]
            A: [Answer / Derivation / Significance]
            ...
        """.trimIndent()
        return executePrompt(prompt)
    }

    private suspend fun executePrompt(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(IllegalStateException("AI service unavailable: Gemini API key is not configured."))
        }

        try {
            val root = JSONObject()
            val contents = JSONArray()
            val contentObj = JSONObject()
            val parts = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            parts.put(partObj)
            contentObj.put("parts", parts)
            contents.put(contentObj)
            root.put("contents", contents)

            val requestBody = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$endpoint?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val resParts = content?.optJSONArray("parts")
                    val text = resParts?.optJSONObject(0)?.optString("text")
                    if (!text.isNullOrBlank()) {
                        return@withContext Result.success(text)
                    }
                }
                Result.failure(Exception("AI response was empty or invalid."))
            } else {
                Result.failure(Exception("AI service unavailable (HTTP ${response.code})."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("AI service unavailable: ${e.localizedMessage ?: "Network error"}"))
        }
    }
}

