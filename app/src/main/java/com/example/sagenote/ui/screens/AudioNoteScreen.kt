package com.example.sagenote.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.sagenote.data.Note
import com.example.sagenote.data.NoteType
import com.example.sagenote.util.getTextColorForBackground
import com.example.sagenote.viewmodel.NoteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

enum class RecordingState {
    IDLE, RECORDING, PAUSED
}

enum class PlaybackState {
    IDLE, PLAYING, PAUSED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioNoteScreen(
    noteViewModel: NoteViewModel,
    noteId: Long?,
    onNavigateBack: () -> Unit
) {
    val allNotes by noteViewModel.allNotes.collectAsState()
    val errorMessage by noteViewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDarkTheme = isSystemInDarkTheme()
    
    // Find the note if we're editing
    val note = allNotes.find { it.id == noteId }
    
    // Default color for new notes
    val defaultColor = Color(0xFFFFFFFF).toArgb()
    
    // State for the form fields
    var title by rememberSaveable { mutableStateOf(note?.title ?: "") }
    var color by rememberSaveable { mutableStateOf(note?.color ?: defaultColor) }
    var isPinned by rememberSaveable { mutableStateOf(note?.isPinned ?: false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showTitleDialog by remember { mutableStateOf(false) }
    
    // Audio recording state
    var recordingState by remember { mutableStateOf(RecordingState.IDLE) }
    var playbackState by remember { mutableStateOf(PlaybackState.IDLE) }
    var audioFilePath by remember { mutableStateOf(note?.audioPath ?: "") }
    var recordingDuration by remember { mutableLongStateOf(0L) }
    var playbackPosition by remember { mutableFloatStateOf(0f) }
    var playbackDuration by remember { mutableFloatStateOf(0f) }
    
    // Permission state
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Media recorder and player
    val recorder = remember { mutableStateOf<MediaRecorder?>(null) }
    val player = remember { mutableStateOf<MediaPlayer?>(null) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordPermission = isGranted
        if (isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permission granted! You can now record audio.")
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permission denied. Cannot record audio.")
            }
        }
    }
    
    // Derive text color from background color, considering dark theme
    val textColor by remember(color, isDarkTheme) {
        derivedStateOf {
            if (isDarkTheme && Color(color).red < 0.5f) {
                Color.White
            } else {
                Color(getTextColorForBackground(color))
            }
        }
    }
    
    // Available colors for notes
    val noteColors = listOf(
        Color(0xFFFFFFFF).toArgb(), // White
        Color(0xFFF28B82).toArgb(), // Light Red
        Color(0xFFFBBC04).toArgb(), // Yellow
        Color(0xFFFFF475).toArgb(), // Light Yellow
        Color(0xFFCBFF90).toArgb(), // Light Green
        Color(0xFFA7FFEB).toArgb(), // Teal
        Color(0xFFCBF0F8).toArgb(), // Light Blue
        Color(0xFFAECBFA).toArgb(), // Blue
        Color(0xFFD7AEFB).toArgb(), // Purple
        Color(0xFFFDCFE8).toArgb()  // Pink
    )
    
    // Format time
    val formatTime = remember {
        { timeMs: Long ->
            val totalSeconds = timeMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    // Lifecycle observer to clean up resources
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                // Stop recording if active
                if (recordingState != RecordingState.IDLE) {
                    stopRecording(recorder)
                    recordingState = RecordingState.IDLE
                }
                
                // Stop playback if active
                if (playbackState != PlaybackState.IDLE) {
                    player.value?.stop()
                    player.value?.release()
                    player.value = null
                    playbackState = PlaybackState.IDLE
                }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            
            // Clean up resources
            recorder.value?.release()
            recorder.value = null
            
            player.value?.release()
            player.value = null
        }
    }
    
    // Timer for recording duration
    LaunchedEffect(recordingState) {
        if (recordingState == RecordingState.RECORDING) {
            while (true) {
                delay(1.seconds / 10)
                recordingDuration += 100
            }
        }
    }
    
    // Timer for playback progress
    LaunchedEffect(playbackState) {
        if (playbackState == PlaybackState.PLAYING) {
            while (true) {
                player.value?.let {
                    if (it.isPlaying) {
                        playbackPosition = it.currentPosition.toFloat()
                        delay(100.milliseconds)
                    } else if (playbackPosition >= playbackDuration) {
                        playbackPosition = 0f
                        playbackState = PlaybackState.IDLE
                        return@LaunchedEffect
                    } else {
                        return@LaunchedEffect
                    }
                } ?: return@LaunchedEffect
            }
        }
    }
    
    // Load existing audio if editing
    LaunchedEffect(noteId) {
        if (noteId != null && note?.audioPath?.isNotEmpty() == true) {
            audioFilePath = note.audioPath
            
            // Initialize player to get duration
            try {
                val file = File(context.filesDir, audioFilePath)
                if (file.exists()) {
                    val mp = MediaPlayer().apply {
                        setDataSource(file.absolutePath)
                        prepare()
                    }
                    playbackDuration = mp.duration.toFloat()
                    mp.release()
                }
            } catch (e: Exception) {
                Log.e("AudioNoteScreen", "Error loading audio", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error loading audio: ${e.message}")
                }
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (noteId == null) "Create Audio Note" else "Edit Audio Note",
                        color = textColor
                    )
                },
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
                            isPinned = !isPinned
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (isPinned) "Unpin note" else "Pin note",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.5f)
                        )
                    }
                    
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (title.isBlank()) {
                        showTitleDialog = true
                        return@FloatingActionButton
                    }
                    
