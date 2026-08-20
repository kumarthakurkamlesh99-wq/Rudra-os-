package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*

@Database(
    entities = [
        SubjectEntity::class,
        ChapterEntity::class,
        RevisionLogEntity::class,
        TimelinePresetEntity::class,
        TimelineBlockEntity::class,
        TaskEntity::class,
        StudySessionEntity::class,
        ScorecardEntity::class,
        JournalEntryEntity::class,
        BrainDumpEntity::class,
        ResourceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun revisionLogDao(): RevisionLogDao
    abstract fun timelineDao(): TimelineDao
    abstract fun taskDao(): TaskDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun scorecardDao(): ScorecardDao
    abstract fun journalDao(): JournalDao
    abstract fun brainDumpDao(): BrainDumpDao
    abstract fun resourceDao(): ResourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rudra_life_os.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

