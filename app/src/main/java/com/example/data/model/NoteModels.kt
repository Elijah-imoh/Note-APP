package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

data class ChecklistItem(
    val id: String = "",
    val text: String = "",
    val isChecked: Boolean = false
) {
    fun toSerializedString(): String {
        val prefix = if (isChecked) "[x] " else "[ ] "
        return (prefix + text.trim()).take(500)
    }

    companion object {
        fun fromSerializedString(index: Int, raw: String): ChecklistItem {
            val trimmed = raw.trim()
            return when {
                trimmed.startsWith("[x] ", ignoreCase = true) -> ChecklistItem(
                    id = "chk_${index}_${trimmed.hashCode()}",
                    text = trimmed.substring(4),
                    isChecked = true
                )
                trimmed.startsWith("[ ] ") -> ChecklistItem(
                    id = "chk_${index}_${trimmed.hashCode()}",
                    text = trimmed.substring(4),
                    isChecked = false
                )
                else -> ChecklistItem(
                    id = "chk_${index}_${trimmed.hashCode()}",
                    text = trimmed,
                    isChecked = false
                )
            }
        }
    }
}

data class Note(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val folderId: String = "",
    val tags: List<String> = emptyList(),
    val checklistItems: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isTrashed: Boolean = false,
    val isLocked: Boolean = false,
    val colorKey: String = NoteColorPalette.DEFAULT.key,
    val voiceTranscript: String = "",
    val voiceDurationSec: Long = 0L,
    val reminderAtMillis: Long = 0L,
    val wordCount: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val parsedChecklist: List<ChecklistItem>
        get() = checklistItems.mapIndexed { idx, raw ->
            ChecklistItem.fromSerializedString(idx, raw)
        }

    val completedChecklistCount: Int
        get() = parsedChecklist.count { it.isChecked }

    val readingTimeMinutes: Int
        get() = ((wordCount.toInt() + parsedChecklist.size * 4) / 180).coerceAtLeast(1)

    fun toCreateMap(ownerUid: String, docId: String): Map<String, Any> {
        val sanitizedTags = tags.map { it.trim().take(40) }.filter { it.isNotEmpty() }.take(10)
        val sanitizedChecklist = checklistItems.map { it.take(500) }.take(50)
        val computedWords = computeWordCount(title, content, voiceTranscript)
        return mapOf(
            "id" to docId,
            "userId" to ownerUid,
            "title" to title.take(300),
            "content" to content.take(50000),
            "folderId" to folderId.take(128),
            "tags" to sanitizedTags,
            "checklistItems" to sanitizedChecklist,
            "isPinned" to isPinned,
            "isArchived" to isArchived,
            "isTrashed" to isTrashed,
            "isLocked" to isLocked,
            "colorKey" to colorKey.ifBlank { NoteColorPalette.DEFAULT.key }.take(40),
            "voiceTranscript" to voiceTranscript.take(10000),
            "voiceDurationSec" to voiceDurationSec.coerceIn(0L, 86400L),
            "reminderAtMillis" to reminderAtMillis.coerceAtLeast(0L),
            "wordCount" to computedWords,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    fun toUpdateMap(): Map<String, Any> {
        val sanitizedTags = tags.map { it.trim().take(40) }.filter { it.isNotEmpty() }.take(10)
        val sanitizedChecklist = checklistItems.map { it.take(500) }.take(50)
        val computedWords = computeWordCount(title, content, voiceTranscript)
        return mapOf(
            "title" to title.take(300),
            "content" to content.take(50000),
            "folderId" to folderId.take(128),
            "tags" to sanitizedTags,
            "checklistItems" to sanitizedChecklist,
            "isPinned" to isPinned,
            "isArchived" to isArchived,
            "isTrashed" to isTrashed,
            "isLocked" to isLocked,
            "colorKey" to colorKey.ifBlank { NoteColorPalette.DEFAULT.key }.take(40),
            "voiceTranscript" to voiceTranscript.take(10000),
            "voiceDurationSec" to voiceDurationSec.coerceIn(0L, 86400L),
            "reminderAtMillis" to reminderAtMillis.coerceAtLeast(0L),
            "wordCount" to computedWords,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    companion object {
        fun computeWordCount(title: String, content: String, voiceTranscript: String): Long {
            val combined = "$title $content $voiceTranscript".trim()
            if (combined.isBlank()) return 0L
            return combined.split(Regex("\\s+")).size.toLong().coerceIn(0L, 100000L)
        }

        fun fromDocument(doc: DocumentSnapshot): Note {
            val data = doc.data ?: emptyMap()
            val rawTags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val rawChecklist = (data["checklistItems"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            return Note(
                id = (data["id"] as? String)?.ifBlank { doc.id } ?: doc.id,
                userId = data["userId"] as? String ?: "",
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                folderId = data["folderId"] as? String ?: "",
                tags = rawTags,
                checklistItems = rawChecklist,
                isPinned = data["isPinned"] as? Boolean ?: false,
                isArchived = data["isArchived"] as? Boolean ?: false,
                isTrashed = data["isTrashed"] as? Boolean ?: false,
                isLocked = data["isLocked"] as? Boolean ?: false,
                colorKey = (data["colorKey"] as? String)?.ifBlank { NoteColorPalette.DEFAULT.key }
                    ?: NoteColorPalette.DEFAULT.key,
                voiceTranscript = data["voiceTranscript"] as? String ?: "",
                voiceDurationSec = (data["voiceDurationSec"] as? Number)?.toLong() ?: 0L,
                reminderAtMillis = (data["reminderAtMillis"] as? Number)?.toLong() ?: 0L,
                wordCount = (data["wordCount"] as? Number)?.toLong() ?: 0L,
                createdAt = doc.getTimestamp(
                    "createdAt",
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ),
                updatedAt = doc.getTimestamp(
                    "updatedAt",
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            )
        }
    }
}

data class Folder(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val iconKey: String = "book",
    val accentKey: String = "terracotta",
    val sortOrder: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(ownerUid: String, docId: String): Map<String, Any> {
        return mapOf(
            "id" to docId,
            "userId" to ownerUid,
            "name" to name.trim().ifBlank { "Untitled Folder" }.take(80),
            "iconKey" to iconKey.ifBlank { "book" }.take(40),
            "accentKey" to accentKey.ifBlank { "terracotta" }.take(40),
            "sortOrder" to sortOrder.coerceIn(0L, 10000L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    fun toUpdateMap(): Map<String, Any> {
        return mapOf(
            "name" to name.trim().ifBlank { "Untitled Folder" }.take(80),
            "iconKey" to iconKey.ifBlank { "book" }.take(40),
            "accentKey" to accentKey.ifBlank { "terracotta" }.take(40),
            "sortOrder" to sortOrder.coerceIn(0L, 10000L),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    companion object {
        fun fromDocument(doc: DocumentSnapshot): Folder {
            val data = doc.data ?: emptyMap()
            return Folder(
                id = (data["id"] as? String)?.ifBlank { doc.id } ?: doc.id,
                userId = data["userId"] as? String ?: "",
                name = data["name"] as? String ?: "",
                iconKey = data["iconKey"] as? String ?: "book",
                accentKey = data["accentKey"] as? String ?: "terracotta",
                sortOrder = (data["sortOrder"] as? Number)?.toLong() ?: 0L,
                createdAt = doc.getTimestamp(
                    "createdAt",
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ),
                updatedAt = doc.getTimestamp(
                    "updatedAt",
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            )
        }
    }
}

enum class NoteColorPalette(val key: String, val label: String) {
    DEFAULT("default", "Linen Paper"),
    WARM_SAND("warm_sand", "Warm Travertine"),
    TERRACOTTA("terracotta_clay", "Terracotta Mist"),
    SAGE("sage_mist", "Botanical Sage"),
    OCHRE("amber_ochre", "Honey Ochre"),
    SLATE("slate_ink", "Evening Slate");

    companion object {
        fun fromKey(key: String): NoteColorPalette =
            entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

enum class NoteWorkspaceFilter(val label: String) {
    ALL("All Notes"),
    PINNED("Pinned"),
    CHECKLISTS("Checklists"),
    VOICE("Audio & Voice"),
    REMINDERS("Reminders"),
    PROTECTED("Locked"),
    ARCHIVED("Archive"),
    TRASH("Trash")
}

enum class NoteSortOrder(val label: String) {
    UPDATED_DESC("Recently Edited"),
    CREATED_DESC("Date Created"),
    TITLE_ASC("Title (A–Z)"),
    WORD_COUNT_DESC("Longest Reads")
}

enum class NoteLayoutStyle(val label: String) {
    MASONRY_GRID("Editorial Grid"),
    COMFORTABLE_LIST("Tactile List"),
    COMPACT_INDEX("Compact Index")
}
