package com.example.sagenote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sagenote.util.formatDate
import com.example.sagenote.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteViewModel: NoteViewModel,
    noteId: Long,
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit
) {
    val allNotes by noteViewModel.allNotes.collectAsState()
    val errorMessage by noteViewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Find the note
    val note = allNotes.find { it.id == noteId }
    
    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            noteViewModel.clearError()
        }
    }
    
    // If note not found, navigate back
    LaunchedEffect(note) {
        if (note == null) {
            onNavigateBack()
        }
    }
    
    note?.let { currentNote ->
        val textColor = Color(currentNote.textColor)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Note Details", color = textColor) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textColor
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                noteViewModel.togglePinStatus(currentNote)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = if (currentNote.isPinned) "Unpin note" else "Pin note",
                                tint = if (currentNote.isPinned) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.5f)
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    noteViewModel.deleteNote(currentNote)
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
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onEditClick(noteId) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit note"
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(currentNote.color)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Title
                    Text(
                        text = currentNote.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Date
                    Text(
                        text = "Created: ${formatDate(currentNote.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.6f)
                    )
                    
                    Text(
                        text = "Updated: ${formatDate(currentNote.updatedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Content
                    Text(
                        text = currentNote.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                }
            }
        }
    }
}
