package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subjects",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["orderIndex"]),
        Index(value = ["name"])
    ]
)
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String,
    val colorHex: String,
    val iconName: String = "menu_book",
    val description: String = "",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

