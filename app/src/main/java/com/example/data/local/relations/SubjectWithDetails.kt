package com.example.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.StudySessionEntity
import com.example.data.local.entities.SubjectEntity
import com.example.data.local.entities.TaskEntity

/**
 * 1:N relationship model between Subject and its Chapters.
 */
data class SubjectWithChapters(
    @Embedded
    val subject: SubjectEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "subjectId"
    )
    val chapters: List<ChapterEntity>
)

/**
 * 1:N relationship model between Subject and its associated Tasks.
 */
data class SubjectWithTasks(
    @Embedded
    val subject: SubjectEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "subjectId"
    )
    val tasks: List<TaskEntity>
)

/**
 * 1:N relationship model between Subject and its recorded Study Sessions.
 */
data class SubjectWithStudySessions(
    @Embedded
    val subject: SubjectEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "subjectId"
    )
    val studySessions: List<StudySessionEntity>
)
