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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatEditorialTimestamp(epochMillis: Long?): String {
    val date = if (epochMillis != null && epochMillis > 0L) Date(epochMillis) else Date()
    return SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()).format(date)
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
                    false
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
                    .clip(RoundedCornerShape(24.dp))
                    .background(bgColor)
                    .padding(horizontal = 22.dp),
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
        shape = RoundedCornerShape(24.dp),
        color = surfaceColors.container,
        border = BorderStroke(
            width = 1.dp,
            color = surfaceColors.border
        ),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .testTag("note_card_${note.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = if (compactMode) 16.dp else 20.dp
                )
        ) {
            // Top Row: Bold Title on Left + Minimal Monochrome Status Pill Badge on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title.ifBlank { "Untitled thought" },
                        style = if (compactMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = formatEditorialTimestamp(note.updatedAtMillis),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                        if (folder != null) {
                            Text(
                                text = "· ${folder.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Top-Right Minimal Monochrome Badge Pill (like the "8 ☺" badge in the reference UI)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSpeakingThisNote) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (!isHiddenByLock) {
                                onReadAloudClick()
                            } else {
                                onLongPress()
                            }
                        }
                        .testTag("listen_note_${note.id}")
                ) {
                    val badgeContentColor = if (isSpeakingThisNote) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        val badgeNumber = when {
                            note.checklistItems.isNotEmpty() -> "${note.completedChecklistCount}/${note.checklistItems.size}"
                            else -> ((note.wordCount / 3L).coerceIn(1L, 99L)).toString()
                        }
                        Text(
                            text = badgeNumber,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeContentColor
                        )
                        Icon(
                            imageVector = when {
                                note.isLocked -> Icons.Filled.Lock
                                isSpeakingThisNote -> Icons.AutoMirrored.Outlined.VolumeUp
                                note.isPinned -> Icons.Filled.PushPin
                                else -> Icons.Outlined.SentimentSatisfied
                            },
                            contentDescription = if (isSpeakingThisNote) "Stop reading aloud" else "Listen to note",
                            tint = badgeContentColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            if (isHiddenByLock) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                // Clean Body Paragraph Preview
                if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Minimal Interactive Checklist Preview (up to 3 items)
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onToggleChecklistItem(idx) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.isChecked) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = if (item.isChecked) "Checked" else "Unchecked",
                                    tint = if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Row: Outlined Minimal Tag Pills (matching reference image)
            val hasFooterItems = note.tags.isNotEmpty() || note.reminderAtMillis > 0L
            if (hasFooterItems && !isHiddenByLock) {
                Spacer(modifier = Modifier.height(14.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    note.tags.take(4).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onTagClick(tag) }
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }

                    if (note.reminderAtMillis > 0L) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Alarm,
                                    contentDescription = "Reminder",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = formatReminderTime(note.reminderAtMillis),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
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
                .height(160.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_empty_notes_bw),
                contentDescription = "Open monochrome notebook illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isSearchFiltered) {
                stringResource(R.string.empty_search_title)
            } else {
                "No notes in $activeFilterLabel"
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
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("clear_filters_button")
            ) {
                Text("Show All Notes")
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onCreateBlankNote,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("empty_state_new_note_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New Note",
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = onCreateChecklistNote,
                    shape = RoundedCornerShape(14.dp),
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
            }
        }
    }
}
