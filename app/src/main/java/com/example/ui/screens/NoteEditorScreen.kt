package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ChecklistItem
import com.example.data.model.Folder
import com.example.data.model.Note
import com.example.data.model.NoteColorPalette
import com.example.ui.components.ReminderPickerBottomSheet
import com.example.ui.components.VoiceDictationBottomSheet
import com.example.ui.components.formatEditorialTimestamp
import com.example.ui.components.formatReminderTime
import com.example.ui.components.resolveFolderIcon
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.resolveColors
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    initialNote: Note?,
    folders: List<Folder>,
    startWithChecklist: Boolean = false,
    startWithVoiceSheet: Boolean = false,
    isSpeakingThisNote: Boolean,
    onToggleReadAloud: (String) -> Unit,
    onStopReadAloud: () -> Unit,
    onRequestPasscodeSetupOrVerify: (onSuccess: () -> Unit) -> Unit,
    onSaveAndClose: (
        title: String,
        content: String,
        folderId: String,
        tags: List<String>,
        checklist: List<ChecklistItem>,
        isPinned: Boolean,
        isArchived: Boolean,
        isLocked: Boolean,
        colorKey: String,
        voiceTranscript: String,
        voiceDurationSec: Long,
        reminderAtMillis: Long
    ) -> Unit,
    onDiscardOrDelete: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var folderId by remember { mutableStateOf(initialNote?.folderId ?: "") }
    var isPinned by remember { mutableStateOf(initialNote?.isPinned ?: false) }
    var isArchived by remember { mutableStateOf(initialNote?.isArchived ?: false) }
    var isLocked by remember { mutableStateOf(initialNote?.isLocked ?: false) }
    var colorKey by remember { mutableStateOf(initialNote?.colorKey ?: NoteColorPalette.DEFAULT.key) }
    var voiceTranscript by remember { mutableStateOf(initialNote?.voiceTranscript ?: "") }
    var voiceDurationSec by remember { mutableLongStateOf(initialNote?.voiceDurationSec ?: 0L) }
    var reminderAtMillis by remember { mutableLongStateOf(initialNote?.reminderAtMillis ?: 0L) }

    val tags = remember {
        mutableStateListOf<String>().apply {
            initialNote?.tags?.let { addAll(it) }
        }
    }
    val checklist = remember {
        mutableStateListOf<ChecklistItem>().apply {
            val existing = initialNote?.parsedChecklist.orEmpty()
            if (existing.isNotEmpty()) {
                addAll(existing)
            } else if (startWithChecklist) {
                add(ChecklistItem(id = UUID.randomUUID().toString(), text = "", isChecked = false))
            }
        }
    }

    var newChecklistText by remember { mutableStateOf("") }
    var showChecklistInput by remember { mutableStateOf(startWithChecklist || checklist.isNotEmpty()) }
    var newTagText by remember { mutableStateOf("") }
    var showTagInput by remember { mutableStateOf(false) }
    var showColorRibbon by remember { mutableStateOf(false) }
    var showFolderDropdown by remember { mutableStateOf(false) }
    var showVoiceSheet by remember { mutableStateOf(startWithVoiceSheet) }
    var showReminderSheet by remember { mutableStateOf(false) }

    val activePalette = NoteColorPalette.fromKey(colorKey)
    val surfaceColors = activePalette.resolveColors(isDark)

    val liveWordCount = remember(title, content, voiceTranscript) {
        Note.computeWordCount(title, content, voiceTranscript)
    }
    val readingMinutes = remember(liveWordCount, checklist.size) {
        ((liveWordCount.toInt() + checklist.size * 4) / 180).coerceAtLeast(1)
    }

    fun handleBackAndSave() {
        onStopReadAloud()
        val hasAnyContent = title.isNotBlank() ||
            content.isNotBlank() ||
            voiceTranscript.isNotBlank() ||
            checklist.any { it.text.isNotBlank() }

        if (hasAnyContent || initialNote != null) {
            onSaveAndClose(
                title.ifBlank { "Untitled thought" },
                content,
                folderId,
                tags.toList(),
                checklist.toList(),
                isPinned,
                isArchived,
                isLocked,
                colorKey,
                voiceTranscript,
                voiceDurationSec,
                reminderAtMillis
            )
        } else {
            onDiscardOrDelete()
        }
    }

    BackHandler {
        handleBackAndSave()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = surfaceColors.container,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColors.container
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { handleBackAndSave() },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("editor_back_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Save and return"
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        ) {
                            Text(
                                text = "SAVED TO CLOUD",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Read Aloud Toggle
                    IconButton(
                        onClick = {
                            if (isSpeakingThisNote) {
                                onStopReadAloud()
                            } else {
                                val spokenChecklist = checklist.filter { it.text.isNotBlank() }
                                    .joinToString(". ") { it.text }
                                val fullSpoken = listOf(title, content, spokenChecklist, voiceTranscript)
                                    .filter { it.isNotBlank() }
                                    .joinToString(". ")
                                onToggleReadAloud(fullSpoken.ifBlank { "This note is currently empty." })
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("editor_read_aloud_button")
                    ) {
                        Icon(
                            imageVector = if (isSpeakingThisNote) Icons.Outlined.StopCircle else Icons.AutoMirrored.Outlined.VolumeUp,
                            contentDescription = stringResource(
                                if (isSpeakingThisNote) R.string.action_stop_reading else R.string.action_read_aloud
                            ),
                            tint = if (isSpeakingThisNote) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Pin Toggle
                    IconButton(
                        onClick = { isPinned = !isPinned },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("editor_pin_button")
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin note",
                            tint = if (isPinned) surfaceColors.accentBar else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Lock Toggle
                    IconButton(
                        onClick = {
                            if (!isLocked) {
                                onRequestPasscodeSetupOrVerify {
                                    isLocked = true
                                }
                            } else {
                                isLocked = false
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("editor_lock_button")
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Filled.Lock else Icons.Outlined.LockOpen,
                            contentDescription = if (isLocked) "Unlock note" else "Lock note",
                            tint = if (isLocked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Done / Save Button
                    TextButton(
                        onClick = { handleBackAndSave() },
                        modifier = Modifier.testTag("editor_done_button")
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColors.container)
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                if (showColorRibbon) {
                    HorizontalDivider(color = surfaceColors.border)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Paper:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        NoteColorPalette.entries.forEach { palette ->
                            val swatch = palette.resolveColors(isDark)
                            val isSelected = colorKey == palette.key
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(swatch.swatchPreview)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else swatch.border,
                                        shape = CircleShape
                                    )
                                    .clickable { colorKey = palette.key },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = palette.label,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = surfaceColors.border)

                // Tactile Editor Bottom Dock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { showChecklistInput = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dock_add_checklist_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckBox,
                                contentDescription = "Add checklist",
                                tint = if (showChecklistInput) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showVoiceSheet = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dock_voice_dictate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MicNone,
                                contentDescription = "Voice dictation",
                                tint = if (voiceTranscript.isNotBlank()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showColorRibbon = !showColorRibbon },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dock_color_palette_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = "Paper tint",
                                tint = if (showColorRibbon) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showTagInput = !showTagInput },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dock_add_tag_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tag,
                                contentDescription = "Tags",
                                tint = if (tags.isNotEmpty() || showTagInput) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showReminderSheet = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dock_reminder_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Alarm,
                                contentDescription = "Set reminder",
                                tint = if (reminderAtMillis > 0L) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "$liveWordCount words • ~$readingMinutes min read",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                // Metadata & Folder Selector Ribbon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        val currentFolder = folders.firstOrNull { it.id == folderId }
                        AssistChip(
                            onClick = { showFolderDropdown = true },
                            leadingIcon = {
                                Icon(
                                    imageVector = resolveFolderIcon(currentFolder?.iconKey ?: "folder"),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = currentFolder?.name ?: "Unfiled • Choose Folder",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            modifier = Modifier.testTag("editor_folder_selector_chip")
                        )

                        DropdownMenu(
                            expanded = showFolderDropdown,
                            onDismissRequest = { showFolderDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Unfiled") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Folder, contentDescription = null)
                                },
                                onClick = {
                                    folderId = ""
                                    showFolderDropdown = false
                                }
                            )
                            folders.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(folder.name) },
                                    leadingIcon = {
                                        Icon(resolveFolderIcon(folder.iconKey), contentDescription = null)
                                    },
                                    onClick = {
                                        folderId = folder.id
                                        showFolderDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = formatEditorialTimestamp(initialNote?.updatedAt ?: initialNote?.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Active Reminder Banner
                if (reminderAtMillis > 0L) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showReminderSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reminder set for ${formatReminderTime(reminderAtMillis)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            IconButton(
                                onClick = { reminderAtMillis = 0L },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear reminder",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Editorial Serif Title Input
                BasicTextField(
                    value = title,
                    onValueChange = { title = it.take(300) },
                    textStyle = MaterialTheme.typography.displaySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (title.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.editor_title_placeholder),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tags Strip
                if (tags.isNotEmpty() || showTagInput) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove tag $tag",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { tags.remove(tag) }
                                    )
                                }
                            }
                        }
                    }

                    if (showTagInput && tags.size < 10) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newTagText,
                                onValueChange = {
                                    newTagText = it.replace("#", "").replace(" ", "-").lowercase().take(30)
                                },
                                placeholder = { Text("Add tag (e.g. design, essay)…") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        val cleaned = newTagText.trim()
                                        if (cleaned.isNotEmpty() && !tags.contains(cleaned) && tags.size < 10) {
                                            tags.add(cleaned)
                                            newTagText = ""
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("editor_new_tag_input")
                            )
                            IconButton(
                                onClick = {
                                    val cleaned = newTagText.trim()
                                    if (cleaned.isNotEmpty() && !tags.contains(cleaned) && tags.size < 10) {
                                        tags.add(cleaned)
                                        newTagText = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add tag")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Voice Memo Card inside Editor
                if (voiceTranscript.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.GraphicEq,
                                        contentDescription = "Voice transcript",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "VOICE MEMO TRANSCRIPT (${voiceDurationSec}s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row {
                                    TextButton(
                                        onClick = { showVoiceSheet = true }
                                    ) {
                                        Text("Edit / Dictate")
                                    }
                                    IconButton(
                                        onClick = {
                                            voiceTranscript = ""
                                            voiceDurationSec = 0L
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Remove voice transcript",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = voiceTranscript,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Interactive Checklist Section
                if (showChecklistInput || checklist.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                        border = BorderStroke(1.dp, surfaceColors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            val doneCount = checklist.count { it.isChecked }
                            val totalCount = checklist.size
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CHECKLIST ($doneCount/$totalCount)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                if (totalCount > 0) {
                                    LinearProgressIndicator(
                                        progress = { doneCount.toFloat() / totalCount.toFloat() },
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(6.dp)
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            checklist.forEachIndexed { index, item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            checklist[index] = item.copy(isChecked = !item.isChecked)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (item.isChecked) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = "Toggle checklist item",
                                            tint = if (item.isChecked) MaterialTheme.colorScheme.secondary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    BasicTextField(
                                        value = item.text,
                                        onValueChange = { updatedText ->
                                            checklist[index] = item.copy(text = updatedText.take(400))
                                        },
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                            else MaterialTheme.colorScheme.onSurface,
                                            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 6.dp)
                                    )

                                    IconButton(
                                        onClick = { checklist.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Remove item",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (checklist.size < 50) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = newChecklistText,
                                        onValueChange = { newChecklistText = it.take(400) },
                                        placeholder = { Text("Add checklist item…") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                if (newChecklistText.isNotBlank()) {
                                                    checklist.add(
                                                        ChecklistItem(
                                                            id = UUID.randomUUID().toString(),
                                                            text = newChecklistText.trim(),
                                                            isChecked = false
                                                        )
                                                    )
                                                    newChecklistText = ""
                                                }
                                            }
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("editor_new_checklist_input")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (newChecklistText.isNotBlank()) {
                                                checklist.add(
                                                    ChecklistItem(
                                                        id = UUID.randomUUID().toString(),
                                                        text = newChecklistText.trim(),
                                                        isChecked = false
                                                    )
                                                )
                                                newChecklistText = ""
                                            }
                                        },
                                        modifier = Modifier.testTag("editor_add_checklist_confirm")
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Add item")
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Main Editorial Body Input
                BasicTextField(
                    value = content,
                    onValueChange = { content = it.take(50000) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp)
                        ) {
                            if (content.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.editor_body_placeholder),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_content_input")
                )
            }
        }
    }

    if (showVoiceSheet) {
        VoiceDictationBottomSheet(
            initialTranscript = voiceTranscript,
            initialDurationSec = voiceDurationSec,
            onDismiss = { showVoiceSheet = false },
            onSaveVoiceCapture = { capturedTranscript, duration, insertIntoBody ->
                voiceTranscript = capturedTranscript
                voiceDurationSec = duration
                if (insertIntoBody && capturedTranscript.isNotBlank()) {
                    content = if (content.isBlank()) {
                        capturedTranscript
                    } else {
                        "$content\n\n$capturedTranscript"
                    }
                }
            }
        )
    }

    if (showReminderSheet) {
        ReminderPickerBottomSheet(
            noteTitle = title.ifBlank { "Untitled thought" },
            currentReminderMillis = reminderAtMillis,
            onDismiss = { showReminderSheet = false },
            onSelectReminderMillis = { millis ->
                reminderAtMillis = millis
            }
        )
    }
}
