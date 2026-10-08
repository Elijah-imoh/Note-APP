package com.example.data.repository

import android.content.Context
import com.example.data.local.FolderEntity
import com.example.data.local.MyNotesDatabase
import com.example.data.local.NoteDao
import com.example.data.local.NoteEntity
import com.example.data.model.Folder
import com.example.data.model.Note
import com.example.data.model.NoteColorPalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class NoteRepository(
    private val noteDao: NoteDao
) {
    constructor(context: Context) : this(
        MyNotesDatabase.getInstance(context.applicationContext).noteDao()
    )

    fun observeNotes(): Flow<List<Note>> =
        noteDao.observeAllNotes().map { list ->
            list.map { it.toDomainModel() }
        }

    fun observeFolders(): Flow<List<Folder>> =
        noteDao.observeAllFolders().map { list ->
            list.map { it.toDomainModel() }
        }

    suspend fun createNote(note: Note): Result<String> {
        return try {
            val now = System.currentTimeMillis()
            val docId = note.id.ifBlank { "note_${UUID.randomUUID().toString().take(12)}" }
            val entity = NoteEntity.fromDomainModel(
                note.copy(
                    id = docId,
                    createdAtMillis = now,
                    updatedAtMillis = now
                )
            )
            noteDao.upsertNote(entity)
            Result.success(docId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(note: Note): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val entity = NoteEntity.fromDomainModel(
                note.copy(updatedAtMillis = now)
            )
            noteDao.upsertNote(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNoteById(noteId: String): Result<Note> {
        return try {
            val entity = noteDao.getNoteById(noteId)
            if (entity == null) {
                Result.failure(NoSuchElementException("Note not found"))
            } else {
                Result.success(entity.toDomainModel())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNotePermanently(noteId: String): Result<Unit> {
        return try {
            noteDao.deleteNoteById(noteId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun emptyTrash(): Result<Unit> {
        return try {
            noteDao.deleteAllTrashedNotes()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFolder(folder: Folder): Result<String> {
        return try {
            val now = System.currentTimeMillis()
            val docId = folder.id.ifBlank { "folder_${UUID.randomUUID().toString().take(10)}" }
            val entity = FolderEntity.fromDomainModel(
                folder.copy(
                    id = docId,
                    createdAtMillis = now,
                    updatedAtMillis = now
                )
            )
            noteDao.upsertFolder(entity)
            Result.success(docId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFolder(folder: Folder): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val entity = FolderEntity.fromDomainModel(
                folder.copy(updatedAtMillis = now)
            )
            noteDao.upsertFolder(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFolder(folderId: String): Result<Unit> {
        return try {
            noteDao.clearFolderFromNotes(folderId)
            noteDao.deleteFolderById(folderId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun seedWorkspaceStarterKitIfEmpty(): Result<Unit> {
        return try {
            if (noteDao.getNotesCount() > 0) {
                return Result.success(Unit)
            }

            val now = System.currentTimeMillis()
            val starterFolders = listOf(
                Folder(
                    id = "folder_essays",
                    name = "Essays & Ideas",
                    iconKey = "book",
                    accentKey = "ink_100",
                    sortOrder = 1L,
                    createdAtMillis = now - 3600_000L,
                    updatedAtMillis = now - 3600_000L
                ),
                Folder(
                    id = "folder_projects",
                    name = "Studio Projects",
                    iconKey = "work",
                    accentKey = "ink_80",
                    sortOrder = 2L,
                    createdAtMillis = now - 3200_000L,
                    updatedAtMillis = now - 3200_000L
                ),
                Folder(
                    id = "folder_journal",
                    name = "Daily Reflections",
                    iconKey = "journal",
                    accentKey = "ink_60",
                    sortOrder = 3L,
                    createdAtMillis = now - 2800_000L,
                    updatedAtMillis = now - 2800_000L
                )
            )
            starterFolders.forEach { createFolder(it) }

            val starterNotes = listOf(
                Note(
                    id = "note_meeting_outcome",
                    title = "Meeting Outcome: New Equipment",
                    content = "You had a meeting with Michael and decided to purchase two new computers and a new projector to meet current studio needs and upcoming client presentations.",
                    folderId = "folder_projects",
                    tags = listOf("meeting", "equipment", "technology"),
                    checklistItems = emptyList(),
                    isPinned = true,
                    colorKey = NoteColorPalette.DEFAULT.key,
                    createdAtMillis = now - 600_000L,
                    updatedAtMillis = now - 600_000L
                ),
                Note(
                    id = "note_client_search",
                    title = "Client Search Functionality Discussion",
                    content = "You plan to discuss the search functionality with the client and implement any design changes that are agreed upon during the review session.",
                    folderId = "folder_projects",
                    tags = listOf("client", "search", "design"),
                    checklistItems = emptyList(),
                    isPinned = false,
                    colorKey = NoteColorPalette.DEFAULT.key,
                    createdAtMillis = now - 1800_000L,
                    updatedAtMillis = now - 1800_000L
                ),
                Note(
                    id = "note_welcome_guide",
                    title = "Minimal Offline Note Studio",
                    content = "All notes are saved 100% offline on your device. Tap the center button in the bottom dock to start writing, or use your keyboard's microphone to dictate directly into any note.",
                    folderId = "folder_essays",
                    tags = listOf("offline", "minimal", "notes"),
                    checklistItems = listOf(
                        "[x] Clean black & white minimal interface",
                        "[ ] Try keyboard voice dictation in the editor"
                    ),
                    isPinned = false,
                    colorKey = NoteColorPalette.DEFAULT.key,
                    createdAtMillis = now - 7200_000L,
                    updatedAtMillis = now - 3600_000L
                )
            )
            starterNotes.forEach { createNote(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