                    if (audioFilePath.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please record audio before saving")
                        }
                        return@FloatingActionButton
                    }
                    
                    if (noteId == null) {
                        // Create new note
                        val newNote = Note(
                            title = title,
                            content = "Audio recording",
                            color = color,
                            isPinned = isPinned,
                            type = NoteType.AUDIO,
                            audioPath = audioFilePath
                        )
                        noteViewModel.insertNote(newNote)
                    } else {
                        // Update existing note
                        note?.let {
                            val updatedNote = it.copy(
                                title = title,
                                color = color,
                                isPinned = isPinned,
                                audioPath = audioFilePath
                            )
                            noteViewModel.updateNote(updatedNote)
                        }
                    }
                    onNavigateBack()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save audio note"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(color)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title", color = textColor.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = textColor,
                    focusedIndicatorColor = textColor,
                    unfocusedIndicatorColor = textColor.copy(alpha = 0.5f),
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
                color = textColor
            )
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .border(
                        width = 1.dp,
                        color = textColor.copy(alpha = 0.2f),
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
            if (showColorPicker) {
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Audio recording UI
            if (audioFilePath.isBlank()) {
                // Recording controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Record Audio",
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Recording time
                    Text(
                        text = formatTime(recordingDuration),
                        style = MaterialTheme.typography.headlineMedium,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Recording button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                when (recordingState) {
                                    RecordingState.RECORDING -> MaterialTheme.colorScheme.error
                                    RecordingState.PAUSED -> MaterialTheme.colorScheme.tertiary
                                    RecordingState.IDLE -> MaterialTheme.colorScheme.primary
                                }
                            )
                            .clickable {
                                when (recordingState) {
                                    RecordingState.IDLE -> {
                                        if (hasRecordPermission) {
                                            startRecording(context, recorder)
                                            recordingState = RecordingState.RECORDING
                                            recordingDuration = 0L
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                    RecordingState.RECORDING -> {
                                        pauseRecording(recorder)
                                        recordingState = RecordingState.PAUSED
                                    }
                                    RecordingState.PAUSED -> {
                                        resumeRecording(recorder)
                                        recordingState = RecordingState.RECORDING
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (recordingState) {
                                RecordingState.RECORDING -> Icons.Default.Pause
                                RecordingState.PAUSED -> Icons.Default.Mic
                                RecordingState.IDLE -> Icons.Default.Mic
                            },
                            contentDescription = when (recordingState) {
                                RecordingState.RECORDING -> "Pause Recording"
                                RecordingState.PAUSED -> "Resume Recording"
                                RecordingState.IDLE -> "Start Recording"
                            },
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Stop button (only shown when recording or paused)
                    if (recordingState != RecordingState.IDLE) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                                .clickable {
                                    val filePath = stopRecording(recorder)
                                    filePath?.let {
                                        audioFilePath = it
                                    }
                                    recordingState = RecordingState.IDLE
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Recording",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            } else {
                // Playback controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Audio Recording",
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
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
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor
                        )
                        Text(
                            text = formatTime(playbackDuration.toLong()),
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Playback controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play/Pause button
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    when (playbackState) {
                                        PlaybackState.IDLE -> {
                                            startPlayback(context, player, audioFilePath)
                                            playbackState = PlaybackState.PLAYING
                                        }
                                        PlaybackState.PLAYING -> {
                                            pausePlayback(player)
                                            playbackState = PlaybackState.PAUSED
                                        }
                                        PlaybackState.PAUSED -> {
                                            resumePlayback(player)
                                            playbackState = PlaybackState.PLAYING
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (playbackState == PlaybackState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playbackState == PlaybackState.PLAYING) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Record new button
                    TextButton(
                        onClick = {
                            audioFilePath = ""
                            playbackState = PlaybackState.IDLE
                            player.value?.release()
                            player.value = null
                        }
                    ) {
                        Text("Record New Audio", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        
        // Title dialog
        if (showTitleDialog) {
            AlertDialog(
                onDismissRequest = { showTitleDialog = false },
                title = { Text("Enter Note Title") },
                text = {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                showTitleDialog = false
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Title cannot be empty")
                                }
                            }
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTitleDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}



// Pause recording (only available on API 24+)
private fun pauseRecording(recorder: androidx.compose.runtime.MutableState<MediaRecorder?>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        recorder.value?.pause()
    }
}

// Resume recording (only available on API 24+)
private fun resumeRecording(recorder: androidx.compose.runtime.MutableState<MediaRecorder?>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        recorder.value?.resume()
    }
}

// Store the current recording file path
private var currentRecordingPath: String? = null

// Start recording
private fun startRecording(context: android.content.Context, recorder: androidx.compose.runtime.MutableState<MediaRecorder?>) {
    try {
        // Create a unique file name
        val fileName = "audio_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.3gp"
        val file = File(context.filesDir, fileName)
        currentRecordingPath = fileName
        
        // Initialize recorder
        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(file.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
            start()
        }
        
        recorder.value = mediaRecorder
    } catch (e: IOException) {
        Log.e("AudioNoteScreen", "Error starting recording", e)
        currentRecordingPath = null
    }
}

// Stop recording
private fun stopRecording(recorder: androidx.compose.runtime.MutableState<MediaRecorder?>): String? {
    try {
        val currentRecorder = recorder.value
        val outputPath = currentRecordingPath
        
        if (currentRecorder is MediaRecorder) {
            currentRecorder.apply {
                stop()
                release()
            }
        }
        
        recorder.value = null
        currentRecordingPath = null
        
        return outputPath
    } catch (e: Exception) {
        Log.e("AudioNoteScreen", "Error stopping recording", e)
        return null
    }
}

// Start playback
private fun startPlayback(
    context: android.content.Context,
    player: androidx.compose.runtime.MutableState<MediaPlayer?>,
    audioFilePath: String
) {
    try {
        val file = File(context.filesDir, audioFilePath)
        if (!file.exists()) {
            Log.e("AudioNoteScreen", "Audio file does not exist: ${file.absolutePath}")
            return
        }
        
        player.value?.release()
        
        player.value = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
            
            setOnCompletionListener {
                player.value = null
            }
        }
    } catch (e: Exception) {
        Log.e("AudioNoteScreen", "Error starting playback", e)
    }
}

// Pause playback
private fun pausePlayback(player: androidx.compose.runtime.MutableState<MediaPlayer?>) {
    player.value?.pause()
}

// Resume playback
private fun resumePlayback(player: androidx.compose.runtime.MutableState<MediaPlayer?>) {
    player.value?.start()
}
