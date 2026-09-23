package com.mobin.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobin.app.data.model.ChatConversation
import com.mobin.app.data.model.ChatMessage
import com.mobin.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversation: ChatConversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
)

class ChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()
    private var currentChatId: String = "1"

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    fun loadChat(chatId: String, initialMessage: String = "") {
        currentChatId = chatId
        if (initialMessage.isNotBlank() && _uiState.value.inputText.isBlank()) {
            _uiState.value = _uiState.value.copy(inputText = initialMessage)
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val conv = chatRepository.getConversation(chatId)
            _uiState.value = _uiState.value.copy(conversation = conv)
            chatRepository.refreshRemoteMessages()

            ChatRepository.messagesFlow.collectLatest { map ->
                val messages = map[chatId] ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messages = messages,
                )
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(inputText = "")
            chatRepository.sendMessage(currentChatId, text)
        }
    }
}
