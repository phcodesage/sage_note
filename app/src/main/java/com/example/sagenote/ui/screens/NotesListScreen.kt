package com.example.sagenote.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sagenote.data.Note
import com.example.sagenote.data.NoteType
import com.example.sagenote.ui.components.MultiFloatingActionButton
import com.example.sagenote.ui.components.NoteItem
import com.example.sagenote.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    noteViewModel: NoteViewModel,
    onNoteClick: (Long) -> Unit,
    onAddClick: (NoteType) -> Unit
) {
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
            TopAppBar(
                title = { Text("SageNote") },
                actions = {
                    IconButton(onClick = { isSearchActive = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search notes"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            MultiFloatingActionButton(
                onFabItemClicked = { noteType ->
                    onAddClick(noteType)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
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
                                                onClick = { onNoteClick(note.id) },
                                                onPinClick = { noteViewModel.togglePinStatus(note) }
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
                                                onClick = { onNoteClick(note.id) },
                                                onPinClick = { noteViewModel.togglePinStatus(note) }
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
