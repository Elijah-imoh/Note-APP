package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.Folder
import com.example.data.model.Note
import com.example.data.model.NoteColorPalette
import kotlinx.coroutines.flow.Flow

private const val LIST_DELIMITER = "\u001F"

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val folderId: String,
    val serializedTags: String,
    val serializedChecklist: String,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isTrashed: Boolean,
    val isLocked: Boolean,
    val colorKey: String,
    val reminderAtMillis: Long,
    val wordCount: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toDomainModel(): Note {
        val tags = if (serializedTags.isBlank()) {
            emptyList()
        } else {
            serializedTags.split(LIST_DELIMITER).filter { it.isNotBlank() }
        }
        val checklist = if (serializedChecklist.isBlank()) {
            emptyList()
        } else {
            serializedChecklist.split(LIST_DELIMITER).filter { it.isNotBlank() }
        }
        return Note(
            id = id,
            title = title,
            content = content,
            folderId = folderId,
            tags = tags,
            checklistItems = checklist,
            isPinned = isPinned,
            isArchived = isArchived,
            isTrashed = isTrashed,
            isLocked = isLocked,
            colorKey = colorKey.ifBlank { NoteColorPalette.DEFAULT.key },
            reminderAtMillis = reminderAtMillis,
            wordCount = wordCount,
            createdAtMillis = createdAtMillis,
            updatedAtMillis = updatedAtMillis
        )
    }

    companion object {
        fun fromDomainModel(note: Note): NoteEntity {
            val cleanTags = note.tags.map { it.trim() }.filter { it.isNotEmpty() }.take(10)
            val cleanChecklist = note.checklistItems.map { it.trim() }.filter { it.isNotEmpty() }.take(50)
            val words = Note.computeWordCount(note.title, note.content)
            return NoteEntity(
                id = note.id,
                title = note.title.take(300),
                content = note.content.take(50000),
                folderId = note.folderId,
                serializedTags = cleanTags.joinToString(LIST_DELIMITER),
                serializedChecklist = cleanChecklist.joinToString(LIST_DELIMITER),
                isPinned = note.isPinned,
                isArchived = note.isArchived,
                isTrashed = note.isTrashed,
                isLocked = note.isLocked,
                colorKey = note.colorKey.ifBlank { NoteColorPalette.DEFAULT.key },
                reminderAtMillis = note.reminderAtMillis.coerceAtLeast(0L),
                wordCount = words,
                createdAtMillis = note.createdAtMillis,
                updatedAtMillis = note.updatedAtMillis
            )
        }
    }
}

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val accentKey: String,
    val sortOrder: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toDomainModel(): Folder = Folder(
        id = id,
        name = name,
        iconKey = iconKey,
        accentKey = accentKey,
        sortOrder = sortOrder,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromDomainModel(folder: Folder): FolderEntity = FolderEntity(
            id = folder.id,
            name = folder.name.trim().ifBlank { "Untitled Folder" }.take(80),
            iconKey = folder.iconKey.ifBlank { "book" },
            accentKey = folder.accentKey.ifBlank { "ink_100" },
            sortOrder = folder.sortOrder,
            createdAtMillis = folder.createdAtMillis,
            updatedAtMillis = folder.updatedAtMillis
        )
    }
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAtMillis DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: String): NoteEntity?

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNotesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)

    @Query("DELETE FROM notes WHERE isTrashed = 1")
    suspend fun deleteAllTrashedNotes()

    @Query("UPDATE notes SET folderId = '' WHERE folderId = :folderId")
    suspend fun clearFolderFromNotes(folderId: String)

    @Query("SELECT * FROM folders ORDER BY sortOrder ASC, createdAtMillis ASC")
    fun observeAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: String)
}

@Database(
    entities = [NoteEntity::class, FolderEntity::class],
    version = 3,
    exportSchema = false
)
abstract class MyNotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: MyNotesDatabase? = null

        fun getInstance(context: Context): MyNotesDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MyNotesDatabase::class.java,
                    "my_notes_offline.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
