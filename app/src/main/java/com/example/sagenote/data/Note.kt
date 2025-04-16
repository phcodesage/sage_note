package com.example.sagenote.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sagenote.util.getTextColorForBackground
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Entity(tableName = "notes")
@Serializable
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val color: Int = Color.White.toArgb(), // Default white color
    val textColor: Int = getTextColorForBackground(Color.White.toArgb()), // Text color based on background
    val isPinned: Boolean = false
)
