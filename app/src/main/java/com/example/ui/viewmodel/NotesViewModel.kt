package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChecklistItem
import com.example.data.model.Folder
import com.example.data.model.Note
import com.example.data.model.NoteColorPalette
import com.example.data.model.NoteLayoutStyle
import com.example.data.model.NoteSortOrder
import com.example.data.model.NoteWorkspaceFilter
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class WorkspaceFilterState(
    val workspaceFilter: NoteWorkspaceFilter = NoteWorkspaceFilter.ALL,
    val selectedFolderId: String? = null,
    val selectedTag: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val monthOffset: Int = 0,
    val filterByMonth: Boolean = false
)

class NotesViewModel(
    private val repository: NoteRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(WorkspaceFilterState())
    val filterState: StateFlow<WorkspaceFilterState> = _filterState.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _lastTrashedNote = MutableStateFlow<Note?>(null)
    val lastTrashedNote: StateFlow<Note?> = _lastTrashedNote.asStateFlow()

    private val _unlockedNoteIds = MutableStateFlow<Set<String>>(emptySet())
    val unlockedNoteIds: StateFlow<Set<String>> = _unlockedNoteIds.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UserPreferences()
        )

    val notesState: StateFlow<UiState<List<Note>>> = repository.observeNotes()
        .map<List<Note>, UiState<List<Note>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing local notes", error)
            emit(UiState.Error(error.message ?: "Unable to load notes"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val foldersState: StateFlow<UiState<List<Folder>>> = repository.observeFolders()
        .map<List<Folder>, UiState<List<Folder>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing local folders", error)
            emit(UiState.Error(error.message ?: "Unable to load folders"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val filteredNotes: StateFlow<List<Note>> = combine(
        notesState,
        _filterState,
        userPreferences
    ) { state, filter, prefs ->
        val allNotes = (state as? UiState.Success)?.data ?: emptyList()
        applyFiltersAndSort(allNotes, filter, prefs.sortOrder)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    val availableTags: StateFlow<List<String>> = notesState.map { state ->
        val notes = (state as? UiState.Success)?.data ?: emptyList()
        notes.filter { !it.isTrashed }
            .flatMap { it.tags }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.seedWorkspaceStarterKitIfEmpty()
        }
    }

    private fun applyFiltersAndSort(
        notes: List<Note>,
        filter: WorkspaceFilterState,
        sortOrder: NoteSortOrder
    ): List<Note> {
        val baseFiltered = notes.filter { note ->
            when (filter.workspaceFilter) {
                NoteWorkspaceFilter.ALL -> !note.isTrashed && !note.isArchived
                NoteWorkspaceFilter.PINNED -> !note.isTrashed && !note.isArchived && note.isPinned
                NoteWorkspaceFilter.CHECKLISTS -> !note.isTrashed && !note.isArchived && note.checklistItems.isNotEmpty()
                NoteWorkspaceFilter.REMINDERS -> !note.isTrashed && !note.isArchived && note.reminderAtMillis > 0L
                NoteWorkspaceFilter.PROTECTED -> !note.isTrashed && !note.isArchived && note.isLocked
                NoteWorkspaceFilter.ARCHIVED -> !note.isTrashed && note.isArchived
                NoteWorkspaceFilter.TRASH -> note.isTrashed
            }
        }.filter { note ->
            if (filter.selectedFolderId != null) {
                note.folderId == filter.selectedFolderId
            } else {
                true
            }
        }.filter { note ->
            if (filter.selectedTag != null) {
                note.tags.any { it.equals(filter.selectedTag, ignoreCase = true) }
            } else {
                true
            }
        }.filter { note ->
            if (!filter.filterByMonth) {
                true
            } else {
                val targetCal = Calendar.getInstance().apply {
                    add(Calendar.MONTH, filter.monthOffset)
                }
                val noteCal = Calendar.getInstance().apply {
                    timeInMillis = note.updatedAtMillis
                }
                targetCal.get(Calendar.YEAR) == noteCal.get(Calendar.YEAR) &&
                    targetCal.get(Calendar.MONTH) == noteCal.get(Calendar.MONTH)
            }
        }.filter { note ->
            val q = filter.searchQuery.trim()
            if (q.isEmpty()) {
                true
            } else {
                val cleanTagQuery = q.removePrefix("#")
                note.title.contains(q, ignoreCase = true) ||
                    (!note.isLocked && note.content.contains(q, ignoreCase = true)) ||
                    (!note.isLocked && note.checklistItems.any { it.contains(q, ignoreCase = true) }) ||
                    note.tags.any { it.contains(cleanTagQuery, ignoreCase = true) }
            }
        }

        val comparator = when (sortOrder) {
            NoteSortOrder.UPDATED_DESC -> compareByDescending<Note> { it.isPinned }
                .thenByDescending { it.updatedAtMillis }
            NoteSortOrder.CREATED_DESC -> compareByDescending<Note> { it.isPinned }
                .thenByDescending { it.createdAtMillis }
            NoteSortOrder.TITLE_ASC -> compareByDescending<Note> { it.isPinned }
                .thenBy { it.title.lowercase().ifBlank { "zzzz" } }
            NoteSortOrder.WORD_COUNT_DESC -> compareByDescending<Note> { it.isPinned }
                .thenByDescending { it.wordCount }
        }
        return baseFiltered.sortedWith(comparator)
    }

    fun stepMonth(delta: Int) {
        val current = _filterState.value
        val nextOffset = current.monthOffset + delta
        _filterState.value = current.copy(
            monthOffset = nextOffset,
            filterByMonth = nextOffset != 0
        )
    }

    fun resetMonthFilter() {
        _filterState.value = _filterState.value.copy(
            monthOffset = 0,
            filterByMonth = false
        )
    }

    fun selectWorkspaceFilter(workspaceFilter: NoteWorkspaceFilter) {
        _filterState.value = _filterState.value.copy(workspaceFilter = workspaceFilter)
    }

    fun selectFolderFilter(folderId: String?) {
        _filterState.value = _filterState.value.copy(
            selectedFolderId = if (_filterState.value.selectedFolderId == folderId) null else folderId
        )
    }

    fun setExplicitFolderFilter(folderId: String?) {
        _filterState.value = _filterState.value.copy(
            selectedFolderId = folderId,
            workspaceFilter = NoteWorkspaceFilter.ALL
        )
    }

    fun selectTagFilter(tag: String?) {
        _filterState.value = _filterState.value.copy(
            selectedTag = if (_filterState.value.selectedTag == tag) null else tag
        )
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun clearAllFilters() {
        _filterState.value = WorkspaceFilterState()
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun markNoteUnlockedForSession(noteId: String) {
        _unlockedNoteIds.value = _unlockedNoteIds.value + noteId
    }

    fun saveNote(
        existingNote: Note?,
        title: String,
        content: String,
        folderId: String,
        tags: List<String>,
        checklist: List<ChecklistItem>,
        isPinned: Boolean,
        isArchived: Boolean,
        isLocked: Boolean,
        colorKey: String,
        reminderAtMillis: Long,
        onSaved: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val serializedChecklist = checklist
                .filter { it.text.isNotBlank() }
                .map { it.toSerializedString() }

            if (existingNote == null || existingNote.id.isBlank()) {
                val newNote = Note(
                    title = title.trim(),
                    content = content.trim(),
                    folderId = folderId,
                    tags = tags,
                    checklistItems = serializedChecklist,
                    isPinned = isPinned,
                    isArchived = isArchived,
                    isTrashed = false,
                    isLocked = isLocked,
                    colorKey = colorKey,
                    reminderAtMillis = reminderAtMillis
                )
                val result = repository.createNote(newNote)
                result.onSuccess { newId ->
                    if (isLocked) markNoteUnlockedForSession(newId)
                    onSaved(newId)
                }.onFailure { err ->
                    _statusBannerMessage.value = err.localizedMessage ?: "Could not save note"
                }
            } else {
                val updated = existingNote.copy(
                    title = title.trim(),
                    content = content.trim(),
                    folderId = folderId,
                    tags = tags,
                    checklistItems = serializedChecklist,
                    isPinned = isPinned,
                    isArchived = isArchived,
                    isLocked = isLocked,
                    colorKey = colorKey,
                    reminderAtMillis = reminderAtMillis
                )
                val result = repository.updateNote(updated)
                result.onSuccess {
                    if (isLocked) markNoteUnlockedForSession(existingNote.id)
                    onSaved(existingNote.id)
                }.onFailure { err ->
                    _statusBannerMessage.value = err.localizedMessage ?: "Could not update note"
                }
            }
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun toggleArchive(note: Note) {
        viewModelScope.launch {
            val targetArchive = !note.isArchived
            repository.updateNote(
                note.copy(
                    isArchived = targetArchive,
                    isPinned = if (targetArchive) false else note.isPinned
                )
            )
            _statusBannerMessage.value = if (targetArchive) "Note archived" else "Restored from Archive"
        }
    }

    fun toggleLock(note: Note, targetLocked: Boolean) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isLocked = targetLocked))
            if (targetLocked) {
                _unlockedNoteIds.value = _unlockedNoteIds.value - note.id
                _statusBannerMessage.value = "Thought locked with passcode"
            } else {
                _unlockedNoteIds.value = _unlockedNoteIds.value + note.id
                _statusBannerMessage.value = "Lock removed"
            }
        }
    }

    fun updateNoteColor(note: Note, palette: NoteColorPalette) {
        viewModelScope.launch {
            repository.updateNote(note.copy(colorKey = palette.key))
        }
    }

    fun moveNoteToFolder(note: Note, folderId: String) {
        viewModelScope.launch {
            repository.updateNote(note.copy(folderId = folderId))
            _statusBannerMessage.value = if (folderId.isBlank()) "Removed from folder" else "Moved to folder"
        }
    }

    fun toggleChecklistItemOnCard(note: Note, itemIndex: Int) {
        viewModelScope.launch {
            val currentList = note.parsedChecklist.toMutableList()
            if (itemIndex in currentList.indices) {
                val item = currentList[itemIndex]
                currentList[itemIndex] = item.copy(isChecked = !item.isChecked)
                val updatedRaw = currentList.map { it.toSerializedString() }
                repository.updateNote(note.copy(checklistItems = updatedRaw))
            }
        }
    }

    fun moveToTrash(note: Note) {
        viewModelScope.launch {
            _lastTrashedNote.value = note
            repository.updateNote(
                note.copy(
                    isTrashed = true,
                    isPinned = false
                )
            )
        }
    }

    fun undoMoveToTrash() {
        val trashed = _lastTrashedNote.value ?: return
        viewModelScope.launch {
            repository.updateNote(
                trashed.copy(
                    isTrashed = false,
                    isPinned = trashed.isPinned
                )
            )
            _lastTrashedNote.value = null
            _statusBannerMessage.value = "Note restored"
        }
    }

    fun dismissTrashUndo() {
        _lastTrashedNote.value = null
    }

    fun restoreFromTrash(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isTrashed = false))
            _statusBannerMessage.value = "Restored from Trash"
        }
    }

    fun duplicateNote(note: Note) {
        viewModelScope.launch {
            val copy = note.copy(
                id = "",
                title = "${note.title.ifBlank { "Untitled" }} (Copy)",
                isPinned = false
            )
            repository.createNote(copy)
            _statusBannerMessage.value = "Duplicated note"
        }
    }

    fun deleteNoteForever(noteId: String) {
        viewModelScope.launch {
            repository.deleteNotePermanently(noteId)
            _statusBannerMessage.value = "Deleted permanently"
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            _statusBannerMessage.value = "Trash emptied"
        }
    }

    fun saveFolder(existingFolder: Folder?, name: String, iconKey: String, accentKey: String) {
        viewModelScope.launch {
            val currentFolders = (foldersState.value as? UiState.Success)?.data ?: emptyList()
            if (existingFolder == null) {
                val folder = Folder(
                    name = name.trim(),
                    iconKey = iconKey,
                    accentKey = accentKey,
                    sortOrder = (currentFolders.size + 1).toLong()
                )
                repository.createFolder(folder)
                _statusBannerMessage.value = "Folder \"${name.trim()}\" created"
            } else {
                repository.updateFolder(
                    existingFolder.copy(
                        name = name.trim(),
                        iconKey = iconKey,
                        accentKey = accentKey
                    )
                )
                _statusBannerMessage.value = "Folder updated"
            }
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            repository.deleteFolder(folder.id)
            if (_filterState.value.selectedFolderId == folder.id) {
                _filterState.value = _filterState.value.copy(selectedFolderId = null)
            }
            _statusBannerMessage.value = "Folder deleted"
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setLayoutStyle(style: NoteLayoutStyle) {
        viewModelScope.launch { preferencesRepository.setLayoutStyle(style) }
    }

    fun setSortOrder(order: NoteSortOrder) {
        viewModelScope.launch { preferencesRepository.setSortOrder(order) }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch { preferencesRepository.setFontScale(scale) }
    }

    fun setLockPinCode(pin: String) {
        viewModelScope.launch {
            preferencesRepository.setLockPinCode(pin)
            _statusBannerMessage.value = if (pin.isBlank()) "Passcode removed" else "4-digit privacy passcode saved"
        }
    }

    fun setTtsSpeechRate(rate: Float) {
        viewModelScope.launch { preferencesRepository.setTtsSpeechRate(rate) }
    }

    private companion object {
        const val TAG = "NotesViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
