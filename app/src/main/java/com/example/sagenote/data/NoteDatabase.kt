package com.example.sagenote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sagenote.util.DateTimeConverters
import com.example.sagenote.util.NoteTypeConverters

@Database(entities = [Note::class], version = 3, exportSchema = false)
@TypeConverters(DateTimeConverters::class, NoteTypeConverters::class)
abstract class NoteDatabase : RoomDatabase() {
    
    abstract fun noteDao(): NoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null
        
        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                .fallbackToDestructiveMigration() // This will recreate the database if the version changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
