package com.example.sagenote.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sagenote.ui.screens.AddEditNoteScreen
import com.example.sagenote.ui.screens.NoteDetailScreen
import com.example.sagenote.ui.screens.NotesListScreen
import com.example.sagenote.viewmodel.NoteViewModel

sealed class Screen(val route: String) {
    object NotesList : Screen("notes_list")
    object AddNote : Screen("add_note")
    object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: Long) = "edit_note/$noteId"
    }
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Long) = "note_detail/$noteId"
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
                onAddClick = {
                    navController.navigate(Screen.AddNote.route)
                }
            )
        }
        
        composable(route = Screen.AddNote.route) {
            AddEditNoteScreen(
                noteViewModel = noteViewModel,
                onNavigateBack = { navController.popBackStack() },
                noteId = null
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
                onEditClick = { id ->
                    navController.navigate(Screen.EditNote.createRoute(id))
                }
            )
        }
    }
}
