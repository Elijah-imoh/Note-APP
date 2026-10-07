package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.Folder
import com.example.data.model.Note
import com.example.data.model.NoteColorPalette
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.resolveColors
import com.example.ui.theme.resolveFolderAccentColor
import com.example.util.NoteReminderNotificationHelper
import com.example.util.VoiceDictationHelper
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteContextBottomSheet(
    note: Note,
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onSelectColor: (NoteColorPalette) -> Unit,
    onMoveToFolder: (String) -> Unit,
    onToggleLock: () -> Unit,
    onReadAloud: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleArchive: () -> Unit,
    onMoveToTrash: () -> Unit,
    onRestoreFromTrash: () -> Unit,
    onRequestDeleteForever: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = note.title.ifBlank { "Untitled thought" },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = "${note.wordCount} words • ${note.readingTimeMinutes} min read",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Paper Tint Swatches
            Text(
                text = "PAPER TINT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NoteColorPalette.entries.forEach { palette ->
                    val swatchColors = palette.resolveColors(isDark)
                    val selected = note.colorKey == palette.key
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(swatchColors.swatchPreview)
                            .border(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else swatchColors.border,
                                shape = CircleShape
                            )
                            .clickable {
                                onSelectColor(palette)
                            }
                            .testTag("context_color_${palette.key}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = palette.label,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (folders.isNotEmpty() && !note.isTrashed) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "MOVE TO FOLDER",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = note.folderId.isBlank(),
                        onClick = {
                            onMoveToFolder("")
                            onDismiss()
                        },
                        label = { Text("Unfiled") }
                    )
                    folders.forEach { folder ->
                        FilterChip(
                            selected = note.folderId == folder.id,
                            onClick = {
                                onMoveToFolder(folder.id)
                                onDismiss()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = resolveFolderIcon(folder.iconKey),
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            label = { Text(folder.name) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            if (!note.isTrashed) {
                ContextMenuActionRow(
                    icon = Icons.Outlined.PushPin,
                    label = if (note.isPinned) "Unpin from top" else "Pin to top",
                    onClick = {
                        onTogglePin()
                        onDismiss()
                    }
                )
                ContextMenuActionRow(
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                    label = "Listen aloud (Text-to-Speech)",
                    onClick = {
                        onReadAloud()
                        onDismiss()
                    }
                )
                ContextMenuActionRow(
                    icon = if (note.isLocked) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                    label = if (note.isLocked) "Remove passcode lock" else "Protect with 4-digit passcode",
                    onClick = {
                        onToggleLock()
                        onDismiss()
                    }
                )
                ContextMenuActionRow(
                    icon = Icons.Outlined.ContentCopy,
                    label = "Duplicate note",
                    onClick = {
                        onDuplicate()
                        onDismiss()
                    }
                )
                ContextMenuActionRow(
                    icon = if (note.isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                    label = if (note.isArchived) "Unarchive note" else "Archive note",
                    onClick = {
                        onToggleArchive()
                        onDismiss()
                    }
                )
                ContextMenuActionRow(
                    icon = Icons.Outlined.DeleteOutline,
                    label = "Move to Trash",
                    isDestructive = true,
                    onClick = {
                        onMoveToTrash()
                        onDismiss()
                    }
                )
            } else {
                ContextMenuActionRow(
                    icon = Icons.Outlined.RestoreFromTrash,
                    label = "Restore note from Trash",
                    onClick = {
                        onRestoreFromTrash()
                        onDismiss()
                    }
                )
                ContextMenuActionRow(
                    icon = Icons.Outlined.DeleteForever,
                    label = "Delete forever",
                    isDestructive = true,
                    onClick = {
                        onDismiss()
                        onRequestDeleteForever()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ContextMenuActionRow(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceDictationBottomSheet(
    initialTranscript: String,
    initialDurationSec: Long,
    onDismiss: () -> Unit,
    onSaveVoiceCapture: (transcript: String, durationSec: Long, appendToBody: Boolean) -> Unit
) {
    val context = LocalContext.current
    var transcriptText by remember { mutableStateOf(initialTranscript) }
    var partialText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableLongStateOf(initialDurationSec) }
    var statusMessage by remember {
        mutableStateOf("Tap the microphone to dictate hands-free, or refine the transcript below.")
    }

    val dictationHelper = remember(context) {
        VoiceDictationHelper(
            context = context,
            onPartialResult = { partial ->
                partialText = partial
            },
            onFinalResult = { finalResult ->
                partialText = ""
                transcriptText = if (transcriptText.isBlank()) {
                    finalResult
                } else {
                    "$transcriptText $finalResult"
                }
                statusMessage = "Captured speech. Tap microphone to continue or save below."
            },
            onError = { err ->
                statusMessage = err
            },
            onListeningStateChanged = { listening ->
                isListening = listening
                if (listening) {
                    statusMessage = "Listening… Speak naturally."
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            dictationHelper.destroy()
        }
    }

    LaunchedEffect(isListening) {
        while (isListening) {
            delay(1000L)
            elapsedSeconds += 1L
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            dictationHelper.startListening()
        } else {
            statusMessage = "Microphone permission was declined. You can still type a voice memo transcript below."
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            dictationHelper.stopListening()
            onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Voice Capture & Dictation",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Tactile Waveform & Timer Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WaveformVisualizer(isAnimating = isListening)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Microphone Record / Stop Button
            Button(
                onClick = {
                    if (isListening) {
                        dictationHelper.stopListening()
                    } else {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            dictationHelper.startListening()
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .size(64.dp)
                    .testTag("voice_record_toggle_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                    contentDescription = if (isListening) "Stop listening" else "Start voice dictation",
                    modifier = Modifier.size(28.dp)
                )
            }

            if (partialText.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "“$partialText…”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = transcriptText,
                onValueChange = {
                    transcriptText = it
                    if (elapsedSeconds == 0L && it.isNotBlank()) {
                        elapsedSeconds = (it.split(Regex("\\s+")).size * 2L).coerceAtLeast(3L)
                    }
                },
                label = { Text("Voice Transcript") },
                placeholder = { Text("Spoken words appear here…") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_transcript_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        dictationHelper.stopListening()
                        onSaveVoiceCapture(transcriptText.trim(), elapsedSeconds, false)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Attach Audio Memo")
                }

                Button(
                    onClick = {
                        dictationHelper.stopListening()
                        onSaveVoiceCapture(transcriptText.trim(), elapsedSeconds, true)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_voice_to_body_button")
                ) {
                    Text("Insert into Note")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun WaveformVisualizer(isAnimating: Boolean) {
    val transition = rememberInfiniteTransition(label = "waveform")
    val phase by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_phase"
    )
    val baseHeights = listOf(10, 18, 28, 16, 34, 24, 38, 20, 30, 16, 26, 14)

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(42.dp)
    ) {
        baseHeights.forEachIndexed { index, h ->
            val factor = if (!isAnimating) 0.35f else {
                if (index % 2 == 0) phase else (1.25f - phase).coerceIn(0.3f, 1.0f)
            }
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((h * factor).dp)
                    .clip(CircleShape)
                    .background(
                        if (isAnimating) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPickerBottomSheet(
    noteTitle: String,
    currentReminderMillis: Long,
    onDismiss: () -> Unit,
    onSelectReminderMillis: (Long) -> Unit
) {
    val context = LocalContext.current
    var pendingReminderMillis by remember { mutableLongStateOf(0L) }
    var pendingLabel by remember { mutableStateOf("") }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        if (pendingReminderMillis > 0L) {
            NoteReminderNotificationHelper.sendReminderConfirmationNotification(
                context = context,
                noteTitle = noteTitle,
                formattedTime = pendingLabel
            )
            onSelectReminderMillis(pendingReminderMillis)
            onDismiss()
        }
    }

    fun triggerReminderSelection(targetMillis: Long) {
        val formatted = formatReminderTime(targetMillis)
        pendingReminderMillis = targetMillis
        pendingLabel = formatted

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPerm) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        NoteReminderNotificationHelper.sendReminderConfirmationNotification(
            context = context,
            noteTitle = noteTitle,
            formattedTime = formatted
        )
        onSelectReminderMillis(targetMillis)
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Schedule Thought Reminder",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Receive a gentle notification to revisit this note.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val now = System.currentTimeMillis()
            val presets = listOf(
                "In 1 Hour" to (now + 3600_000L),
                "Later Today • Evening" to Calendar.getInstance().apply {
                    add(Calendar.HOUR_OF_DAY, 4)
                }.timeInMillis,
                "Tomorrow Morning • 9:00 AM" to Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                }.timeInMillis,
                "Next Week • Monday 9:00 AM" to Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 7)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                }.timeInMillis
            )

            presets.forEach { (title, timeMillis) ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { triggerReminderSelection(timeMillis) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = formatReminderTime(timeMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (currentReminderMillis > 0L) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        onSelectReminderMillis(0L)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Active Reminder")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PasscodeLockDialog(
    storedPinCode: String,
    isSettingNewPin: Boolean,
    onDismiss: () -> Unit,
    onPinVerifiedOrCreated: (String) -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val requiresSetup = storedPinCode.isBlank() || isSettingNewPin

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (requiresSetup) "Create 4-Digit Studio Passcode" else "Unlock Protected Thought",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = if (requiresSetup) {
                        "Set a 4-digit passcode to protect locked notes in your workspace."
                    } else {
                        "Enter your 4-digit studio passcode to view or unlock this thought."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = { value ->
                        errorText = null
                        enteredPin = value.filter { it.isDigit() }.take(4)
                    },
                    label = { Text("4-Digit Passcode") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("passcode_input")
                )
                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorText ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredPin.length < 4) {
                        errorText = "Please enter 4 digits."
                    } else if (requiresSetup || enteredPin == storedPinCode) {
                        onPinVerifiedOrCreated(enteredPin)
                    } else {
                        errorText = "Incorrect passcode. Please try again."
                    }
                },
                modifier = Modifier.testTag("passcode_confirm_button")
            ) {
                Text(if (requiresSetup) "Save Passcode" else "Unlock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderEditorDialog(
    initialFolder: Folder?,
    onDismiss: () -> Unit,
    onSave: (name: String, iconKey: String, accentKey: String) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var name by remember { mutableStateOf(initialFolder?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(initialFolder?.iconKey ?: "book") }
    var selectedAccent by remember { mutableStateOf(initialFolder?.accentKey ?: "terracotta") }

    val iconOptions = listOf(
        "book" to "Book",
        "work" to "Studio",
        "ideas" to "Ideas",
        "journal" to "Journal",
        "bookmark" to "Saved",
        "voice" to "Audio"
    )
    val accentOptions = listOf("terracotta", "sage", "ochre", "slate", "plum")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialFolder == null) "New Collection Folder" else "Edit Folder",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(80) },
                    label = { Text("Folder Name") },
                    placeholder = { Text("e.g., Essays, Field Notes, Bookmarks") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("folder_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ICON",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    iconOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedIcon == key,
                            onClick = { selectedIcon = key },
                            leadingIcon = {
                                Icon(
                                    imageVector = resolveFolderIcon(key),
                                    contentDescription = label,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ACCENT COLOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    accentOptions.forEach { accent ->
                        val color = resolveFolderAccentColor(accent, isDark)
                        val isSelected = selectedAccent == accent
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedAccent = accent },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = accent,
                                    tint = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedIcon, selectedAccent)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_folder_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmPermanentDeleteDialog(
    title: String,
    body: String,
    confirmLabel: String = "Delete Forever",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_permanent_delete_button")
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
