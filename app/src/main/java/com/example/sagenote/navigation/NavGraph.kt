package com.example.sagenote.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sagenote.data.NoteType
import com.example.sagenote.ui.screens.AddEditNoteScreen
import com.example.sagenote.ui.screens.AudioNoteScreen
import com.example.sagenote.ui.screens.DrawingNoteScreen
import com.example.sagenote.ui.screens.ListNoteScreen
import com.example.sagenote.ui.screens.NoteDetailScreen
import com.example.sagenote.ui.screens.NotesListScreen
import com.example.sagenote.viewmodel.NoteViewModel

sealed class Screen(val route: String) {
    object NotesList : Screen("notes_list")
    object AddNote : Screen("add_note/{noteType}") {
        fun createRoute(noteType: NoteType) = "add_note/${noteType.name}"
    }
    object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: Long) = "edit_note/$noteId"
    }
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Long) = "note_detail/$noteId"
    }
    object DrawingNote : Screen("drawing_note/{noteId}") {
        fun createRoute(noteId: Long?) = "drawing_note/${noteId ?: -1L}"
    }
    object ListNote : Screen("list_note/{noteId}") {
        fun createRoute(noteId: Long?) = "list_note/${noteId ?: -1L}"
    }
    object AudioNote : Screen("audio_note/{noteId}") {
        fun createRoute(noteId: Long?) = "audio_note/${noteId ?: -1L}"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    noteViewModel: NoteViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.NotesList.route
    ) {
        composable(route = Screen.NotesList.route) {
            NotesListScreen(
                noteViewModel = noteViewModel,
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                },
                onAddClick = { noteType ->
                    when (noteType) {
                        NoteType.TEXT -> navController.navigate(Screen.AddNote.createRoute(NoteType.TEXT))
                        NoteType.DRAWING -> navController.navigate(Screen.DrawingNote.createRoute(null))
                        NoteType.LIST -> navController.navigate(Screen.ListNote.createRoute(null))
                        NoteType.AUDIO -> navController.navigate(Screen.AudioNote.createRoute(null))
                    }
                }
            )
        }
        
        composable(
            route = Screen.AddNote.route,
            arguments = listOf(
                navArgument("noteType") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val noteTypeStr = backStackEntry.arguments?.getString("noteType") ?: NoteType.TEXT.name
            val noteType = try {
                NoteType.valueOf(noteTypeStr)
            } catch (e: Exception) {
                NoteType.TEXT
            }
            
            AddEditNoteScreen(
                noteViewModel = noteViewModel,
                onNavigateBack = { navController.popBackStack() },
                noteId = null,
                noteType = noteType
            )
        }
        
        composable(
            route = Screen.EditNote.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            AddEditNoteScreen(
                noteViewModel = noteViewModel,
                onNavigateBack = { navController.popBackStack() },
                noteId = noteId
            )
        }
        
        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            NoteDetailScreen(
                noteViewModel = noteViewModel,
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { id, noteType ->
                    when (noteType) {
                        NoteType.TEXT -> navController.navigate(Screen.EditNote.createRoute(id))
                        NoteType.DRAWING -> navController.navigate(Screen.DrawingNote.createRoute(id))
                        NoteType.LIST -> navController.navigate(Screen.ListNote.createRoute(id))
                        NoteType.AUDIO -> navController.navigate(Screen.AudioNote.createRoute(id))
                    }
                }
            )
        }
        
        composable(
            route = Screen.DrawingNote.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            val isNewNote = noteId == -1L
            
            DrawingNoteScreen(
                noteViewModel = noteViewModel,
                noteId = if (isNewNote) null else noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.ListNote.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            val isNewNote = noteId == -1L
            
            ListNoteScreen(
                noteViewModel = noteViewModel,
                noteId = if (isNewNote) null else noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.AudioNote.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            val isNewNote = noteId == -1L
            
            AudioNoteScreen(
                noteViewModel = noteViewModel,
                noteId = if (isNewNote) null else noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
