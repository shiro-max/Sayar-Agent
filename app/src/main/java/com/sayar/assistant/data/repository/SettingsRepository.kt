package com.sayar.assistant.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class AiProvider {
    GEMINI,
    OPENAI,
    OLLAMA
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage {
    ENGLISH,
    MYANMAR
}

data class AppSettings(
    val aiProvider: AiProvider = AiProvider.GEMINI,
    val geminiApiKey: String = "",
    val openAiApiKey: String = "",
    val ollamaUrl: String = "http://localhost:11434",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val teacherGrade: String = "",
    val teacherSubject: String = ""
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val OLLAMA_URL = stringPreferencesKey("ollama_url")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val TEACHER_GRADE = stringPreferencesKey("teacher_grade")
        val TEACHER_SUBJECT = stringPreferencesKey("teacher_subject")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            aiProvider = preferences[PreferencesKeys.AI_PROVIDER]?.let {
                AiProvider.valueOf(it)
            } ?: AiProvider.GEMINI,
            geminiApiKey = preferences[PreferencesKeys.GEMINI_API_KEY] ?: "",
            openAiApiKey = preferences[PreferencesKeys.OPENAI_API_KEY] ?: "",
            ollamaUrl = preferences[PreferencesKeys.OLLAMA_URL] ?: "http://localhost:11434",
            themeMode = preferences[PreferencesKeys.THEME_MODE]?.let {
                ThemeMode.valueOf(it)
            } ?: ThemeMode.SYSTEM,
            language = preferences[PreferencesKeys.LANGUAGE]?.let {
                AppLanguage.valueOf(it)
            } ?: AppLanguage.ENGLISH,
            teacherGrade = preferences[PreferencesKeys.TEACHER_GRADE] ?: "",
            teacherSubject = preferences[PreferencesKeys.TEACHER_SUBJECT] ?: ""
        )
    }

    suspend fun updateAiProvider(provider: AiProvider) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_PROVIDER] = provider.name
        }
    }

    suspend fun updateGeminiApiKey(apiKey: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.GEMINI_API_KEY] = apiKey
        }
    }

    suspend fun updateOpenAiApiKey(apiKey: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.OPENAI_API_KEY] = apiKey
        }
    }

    suspend fun updateOllamaUrl(url: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.OLLAMA_URL] = url
        }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language.name
        }
    }

    suspend fun updateTeacherGrade(grade: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.TEACHER_GRADE] = grade
        }
    }

    suspend fun updateTeacherSubject(subject: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.TEACHER_SUBJECT] = subject
        }
    }
}
