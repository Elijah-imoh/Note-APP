package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.Folder
import com.example.data.model.Note
import com.example.data.model.NoteLayoutStyle
import com.example.data.model.NoteWorkspaceFilter
import com.example.ui.components.ConfirmPermanentDeleteDialog
import com.example.ui.components.EditorialEmptyState
import com.example.ui.components.FolderEditorDialog
import com.example.ui.components.NoteContextBottomSheet
import com.example.ui.components.PasscodeLockDialog
import com.example.ui.components.SwipeableNoteItem
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.viewmodel.NotesViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.NoteTextToSpeechManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class PrimaryStudioTab(val labelRes: Int) {
    NOTES(R.string.nav_notes),
    FOLDERS(R.string.nav_folders),
    VOICE(R.string.nav_voice),
    SETTINGS(R.string.nav_settings)
}

private data class EditorSessionState(
    val note: Note?,
    val startWithChecklist: Boolean = false
)

@Composable
fun MainWorkspaceScreen(
    viewModel: NotesViewModel
) {
    val context = LocalContext.current
    val ttsManager = remember(context) { NoteTextToSpeechManager(context) }

    DisposableEffect(ttsManager) {
        onDispose { ttsManager.shutdown() }
    }

    val notesUiState by viewModel.notesState.collectAsStateWithLifecycle()
    val foldersUiState by viewModel.foldersState.collectAsStateWithLifecycle()
    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val unlockedNoteIds by viewModel.unlockedNoteIds.collectAsStateWithLifecycle()
    val lastTrashedNote by viewModel.lastTrashedNote.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    val isSpeaking by ttsManager.isSpeaking.collectAsStateWithLifecycle()
    val speakingNoteId by ttsManager.speakingNoteId.collectAsStateWithLifecycle()

    val allNotes = (notesUiState as? UiState.Success)?.data ?: emptyList()
    val allFolders = (foldersUiState as? UiState.Success)?.data ?: emptyList()

    var currentTab by remember { mutableStateOf(PrimaryStudioTab.NOTES) }
    var activeEditorSession by remember { mutableStateOf<EditorSessionState?>(null) }
    var contextMenuNote by remember { mutableStateOf<Note?>(null) }
    var notePendingPermanentDelete by remember { mutableStateOf<Note?>(null) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var folderEditorTarget by remember { mutableStateOf<Pair<Boolean, Folder?>>(false to null) }
    var passcodeDialogAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var isSettingNewPinInDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(lastTrashedNote) {
        val trashed = lastTrashedNote ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Moved “${trashed.title.ifBlank { "Untitled" }}” to Trash",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoMoveToTrash()
        } else {
            viewModel.dismissTrashUndo()
        }
    }

    LaunchedEffect(statusBannerMessage) {
        val msg = statusBannerMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
        viewModel.clearStatusBanner()
    }

    fun speakNoteHelper(note: Note) {
        if (isSpeaking && speakingNoteId == note.id) {
            ttsManager.stop()
        } else {
            val checklistSpoken = note.parsedChecklist.joinToString(". ") { it.text }
            val fullText = listOf(note.title, note.content, checklistSpoken)
                .filter { it.isNotBlank() }
                .joinToString(". ")
            ttsManager.speak(
                noteId = note.id,
                text = fullText.ifBlank { "Empty note" },
                speechRate = preferences.ttsSpeechRate
            )
        }
    }

    fun openNoteWithLockCheck(note: Note) {
        if (note.isLocked && !unlockedNoteIds.contains(note.id)) {
            isSettingNewPinInDialog = false
            passcodeDialogAction = {
                viewModel.markNoteUnlockedForSession(note.id)
                activeEditorSession = EditorSessionState(note = note)
            }
        } else {
            activeEditorSession = EditorSessionState(note = note)
        }
    }

    val editorSession = activeEditorSession
    if (editorSession != null) {
        val liveNote = editorSession.note?.let { existing ->
            allNotes.firstOrNull { it.id == existing.id } ?: existing
        }
        NoteEditorScreen(
            initialNote = liveNote,
            folders = allFolders,
            startWithChecklist = editorSession.startWithChecklist,
            isSpeakingThisNote = isSpeaking && speakingNoteId == (liveNote?.id ?: "new_note"),
            onToggleReadAloud = { textToSpeak ->
                ttsManager.speak(
                    noteId = liveNote?.id ?: "new_note",
                    text = textToSpeak,
                    speechRate = preferences.ttsSpeechRate
                )
            },
            onStopReadAloud = { ttsManager.stop() },
            onRequestPasscodeSetupOrVerify = { onVerified ->
                isSettingNewPinInDialog = false
                passcodeDialogAction = onVerified
            },
            onSaveAndClose = { title, content, folderId, tags, checklist, isPinned, isArchived, isLocked, colorKey, reminderAtMillis ->
                viewModel.saveNote(
                    existingNote = liveNote,
                    title = title,
                    content = content,
                    folderId = folderId,
                    tags = tags,
                    checklist = checklist,
                    isPinned = isPinned,
                    isArchived = isArchived,
                    isLocked = isLocked,
                    colorKey = colorKey,
                    reminderAtMillis = reminderAtMillis
                )
                activeEditorSession = null
            },
            onDiscardOrDelete = {
                activeEditorSession = null
            }
        )
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                MinimalFloatingDock(
                    currentTab = currentTab,
                    isChecklistFilterActive = filterState.workspaceFilter == NoteWorkspaceFilter.CHECKLISTS,
                    onSelectTab = { tab ->
                        if (tab == PrimaryStudioTab.NOTES) {
                            viewModel.selectWorkspaceFilter(NoteWorkspaceFilter.ALL)
                        }
                        currentTab = tab
                    },
                    onNewNoteClick = {
                        activeEditorSession = EditorSessionState(note = null)
                    },
                    onToggleChecklistView = {
                        currentTab = PrimaryStudioTab.NOTES
                        val nextFilter = if (filterState.workspaceFilter == NoteWorkspaceFilter.CHECKLISTS) {
                            NoteWorkspaceFilter.ALL
                        } else {
                            NoteWorkspaceFilter.CHECKLISTS
                        }
                        viewModel.selectWorkspaceFilter(nextFilter)
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    PrimaryStudioTab.NOTES -> NotesStreamScreen(
                        notesUiState = notesUiState,
                        filteredNotes = filteredNotes,
                        folders = allFolders,
                        filterState = filterState,
                        layoutStyle = preferences.layoutStyle,
                        unlockedNoteIds = unlockedNoteIds,
                        isSpeaking = isSpeaking,
                        speakingNoteId = speakingNoteId,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onStepMonth = viewModel::stepMonth,
                        onResetMonth = viewModel::resetMonthFilter,
                        onSelectWorkspaceFilter = viewModel::selectWorkspaceFilter,
                        onSelectFolderFilter = viewModel::selectFolderFilter,
                        onSelectTagFilter = viewModel::selectTagFilter,
                        onClearFilters = viewModel::clearAllFilters,
                        onOpenSettings = { currentTab = PrimaryStudioTab.SETTINGS },
                        onOpenNote = ::openNoteWithLockCheck,
                        onOpenContextMenu = { contextMenuNote = it },
                        onTogglePin = viewModel::togglePin,
                        onMoveToTrash = viewModel::moveToTrash,
                        onToggleChecklistItem = viewModel::toggleChecklistItemOnCard,
                        onReadAloud = ::speakNoteHelper,
                        onCreateBlankNote = {
                            activeEditorSession = EditorSessionState(note = null)
                        },
                        onCreateChecklistNote = {
                            activeEditorSession = EditorSessionState(
                                note = null,
                                startWithChecklist = true
                            )
                        },
                        onEmptyTrashClick = { showEmptyTrashDialog = true }
                    )

                    PrimaryStudioTab.FOLDERS -> FoldersScreen(
                        folders = allFolders,
                        allNotes = allNotes,
                        onOpenFolderNotes = { folder ->
                            viewModel.setExplicitFolderFilter(folder.id)
                            currentTab = PrimaryStudioTab.NOTES
                        },
                        onCreateFolderClick = {
                            folderEditorTarget = true to null
                        },
                        onEditFolderClick = { folder ->
                            folderEditorTarget = true to folder
                        },
                        onDeleteFolderClick = { folder ->
                            viewModel.deleteFolder(folder)
                        }
                    )

                    PrimaryStudioTab.VOICE -> VoiceAndListenScreen(
                        allNotes = allNotes,
                        unlockedNoteIds = unlockedNoteIds,
                        isSpeaking = isSpeaking,
                        speakingNoteId = speakingNoteId,
                        speechRate = preferences.ttsSpeechRate,
                        onChangeSpeechRate = { rate ->
                            viewModel.setTtsSpeechRate(rate)
                            ttsManager.setRate(rate)
                        },
                        onPlayNoteAloud = ::speakNoteHelper,
                        onStopAudio = { ttsManager.stop() },
                        onOpenNote = ::openNoteWithLockCheck
                    )

                    PrimaryStudioTab.SETTINGS -> SettingsScreen(
                        preferences = preferences,
                        totalNotesCount = allNotes.count { !it.isTrashed },
                        archivedCount = allNotes.count { !it.isTrashed && it.isArchived },
                        trashedCount = allNotes.count { it.isTrashed },
                        lockedCount = allNotes.count { !it.isTrashed && it.isLocked },
                        onSelectTheme = viewModel::setThemeMode,
                        onSelectLayoutStyle = viewModel::setLayoutStyle,
                        onSelectSortOrder = viewModel::setSortOrder,
                        onChangeFontScale = viewModel::setFontScale,
                        onConfigurePasscodeClick = {
                            isSettingNewPinInDialog = true
                            passcodeDialogAction = {}
                        },
                        onOpenWorkspaceFilter = { filter ->
                            viewModel.selectWorkspaceFilter(filter)
                            currentTab = PrimaryStudioTab.NOTES
                        }
                    )
                }
            }
        }
    }

    val targetMenuNote = contextMenuNote
    if (targetMenuNote != null) {
        NoteContextBottomSheet(
            note = targetMenuNote,
            folders = allFolders,
            onDismiss = { contextMenuNote = null },
            onTogglePin = { viewModel.togglePin(targetMenuNote) },
            onSelectColor = { palette ->
                viewModel.updateNoteColor(targetMenuNote, palette)
                contextMenuNote = targetMenuNote.copy(colorKey = palette.key)
            },
            onMoveToFolder = { folderId ->
                viewModel.moveNoteToFolder(targetMenuNote, folderId)
            },
            onToggleLock = {
                if (targetMenuNote.isLocked) {
                    isSettingNewPinInDialog = false
                    passcodeDialogAction = {
                        viewModel.toggleLock(targetMenuNote, false)
                    }
                } else {
                    isSettingNewPinInDialog = false
                    passcodeDialogAction = {
                        viewModel.toggleLock(targetMenuNote, true)
                    }
                }
            },
            onReadAloud = { speakNoteHelper(targetMenuNote) },
            onDuplicate = { viewModel.duplicateNote(targetMenuNote) },
            onToggleArchive = { viewModel.toggleArchive(targetMenuNote) },
            onMoveToTrash = { viewModel.moveToTrash(targetMenuNote) },
            onRestoreFromTrash = { viewModel.restoreFromTrash(targetMenuNote) },
            onRequestDeleteForever = {
                notePendingPermanentDelete = targetMenuNote
            }
        )
    }

    val deleteTarget = notePendingPermanentDelete
    if (deleteTarget != null) {
        ConfirmPermanentDeleteDialog(
            title = stringResource(R.string.dialog_delete_forever_title),
            body = stringResource(R.string.dialog_delete_forever_body),
            onDismiss = { notePendingPermanentDelete = null },
            onConfirm = {
                viewModel.deleteNoteForever(deleteTarget.id)
                notePendingPermanentDelete = null
            }
        )
    }

    if (showEmptyTrashDialog) {
        ConfirmPermanentDeleteDialog(
            title = "Empty Trash permanently?",
            body = "All notes in Trash will be permanently deleted and cannot be recovered.",
            confirmLabel = "Empty Trash",
            onDismiss = { showEmptyTrashDialog = false },
            onConfirm = {
                viewModel.emptyTrash()
                showEmptyTrashDialog = false
            }
        )
    }

    if (folderEditorTarget.first) {
        FolderEditorDialog(
            initialFolder = folderEditorTarget.second,
            onDismiss = { folderEditorTarget = false to null },
            onSave = { name, iconKey, accentKey ->
                viewModel.saveFolder(folderEditorTarget.second, name, iconKey, accentKey)
            }
        )
    }

    val pendingPasscodeCallback = passcodeDialogAction
    if (pendingPasscodeCallback != null) {
        PasscodeLockDialog(
            storedPinCode = preferences.lockPinCode,
            isSettingNewPin = isSettingNewPinInDialog,
            onDismiss = {
                passcodeDialogAction = null
                isSettingNewPinInDialog = false
            },
            onPinVerifiedOrCreated = { enteredPin ->
                if (preferences.lockPinCode.isBlank() || isSettingNewPinInDialog) {
                    viewModel.setLockPinCode(enteredPin)
                }
                val action = pendingPasscodeCallback
                passcodeDialogAction = null
                isSettingNewPinInDialog = false
                action.invoke()
            }
        )
    }
}

