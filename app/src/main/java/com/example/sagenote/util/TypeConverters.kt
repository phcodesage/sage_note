package com.example.sagenote.util

import androidx.room.TypeConverter
import com.example.sagenote.data.ListItem
import com.example.sagenote.data.NoteType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Type converters for Room to handle custom types
 */
class NoteTypeConverters {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // NoteType converters
    @TypeConverter
    fun fromNoteType(noteType: NoteType): String {
        return noteType.name
    }
    
    @TypeConverter
    fun toNoteType(value: String): NoteType {
        return try {
            NoteType.valueOf(value)
        } catch (e: Exception) {
            NoteType.TEXT // Default to TEXT if there's an error
        }
    }
    
    // ListItem converters
    @TypeConverter
    fun fromListItems(listItems: List<ListItem>): String {
        return json.encodeToString(listItems)
    }
    
    @TypeConverter
    fun toListItems(value: String): List<ListItem> {
        return try {
            if (value.isBlank()) {
                emptyList()
            } else {
                json.decodeFromString(value)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
