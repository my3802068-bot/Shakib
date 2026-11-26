package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cloud_notes")
data class CloudNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneId: Long,
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)
