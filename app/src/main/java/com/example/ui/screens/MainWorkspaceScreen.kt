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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewHeadline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.Folder
import com.example.data.model.Note
import com.example.data.model.NoteLayoutStyle
import com.example.data.model.NoteSortOrder
import com.example.data.model.NoteWorkspaceFilter
import com.example.ui.auth.signOut
import com.example.ui.components.ConfirmPermanentDeleteDialog
import com.example.ui.components.EditorialEmptyState
import com.example.ui.components.FolderEditorDialog
import com.example.ui.components.NoteContextBottomSheet
import com.example.ui.components.PasscodeLockDialog
import com.example.ui.components.SwipeableNoteItem
import com.example.ui.components.resolveFolderIcon
import com.example.ui.viewmodel.NotesViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.NoteTextToSpeechManager

enum class PrimaryStudioTab(val labelRes: Int) {
    NOTES(R.string.nav_notes),
    FOLDERS(R.string.nav_folders),
    VOICE(R.string.nav_voice),
    SETTINGS(R.string.nav_settings)
}

private data class EditorSessionState(
    val note: Note?,
    val startWithChecklist: Boolean = false,
    val startWithVoice: Boolean = false
)

@Composable
fun MainWorkspaceScreen(
    viewModel: NotesViewModel,
    userEmail: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val ttsManager = remember(context) { NoteTextToSpeechManager(context) }

    DisposableEffect(ttsManager) {
        onDispose { ttsManager.shutdown() }
    }

    val notesUiState by viewModel.notesState.collectAsStateWithLifecycle()
    val foldersUiState by viewModel.foldersState.collectAsStateWithLifecycle()
    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
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

    // Undo Snackbar when a note is moved to Trash
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
            val fullText = listOf(note.title, note.content, checklistSpoken, note.voiceTranscript)
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

    // Full-screen Note Editor when active
    val editorSession = activeEditorSession
    if (editorSession != null) {
        val liveNote = editorSession.note?.let { existing ->
            allNotes.firstOrNull { it.id == existing.id } ?: existing
        }
        NoteEditorScreen(
            initialNote = liveNote,
            folders = allFolders,
            startWithChecklist = editorSession.startWithChecklist,
            startWithVoiceSheet = editorSession.startWithVoice,
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
            onSaveAndClose = { title, content, folderId, tags, checklist, isPinned, isArchived, isLocked, colorKey, voiceTranscript, voiceDurationSec, reminderAtMillis ->
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
                    voiceTranscript = voiceTranscript,
                    voiceDurationSec = voiceDurationSec,
                    reminderAtMillis = reminderAtMillis
                )
                activeEditorSession = null
            },
            onDiscardOrDelete = {
                activeEditorSession = null
            }
        )
    } else {
        // Adaptive Workspace Layout (NavigationBar on handheld, NavigationRail on expanded tablets)
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 700.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                floatingActionButton = {
                    if (currentTab == PrimaryStudioTab.NOTES) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Quick Voice Note mini FAB
                            SmallFloatingActionButton(
                                onClick = {
                                    activeEditorSession = EditorSessionState(
                                        note = null,
                                        startWithVoice = true
                                    )
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("fab_quick_voice_note")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MicNone,
                                    contentDescription = stringResource(R.string.action_new_voice_note)
                                )
                            }

                            // Primary Editorial New Note FAB
                            ExtendedFloatingActionButton(
                                onClick = {
                                    activeEditorSession = EditorSessionState(note = null)
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(18.dp),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.action_new_note),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                modifier = Modifier.testTag("fab_new_note")
                            )
                        }
                    }
                },
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            tonalElevation = 0.dp
                        ) {
                            PrimaryStudioTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = when (tab) {
                                                PrimaryStudioTab.NOTES -> Icons.Outlined.AutoStories
                                                PrimaryStudioTab.FOLDERS -> if (selected) Icons.Filled.Folder else Icons.Outlined.Folder
                                                PrimaryStudioTab.VOICE -> if (selected) Icons.Filled.GraphicEq else Icons.Outlined.GraphicEq
                                                PrimaryStudioTab.SETTINGS -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
                                            },
                                            contentDescription = stringResource(tab.labelRes)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(tab.labelRes),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryStudioTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = when (tab) {
                                                PrimaryStudioTab.NOTES -> Icons.Outlined.AutoStories
                                                PrimaryStudioTab.FOLDERS -> if (selected) Icons.Filled.Folder else Icons.Outlined.Folder
                                                PrimaryStudioTab.VOICE -> if (selected) Icons.Filled.GraphicEq else Icons.Outlined.GraphicEq
                                                PrimaryStudioTab.SETTINGS -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
                                            },
                                            contentDescription = stringResource(tab.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(tab.labelRes)) }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (currentTab) {
                            PrimaryStudioTab.NOTES -> NotesStreamScreen(
                                notesUiState = notesUiState,
                                filteredNotes = filteredNotes,
                                folders = allFolders,
                                availableTags = availableTags,
                                filterState = filterState,
                                layoutStyle = preferences.layoutStyle,
                                sortOrder = preferences.sortOrder,
                                unlockedNoteIds = unlockedNoteIds,
                                isSpeaking = isSpeaking,
                                speakingNoteId = speakingNoteId,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onSelectWorkspaceFilter = viewModel::selectWorkspaceFilter,
                                onSelectFolderFilter = viewModel::selectFolderFilter,
                                onSelectTagFilter = viewModel::selectTagFilter,
                                onClearFilters = viewModel::clearAllFilters,
                                onSelectLayoutStyle = viewModel::setLayoutStyle,
                                onSelectSortOrder = viewModel::setSortOrder,
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
                                onCreateVoiceNote = {
                                    activeEditorSession = EditorSessionState(
                                        note = null,
                                        startWithVoice = true
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
                                onOpenNote = ::openNoteWithLockCheck,
                                onNewVoiceNoteClick = {
                                    activeEditorSession = EditorSessionState(
                                        note = null,
                                        startWithVoice = true
                                    )
                                }
                            )

                            PrimaryStudioTab.SETTINGS -> SettingsScreen(
                                userEmail = userEmail,
                                preferences = preferences,
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
                                },
                                onSignOutClick = {
                                    ttsManager.stop()
                                    signOut(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onSignOutComplete = {},
                                        scope = scope
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Context Menu Bottom Sheet
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

    // Permanent Delete Confirmation Dialog
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

    // Folder Editor Dialog
    if (folderEditorTarget.first) {
        FolderEditorDialog(
            initialFolder = folderEditorTarget.second,
            onDismiss = { folderEditorTarget = false to null },
            onSave = { name, iconKey, accentKey ->
                viewModel.saveFolder(folderEditorTarget.second, name, iconKey, accentKey)
            }
        )
    }

    // Passcode Lock Dialog
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
private fun NotesStreamScreen(
    notesUiState: UiState<List<Note>>,
    filteredNotes: List<Note>,
    folders: List<Folder>,
    availableTags: List<String>,
    filterState: com.example.ui.viewmodel.WorkspaceFilterState,
    layoutStyle: NoteLayoutStyle,
    sortOrder: NoteSortOrder,
    unlockedNoteIds: Set<String>,
    isSpeaking: Boolean,
    speakingNoteId: String?,
    onSearchQueryChange: (String) -> Unit,
    onSelectWorkspaceFilter: (NoteWorkspaceFilter) -> Unit,
    onSelectFolderFilter: (String?) -> Unit,
    onSelectTagFilter: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onSelectLayoutStyle: (NoteLayoutStyle) -> Unit,
    onSelectSortOrder: (NoteSortOrder) -> Unit,
    onOpenNote: (Note) -> Unit,
    onOpenContextMenu: (Note) -> Unit,
    onTogglePin: (Note) -> Unit,
    onMoveToTrash: (Note) -> Unit,
    onToggleChecklistItem: (Note, Int) -> Unit,
    onReadAloud: (Note) -> Unit,
    onCreateBlankNote: () -> Unit,
    onCreateChecklistNote: () -> Unit,
    onCreateVoiceNote: () -> Unit,
    onEmptyTrashClick: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    val folderMap = remember(folders) { folders.associateBy { it.id } }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 920.dp)
        ) {
            // Editorial Header & Quick Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        val activeFolderName = filterState.selectedFolderId?.let { folderMap[it]?.name }
                        Text(
                            text = buildString {
                                append(activeFolderName ?: filterState.workspaceFilter.label)
                                append(" • ${filteredNotes.size} ${if (filteredNotes.size == 1) "thought" else "thoughts"}")
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                        // Layout Mode Switcher (Grid -> List -> Compact)
                        IconButton(
                            onClick = {
                                val next = when (layoutStyle) {
                                    NoteLayoutStyle.MASONRY_GRID -> NoteLayoutStyle.COMFORTABLE_LIST
                                    NoteLayoutStyle.COMFORTABLE_LIST -> NoteLayoutStyle.COMPACT_INDEX
                                    NoteLayoutStyle.COMPACT_INDEX -> NoteLayoutStyle.MASONRY_GRID
                                }
                                onSelectLayoutStyle(next)
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("toggle_layout_style_button")
                        ) {
                            Icon(
                                imageVector = when (layoutStyle) {
                                    NoteLayoutStyle.MASONRY_GRID -> Icons.Outlined.GridView
                                    NoteLayoutStyle.COMFORTABLE_LIST -> Icons.Outlined.ViewAgenda
                                    NoteLayoutStyle.COMPACT_INDEX -> Icons.Outlined.ViewHeadline
                                },
                                contentDescription = "Switch layout view",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Sort Menu Button
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("sort_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Sort,
                                    contentDescription = "Sort notes",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                NoteSortOrder.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.label,
                                                fontWeight = if (sortOrder == option) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            onSelectSortOrder(option)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tactile Search Bar + Quick Capture Strip
                OutlinedTextField(
                    value = filterState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (filterState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                IconButton(
                                    onClick = onCreateChecklistNote,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("quick_capture_checklist_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckBox,
                                        contentDescription = "New checklist note",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workspace_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal Filter & Folder Pills Strip
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    NoteWorkspaceFilter.entries.forEach { wf ->
                        val selected = filterState.workspaceFilter == wf && filterState.selectedFolderId == null
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (filterState.selectedFolderId != null) {
                                    onSelectFolderFilter(null)
                                }
                                onSelectWorkspaceFilter(wf)
                            },
                            label = {
                                Text(
                                    text = wf.label,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("filter_chip_${wf.name.lowercase()}")
                        )
                    }
                }

                // Secondary Strip for Folders & Active Tags
                if (folders.isNotEmpty() || availableTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        folders.forEach { folder ->
                            val isSelected = filterState.selectedFolderId == folder.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectFolderFilter(folder.id) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = resolveFolderIcon(folder.iconKey),
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                        }

                        availableTags.take(8).forEach { tag ->
                            val isTagSelected = filterState.selectedTag.equals(tag, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isTagSelected) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isTagSelected) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectTagFilter(tag) }
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isTagSelected) MaterialTheme.colorScheme.onSecondaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Main Notes Stream Content
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
                        filterState.workspaceFilter != NoteWorkspaceFilter.ALL

                    AnimatedVisibility(
                        visible = filteredNotes.isEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        EditorialEmptyState(
                            isSearchFiltered = isFilteredOrSearched,
                            activeFilterLabel = filterState.workspaceFilter.label,
                            onCreateBlankNote = onCreateBlankNote,
                            onCreateChecklistNote = onCreateChecklistNote,
                            onCreateVoiceNote = onCreateVoiceNote,
                            onClearFilters = onClearFilters
                        )
                    }

                    if (filteredNotes.isNotEmpty()) {
                        val pinnedNotes = filteredNotes.filter { it.isPinned }
                        val unpinnedNotes = filteredNotes.filter { !it.isPinned }

                        if (layoutStyle == NoteLayoutStyle.MASONRY_GRID) {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Adaptive(minSize = 240.dp),
                                verticalItemSpacing = 12.dp,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 4.dp,
                                    bottom = 120.dp
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("notes_masonry_grid")
                            ) {
                                if (pinnedNotes.isNotEmpty() && unpinnedNotes.isNotEmpty()) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        SectionDividerHeader("PINNED THOUGHTS")
                                    }
                                }

                                items(pinnedNotes, key = { "pin_${it.id}" }) { note ->
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

                                if (pinnedNotes.isNotEmpty() && unpinnedNotes.isNotEmpty()) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        SectionDividerHeader("RECENT NOTES")
                                    }
                                }

                                items(unpinnedNotes, key = { "note_${it.id}" }) { note ->
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
                                verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp),
                                contentPadding = PaddingValues(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 4.dp,
                                    bottom = 120.dp
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("notes_list_column")
                            ) {
                                if (pinnedNotes.isNotEmpty() && unpinnedNotes.isNotEmpty()) {
                                    item { SectionDividerHeader("PINNED THOUGHTS") }
                                }

                                items(pinnedNotes, key = { "pin_${it.id}" }) { note ->
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

                                if (pinnedNotes.isNotEmpty() && unpinnedNotes.isNotEmpty()) {
                                    item { SectionDividerHeader("RECENT NOTES") }
                                }

                                items(unpinnedNotes, key = { "note_${it.id}" }) { note ->
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

@Composable
private fun SectionDividerHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        )
    }
}
