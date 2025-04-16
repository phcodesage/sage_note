package com.example.sagenote.ui.screens

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.sagenote.data.ListItem
import com.example.sagenote.data.Note
import com.example.sagenote.data.NoteType
import com.example.sagenote.util.getTextColorForBackground
import com.example.sagenote.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListNoteScreen(
    noteViewModel: NoteViewModel,
    noteId: Long?,
    onNavigateBack: () -> Unit
) {
    val allNotes by noteViewModel.allNotes.collectAsState()
    val errorMessage by noteViewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
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
    var newItemText by remember { mutableStateOf("") }
    
    // List items state
    val listItems = remember {
        mutableStateListOf<ListItem>().apply {
            if (note != null && note.listItems.isNotEmpty()) {
                addAll(note.listItems)
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
        Color.White.toArgb(), // White
        Color.Red.copy(red = 0.95f, green = 0.55f, blue = 0.51f).toArgb(), // Light Red
        Color.Yellow.copy(red = 0.98f, green = 0.74f, blue = 0.02f).toArgb(), // Yellow
        Color.Yellow.copy(red = 1.0f, green = 0.96f, blue = 0.46f).toArgb(), // Light Yellow
        Color.Green.copy(red = 0.8f, green = 1.0f, blue = 0.56f).toArgb(), // Light Green
        Color.Cyan.copy(red = 0.65f, green = 1.0f, blue = 0.92f).toArgb(), // Teal
        Color.Cyan.copy(red = 0.8f, green = 0.94f, blue = 0.97f).toArgb(), // Light Blue
        Color.Blue.copy(red = 0.68f, green = 0.8f, blue = 0.98f).toArgb(), // Blue
        Color.Magenta.copy(red = 0.84f, green = 0.68f, blue = 0.98f).toArgb(), // Purple
        Color.Magenta.copy(red = 0.99f, green = 0.81f, blue = 0.91f).toArgb()  // Pink
    )
    
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
                        text = if (noteId == null) "Create List" else "Edit List",
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
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Title cannot be empty")
                        }
                        return@FloatingActionButton
                    }
                    
                    // Sort list items by position
                    val sortedItems = listItems.mapIndexed { index, item ->
                        item.copy(position = index)
                    }
                    
                    if (noteId == null) {
                        // Create new note
                        val newNote = Note(
                            title = title,
                            content = "List with ${sortedItems.size} items",
                            color = color,
                            isPinned = isPinned,
                            type = NoteType.LIST,
                            listItems = sortedItems
                        )
                        noteViewModel.insertNote(newNote)
                    } else {
                        // Update existing note
                        note?.let {
                            val updatedNote = it.copy(
                                title = title,
                                content = "List with ${sortedItems.size} items",
                                color = color,
                                isPinned = isPinned,
                                listItems = sortedItems
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
                    contentDescription = "Save list"
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // List items
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(listItems) { index, item ->
                    ListItemRow(
                        item = item,
                        textColor = textColor,
                        onCheckedChange = { isChecked ->
                            listItems[index] = item.copy(isChecked = isChecked)
                        },
                        onDelete = {
                            listItems.removeAt(index)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add new item field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    label = { Text("Add item", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newItemText.isNotBlank()) {
                                addNewItem(newItemText, listItems)
                                newItemText = ""
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        }
                    ),
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
                
                IconButton(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            addNewItem(newItemText, listItems)
                            newItemText = ""
                            focusManager.clearFocus()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add item",
                        tint = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun ListItemRow(
    item: ListItem,
    textColor: Color,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete item",
                tint = textColor.copy(alpha = 0.7f)
            )
        }
    }
}

private fun addNewItem(text: String, items: MutableList<ListItem>) {
    val newItem = ListItem(
        text = text,
        isChecked = false,
        position = items.size
    )
    items.add(newItem)
}
