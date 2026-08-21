package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.GeneratedTest
import com.example.data.model.QuestionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExamExporter {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points at 72 DPI
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points at 72 DPI
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    /**
     * Generates a beautifully formatted Question Paper PDF and returns the file Uri.
     */
    fun generateQuestionPaperPdf(context: Context, test: GeneratedTest): File {
        val pdfDoc = PdfDocument()
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas
        var currentY = MARGIN

        val titlePaint = TextPaint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            textSize = 15f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val subHeaderPaint = TextPaint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = TextPaint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldBodyPaint = TextPaint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = TextPaint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val boxPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }

        fun drawHeader() {
            // Header Title
            val headerText = "RUDRA AI EXAMINATION SYSTEM"
            val boardText = "TARGET 2027 BOARD PREPARATION • ${test.difficulty.uppercase(Locale.getDefault())}"
            
            canvas.drawText(headerText, MARGIN, currentY + 12f, titlePaint)
            currentY += 18f
            canvas.drawText(boardText, MARGIN, currentY + 10f, metaPaint)
            currentY += 16f
            
            canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
            currentY += 10f

            // Test Info Box
            canvas.drawRoundRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 45f, 6f, 6f, boxPaint)
            
            canvas.drawText("Test Name: ${test.title}", MARGIN + 10f, currentY + 14f, boldBodyPaint)
            canvas.drawText("Subject: ${test.subject} (${test.mode})", MARGIN + 10f, currentY + 26f, bodyPaint)
            val chaptersStr = if (test.chapters.isEmpty()) "Full Syllabus" else test.chapters.joinToString(", ")
            canvas.drawText("Chapters: $chaptersStr", MARGIN + 10f, currentY + 38f, metaPaint)

            val rightMargin = PAGE_WIDTH - MARGIN - 10f
            val timeText = "Time: ${test.timeLimitMinutes} Mins"
            val marksText = "Max Marks: ${test.totalMarks.toInt()}"
            val questionsText = "Questions: ${test.questions.size}"

            canvas.drawText(timeText, rightMargin - subHeaderPaint.measureText(timeText), currentY + 14f, subHeaderPaint)
            canvas.drawText(marksText, rightMargin - boldBodyPaint.measureText(marksText), currentY + 26f, boldBodyPaint)
            canvas.drawText(questionsText, rightMargin - metaPaint.measureText(questionsText), currentY + 38f, metaPaint)

            currentY += 55f

            // General Instructions
            canvas.drawText("GENERAL INSTRUCTIONS:", MARGIN, currentY + 10f, subHeaderPaint)
            currentY += 14f
            val instructions = if (test.generalInstructions.isNotEmpty()) {
                test.generalInstructions
            } else {
                listOf(
                    "1. All questions are compulsory. Read questions carefully before answering.",
                    "2. Section A contains Objective Type (MCQ & Assertion-Reason) questions.",
                    "3. Section B contains Very Short & Short Answer conceptual questions.",
                    "4. Section C contains Long Answers, Derivations, Numericals and Case Studies."
                )
            }
            for (inst in instructions) {
                canvas.drawText(inst, MARGIN + 6f, currentY + 9f, metaPaint)
                currentY += 12f
            }

            currentY += 6f
            canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
            currentY += 14f
        }

        fun drawFooter() {
            val footerText = "Page $pageNumber • Rudra Life OS AI Exam Engine"
            canvas.drawText(footerText, (PAGE_WIDTH - metaPaint.measureText(footerText)) / 2, PAGE_HEIGHT - 20f, metaPaint)
        }

        // Draw initial page header
        drawHeader()

        fun checkAndCreateNewPage(neededHeight: Float) {
            if (currentY + neededHeight > PAGE_HEIGHT - MARGIN - 20f) {
                drawFooter()
                pdfDoc.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas
                currentY = MARGIN

                // Minimal header for subsequent pages
                canvas.drawText("${test.title} • ${test.subject} • ${test.difficulty}", MARGIN, currentY + 10f, metaPaint)
                val marksHeader = "Page $pageNumber"
                canvas.drawText(marksHeader, PAGE_WIDTH - MARGIN - metaPaint.measureText(marksHeader), currentY + 10f, metaPaint)
                currentY += 16f
                canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
                currentY += 12f
            }
        }

        // Group Questions by Section
        val sectionA = test.questions.filter { it.type == QuestionType.MCQ || it.type == QuestionType.ASSERTION_REASON }
        val sectionB = test.questions.filter { it.type == QuestionType.VERY_SHORT_ANSWER || it.type == QuestionType.SHORT_ANSWER }
        val sectionC = test.questions.filter { it.type !in listOf(QuestionType.MCQ, QuestionType.ASSERTION_REASON, QuestionType.VERY_SHORT_ANSWER, QuestionType.SHORT_ANSWER) }

        val sections = listOf(
            Triple("SECTION A (Objective & Conceptual Types)", "1 Mark Each", sectionA),
            Triple("SECTION B (Short & Descriptive Types)", "2–3 Marks Each", sectionB),
            Triple("SECTION C (Long Answers, Numericals & Case Studies)", "3–5 Marks Each", sectionC)
        ).filter { it.third.isNotEmpty() }

        for ((secTitle, secDesc, qList) in sections) {
            checkAndCreateNewPage(40f)
            canvas.drawText("$secTitle — $secDesc", MARGIN, currentY + 11f, subHeaderPaint)
            currentY += 16f
            canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
            currentY += 10f

            for (q in qList) {
                val qNumber = test.questions.indexOf(q) + 1
                val qTypeTag = "[${q.type.displayName} • ${q.marks.toInt()}M]"
                val qHeader = "Q$qNumber. $qTypeTag"

                val fullText = "$qHeader\n${q.questionText}"
                val textLayout = createStaticLayout(fullText, boldBodyPaint, bodyPaint, CONTENT_WIDTH.toInt())

                var estHeight = textLayout.height.toFloat() + 10f
                if (q.options.isNotEmpty()) {
                    estHeight += (q.options.size * 14f) + 6f
                }

                checkAndCreateNewPage(estHeight)

                canvas.save()
                canvas.translate(MARGIN, currentY)
                textLayout.draw(canvas)
                canvas.restore()
                currentY += textLayout.height.toFloat() + 6f

                // Draw Options if present
                if (q.options.isNotEmpty()) {
                    for (opt in q.options) {
                        checkAndCreateNewPage(14f)
                        canvas.drawText(opt, MARGIN + 14f, currentY + 9f, bodyPaint)
                        currentY += 13f
                    }
                    currentY += 4f
                }

                currentY += 6f
            }
        }

        drawFooter()
        pdfDoc.finishPage(page)

        // Save PDF to cache dir
        val outputDir = File(context.cacheDir, "exam_papers").apply { mkdirs() }
        val fileName = "Exam_${test.subject}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val pdfFile = File(outputDir, fileName)
        val fos = FileOutputStream(pdfFile)
        pdfDoc.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDoc.close()

        return pdfFile
    }

    /**
     * Generates a comprehensive Answer Key & Solutions PDF.
     */
    fun generateAnswerKeyPdf(context: Context, test: GeneratedTest): File {
        val pdfDoc = PdfDocument()
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas
        var currentY = MARGIN

        val titlePaint = TextPaint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val subHeaderPaint = TextPaint().apply {
            color = Color.rgb(16, 185, 129) // Emerald Green
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = TextPaint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldBodyPaint = TextPaint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = TextPaint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        fun drawFooter() {
            val footerText = "Answer Key • Page $pageNumber • Rudra Life OS"
            canvas.drawText(footerText, (PAGE_WIDTH - metaPaint.measureText(footerText)) / 2, PAGE_HEIGHT - 20f, metaPaint)
        }

        fun checkAndCreateNewPage(neededHeight: Float) {
            if (currentY + neededHeight > PAGE_HEIGHT - MARGIN - 20f) {
                drawFooter()
                pdfDoc.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas
                currentY = MARGIN

                canvas.drawText("ANSWER KEY: ${test.title} (${test.subject})", MARGIN, currentY + 10f, metaPaint)
                currentY += 16f
                canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
                currentY += 12f
            }
        }

        // Header
        canvas.drawText("OFFICIAL MODEL ANSWER KEY & SOLUTIONS", MARGIN, currentY + 12f, titlePaint)
        currentY += 18f
        canvas.drawText("${test.title} • ${test.subject} • Total Marks: ${test.totalMarks.toInt()}", MARGIN, currentY + 10f, metaPaint)
        currentY += 16f
        canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
        currentY += 14f

        for (q in test.questions) {
            val qNumber = test.questions.indexOf(q) + 1
            checkAndCreateNewPage(60f)

            canvas.drawText("Q$qNumber. [${q.type.displayName} • ${q.marks.toInt()}M] ${q.questionText.take(70)}...", MARGIN, currentY + 10f, boldBodyPaint)
            currentY += 14f

            val ansText = "Correct Answer: ${q.correctAnswer}"
            canvas.drawText(ansText, MARGIN + 8f, currentY + 10f, subHeaderPaint)
            currentY += 14f

            if (q.stepByStepSolution.isNotBlank()) {
                val solText = "Step-by-Step Solution / Derivation:\n${q.stepByStepSolution}"
                val solLayout = createStaticLayout(solText, bodyPaint, bodyPaint, CONTENT_WIDTH.toInt() - 16)
                checkAndCreateNewPage(solLayout.height.toFloat() + 10f)
                canvas.save()
                canvas.translate(MARGIN + 8f, currentY)
                solLayout.draw(canvas)
                canvas.restore()
                currentY += solLayout.height.toFloat() + 6f
            }

            if (q.markingScheme.isNotBlank()) {
                val msText = "Marking Scheme: ${q.markingScheme}"
                val msLayout = createStaticLayout(msText, metaPaint, metaPaint, CONTENT_WIDTH.toInt() - 16)
                checkAndCreateNewPage(msLayout.height.toFloat() + 6f)
                canvas.save()
                canvas.translate(MARGIN + 8f, currentY)
                msLayout.draw(canvas)
                canvas.restore()
                currentY += msLayout.height.toFloat() + 6f
            }

            if (q.importantConcepts.isNotEmpty()) {
                val impText = "Key Concepts to Remember: ${q.importantConcepts.joinToString(", ")}"
                val impLayout = createStaticLayout(impText, metaPaint, metaPaint, CONTENT_WIDTH.toInt() - 16)
                checkAndCreateNewPage(impLayout.height.toFloat() + 6f)
                canvas.save()
                canvas.translate(MARGIN + 8f, currentY)
                impLayout.draw(canvas)
                canvas.restore()
                currentY += impLayout.height.toFloat() + 6f
            }

            currentY += 6f
            canvas.drawLine(MARGIN + 8f, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
            currentY += 10f
        }

        drawFooter()
        pdfDoc.finishPage(page)

        val outputDir = File(context.cacheDir, "exam_papers").apply { mkdirs() }
        val fileName = "AnswerKey_${test.subject}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val pdfFile = File(outputDir, fileName)
        val fos = FileOutputStream(pdfFile)
        pdfDoc.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDoc.close()

        return pdfFile
    }

    /**
     * Generates a complete text version of the question paper.
     */
    fun generateQuestionPaperText(test: GeneratedTest): String {
        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("        RUDRA AI EXAMINATION SYSTEM (2027)")
        sb.appendLine("==================================================")
        sb.appendLine("Title: ${test.title}")
        sb.appendLine("Subject: ${test.subject} (${test.mode})")
        sb.appendLine("Difficulty: ${test.difficulty}")
        sb.appendLine("Chapters: ${if (test.chapters.isEmpty()) "Full Syllabus" else test.chapters.joinToString(", ")}")
        sb.appendLine("Time Limit: ${test.timeLimitMinutes} Minutes | Max Marks: ${test.totalMarks.toInt()}")
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("GENERAL INSTRUCTIONS:")
        if (test.generalInstructions.isNotEmpty()) {
            test.generalInstructions.forEach { sb.appendLine(it) }
        } else {
            sb.appendLine("1. All questions are compulsory.")
            sb.appendLine("2. Section A: Objective (MCQ, Assertion-Reason)")
            sb.appendLine("3. Section B: Short & Conceptual Questions")
            sb.appendLine("4. Section C: Long Answers, Derivations & Numericals")
        }
        sb.appendLine("==================================================\n")

        test.questions.forEachIndexed { index, q ->
            sb.appendLine("Q${index + 1}. [${q.type.displayName} - ${q.marks.toInt()} Mark(s)]")
            sb.appendLine(q.questionText)
            if (q.options.isNotEmpty()) {
                q.options.forEach { opt -> sb.appendLine("   $opt") }
            }
            sb.appendLine()
        }

        sb.appendLine("----------------- END OF PAPER -----------------")
        return sb.toString()
    }

    /**
     * Generates a complete text version of the answer key with step-by-step solutions.
     */
    fun generateAnswerKeyText(test: GeneratedTest): String {
        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("     RUDRA AI MODEL ANSWER KEY & SOLUTIONS")
        sb.appendLine("==================================================")
        sb.appendLine("Test: ${test.title} | Subject: ${test.subject}")
        sb.appendLine("Total Marks: ${test.totalMarks.toInt()} | Questions: ${test.questions.size}")
        sb.appendLine("==================================================\n")

        test.questions.forEachIndexed { index, q ->
            sb.appendLine("Q${index + 1}. [${q.type.displayName} - ${q.marks.toInt()} Mark(s)]")
            sb.appendLine("Question: ${q.questionText}")
            sb.appendLine("👉 Model Answer: ${q.correctAnswer}")
            if (q.stepByStepSolution.isNotBlank()) {
                sb.appendLine("📝 Step-by-Step Solution:\n${q.stepByStepSolution}")
            }
            if (q.markingScheme.isNotBlank()) {
                sb.appendLine("⚖️ Marking Scheme: ${q.markingScheme}")
            }
            if (q.importantConcepts.isNotEmpty()) {
                sb.appendLine("💡 Key Concepts: ${q.importantConcepts.joinToString(", ")}")
            }
            sb.appendLine("--------------------------------------------------\n")
        }
        return sb.toString()
    }

    /**
     * Opens or shares the generated PDF using Android FileProvider.
     */
    fun sharePdf(context: Context, pdfFile: File, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Question Paper / Answer Key PDF"))
    }

    private fun createStaticLayout(
        text: String,
        primaryPaint: TextPaint,
        secondaryPaint: TextPaint,
        width: Int
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, primaryPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2f, 1f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                primaryPaint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                1f,
                2f,
                false
            )
        }
    }
}
