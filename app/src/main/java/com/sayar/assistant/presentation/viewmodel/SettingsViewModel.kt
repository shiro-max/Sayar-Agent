package com.sayar.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayar.assistant.data.repository.AiProvider
import com.sayar.assistant.data.repository.AppLanguage
import com.sayar.assistant.data.repository.AppSettings
import com.sayar.assistant.data.repository.SettingsRepository
import com.sayar.assistant.data.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun updateAiProvider(provider: AiProvider) {
        viewModelScope.launch {
            settingsRepository.updateAiProvider(provider)
        }
    }

    fun updateGeminiApiKey(apiKey: String) {
        viewModelScope.launch {
            settingsRepository.updateGeminiApiKey(apiKey)
        }
    }

    fun updateOpenAiApiKey(apiKey: String) {
        viewModelScope.launch {
            settingsRepository.updateOpenAiApiKey(apiKey)
        }
    }

    fun updateOllamaUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.updateOllamaUrl(url)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
        }
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.updateLanguage(language)
        }
    }

    fun updateTeacherGrade(grade: String) {
        viewModelScope.launch {
            settingsRepository.updateTeacherGrade(grade)
        }
    }

    fun updateTeacherSubject(subject: String) {
        viewModelScope.launch {
            settingsRepository.updateTeacherSubject(subject)
        }
    }
}
