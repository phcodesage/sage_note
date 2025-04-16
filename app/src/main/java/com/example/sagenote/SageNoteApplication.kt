package com.example.sagenote

import android.app.Application
import android.util.Log
import com.example.sagenote.data.NoteDatabase
import com.example.sagenote.data.NoteRepository

class SageNoteApplication : Application() {
    
    companion object {
        private const val TAG = "SageNoteApplication"
    }
    
    // Lazy initialization of the database
    val database by lazy { 
        Log.d(TAG, "Initializing database")
        NoteDatabase.getDatabase(this).also {
            Log.d(TAG, "Database initialized successfully")
        }
    }
    
    // Lazy initialization of the repository
    val repository by lazy { 
        Log.d(TAG, "Initializing repository")
        NoteRepository(database.noteDao()).also {
            Log.d(TAG, "Repository initialized successfully")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SageNoteApplication onCreate called")
        // Initialize database and repository early
        val db = database
        val repo = repository
        Log.d(TAG, "Database and repository initialized in onCreate")
    }
}
