package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Folder
import com.example.data.model.Note
import com.example.data.model.NoteLayoutStyle
import com.example.data.model.NoteSortOrder
import com.example.data.model.NoteWorkspaceFilter
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferences
import com.example.ui.components.formatEditorialTimestamp
import com.example.ui.components.resolveFolderIcon
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.resolveFolderAccentColor

@Composable
fun FoldersScreen(
    folders: List<Folder>,
    allNotes: List<Note>,
    onOpenFolderNotes: (Folder) -> Unit,
    onCreateFolderClick: () -> Unit,
    onEditFolderClick: (Folder) -> Unit,
    onDeleteFolderClick: (Folder) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val activeNotes = allNotes.filter { !it.isTrashed }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Collections & Binders",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${folders.size} folders • ${activeNotes.size} total active notes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onCreateFolderClick,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("create_folder_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CreateNewFolder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Folder")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(folders, key = { it.id }) { folder ->
                    val folderNotes = activeNotes.filter { it.folderId == folder.id }
                    val accentColor = resolveFolderAccentColor(folder.accentKey, isDark)
                    val latestTimestamp = folderNotes.maxOfOrNull {
                        it.updatedAtMillis
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onOpenFolderNotes(folder) }
                            .testTag("folder_card_${folder.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(accentColor.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = resolveFolderIcon(folder.iconKey),
                                        contentDescription = folder.name,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { onEditFolderClick(folder) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Edit folder",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteFolderClick(folder) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Delete folder",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${folderNotes.size} ${if (folderNotes.size == 1) "note" else "notes"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = accentColor
                            )

                            if (latestTimestamp != null && latestTimestamp > 0L) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Updated ${formatEditorialTimestamp(latestTimestamp)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceAndListenScreen(
    allNotes: List<Note>,
    unlockedNoteIds: Set<String>,
    isSpeaking: Boolean,
    speakingNoteId: String?,
    speechRate: Float,
    onChangeSpeechRate: (Float) -> Unit,
    onPlayNoteAloud: (Note) -> Unit,
    onStopAudio: () -> Unit,
    onOpenNote: (Note) -> Unit
) {
    val listenableNotes = allNotes.filter { !it.isTrashed && (!it.isLocked || unlockedNoteIds.contains(it.id)) }
    val currentlyPlayingNote = listenableNotes.firstOrNull { it.id == speakingNoteId }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Listen Aloud",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Listen back to your notes hands-free with on-device speech synthesis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Active Audio Player / Speed Controller Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSpeaking) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Filled.GraphicEq else Icons.AutoMirrored.Outlined.VolumeUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isSpeaking && currentlyPlayingNote != null) {
                                            "READING ALOUD NOW"
                                        } else {
                                            "SPEECH SYNTHESIS READY"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = currentlyPlayingNote?.title?.ifBlank { "Untitled thought" }
                                            ?: "Tap play on any note below to listen hands-free",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (isSpeaking) {
                                IconButton(
                                    onClick = onStopAudio,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("stop_tts_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Stop,
                                        contentDescription = "Stop reading",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Reading Speed:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            listOf(0.85f to "0.85× Calm", 1.0f to "1.0× Natural", 1.25f to "1.25× Brisk").forEach { (rate, label) ->
                                FilterChip(
                                    selected = kotlin.math.abs(speechRate - rate) < 0.05f,
                                    onClick = { onChangeSpeechRate(rate) },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ALL NOTES READING QUEUE (${listenableNotes.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(listenableNotes, key = { "queue_${it.id}" }) { note ->
                AudioQueueNoteRow(
                    note = note,
                    isPlaying = isSpeaking && speakingNoteId == note.id,
                    onPlayToggle = {
                        if (isSpeaking && speakingNoteId == note.id) onStopAudio()
                        else onPlayNoteAloud(note)
                    },
                    onClick = { onOpenNote(note) }
                )
            }
        }
    }
}

@Composable
private fun AudioQueueNoteRow(
    note: Note,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPlayToggle,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Outlined.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Listen aloud",
                    tint = if (isPlaying) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "Untitled thought" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.content.isNotBlank()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${note.wordCount} words • ~${note.readingTimeMinutes} min listen",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    totalNotesCount: Int,
    archivedCount: Int,
    trashedCount: Int,
    lockedCount: Int,
    onSelectTheme: (ThemeMode) -> Unit,
    onSelectLayoutStyle: (NoteLayoutStyle) -> Unit,
    onSelectSortOrder: (NoteSortOrder) -> Unit,
    onChangeFontScale: (Float) -> Unit,
    onConfigurePasscodeClick: () -> Unit,
    onOpenWorkspaceFilter: (NoteWorkspaceFilter) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Writing Studio & Preferences",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Customize typography, paper surfaces, layout density, and privacy locks.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Appearance & Theme Section
            SettingsSectionCard(title = "APPEARANCE & PAPER SURFACE") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = preferences.themeMode == mode,
                            onClick = { onSelectTheme(mode) },
                            label = { Text(mode.label) },
                            modifier = Modifier.testTag("theme_chip_${mode.name}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Typography Scale Section
            SettingsSectionCard(title = "TYPOGRAPHY SCALE & READING COMFORT") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editorial Text Size",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${(preferences.fontScaleMultiplier * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = preferences.fontScaleMultiplier,
                    onValueChange = onChangeFontScale,
                    valueRange = 0.85f..1.30f,
                    steps = 4,
                    modifier = Modifier.testTag("font_scale_slider")
                )
                Text(
                    text = "“Typography is what language looks like.” — Ellen Lupton",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Default Card Layout & Sort Order
            SettingsSectionCard(title = "NOTE STREAM DENSITY & ORDER") {
                Text(
                    text = "Card Layout",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NoteLayoutStyle.entries.forEach { style ->
                        FilterChip(
                            selected = preferences.layoutStyle == style,
                            onClick = { onSelectLayoutStyle(style) },
                            label = { Text(style.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Default Sorting",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NoteSortOrder.entries.forEach { order ->
                        FilterChip(
                            selected = preferences.sortOrder == order,
                            onClick = { onSelectSortOrder(order) },
                            label = { Text(order.label) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Privacy & Vault Section
            SettingsSectionCard(title = "PRIVACY & ARCHIVE VAULT") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (preferences.lockPinCode.isNotBlank()) "4-Digit Passcode Active"
                            else "No Studio Passcode Set",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "$lockedCount protected thoughts in your workspace",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = onConfigurePasscodeClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("configure_passcode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (preferences.lockPinCode.isNotBlank()) "Change PIN" else "Set PIN")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { onOpenWorkspaceFilter(NoteWorkspaceFilter.ARCHIVED) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Archive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Archive ($archivedCount)")
                    }

                    OutlinedButton(
                        onClick = { onOpenWorkspaceFilter(NoteWorkspaceFilter.TRASH) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Trash ($trashedCount)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Offline On-Device Storage Section
            SettingsSectionCard(title = "OFFLINE LOCAL DATABASE") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "100% On-Device SQLite Storage",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$totalNotesCount notes stored privately on this device • Zero cloud or account required",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
