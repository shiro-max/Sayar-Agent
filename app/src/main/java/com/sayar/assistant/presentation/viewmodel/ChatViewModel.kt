package com.sayar.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayar.assistant.data.repository.ChatRepository
import com.sayar.assistant.data.repository.ChatResult
import com.sayar.assistant.domain.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> = chatRepository.messages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isLoading: StateFlow<Boolean> = chatRepository.isLoading
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _errorMessage.value = null
            when (val result = chatRepository.sendMessage(content)) {
                is ChatResult.Success -> {
                    // Message added automatically by repository
                }
                is ChatResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearHistory() {
        chatRepository.clearHistory()
    }
}
