package com.example.sagenote.ui.screens

import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.example.sagenote.data.Note
import com.example.sagenote.data.NoteType
import com.example.sagenote.ui.theme.*
import com.example.sagenote.util.getTextColorForBackground
import com.example.sagenote.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    noteViewModel: NoteViewModel,
    onNavigateBack: () -> Unit,
    noteId: Long?,
    noteType: NoteType = NoteType.TEXT
) {
    val allNotes by noteViewModel.allNotes.collectAsState()
    val errorMessage by noteViewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val isDarkTheme = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current
    
    // Find the note if we're editing
    val note = allNotes.find { it.id == noteId }
    
    // Default color for new notes
    val defaultColor = Color(0xFFFFFFFF).toArgb()
    
    // State for the form fields
    var title by rememberSaveable { mutableStateOf(note?.title ?: "") }
    var content by rememberSaveable { mutableStateOf(note?.content ?: "") }
    var color by rememberSaveable { mutableStateOf(note?.color ?: defaultColor) }
    var isPinned by rememberSaveable { mutableStateOf(note?.isPinned ?: false) }
    var showColorPicker by remember { mutableStateOf(false) }
    
    // For app bar, always use white text in dark mode
    val appBarTextColor = if (isDarkTheme) {
        Color.White
    } else {
        Color(getTextColorForBackground(color))
    }
    
    // For content, use the calculated text color based on background
    val contentTextColor = if (isDarkTheme && Color(color).red < 0.5f) {
        Color.White
    } else {
        Color(getTextColorForBackground(color))
    }
    
    // Available colors for notes - using softer colors
    val noteColors = listOf(
        Color(0xFFFFFFF0).toArgb(),  // Soft white
        Color(0xFFFFCCBC).toArgb(),  // Soft red
        Color(0xFFFFE0B2).toArgb(),  // Soft yellow
        Color(0xFFFFF9C4).toArgb(),  // Very soft yellow
        Color(0xFFC8E6C9).toArgb(),  // Soft green
        Color(0xFFB2DFDB).toArgb(),  // Soft teal
        Color(0xFFBBDEFB).toArgb(),  // Soft light blue
        Color(0xFF90CAF9).toArgb(),  // Soft blue
        Color(0xFFD1C4E9).toArgb(),  // Soft purple
        Color(0xFFF8BBD0).toArgb()   // Soft pink
    )
    
    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            noteViewModel.clearError()
        }
    }
    
    // Auto-save functionality
    LaunchedEffect(title, content, color, isPinned) {
        // Only auto-save if we're editing an existing note and have valid data
        if (noteId != null && title.isNotBlank()) {
            // Debounce the auto-save to avoid too many database operations
            kotlinx.coroutines.delay(1000) // Wait 1 second after typing stops
            note?.let {
                val updatedNote = it.copy(
                    title = title,
                    content = content,
                    color = color,
                    isPinned = isPinned
                )
                noteViewModel.updateNote(updatedNote)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (noteId == null) "Create Note" else "Edit Note",
                        color = appBarTextColor
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = appBarTextColor
                        )
                    }
                },
                actions = {
                    // Pin button
                    IconButton(
                        onClick = {
                            isPinned = !isPinned
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (isPinned) "Unpin note" else "Pin note",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else appBarTextColor.copy(alpha = 0.5f)
                        )
                    }
                    
                    // Save button in top-right corner
                    IconButton(
                        onClick = {
                            if (title.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Title cannot be empty")
                                }
                                return@IconButton
                            }
                            
                            if (noteId == null) {
                                // Create new note
                                val newNote = Note(
                                    title = title,
                                    content = content,
                                    color = color,
                                    isPinned = isPinned,
                                    type = noteType
                                )
                                noteViewModel.insertNote(newNote)
                            } else {
                                // Update existing note
                                note?.let {
                                    val updatedNote = it.copy(
                                        title = title,
                                        content = content,
                                        color = color,
                                        isPinned = isPinned
                                    )
                                    noteViewModel.updateNote(updatedNote)
                                }
                            }
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save note",
                            tint = appBarTextColor
                        )
                    }
                    
                    // Delete button (only for existing notes)
                    if (noteId != null) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    note?.let { noteViewModel.deleteNote(it) }
                                    onNavigateBack()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete note",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        // Removed floating action button as save is now in the top bar
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(color)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title", color = contentTextColor.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = contentTextColor,
                    unfocusedTextColor = contentTextColor,
                    cursorColor = contentTextColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content", color = contentTextColor.copy(alpha = 0.7f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Make content field expand to fill available space
                singleLine = false,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = contentTextColor,
                    unfocusedTextColor = contentTextColor,
                    cursorColor = contentTextColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Color picker button
            Text(
                text = "Note Color",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
                color = contentTextColor
            )
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .border(
                        width = 1.dp,
                        color = contentTextColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .clickable { 
                        // Hide keyboard when showing color picker
                        focusManager.clearFocus()
                        showColorPicker = !showColorPicker 
                    }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Color picker
            AnimatedVisibility(
                visible = showColorPicker,
                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    noteColors.forEach { noteColor ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(noteColor))
                                .border(
                                    width = if (color == noteColor) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable {
                                    color = noteColor
                                    showColorPicker = false
                                }
                        )
                    }
                }
            }
        }
    }
}
