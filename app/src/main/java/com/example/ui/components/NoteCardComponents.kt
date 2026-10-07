package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Folder
import com.example.data.model.Note
import com.example.data.model.NoteColorPalette
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.resolveColors
import com.example.ui.theme.resolveFolderAccentColor
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatEditorialTimestamp(timestamp: Timestamp?): String {
    val date = timestamp?.toDate() ?: Date()
    val now = System.currentTimeMillis()
    val diffMs = now - date.time
    val oneDayMs = 24 * 60 * 60 * 1000L
    return when {
        diffMs < 60_000L -> "Just now"
        diffMs < 3600_000L -> "${(diffMs / 60_000L).coerceAtLeast(1)}m ago"
        diffMs < oneDayMs -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
        diffMs < 7 * oneDayMs -> SimpleDateFormat("EEE • MMM d", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
    }
}

fun formatReminderTime(millis: Long): String {
    if (millis <= 0L) return ""
    return SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(millis))
}

fun resolveFolderIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "book" -> Icons.AutoMirrored.Outlined.MenuBook
        "work" -> Icons.Outlined.WorkOutline
        "ideas" -> Icons.Outlined.Lightbulb
        "journal" -> Icons.Outlined.EditNote
        "bookmark" -> Icons.Outlined.BookmarkBorder
        "voice" -> Icons.Outlined.GraphicEq
        "lock" -> Icons.Outlined.Lock
        else -> Icons.Outlined.Folder
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNoteItem(
    note: Note,
    folder: Folder?,
    isUnlocked: Boolean,
    isSpeakingThisNote: Boolean,
    compactMode: Boolean = false,
    onNoteClick: () -> Unit,
    onNoteLongPress: () -> Unit,
    onTogglePin: () -> Unit,
    onMoveToTrash: () -> Unit,
    onToggleChecklistItem: (Int) -> Unit,
    onReadAloudClick: () -> Unit,
    onTagClick: (String) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { targetValue ->
            when (targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTogglePin()
                    false // Snap back after toggling pin
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onMoveToTrash()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    LaunchedEffect(note.id, note.isPinned, note.isTrashed) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !note.isTrashed,
        enableDismissFromEndToStart = !note.isTrashed,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val bgColor by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.secondaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                            contentDescription = "Pin note",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (note.isPinned) "Unpin" else "Pin to top",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Move to Trash",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Move to Trash",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    ) {
        EditorialNoteCard(
            note = note,
            folder = folder,
            isUnlocked = isUnlocked,
            isSpeakingThisNote = isSpeakingThisNote,
            compactMode = compactMode,
            onClick = onNoteClick,
            onLongPress = onNoteLongPress,
            onToggleChecklistItem = onToggleChecklistItem,
            onReadAloudClick = onReadAloudClick,
            onTagClick = onTagClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun EditorialNoteCard(
    note: Note,
    folder: Folder?,
    isUnlocked: Boolean,
    isSpeakingThisNote: Boolean,
    compactMode: Boolean = false,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onToggleChecklistItem: (Int) -> Unit,
    onReadAloudClick: () -> Unit,
    onTagClick: (String) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val palette = NoteColorPalette.fromKey(note.colorKey)
    val surfaceColors = palette.resolveColors(isDark)
    val isHiddenByLock = note.isLocked && !isUnlocked

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = surfaceColors.container,
        border = BorderStroke(
            width = if (note.isPinned) 1.5.dp else 1.dp,
            color = if (note.isPinned) surfaceColors.accentBar.copy(alpha = 0.65f) else surfaceColors.border
        ),
        tonalElevation = if (note.isPinned) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .testTag("note_card_${note.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Tactile Left Editorial Spine Indicator for Pinned or Colored Notes
            if (note.isPinned || palette != NoteColorPalette.DEFAULT || note.isLocked) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(
                            if (note.isLocked) MaterialTheme.colorScheme.tertiary
                            else surfaceColors.accentBar
                        )
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 16.dp,
                        end = 12.dp,
                        top = if (compactMode) 12.dp else 14.dp,
                        bottom = if (compactMode) 12.dp else 14.dp
                    )
            ) {
                // Top Meta Header Row: Pinned icon / Folder chip / Timestamp / Quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (note.isPinned) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned",
                                tint = surfaceColors.accentBar,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (note.isLocked) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Protected note",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (folder != null) {
                            val folderAccent = resolveFolderAccentColor(folder.accentKey, isDark)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = folderAccent.copy(alpha = 0.14f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = resolveFolderIcon(folder.iconKey),
                                        contentDescription = folder.name,
                                        tint = folderAccent,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = folderAccent,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        Text(
                            text = formatEditorialTimestamp(note.updatedAt ?: note.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isHiddenByLock) {
                            IconButton(
                                onClick = onReadAloudClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .minimumInteractiveComponentSize()
                                    .testTag("listen_note_${note.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                                    contentDescription = if (isSpeakingThisNote) "Stop reading aloud" else "Listen to note",
                                    tint = if (isSpeakingThisNote) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onLongPress,
                            modifier = Modifier
                                .size(36.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("more_note_${note.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreHoriz,
                                contentDescription = "Note options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = note.title.ifBlank { "Untitled thought" },
                    style = if (compactMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (compactMode) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (isHiddenByLock) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.note_locked_preview),
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (!compactMode) {
                    // Body Snippet
                    if (note.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Interactive Checklist Preview (up to 3 items)
                    val checklist = note.parsedChecklist
                    if (checklist.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            checklist.take(3).forEachIndexed { idx, item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onToggleChecklistItem(idx) }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isChecked) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = if (item.isChecked) "Checked" else "Unchecked",
                                        tint = if (item.isChecked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                        else MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (checklist.size > 3) {
                                Text(
                                    text = "+${checklist.size - 3} more items (${note.completedChecklistCount}/${checklist.size} done)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                                )
                            }
                        }
                    }

                    // Voice Transcript Badge
                    if (note.voiceTranscript.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.GraphicEq,
                                    contentDescription = "Voice memo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = note.voiceTranscript,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (note.voiceDurationSec > 0L) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${note.voiceDurationSec}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Footer: Tags, Checklist summary, Reminder badge, Word count
                val hasFooterItems = note.tags.isNotEmpty() || note.reminderAtMillis > 0L || note.checklistItems.isNotEmpty()
                if (hasFooterItems && !isHiddenByLock) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (note.checklistItems.isNotEmpty() && compactMode) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "✓ ${note.completedChecklistCount}/${note.checklistItems.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (note.reminderAtMillis > 0L) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Alarm,
                                        contentDescription = "Reminder",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formatReminderTime(note.reminderAtMillis),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        note.tags.take(4).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier.clickable { onTagClick(tag) }
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
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
fun EditorialEmptyState(
    isSearchFiltered: Boolean,
    activeFilterLabel: String,
    onCreateBlankNote: () -> Unit,
    onCreateChecklistNote: () -> Unit,
    onCreateVoiceNote: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(170.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_empty_notes),
                contentDescription = "Open cream notebook illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isSearchFiltered) {
                stringResource(R.string.empty_search_title)
            } else {
                "Quiet in $activeFilterLabel"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSearchFiltered) {
                stringResource(R.string.empty_search_subtitle)
            } else {
                stringResource(R.string.empty_notes_subtitle)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isSearchFiltered) {
            OutlinedButton(
                onClick = onClearFilters,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("clear_filters_button")
            ) {
                Text("Reset Search & Filters")
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onCreateBlankNote,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("empty_state_new_note_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Write",
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = onCreateChecklistNote,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("empty_state_checklist_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckBox,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Checklist")
                }

                OutlinedButton(
                    onClick = onCreateVoiceNote,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("empty_state_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MicNone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Voice")
                }
            }
        }
    }
}
