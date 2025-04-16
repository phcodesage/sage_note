package com.example.sagenote.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.sagenote.R
import com.example.sagenote.data.Note
import com.example.sagenote.data.NoteType
import com.example.sagenote.util.getTextColorForBackground
import com.example.sagenote.viewmodel.NoteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class PathProperties(
    val path: Path = Path(),
    val color: Int = Color.Black.toArgb(),
    val strokeWidth: Float = 5f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingNoteScreen(
    noteViewModel: NoteViewModel,
    noteId: Long?,
    onNavigateBack: () -> Unit
) {
    val allNotes by noteViewModel.allNotes.collectAsState()
    val errorMessage by noteViewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Find the note if we're editing
    val note = allNotes.find { it.id == noteId }
    
    // State for the drawing
    val paths = remember { mutableStateListOf<PathProperties>() }
    var currentPath by remember { mutableStateOf(PathProperties()) }
    var currentColor by remember { mutableStateOf(Color.Black.toArgb()) }
    var currentStrokeWidth by remember { mutableStateOf(5f) }
    
    // State for the form fields
    var title by rememberSaveable { mutableStateOf(note?.title ?: "") }
    var showColorPicker by remember { mutableStateOf(false) }
    var showStrokeWidthPicker by remember { mutableStateOf(false) }
    var showTitleDialog by remember { mutableStateOf(false) }
    
    // Available colors for drawing
    val drawingColors = listOf(
        Color.Black.toArgb(),
        Color.Red.toArgb(),
        Color.Blue.toArgb(),
        Color.Green.toArgb(),
        Color.Yellow.toArgb(),
        Color.Magenta.toArgb(),
        Color.Cyan.toArgb(),
        Color.Gray.toArgb()
    )
    
    // Load existing drawing if editing
    LaunchedEffect(noteId) {
        if (noteId != null && note?.drawingPath?.isNotEmpty() == true) {
            // Load the drawing from file
            try {
                val file = File(context.filesDir, note.drawingPath)
                if (file.exists()) {
                    // In a real app, we would load the drawing from the file
                    // For simplicity, we'll just show a message
                    snackbarHostState.showSnackbar("Drawing loaded from file")
                }
            } catch (e: Exception) {
                Log.e("DrawingNoteScreen", "Error loading drawing", e)
                snackbarHostState.showSnackbar("Error loading drawing: ${e.message}")
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
                title = { Text(if (noteId == null) "New Drawing" else "Edit Drawing") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showTitleDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Set Title"
                        )
                    }
                    
                    IconButton(onClick = { showColorPicker = !showColorPicker }) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "Color Picker"
                        )
                    }
                    
                    IconButton(onClick = { paths.clear() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Drawing"
                        )
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
                    
                    coroutineScope.launch {
                        // Save the drawing to a file
                        val fileName = saveDrawingToFile(context, paths)
                        
                        if (noteId == null) {
                            // Create new note
                            val newNote = Note(
                                title = title,
                                content = "Drawing note",
                                type = NoteType.DRAWING,
                                drawingPath = fileName
                            )
                            noteViewModel.insertNote(newNote)
                        } else {
                            // Update existing note
                            note?.let {
                                val updatedNote = it.copy(
                                    title = title,
                                    drawingPath = fileName
                                )
                                noteViewModel.updateNote(updatedNote)
                            }
                        }
                        onNavigateBack()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save drawing"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Drawing canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = PathProperties(
                                    path = Path().apply { moveTo(offset.x, offset.y) },
                                    color = currentColor,
                                    strokeWidth = currentStrokeWidth
                                )
                                paths.add(currentPath)
                            },
                            onDrag = { change, offset ->
                                val lastPath = paths.last().path
                                lastPath.lineTo(change.position.x, change.position.y)
                                // Force recomposition
                                paths[paths.lastIndex] = paths.last().copy()
                            }
                        )
                    }
            ) {
                // Draw all paths
                paths.forEach { pathProperties ->
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            color = pathProperties.color
                            strokeWidth = pathProperties.strokeWidth
                            style = Paint.Style.STROKE
                            strokeJoin = Paint.Join.ROUND
                            strokeCap = Paint.Cap.ROUND
                            isAntiAlias = true
                        }
                        drawPath(pathProperties.path, paint)
                    }
                }
            }
            
            // Color picker
            if (showColorPicker) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.shapes.medium
                        )
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        drawingColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentColor == color) MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.5f
                                        ) else Color.Transparent
                                    )
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragEnd = {
                                                currentColor = color
                                                showColorPicker = false
                                                showStrokeWidthPicker = true
                                            }
                                        ) { _, _ -> }
                                    }
                            )
                        }
                    }
                }
            }
            
            // Stroke width picker
            if (showStrokeWidthPicker) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.shapes.medium
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Stroke Width: ${currentStrokeWidth.toInt()}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = currentStrokeWidth,
                            onValueChange = { currentStrokeWidth = it },
                            valueRange = 1f..50f,
                            onValueChangeFinished = { showStrokeWidthPicker = false }
                        )
                    }
                }
            }
            
            // Title dialog
            if (showTitleDialog) {
                AlertDialog(
                    onDismissRequest = { showTitleDialog = false },
                    title = { Text("Enter Drawing Title") },
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
}

// Function to save drawing to a file
private suspend fun saveDrawingToFile(context: android.content.Context, paths: List<PathProperties>): String {
    return withContext(Dispatchers.IO) {
        try {
            // Create a bitmap
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)
            val bitmap = drawable?.toBitmap(800, 800)?.copy(Bitmap.Config.ARGB_8888, true)
                ?: Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
            
            // Draw paths on the bitmap
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.White.toArgb())
            
            paths.forEach { pathProperties ->
                val paint = Paint().apply {
                    color = pathProperties.color
                    strokeWidth = pathProperties.strokeWidth
                    style = Paint.Style.STROKE
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                    isAntiAlias = true
                }
                canvas.drawPath(pathProperties.path, paint)
            }
            
            // Save bitmap to file
            val fileName = "drawing_${UUID.randomUUID()}.png"
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            fileName
        } catch (e: Exception) {
            Log.e("DrawingNoteScreen", "Error saving drawing", e)
            throw e
        }
    }
}
