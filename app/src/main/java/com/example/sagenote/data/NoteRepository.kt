package com.example.sagenote.data

import android.util.Log
import com.example.sagenote.util.getTextColorForBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Repository for handling note data operations
 */
class NoteRepository(private val noteDao: NoteDao) {
    
    companion object {
        private const val TAG = "NoteRepository"
    }
    
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    
    suspend fun getNoteById(id: Long): Note? {
        Log.d(TAG, "Getting note with id: $id")
        return noteDao.getNoteById(id)
    }
    
    suspend fun insertNote(note: Note): Long {
        Log.d(TAG, "Inserting note: $note")
        // Ensure textColor is set based on the background color
        val noteWithTextColor = note.copy(textColor = getTextColorForBackground(note.color))
        val id = noteDao.insertNote(noteWithTextColor)
        Log.d(TAG, "Note inserted with id: $id")
        return id
    }
    
    suspend fun updateNote(note: Note) {
        Log.d(TAG, "Updating note: $note")
        val currentTime = Clock.System.now()
        // Ensure textColor is set based on the background color
        val updatedNote = note.copy(
            updatedAt = currentTime,
            textColor = getTextColorForBackground(note.color)
        )
        noteDao.updateNote(updatedNote)
        Log.d(TAG, "Note updated successfully")
    }
    
    suspend fun deleteNote(note: Note) {
        Log.d(TAG, "Deleting note: $note")
        noteDao.deleteNote(note)
        Log.d(TAG, "Note deleted successfully")
    }
    
    suspend fun deleteNoteById(id: Long) {
        Log.d(TAG, "Deleting note with id: $id")
        noteDao.deleteNoteById(id)
        Log.d(TAG, "Note with id $id deleted successfully")
    }
    
    fun searchNotes(query: String): Flow<List<Note>> {
        Log.d(TAG, "Searching notes with query: $query")
        return noteDao.searchNotes(query)
    }
}
