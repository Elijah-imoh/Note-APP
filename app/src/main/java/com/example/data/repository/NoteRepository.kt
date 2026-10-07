package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.Folder
import com.example.data.model.Note
import com.example.data.model.NoteColorPalette
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NoteRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun sanitizeDocId(rawId: String): String {
        val cleaned = rawId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return cleaned.ifBlank { "doc_${UUID.randomUUID().toString().replace("-", "")}" }.take(128)
    }

    fun observeNotes(userId: String = auth.currentUser?.uid ?: ""): Flow<List<Note>> = callbackFlow {
        val targetUid = userId.ifBlank {
            auth.currentUser?.uid ?: "unauthenticated"
        }
        val path = "users/$targetUid/notes"
        val registration = db.collection("users")
            .document(targetUid)
            .collection("notes")
            .whereEqualTo("userId", targetUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notes = snapshot.documents
                        .map { doc -> Note.fromDocument(doc) }
                        .sortedWith(
                            compareByDescending<Note> { it.isPinned }
                                .thenByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                        )
                    trySend(notes)
                }
            }
        awaitClose { registration.remove() }
    }

    fun observeFolders(userId: String = auth.currentUser?.uid ?: ""): Flow<List<Folder>> = callbackFlow {
        val targetUid = userId.ifBlank {
            auth.currentUser?.uid ?: "unauthenticated"
        }
        val path = "users/$targetUid/folders"
        val registration = db.collection("users")
            .document(targetUid)
            .collection("folders")
            .whereEqualTo("userId", targetUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val folders = snapshot.documents
                        .map { doc -> Folder.fromDocument(doc) }
                        .sortedBy { it.sortOrder }
                    trySend(folders)
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun createNote(note: Note): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docId = sanitizeDocId(note.id.ifBlank { "note_${UUID.randomUUID().toString().take(12)}" })
        val docRef = db.collection("users").document(uid).collection("notes").document(docId)
        return try {
            docRef.set(note.toCreateMap(uid, docId)).await()
            Result.success(docId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateNote(note: Note): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docId = sanitizeDocId(note.id)
        val docRef = db.collection("users").document(uid).collection("notes").document(docId)
        return try {
            docRef.update(note.toUpdateMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getNoteById(noteId: String, ownerUserId: String? = null): Result<Note> {
        val uid = ownerUserId ?: try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docId = sanitizeDocId(noteId)
        val docRef = db.collection("users").document(uid).collection("notes").document(docId)
        return try {
            val snap = docRef.get(com.google.firebase.firestore.Source.SERVER).await()
            if (!snap.exists()) {
                Result.failure(NoSuchElementException("Note not found"))
            } else {
                Result.success(Note.fromDocument(snap))
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateNoteFields(noteId: String, fields: Map<String, Any>): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docId = sanitizeDocId(noteId)
        val docRef = db.collection("users").document(uid).collection("notes").document(docId)
        val payload = fields + ("updatedAt" to FieldValue.serverTimestamp())
        return try {
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteNotePermanently(noteId: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docId = sanitizeDocId(noteId)
        val docRef = db.collection("users").document(uid).collection("notes").document(docId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createFolder(folder: Folder): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docId = sanitizeDocId(folder.id.ifBlank { "folder_${UUID.randomUUID().toString().take(10)}" })
        val docRef = db.collection("users").document(uid).collection("folders").document(docId)
        return try {
            docRef.set(folder.toCreateMap(uid, docId)).await()
            Result.success(docId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateFolder(folder: Folder): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docId = sanitizeDocId(folder.id)
        val docRef = db.collection("users").document(uid).collection("folders").document(docId)
        return try {
            docRef.update(folder.toUpdateMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteFolder(folderId: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docId = sanitizeDocId(folderId)
        val docRef = db.collection("users").document(uid).collection("folders").document(docId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun seedWorkspaceStarterKitIfEmpty(): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val notesPath = "users/$uid/notes"
        return try {
            val existingNotes = db.collection("users")
                .document(uid)
                .collection("notes")
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .await()

            if (!existingNotes.isEmpty) {
                return Result.success(Unit)
            }

            // Create 3 thoughtful starter folders
            val starterFolders = listOf(
                Folder(
                    id = "folder_essays",
                    userId = uid,
                    name = "Essays & Ideas",
                    iconKey = "book",
                    accentKey = "terracotta",
                    sortOrder = 1L
                ),
                Folder(
                    id = "folder_projects",
                    userId = uid,
                    name = "Studio Projects",
                    iconKey = "work",
                    accentKey = "sage",
                    sortOrder = 2L
                ),
                Folder(
                    id = "folder_journal",
                    userId = uid,
                    name = "Daily Reflections",
                    iconKey = "journal",
                    accentKey = "ochre",
                    sortOrder = 3L
                )
            )
            for (folder in starterFolders) {
                createFolder(folder)
            }

            // Create 3 tactile, realistic starter notes so first-time users can explore features immediately
            val starterNotes = listOf(
                Note(
                    id = "note_welcome_guide",
                    userId = uid,
                    title = "Welcome to My Notes — A Quiet Place for Thinking",
                    content = "Designed with tactile warmth and typographic clarity, My Notes keeps every thought effortless to capture, organize, listen to, and protect.\n\n• Tap the Read Aloud icon inside any note to listen to your writing with natural speech synthesis.\n• Use Voice Dictation to capture spoken thoughts hands-free.\n• Swipe a note card right to Pin, or swipe left to move to Trash with instant Undo.\n• Long-press any note card to open the quick context menu.",
                    folderId = "folder_essays",
                    tags = listOf("welcome", "guide", "design"),
                    checklistItems = listOf(
                        "[x] Explore the editorial typography & paper tints",
                        "[ ] Try recording or dictating a voice thought",
                        "[ ] Set a 4-digit privacy passcode for locked notes"
                    ),
                    isPinned = true,
                    colorKey = NoteColorPalette.WARM_SAND.key
                ),
                Note(
                    id = "note_product_principles",
                    userId = uid,
                    title = "Notes on Tactile Mobile Interfaces",
                    content = "Great mobile tools respect thumb reach, visual rhythm, and quiet confidence. Instead of overwhelming dashboards, information hierarchy should guide the eye naturally from headline to body text to subtle metadata.\n\nSecondary actions stay tucked neatly inside contextual sheets until needed.",
                    folderId = "folder_projects",
                    tags = listOf("ux", "typography", "craft"),
                    checklistItems = listOf(
                        "[x] Establish serif display & clean sans body pairing",
                        "[x] Ensure 48dp minimum touch targets",
                        "[ ] Review dark mode surface contrast"
                    ),
                    isPinned = false,
                    colorKey = NoteColorPalette.SAGE.key,
                    voiceTranscript = "Remember to keep transitions spring-driven and responsive under three hundred milliseconds.",
                    voiceDurationSec = 14L
                ),
                Note(
                    id = "note_morning_ritual",
                    userId = uid,
                    title = "Morning Reading & Espresso Log",
                    content = "20g Ethiopian Yirgacheffe, 320g water at 93°C. Bright bergamot and jasmine notes.\n\nReading chapter four on spatial memory in physical notebooks versus digital archives.",
                    folderId = "folder_journal",
                    tags = listOf("morning", "reading"),
                    checklistItems = emptyList(),
                    isPinned = false,
                    colorKey = NoteColorPalette.DEFAULT.key
                )
            )
            for (note in starterNotes) {
                createNote(note)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, notesPath)
            Result.failure(e)
        }
    }
}
