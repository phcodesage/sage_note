package com.example.sagenote.ui.screens

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sagenote.data.NoteType
import com.example.sagenote.viewmodel.NoteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteViewModel: NoteViewModel,
    noteId: Long,
    onNavigateBack: () -> Unit,
    onEditClick: (Long, NoteType) -> Unit
) {
    val allNotes by noteViewModel.allNotes.collectAsState()
    val errorMessage by noteViewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    
    // Find the note
    val note = allNotes.find { it.id == noteId }
    
    // Audio playback state
    var isPlaying by remember { mutableStateOf(false) }
    var playbackPosition by remember { mutableFloatStateOf(0f) }
    var playbackDuration by remember { mutableFloatStateOf(0f) }
    val player = remember { mutableStateOf<MediaPlayer?>(null) }
    
    // Format time
    val formatTime = remember {
        { timeMs: Long ->
            val totalSeconds = timeMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    // Clean up resources when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            player.value?.release()
            player.value = null
        }
    }
    
    // Timer for playback progress
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                player.value?.let {
                    if (it.isPlaying) {
                        playbackPosition = it.currentPosition.toFloat()
                        delay(100.milliseconds)
                    } else {
                        isPlaying = false
                        playbackPosition = 0f
                        return@LaunchedEffect
                    }
                } ?: return@LaunchedEffect
            }
        }
    }
    
    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            noteViewModel.clearError()
        }
    }
    
    // If note is null, show error and navigate back
    if (note == null) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("Note not found")
            onNavigateBack()
        }
        return
    }
    
    // For app bar, always use white text in dark mode
    val appBarTextColor = if (isDarkTheme) {
        Color.White
    } else {
        Color(note.textColor)
    }
    
    // For content, use the calculated text color based on background
    val contentTextColor = Color(note.textColor)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = note.title,
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
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                noteViewModel.togglePinStatus(note)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (note.isPinned) "Unpin note" else "Pin note",
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else appBarTextColor.copy(alpha = 0.5f)
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                noteViewModel.deleteNote(note)
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
                onClick = { onEditClick(noteId, note.type) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit note"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(note.color)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (note.type) {
                NoteType.TEXT -> {
                    // Text note content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = contentTextColor
                        )
                    }
                }
                
                NoteType.LIST -> {
                    // List note content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "Checklist",
                            style = MaterialTheme.typography.titleMedium,
                            color = contentTextColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(note.listItems) { item ->
                                val updatedListItems = note.listItems.toMutableList()
                                val index = updatedListItems.indexOf(item)
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable {
                                            // Toggle the checkbox when clicked
                                            if (index != -1) {
                                                updatedListItems[index] = item.copy(isChecked = !item.isChecked)
                                                // Update the note with the new list items
                                                val updatedNote = note.copy(listItems = updatedListItems)
                                                noteViewModel.updateNote(updatedNote)
                                            }
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = item.isChecked,
                                        onCheckedChange = { isChecked ->
                                            // Update the list item when checkbox is clicked
                                            if (index != -1) {
                                                updatedListItems[index] = item.copy(isChecked = isChecked)
                                                // Update the note with the new list items
                                                val updatedNote = note.copy(listItems = updatedListItems)
                                                noteViewModel.updateNote(updatedNote)
                                            }
                                        },
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    
                                    Text(
                                        text = item.text,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = contentTextColor
                                    )
                                }
                            }
                        }
                    }
                }
                
                NoteType.DRAWING -> {
                    // Drawing note content
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (note.drawingPath.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .background(Color.Gray.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Brush,
                                        contentDescription = "Drawing",
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Text(
                                        text = "Drawing Preview",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(top = 80.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "No drawing available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = contentTextColor
                            )
                        }
                    }
                }
                
                NoteType.AUDIO -> {
                    // Audio note content
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (note.audioPath.isNotEmpty()) {
                            // Audio player UI
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Audio Recording",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    // Playback progress
                                    LinearProgressIndicator(
                                        progress = { if (playbackDuration > 0) playbackPosition / playbackDuration else 0f },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Playback time
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = formatTime(playbackPosition.toLong()),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = formatTime(playbackDuration.toLong()),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    // Play button
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(4.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .clickable(onClick = {
                                                if (isPlaying) {
                                                    player.value?.stop()
                                                    player.value?.release()
                                                    player.value = null
                                                    isPlaying = false
                                                    playbackPosition = 0f
                                                } else {
                                                    try {
                                                        val file = File(context.filesDir, note.audioPath)
                                                        if (file.exists()) {
                                                            player.value?.release()
                                                            player.value = MediaPlayer().apply {
                                                                setDataSource(file.absolutePath)
                                                                prepare()
                                                                playbackDuration = duration.toFloat()
                                                                start()
                                                                
                                                                setOnCompletionListener {
                                                                    isPlaying = false
                                                                    playbackPosition = 0f
                                                                }
                                                            }
                                                            isPlaying = true
                                                        } else {
                                                            coroutineScope.launch {
                                                                snackbarHostState.showSnackbar("Audio file not found")
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("NoteDetailScreen", "Error playing audio", e)
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar("Error playing audio: ${e.message}")
                                                        }
                                                    }
                                                }
                                            }),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Stop" else "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Audio",
                                modifier = Modifier.size(64.dp),
                                tint = appBarTextColor.copy(alpha = 0.5f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No audio recording available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = contentTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}


