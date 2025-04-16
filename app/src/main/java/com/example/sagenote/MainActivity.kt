package com.example.sagenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.sagenote.navigation.NavGraph
import com.example.sagenote.ui.theme.SageNoteTheme
import com.example.sagenote.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SageNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SageNoteApp()
                }
            }
        }
    }
}

@Composable
fun SageNoteApp(
    noteViewModel: NoteViewModel = viewModel()
) {
    val navController = rememberNavController()
    
    NavGraph(
        navController = navController,
        noteViewModel = noteViewModel
    )
}