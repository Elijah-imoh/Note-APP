package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.NoteRepository
import com.example.ui.screens.MainWorkspaceScreen
import com.example.ui.theme.MyNotesTheme
import com.example.ui.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefsRepo = remember(context) { UserPreferencesRepository(context.applicationContext) }
            val userPrefs by prefsRepo.preferencesFlow.collectAsStateWithLifecycle(initialValue = UserPreferences())
            val systemDark = isSystemInDarkTheme()
            val isDark = when (userPrefs.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MyNotesTheme(
                darkTheme = isDark,
                fontScale = userPrefs.fontScaleMultiplier
            ) {
                OfflineWorkspaceRoot()
            }
        }
    }
}

@Composable
fun OfflineWorkspaceRoot() {
    val viewModel: NotesViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                NotesViewModel(
                    repository = NoteRepository(app),
                    preferencesRepository = UserPreferencesRepository(app)
                )
            }
        }
    )
    MainWorkspaceScreen(viewModel = viewModel)
}
