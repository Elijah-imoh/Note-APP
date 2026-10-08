package com.example.data.model

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
    val reminderAtMillis: Long = 0L,
    val wordCount: Long = 0L,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val parsedChecklist: List<ChecklistItem>
        get() = checklistItems.mapIndexed { idx, raw ->
            ChecklistItem.fromSerializedString(idx, raw)
        }

    val completedChecklistCount: Int
        get() = parsedChecklist.count { it.isChecked }

    val readingTimeMinutes: Int
        get() = ((wordCount.toInt() + parsedChecklist.size * 4) / 180).coerceAtLeast(1)

    companion object {
        fun computeWordCount(title: String, content: String): Long {
            val combined = "$title $content".trim()
            if (combined.isBlank()) return 0L
            return combined.split(Regex("\\s+")).size.toLong().coerceIn(0L, 100000L)
        }
    }
}

data class Folder(
    val id: String = "",
    val name: String = "",
    val iconKey: String = "book",
    val accentKey: String = "ink_100",
    val sortOrder: Long = 0L,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

enum class NoteColorPalette(val key: String, val label: String) {
    DEFAULT("default", "Pure Paper"),
    WARM_SAND("warm_sand", "Newsprint 5%"),
    TERRACOTTA("terracotta_clay", "Drafting 10%"),
    SAGE("sage_mist", "Archival 15%"),
    OCHRE("amber_ochre", "Graphite 20%"),
    SLATE("slate_ink", "High-Contrast Ink");

    companion object {
        fun fromKey(key: String): NoteColorPalette =
            entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

enum class NoteWorkspaceFilter(val label: String) {
    ALL("All Notes"),
    PINNED("Pinned"),
    CHECKLISTS("Checklists"),
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
