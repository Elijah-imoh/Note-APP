package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.NoteLayoutStyle
import com.example.data.model.NoteSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_notes_preferences")

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Pure Paper (White)"),
    DARK("Pure Obsidian (Black)")
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val layoutStyle: NoteLayoutStyle = NoteLayoutStyle.COMFORTABLE_LIST,
    val sortOrder: NoteSortOrder = NoteSortOrder.UPDATED_DESC,
    val fontScaleMultiplier: Float = 1.0f,
    val lockPinCode: String = "",
    val hasCompletedOnboarding: Boolean = false,
    val ttsSpeechRate: Float = 1.0f
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAYOUT_STYLE = stringPreferencesKey("layout_style")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val FONT_SCALE = floatPreferencesKey("font_scale_multiplier")
        val LOCK_PIN_CODE = stringPreferencesKey("lock_pin_code")
        val COMPLETED_ONBOARDING = booleanPreferencesKey("completed_onboarding")
        val TTS_SPEECH_RATE = floatPreferencesKey("tts_speech_rate")
    }

    val preferencesFlow: Flow<UserPreferences> = context.applicationContext.dataStore.data.map { prefs ->
        val theme = prefs[Keys.THEME_MODE]?.let {
            runCatching { ThemeMode.valueOf(it) }.getOrNull()
        } ?: ThemeMode.SYSTEM

        val layout = prefs[Keys.LAYOUT_STYLE]?.let {
            runCatching { NoteLayoutStyle.valueOf(it) }.getOrNull()
        } ?: NoteLayoutStyle.COMFORTABLE_LIST

        val sort = prefs[Keys.SORT_ORDER]?.let {
            runCatching { NoteSortOrder.valueOf(it) }.getOrNull()
        } ?: NoteSortOrder.UPDATED_DESC

        UserPreferences(
            themeMode = theme,
            layoutStyle = layout,
            sortOrder = sort,
            fontScaleMultiplier = (prefs[Keys.FONT_SCALE] ?: 1.0f).coerceIn(0.85f, 1.35f),
            lockPinCode = prefs[Keys.LOCK_PIN_CODE] ?: "",
            hasCompletedOnboarding = prefs[Keys.COMPLETED_ONBOARDING] ?: false,
            ttsSpeechRate = (prefs[Keys.TTS_SPEECH_RATE] ?: 1.0f).coerceIn(0.75f, 1.5f)
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.applicationContext.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setLayoutStyle(style: NoteLayoutStyle) {
        context.applicationContext.dataStore.edit { it[Keys.LAYOUT_STYLE] = style.name }
    }

    suspend fun setSortOrder(order: NoteSortOrder) {
        context.applicationContext.dataStore.edit { it[Keys.SORT_ORDER] = order.name }
    }

    suspend fun setFontScale(scale: Float) {
        context.applicationContext.dataStore.edit {
            it[Keys.FONT_SCALE] = scale.coerceIn(0.85f, 1.35f)
        }
    }

    suspend fun setLockPinCode(pin: String) {
        context.applicationContext.dataStore.edit {
            it[Keys.LOCK_PIN_CODE] = pin.filter { ch -> ch.isDigit() }.take(4)
        }
    }

    suspend fun setCompletedOnboarding(completed: Boolean) {
        context.applicationContext.dataStore.edit {
            it[Keys.COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun setTtsSpeechRate(rate: Float) {
        context.applicationContext.dataStore.edit {
            it[Keys.TTS_SPEECH_RATE] = rate.coerceIn(0.75f, 1.5f)
        }
    }
}
