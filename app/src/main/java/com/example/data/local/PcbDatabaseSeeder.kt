package com.example.data.local

import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.StreakRecordEntity
import com.example.data.local.entities.SubjectEntity
import com.example.data.local.entities.TimelineBlockEntity
import com.example.data.local.entities.TimelinePresetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PcbDatabaseSeeder {

    suspend fun seedDatabaseIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val subjectCount = database.subjectDao().getSubjectCount()
        if (subjectCount == 0) {
            // 1. Insert PCB Subjects
            val physicsId = database.subjectDao().insertSubject(
                SubjectEntity(
                    id = 1L,
                    name = "Physics",
                    code = "PHY",
                    colorHex = "#1E88E5",
                    iconName = "electric_bolt",
                    description = "Class 12 Physics (15 Chapters)",
                    orderIndex = 1
                )
            )

            val chemistryId = database.subjectDao().insertSubject(
                SubjectEntity(
                    id = 2L,
                    name = "Chemistry",
                    code = "CHEM",
                    colorHex = "#43A047",
                    iconName = "science",
                    description = "Class 12 Chemistry (15 Chapters)",
                    orderIndex = 2
                )
            )

            val biologyId = database.subjectDao().insertSubject(
                SubjectEntity(
                    id = 3L,
                    name = "Biology",
                    code = "BIO",
                    colorHex = "#8E24AA",
                    iconName = "biotech",
                    description = "Class 12 Biology (16 Chapters)",
                    orderIndex = 3
                )
            )

            // 2. Insert Physics (15 Chapters)
            val physicsChapters = listOf(
                "Electric Charges and Fields",
                "Electrostatic Potential and Capacitance",
                "Current Electricity",
                "Moving Charges and Magnetism",
                "Magnetism and Matter",
                "Electromagnetic Induction",
                "Alternating Current",
                "Electromagnetic Waves",
                "Ray Optics and Optical Instruments",
                "Wave Optics",
                "Dual Nature of Radiation and Matter",
                "Atoms",
                "Nuclei",
                "Semiconductor Electronics",
                "Communication Systems"
            )

            physicsChapters.forEachIndexed { index, title ->
                database.chapterDao().insertChapter(
                    ChapterEntity(
                        subjectId = physicsId,
                        chapterNumber = index + 1,
                        title = title,
                        status = if (index < 2) ChapterEntity.STATUS_COMPLETED else if (index < 4) ChapterEntity.STATUS_LEARNING else ChapterEntity.STATUS_NOT_STARTED,
                        progressPercent = if (index < 2) 100 else if (index < 4) 65 else 0,
                        totalLectures = 10,
                        watchedLectures = if (index < 2) 10 else if (index < 4) 6 else 0,
                        ncertRead = index < 2,
                        ncertRevised = index == 0,
                        notesStatus = if (index < 2) ChapterEntity.NOTES_COMPLETED else if (index < 4) ChapterEntity.NOTES_IN_PROGRESS else ChapterEntity.NOTES_NOT_STARTED,
                        pyqStatus = if (index < 2) ChapterEntity.PYQ_COMPLETED else ChapterEntity.PYQ_PENDING,
                        mockTestStatus = if (index < 2) ChapterEntity.MOCK_ATTEMPTED else ChapterEntity.MOCK_NOT_ATTEMPTED,
                        revision1Done = index < 2,
                        revision1Date = if (index < 2) "2026-08-10" else null,
                        confidenceRating = if (index == 0) 5 else if (index == 1) 4 else if (index < 4) 3 else 2,
                        difficultyRating = if (index in listOf(2, 8, 9)) 5 else 3,
                        totalStudyHours = if (index < 2) 12.5 else if (index < 4) 6.0 else 0.0,
                        priority = if (index in listOf(2, 8, 9)) "High" else "Normal",
                        orderIndex = index + 1
                    )
                )
            }

            // 3. Insert Chemistry (15 Chapters)
            val chemistryChapters = listOf(
                "Solutions",
                "Electrochemistry",
                "Chemical Kinetics",
                "Surface Chemistry",
                "The Solid State",
                "The p-Block Elements",
                "The d- and f-Block Elements",
                "Coordination Compounds",
                "Haloalkanes and Haloarenes",
                "Alcohols, Phenols and Ethers",
                "Aldehydes, Ketones and Carboxylic Acids",
                "Amines",
                "Biomolecules",
                "Polymers",
                "Chemistry in Everyday Life"
            )

            chemistryChapters.forEachIndexed { index, title ->
                database.chapterDao().insertChapter(
                    ChapterEntity(
                        subjectId = chemistryId,
                        chapterNumber = index + 1,
                        title = title,
                        status = if (index < 2) ChapterEntity.STATUS_COMPLETED else if (index < 5) ChapterEntity.STATUS_LEARNING else ChapterEntity.STATUS_NOT_STARTED,
                        progressPercent = if (index < 2) 100 else if (index < 5) 50 else 0,
                        totalLectures = 8,
                        watchedLectures = if (index < 2) 8 else if (index < 5) 4 else 0,
                        ncertRead = index < 3,
                        ncertRevised = index < 2,
                        notesStatus = if (index < 2) ChapterEntity.NOTES_COMPLETED else if (index < 5) ChapterEntity.NOTES_IN_PROGRESS else ChapterEntity.NOTES_NOT_STARTED,
                        pyqStatus = if (index < 2) ChapterEntity.PYQ_COMPLETED else ChapterEntity.PYQ_PENDING,
                        mockTestStatus = if (index < 2) ChapterEntity.MOCK_ATTEMPTED else ChapterEntity.MOCK_NOT_ATTEMPTED,
                        revision1Done = index < 2,
                        revision1Date = if (index < 2) "2026-08-12" else null,
                        confidenceRating = if (index == 0) 4 else if (index == 1) 4 else 3,
                        difficultyRating = if (index in listOf(1, 5, 10)) 5 else 3,
                        totalStudyHours = if (index < 2) 10.0 else if (index < 5) 4.5 else 0.0,
                        priority = if (index in listOf(1, 5, 10)) "High" else "Normal",
                        orderIndex = index + 1
                    )
                )
            }

            // 4. Insert Biology (16 Chapters)
            val biologyChapters = listOf(
                "Reproduction in Organisms",
                "Sexual Reproduction in Flowering Plants",
                "Human Reproduction",
                "Reproductive Health",
                "Principles of Inheritance and Variation",
                "Molecular Basis of Inheritance",
                "Evolution",
                "Human Health and Disease",
                "Strategies for Enhancement in Food Production",
                "Microbes in Human Welfare",
                "Biotechnology: Principles and Processes",
                "Biotechnology and Its Applications",
                "Organisms and Populations",
                "Ecosystem",
                "Biodiversity and Conservation",
                "Environmental Issues"
            )

            biologyChapters.forEachIndexed { index, title ->
                database.chapterDao().insertChapter(
                    ChapterEntity(
                        subjectId = biologyId,
                        chapterNumber = index + 1,
                        title = title,
                        status = if (index < 4) ChapterEntity.STATUS_COMPLETED else if (index < 6) ChapterEntity.STATUS_LEARNING else ChapterEntity.STATUS_NOT_STARTED,
                        progressPercent = if (index < 4) 100 else if (index < 6) 60 else 0,
                        totalLectures = 12,
                        watchedLectures = if (index < 4) 12 else if (index < 6) 7 else 0,
                        ncertRead = index < 4,
                        ncertRevised = index < 3,
                        notesStatus = if (index < 4) ChapterEntity.NOTES_COMPLETED else if (index < 6) ChapterEntity.NOTES_IN_PROGRESS else ChapterEntity.NOTES_NOT_STARTED,
                        pyqStatus = if (index < 4) ChapterEntity.PYQ_COMPLETED else ChapterEntity.PYQ_PENDING,
                        mockTestStatus = if (index < 3) ChapterEntity.MOCK_ATTEMPTED else ChapterEntity.MOCK_NOT_ATTEMPTED,
                        revision1Done = index < 4,
                        revision1Date = if (index < 4) "2026-08-14" else null,
                        confidenceRating = if (index < 3) 5 else if (index < 5) 4 else 3,
                        difficultyRating = if (index in listOf(4, 5, 10)) 5 else 2,
                        totalStudyHours = if (index < 4) 14.0 else if (index < 6) 8.0 else 0.0,
                        priority = if (index in listOf(4, 5, 10)) "High" else "Normal",
                        orderIndex = index + 1
                    )
                )
            }

            // 5. Seed Timeline Presets
            val schoolDayPresetId = database.timelineDao().insertPreset(
                TimelinePresetEntity(
                    id = 1L,
                    name = "School Day (7h Target)",
                    isActive = true,
                    description = "Hard morning block + Afternoon deep study + Evening revision"
                )
            )

            val holidayPresetId = database.timelineDao().insertPreset(
                TimelinePresetEntity(
                    id = 2L,
                    name = "Holiday / Sunday (10h Target)",
                    isActive = false,
                    description = "All-day high-intensity revision & mock test session"
                )
            )

            val lowEnergyPresetId = database.timelineDao().insertPreset(
                TimelinePresetEntity(
                    id = 3L,
                    name = "Low Energy Mode (3.5h Min)",
                    isActive = false,
                    description = "Emergency survival schedule to protect streak"
                )
            )

            // Seed School Day Blocks
            val schoolBlocks = listOf(
                TimelineBlockEntity(presetId = schoolDayPresetId, title = "Block 1: Early Morning Deep Study", startTime = "06:15", endTime = "08:15", category = "Study", triggerAction = "Water peene ke turant baad start", description = "Physics / High cognitive load subject", orderIndex = 1),
                TimelineBlockEntity(presetId = schoolDayPresetId, title = "Block 2: School / College Transit", startTime = "08:30", endTime = "15:00", category = "School", description = "Classes / Lunch / School hours", orderIndex = 2),
                TimelineBlockEntity(presetId = schoolDayPresetId, title = "Block 3: Afternoon Deep Work", startTime = "15:30", endTime = "18:00", category = "Study", triggerAction = "Ghar aane ke 30m baad table pe", description = "Chemistry / Organic mechanisms & PYQs", orderIndex = 3),
                TimelineBlockEntity(presetId = schoolDayPresetId, title = "Block 4: Fitness & Re-energize", startTime = "18:00", endTime = "18:45", category = "Fitness", description = "Running / Workout / Clean meal", orderIndex = 4),
                TimelineBlockEntity(presetId = schoolDayPresetId, title = "Block 5: Evening Revision & Mock", startTime = "20:15", endTime = "21:30", category = "Study", description = "Spaced repetition + Day scorecard logging", orderIndex = 5),
                TimelineBlockEntity(presetId = schoolDayPresetId, title = "Block 6: Shutdown Ritual & Sleep", startTime = "21:30", endTime = "22:00", category = "Shutdown", description = "Phone off, plan tomorrow, sleep by 10:30 PM", orderIndex = 6)
            )
            schoolBlocks.forEach { database.timelineDao().insertBlock(it) }
        }

        // 6. Seed Streaks if empty
        val streaks = listOf(
            StreakRecordEntity(
                streakKey = StreakRecordEntity.KEY_STUDY,
                title = "Study Streak",
                description = "Daily target study hours completed without missing a day",
                iconName = "menu_book",
                currentStreak = 7,
                bestStreak = 21,
                lastActiveDate = "2026-08-20"
            ),
            StreakRecordEntity(
                streakKey = StreakRecordEntity.KEY_RUNNING,
                title = "Running & Fitness",
                description = "Daily physical activity to maintain high brain oxygen & energy",
                iconName = "directions_run",
                currentStreak = 5,
                bestStreak = 14,
                lastActiveDate = "2026-08-20"
            ),
            StreakRecordEntity(
                streakKey = StreakRecordEntity.KEY_NO_PORN,
                title = "No Porn & Brahmacharya",
                description = "Unwavering mental purity and dopamine baseline restoration",
                iconName = "shield",
                currentStreak = 18,
                bestStreak = 30,
                lastActiveDate = "2026-08-20"
            ),
            StreakRecordEntity(
                streakKey = StreakRecordEntity.KEY_NO_PROCRASTINATION,
                title = "Zero Procrastination",
                description = "Zero phone scroll during study blocks & instant task startup",
                iconName = "bolt",
                currentStreak = 4,
                bestStreak = 12,
                lastActiveDate = "2026-08-20"
            ),
            StreakRecordEntity(
                streakKey = StreakRecordEntity.KEY_REVISION,
                title = "Daily Spaced Revision",
                description = "Completed scheduled revision cards before sleeping",
                iconName = "history_edu",
                currentStreak = 6,
                bestStreak = 15,
                lastActiveDate = "2026-08-20"
            )
        )
        streaks.forEach { database.streakDao().insertStreak(it) }
    }
}
