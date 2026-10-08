package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MyNotesDatabase
import com.example.data.model.Note
import com.example.data.repository.NoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: MyNotesDatabase
    private lateinit var repository: NoteRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MyNotesDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = NoteRepository(database.noteDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("My Notes", appName)
    }

    @Test
    fun `offline room repository creates and observes notes`() = runTest {
        val createResult = repository.createNote(
            Note(
                title = "Keyboard Dictated Note",
                content = "This note was dictated using the keyboard microphone."
            )
        )
        assertTrue(createResult.isSuccess)

        val notes = repository.observeNotes().first()
        assertEquals(1, notes.size)
        assertEquals("Keyboard Dictated Note", notes.first().title)
        assertEquals(11L, notes.first().wordCount)
    }
}
