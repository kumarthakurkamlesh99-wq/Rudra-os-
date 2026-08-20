package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val resourceType: String = TYPE_LINK, // Link, FormulaSheet, BSEBNote, Document, QuestionBank
    val urlOrPath: String,
    val subjectId: Long? = null,
    val subjectName: String = "",
    val tags: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_LINK = "Web Link"
        const val TYPE_FORMULA_SHEET = "Formula Sheet"
        const val TYPE_BSEB_NOTE = "BSEB Short Note"
        const val TYPE_QUESTION_BANK = "Question Bank / PYQ"
        const val TYPE_DOCUMENT = "Reference Doc"
    }
}
