package com.example.sagenote.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.sagenote.data.Note
import com.example.sagenote.data.NoteType
import com.example.sagenote.ui.components.MultiFloatingActionButton
import com.example.sagenote.ui.components.NoteItem
import kotlinx.coroutines.launch
import com.example.sagenote.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onNoteClick: (Long) -> Unit,
    onCreateNoteClick: (NoteType) -> Unit,
    noteViewModel: NoteViewModel
) {
    // Selection mode state
    val selectedNotes = remember { mutableStateListOf<Note>() }
    val isSelectionMode = selectedNotes.isNotEmpty()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // More options dropdown menu state
    var showMoreOptions by remember { mutableStateOf(false) }

    // Color picker state
    var showColorPicker by remember { mutableStateOf(false) }

    // Available colors for notes - using softer colors
    val noteColors = listOf(
        Color(0xFFFFFFF0),  // Soft white
        Color(0xFFFFCCBC),  // Soft red
        Color(0xFFFFE0B2),  // Soft yellow
        Color(0xFFFFF9C4),  // Very soft yellow
        Color(0xFFC8E6C9),  // Soft green
        Color(0xFFB2DFDB),  // Soft teal
        Color(0xFFBBDEFB),  // Soft light blue
        Color(0xFF90CAF9),  // Soft blue
        Color(0xFFD1C4E9),  // Soft purple
        Color(0xFFF8BBD0)   // Soft pink
    )

    val notes by noteViewModel.allNotes.collectAsState()
    val searchResults by noteViewModel.searchResults.collectAsState()
    val searchQuery by noteViewModel.searchQuery.collectAsState()
    val errorMessage by noteViewModel.errorMessage.collectAsState()

    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            noteViewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (isSelectionMode) {
                // Selection mode top bar
                TopAppBar(
                    title = { Text("${selectedNotes.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedNotes.clear() }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit selection mode")
                        }
                    },
                    actions = {
                        // Pin/Unpin button
                        IconButton(onClick = {
                            selectedNotes.forEach { note ->
                                val updatedNote = note.copy(isPinned = !note.isPinned)
                                noteViewModel.updateNote(updatedNote)
                            }
                            selectedNotes.clear()
                        }) {
                            Icon(Icons.Default.PushPin, contentDescription = "Toggle pin")
                        }

                        // Color theme button
                        IconButton(onClick = {
                            showColorPicker = true
                        }) {
                            Icon(Icons.Default.Palette, contentDescription = "Change color")
                        }

                        // More options menu
                        Box {
                            IconButton(onClick = { showMoreOptions = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }

                            DropdownMenu(
                                expanded = showMoreOptions,
                                onDismissRequest = { showMoreOptions = false }
                            ) {
                                // Delete option
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                    onClick = {
                                        coroutineScope.launch {
                                            selectedNotes.forEach { note ->
                                                noteViewModel.deleteNote(note)
                                            }
                                            selectedNotes.clear()
                                            showMoreOptions = false
                                        }
                                    }
                                )

                                // Archive option
                                DropdownMenuItem(
                                    text = { Text("Archive") },
                                    leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                                    onClick = {
                                        // TODO: Implement archive functionality
                                        showMoreOptions = false
                                        selectedNotes.clear()
                                    }
                                )

                                // Make a copy option
                                DropdownMenuItem(
                                    text = { Text("Make a copy") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        coroutineScope.launch {
                                            selectedNotes.forEach { note ->
                                                val newNote = note.copy(id = 0, title = "${note.title} (Copy)")
                                                noteViewModel.insertNote(newNote)
                                            }
                                            selectedNotes.clear()
                                            showMoreOptions = false
                                        }
                                    }
                                )

                                // Send option
                                DropdownMenuItem(
                                    text = { Text("Send") },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                    onClick = {
                                        // TODO: Implement share functionality
                                        showMoreOptions = false
                                        selectedNotes.clear()
                                    }
                                )

                                // Copy to clipboard option
                                DropdownMenuItem(
                                    text = { Text("Copy to clipboard") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        val text = selectedNotes.joinToString("\n\n") { 
                                            "${it.title}\n${it.content}" 
                                        }
                                        clipboardManager.setText(AnnotatedString(text))
                                        selectedNotes.clear()
                                        showMoreOptions = false
                                    }
                                )
                            }
                        }
                    }
                )
            } else if (isSearchActive) {
                // Empty top bar when search is active (SearchBar is shown below)
                TopAppBar(
                    title = { /* No title in search mode */ }
                )
            } else {
                // Normal top bar
                TopAppBar(
                    title = { Text("SageNote") },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search notes")
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        },
        floatingActionButton = {
            MultiFloatingActionButton(
                onFabItemClicked = { noteType ->
                    onCreateNoteClick(noteType)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        // Color picker dialog
        if (showColorPicker) {
            AlertDialog(
                onDismissRequest = { showColorPicker = false },
                title = { Text("Select a color") },
                text = {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(noteColors.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(noteColors[index])
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        // Update all selected notes with the new color
                                        coroutineScope.launch {
                                            selectedNotes.forEach { note ->
                                                val updatedNote = note.copy(color = noteColors[index].toArgb())
                                                noteViewModel.updateNote(updatedNote)
                                            }
                                            showColorPicker = false
                                            selectedNotes.clear()
                                        }
                                    }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showColorPicker = false }) {
                        Text("Cancel")
                    }
                },
                properties = DialogProperties(dismissOnClickOutside = true)
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                // Search Bar
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = spring()
                    ),
                    exit = fadeOut()
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { noteViewModel.searchNotes(it) },
                        onSearch = { /* Already handled in onQueryChange */ },
                        active = isSearchActive,
                        onActiveChange = { isSearchActive = it },
                        placeholder = { Text("Search notes...") },
                        leadingIcon = {
                            IconButton(onClick = { isSearchActive = false }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back from search")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Search results
                        if (searchResults.isNotEmpty()) {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                items(searchResults) { note ->
                                    NoteItem(
                                        note = note,
                                        onClick = { onNoteClick(note.id) },
                                        onPinClick = { noteViewModel.togglePinStatus(note) }
                                    )
                                }
                            }
                        } else if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "No results found for '$searchQuery'",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Notes Grid
                if (notes.isEmpty() && !isSearchActive) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notes yet. Tap + to create one!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else if (!isSearchActive) {
                    // Filter notes into pinned and unpinned
                    val pinnedNotes = notes.filter { it.isPinned }
                    val unpinnedNotes = notes.filter { !it.isPinned }

                    // Use a different approach to avoid nested scrollable containers
                    // and ensure the Others section fills the bottom of the screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Pinned Notes Section
                            if (pinnedNotes.isNotEmpty()) {
                                Text(
                                    text = "Pinned",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
                                )

                                // Calculate a reasonable height based on number of notes and screen size
                                val pinnedGridHeight = (pinnedNotes.size / 2 + pinnedNotes.size % 2) * 140

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(pinnedGridHeight.coerceAtLeast(140).dp)
                                ) {
                                    LazyVerticalStaggeredGrid(
                                        columns = StaggeredGridCells.Fixed(2),
                                        contentPadding = PaddingValues(bottom = 16.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(pinnedNotes) { note ->
                                            NoteItem(
                                                note = note,
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        if (selectedNotes.contains(note)) {
                                                            selectedNotes.remove(note)
                                                        } else {
                                                            selectedNotes.add(note)
                                                        }
                                                    } else {
                                                        onNoteClick(note.id)
                                                    }
                                                },
                                                onPinClick = { noteViewModel.togglePinStatus(note) },
                                                isSelected = selectedNotes.contains(note),
                                                onLongClick = {
                                                    if (!isSelectionMode) {
                                                        selectedNotes.add(note)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Unpinned Notes Section
                            if (unpinnedNotes.isNotEmpty()) {
                                Text(
                                    text = "Others",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
                                )

                                // Calculate height to fill remaining space
                                // We use a larger multiplier to ensure it extends to bottom
                                val unpinnedGridHeight = (unpinnedNotes.size / 2 + unpinnedNotes.size % 2) * 140

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(unpinnedGridHeight.coerceAtLeast(500).dp)
                                ) {
                                    LazyVerticalStaggeredGrid(
                                        columns = StaggeredGridCells.Fixed(2),
                                        contentPadding = PaddingValues(bottom = 80.dp), // Extra padding at bottom for FAB
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(unpinnedNotes) { note ->
                                            NoteItem(
                                                note = note,
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        if (selectedNotes.contains(note)) {
                                                            selectedNotes.remove(note)
                                                        } else {
                                                            selectedNotes.add(note)
                                                        }
                                                    } else {
                                                        onNoteClick(note.id)
                                                    }
                                                },
                                                onPinClick = { noteViewModel.togglePinStatus(note) },
                                                isSelected = selectedNotes.contains(note),
                                                onLongClick = {
                                                    if (!isSelectionMode) {
                                                        selectedNotes.add(note)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
