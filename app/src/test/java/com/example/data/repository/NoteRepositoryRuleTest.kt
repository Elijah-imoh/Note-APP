package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Folder
import com.example.data.model.Note
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class NoteRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveNote_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = NoteRepository(firestore, auth)
        val dynamicId = "note_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createNote(
                Note(
                    id = dynamicId,
                    userId = aliceUid,
                    title = "Editorial Note",
                    content = "Testing Firestore rules alignment from Kotlin repository.",
                    tags = listOf("design", "test"),
                    checklistItems = listOf("[x] First item", "[ ] Second item")
                )
            )
        }
        assertTrue("Expected createNote to succeed: ${createResult.exceptionOrNull()}", createResult.isSuccess)
        val createdId = createResult.getOrThrow()
        assertEquals(dynamicId, createdId)

        val observedNotes = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeNotes(aliceUid).first { notes -> notes.any { it.id == createdId } }
        }
        assertTrue(observedNotes.any { it.id == createdId && it.title == "Editorial Note" })
    }

    @Test
    fun updateAndDeleteNote_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = NoteRepository(firestore, auth)
        val dynamicId = "note_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val createdId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createNote(
                Note(
                    id = dynamicId,
                    userId = aliceUid,
                    title = "Initial Title",
                    content = "Initial Content"
                )
            ).getOrThrow()
        }

        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateNote(
                Note(
                    id = createdId,
                    userId = aliceUid,
                    title = "Updated Title",
                    content = "Updated Content",
                    isPinned = true
                )
            )
        }
        assertTrue("Expected updateNote to succeed: ${updateResult.exceptionOrNull()}", updateResult.isSuccess)

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getNoteById(createdId, aliceUid).getOrThrow()
        }
        assertEquals("Updated Title", fetched.title)
        assertTrue(fetched.isPinned)

        val deleteResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteNotePermanently(createdId)
        }
        assertTrue(deleteResult.isSuccess)
    }

    @Test
    fun getNoteById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = NoteRepository(firestore, auth)
        val dynamicId = "note_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val noteId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createNote(
                Note(id = dynamicId, userId = aliceUid, title = "Alice Private Note")
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = NoteRepository(firestore, auth)
        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getNoteById(noteId, ownerUserId = aliceUid)
        }
        assertTrue("Expected cross-user read to fail", crossReadResult.isFailure)
        val ex = crossReadResult.exceptionOrNull() as? FirebaseFirestoreException
        assertNotNull("Expected FirebaseFirestoreException", ex)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun observeNotes_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = NoteRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeNotes("some_user_123").first()
            }
            fail("Expected FirebaseFirestoreException.PERMISSION_DENIED for unauthenticated user")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun createAndObserveFolder_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = NoteRepository(firestore, auth)
        val folderId = "folder_${UUID.randomUUID().toString().replace("-", "").take(10)}"

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createFolder(
                Folder(
                    id = folderId,
                    userId = aliceUid,
                    name = "Design Research",
                    iconKey = "book",
                    accentKey = "terracotta",
                    sortOrder = 1L
                )
            )
        }
        assertTrue(createResult.isSuccess)

        val folders = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeFolders(aliceUid).first { list -> list.any { it.id == folderId } }
        }
        assertTrue(folders.any { it.id == folderId && it.name == "Design Research" })
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
