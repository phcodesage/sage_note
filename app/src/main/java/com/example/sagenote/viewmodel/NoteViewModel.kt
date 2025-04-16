package com.example.sagenote.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sagenote.SageNoteApplication
import com.example.sagenote.data.Note
import com.example.sagenote.data.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "NoteViewModel"
    }
    
    private val repository: NoteRepository
    
    // State for all notes
    val allNotes: StateFlow<List<Note>>
    
    // State for search results
    private val _searchResults = MutableStateFlow<List<Note>>(emptyList())
    val searchResults: StateFlow<List<Note>> = _searchResults.asStateFlow()
    
    // State for the current search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // State for error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        Log.d(TAG, "Initializing NoteViewModel")
        // Get the repository from the application
        repository = (application as SageNoteApplication).repository
        Log.d(TAG, "Repository obtained from application")
        
        allNotes = repository.allNotes
            .catch { e ->
                Log.e(TAG, "Error loading notes: ${e.message}", e)
                _errorMessage.value = "Error loading notes: ${e.message}"
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        Log.d(TAG, "NoteViewModel initialized")
    }
    
    fun insertNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "Inserting note: $note")
        try {
            val id = repository.insertNote(note)
            Log.d(TAG, "Note inserted with id: $id")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating note: ${e.message}", e)
            _errorMessage.value = "Error creating note: ${e.message}"
        }
    }
    
    fun updateNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "Updating note: $note")
        try {
            repository.updateNote(note)
            Log.d(TAG, "Note updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating note: ${e.message}", e)
            _errorMessage.value = "Error updating note: ${e.message}"
        }
    }
    
    fun deleteNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "Deleting note: $note")
        try {
            repository.deleteNote(note)
            Log.d(TAG, "Note deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting note: ${e.message}", e)
            _errorMessage.value = "Error deleting note: ${e.message}"
        }
    }
    
    fun searchNotes(query: String) {
        Log.d(TAG, "Searching notes with query: $query")
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                Log.d(TAG, "Search query is blank, returning empty list")
            } else {
                repository.searchNotes(query)
                    .catch { e ->
                        Log.e(TAG, "Error searching notes: ${e.message}", e)
                        _errorMessage.value = "Error searching notes: ${e.message}"
                        emit(emptyList())
                    }
                    .collect { results ->
                        Log.d(TAG, "Search returned ${results.size} results")
                        _searchResults.value = results
                    }
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun togglePinStatus(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "Toggling pin status for note: $note")
        try {
            val updatedNote = note.copy(isPinned = !note.isPinned)
            repository.updateNote(updatedNote)
            Log.d(TAG, "Pin status toggled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating pin status: ${e.message}", e)
            _errorMessage.value = "Error updating pin status: ${e.message}"
        }
    }
}