@Composable
private fun MinimalFloatingDock(
    currentTab: PrimaryStudioTab,
    isChecklistFilterActive: Boolean,
    onSelectTab: (PrimaryStudioTab) -> Unit,
    onNewNoteClick: () -> Unit,
    onToggleChecklistView: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val dockContainerColor = if (isDark) Color(0xFF1E1E22) else Color(0xFF18181B)
    val dockBorderColor = if (isDark) Color(0xFF323236) else Color(0xFF27272A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(34.dp),
            color = dockContainerColor,
            border = BorderStroke(1.dp, dockBorderColor),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Notes Stream Tab
                IconButton(
                    onClick = { onSelectTab(PrimaryStudioTab.NOTES) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("nav_tab_notes")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Public,
                        contentDescription = stringResource(R.string.nav_notes),
                        tint = if (currentTab == PrimaryStudioTab.NOTES && !isChecklistFilterActive) {
                            Color.White
                        } else {
                            Color(0xFF9E9EA6)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 2. Listen Aloud (Audio) Tab
                IconButton(
                    onClick = { onSelectTab(PrimaryStudioTab.VOICE) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("nav_tab_voice")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Headphones,
                        contentDescription = stringResource(R.string.nav_voice),
                        tint = if (currentTab == PrimaryStudioTab.VOICE) {
                            Color.White
                        } else {
                            Color(0xFF9E9EA6)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 3. Center Circular Monochrome New Note Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onNewNoteClick)
                        .testTag("fab_new_note"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.action_new_note),
                        tint = Color(0xFF111113),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // 4. Folders / Collections Tab
                IconButton(
                    onClick = { onSelectTab(PrimaryStudioTab.FOLDERS) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("nav_tab_folders")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = stringResource(R.string.nav_folders),
                        tint = if (currentTab == PrimaryStudioTab.FOLDERS) {
                            Color.White
                        } else {
                            Color(0xFF9E9EA6)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 5. Checklists / Quick Notes Filter
                IconButton(
                    onClick = onToggleChecklistView,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("quick_capture_checklist_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = stringResource(R.string.action_new_checklist),
                        tint = if (currentTab == PrimaryStudioTab.NOTES && isChecklistFilterActive) {
                            Color.White
                        } else {
                            Color(0xFF9E9EA6)
                        },
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesStreamScreen(
    notesUiState: UiState<List<Note>>,
    filteredNotes: List<Note>,
    folders: List<Folder>,
    filterState: com.example.ui.viewmodel.WorkspaceFilterState,
    layoutStyle: NoteLayoutStyle,
    unlockedNoteIds: Set<String>,
    isSpeaking: Boolean,
    speakingNoteId: String?,
    onSearchQueryChange: (String) -> Unit,
    onStepMonth: (Int) -> Unit,
    onResetMonth: () -> Unit,
    onSelectWorkspaceFilter: (NoteWorkspaceFilter) -> Unit,
    onSelectFolderFilter: (String?) -> Unit,
    onSelectTagFilter: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNote: (Note) -> Unit,
    onOpenContextMenu: (Note) -> Unit,
    onTogglePin: (Note) -> Unit,
    onMoveToTrash: (Note) -> Unit,
    onToggleChecklistItem: (Note, Int) -> Unit,
    onReadAloud: (Note) -> Unit,
    onCreateBlankNote: () -> Unit,
    onCreateChecklistNote: () -> Unit,
    onEmptyTrashClick: () -> Unit
) {
    val folderMap = remember(folders) { folders.associateBy { it.id } }
    val monthTitle = remember(filterState.monthOffset) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, filterState.monthOffset)
        }
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // 1. Minimal Centered Header ("My Notes") + Right Settings Gear Icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (filterState.workspaceFilter == NoteWorkspaceFilter.TRASH && filteredNotes.isNotEmpty()) {
                            TextButton(
                                onClick = onEmptyTrashClick,
                                modifier = Modifier.testTag("empty_trash_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Empty")
                            }
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("nav_tab_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.nav_settings),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Clean White Pill Search Bar ("Search notes...")
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = filterState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(
                                autoCorrectEnabled = true,
                                imeAction = ImeAction.Search
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (filterState.searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search notes...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("workspace_search_input")
                        )
                        if (filterState.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Clean White Pill Month Navigator ("<  October 2026  >")
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onStepMonth(-1) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("month_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                                contentDescription = "Previous month",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onResetMonth() }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )

                        IconButton(
                            onClick = { onStepMonth(1) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("month_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = "Next month",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Active Filter Pill Banner (shown only when a non-default filter, folder, or tag is active)
                val hasActiveContextFilter = filterState.workspaceFilter != NoteWorkspaceFilter.ALL ||
                    filterState.selectedFolderId != null ||
                    filterState.selectedTag != null

                if (hasActiveContextFilter) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        if (filterState.workspaceFilter != NoteWorkspaceFilter.ALL) {
                            FilterChip(
                                selected = true,
                                onClick = { onSelectWorkspaceFilter(NoteWorkspaceFilter.ALL) },
                                label = { Text("${filterState.workspaceFilter.label} ✕") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                        filterState.selectedFolderId?.let { folderId ->
                            val folderName = folderMap[folderId]?.name ?: "Folder"
                            FilterChip(
                                selected = true,
                                onClick = { onSelectFolderFilter(null) },
                                label = { Text("$folderName ✕") }
                            )
                        }
                        filterState.selectedTag?.let { activeTag ->
                            FilterChip(
                                selected = true,
                                onClick = { onSelectTagFilter(null) },
                                label = { Text("$activeTag ✕") }
                            )
                        }
                    }
                }
            }

            // 4. Clean Minimal Note Cards Stream
            when (notesUiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = notesUiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                is UiState.Success -> {
                    val isFilteredOrSearched = filterState.searchQuery.isNotBlank() ||
                        filterState.selectedTag != null ||
                        filterState.selectedFolderId != null ||
                        filterState.filterByMonth ||
                        filterState.workspaceFilter != NoteWorkspaceFilter.ALL

                    AnimatedVisibility(
                        visible = filteredNotes.isEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        EditorialEmptyState(
                            isSearchFiltered = isFilteredOrSearched,
                            activeFilterLabel = if (filterState.filterByMonth) monthTitle else filterState.workspaceFilter.label,
                            onCreateBlankNote = onCreateBlankNote,
                            onCreateChecklistNote = onCreateChecklistNote,
                            onClearFilters = onClearFilters
                        )
                    }

                    if (filteredNotes.isNotEmpty()) {
                        if (layoutStyle == NoteLayoutStyle.MASONRY_GRID) {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Adaptive(minSize = 240.dp),
                                verticalItemSpacing = 14.dp,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 8.dp,
                                    bottom = 24.dp
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("notes_masonry_grid")
                            ) {
                                items(filteredNotes, key = { it.id }) { note ->
                                    SwipeableNoteItem(
                                        note = note,
                                        folder = folderMap[note.folderId],
                                        isUnlocked = unlockedNoteIds.contains(note.id),
                                        isSpeakingThisNote = isSpeaking && speakingNoteId == note.id,
                                        compactMode = false,
                                        onNoteClick = { onOpenNote(note) },
                                        onNoteLongPress = { onOpenContextMenu(note) },
                                        onTogglePin = { onTogglePin(note) },
                                        onMoveToTrash = { onMoveToTrash(note) },
                                        onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) },
                                        onReadAloudClick = { onReadAloud(note) },
                                        onTagClick = { tag -> onSelectTagFilter(tag) }
                                    )
                                }
                            }
                        } else {
                            val isCompact = layoutStyle == NoteLayoutStyle.COMPACT_INDEX
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 14.dp),
                                contentPadding = PaddingValues(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 8.dp,
                                    bottom = 24.dp
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("notes_list_column")
                            ) {
                                items(filteredNotes, key = { it.id }) { note ->
                                    SwipeableNoteItem(
                                        note = note,
                                        folder = folderMap[note.folderId],
                                        isUnlocked = unlockedNoteIds.contains(note.id),
                                        isSpeakingThisNote = isSpeaking && speakingNoteId == note.id,
                                        compactMode = isCompact,
                                        onNoteClick = { onOpenNote(note) },
                                        onNoteLongPress = { onOpenContextMenu(note) },
                                        onTogglePin = { onTogglePin(note) },
                                        onMoveToTrash = { onMoveToTrash(note) },
                                        onToggleChecklistItem = { idx -> onToggleChecklistItem(note, idx) },
                                        onReadAloudClick = { onReadAloud(note) },
                                        onTagClick = { tag -> onSelectTagFilter(tag) }
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
