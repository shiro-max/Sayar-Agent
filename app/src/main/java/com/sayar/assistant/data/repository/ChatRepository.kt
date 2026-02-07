package com.sayar.assistant.data.repository

import com.sayar.assistant.data.remote.GeminiContent
import com.sayar.assistant.data.remote.GeminiGenerationConfig
import com.sayar.assistant.data.remote.GeminiPart
import com.sayar.assistant.data.remote.GeminiRequest
import com.sayar.assistant.data.remote.GeminiService
import com.sayar.assistant.domain.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed class ChatResult {
    data class Success(val message: ChatMessage) : ChatResult()
    data class Error(val message: String) : ChatResult()
}

@Singleton
class ChatRepository @Inject constructor(
    private val geminiService: GeminiService,
    private val settingsRepository: SettingsRepository
) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val maxHistorySize = 10

    fun getRecentContext(): List<ChatMessage> {
        return _messages.value.takeLast(maxHistorySize)
    }

    suspend fun sendMessage(userMessage: String): ChatResult {
        val settings = settingsRepository.settings.first()

        if (settings.geminiApiKey.isBlank()) {
            return ChatResult.Error("Please configure your Gemini API key in Settings")
        }

        // Add user message
        val userChatMessage = ChatMessage(
            content = userMessage,
            isFromUser = true
        )
        _messages.value = _messages.value + userChatMessage
        _isLoading.value = true

        return try {
            // Build system instruction with teacher context
            val systemInstruction = buildSystemInstruction(settings)

            // Build conversation history for context
            val contents = buildConversationHistory()

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = if (systemInstruction.isNotBlank()) {
                    GeminiContent(parts = listOf(GeminiPart(systemInstruction)))
                } else null,
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 8192
                )
            )

            val response = geminiService.generateContent(
                apiKey = settings.geminiApiKey,
                request = request
            )

            val aiResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: response.error?.message
                ?: "Sorry, I couldn't generate a response. Please try again."

            val aiMessage = ChatMessage(
                content = aiResponse,
                isFromUser = false
            )
            _messages.value = _messages.value + aiMessage
            _isLoading.value = false

            ChatResult.Success(aiMessage)
        } catch (e: Exception) {
            _isLoading.value = false
            val errorMessage = when {
                e.message?.contains("401") == true -> "Invalid API key. Please check your Gemini API key in Settings."
                e.message?.contains("403") == true -> "API key doesn't have access. Enable Generative Language API in Google Cloud Console."
                e.message?.contains("404") == true -> "API endpoint not found. Please update the app or check API configuration."
                e.message?.contains("429") == true -> "Rate limit exceeded. Please wait and try again."
                e.message?.contains("network") == true || e.message?.contains("connect") == true ->
                    "Network error. Please check your internet connection."
                else -> "Error: ${e.message ?: "Unknown error occurred"}"
            }
            ChatResult.Error(errorMessage)
        }
    }

    private fun buildSystemInstruction(settings: AppSettings): String {
        val parts = mutableListOf<String>()

        parts.add("""
            You are a helpful AI assistant for Myanmar teachers. You help with:
            - Creating lesson plans and teaching materials
            - Designing exams and assessments
            - Analyzing student performance and grades
            - Creating report cards and progress reports
            - Managing classroom activities

            You can respond in both English and Myanmar (Burmese) language based on the user's preference.
            When creating printable content, format it clearly with proper structure.
            For charts and analysis, provide text-based representations that can be understood easily.
        """.trimIndent())

        if (settings.teacherGrade.isNotBlank()) {
            parts.add("The teacher currently teaches: ${settings.teacherGrade}")
        }

        if (settings.teacherSubject.isNotBlank()) {
            parts.add("Subject specialty: ${settings.teacherSubject}")
        }

        return parts.joinToString("\n\n")
    }

    private fun buildConversationHistory(): List<GeminiContent> {
        return getRecentContext().map { message ->
            GeminiContent(
                role = if (message.isFromUser) "user" else "model",
                parts = listOf(GeminiPart(message.content))
            )
        }
    }

    fun clearHistory() {
        _messages.value = emptyList()
    }
}
