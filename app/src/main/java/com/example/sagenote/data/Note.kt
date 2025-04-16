package com.example.sagenote.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sagenote.util.getTextColorForBackground
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Enum class defining the types of notes supported by the application
 */
enum class NoteType {
    TEXT,    // Regular text note
    LIST,    // Checklist/to-do list note
    DRAWING, // Drawing/sketch note
    AUDIO    // Voice/audio recording note
}

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
    val isPinned: Boolean = false,
    val type: NoteType = NoteType.TEXT, // Default to text note
    val listItems: List<ListItem> = emptyList(), // For LIST type
    val drawingPath: String = "", // For DRAWING type (path to saved drawing)
    val audioPath: String = "" // For AUDIO type (path to saved audio file)
)

/**
 * Data class for list items in a LIST type note
 */
@Serializable
data class ListItem(
    val id: Long = 0,
    val text: String,
    val isChecked: Boolean = false,
    val position: Int // Position in the list
)
