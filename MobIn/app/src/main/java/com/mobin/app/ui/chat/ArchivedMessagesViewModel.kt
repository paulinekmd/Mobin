package com.mobin.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobin.app.data.model.ChatConversation
import com.mobin.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ArchivedMessagesUiState(
    val query: String = "",
    val allConversations: List<ChatConversation> = emptyList(),
    val filteredConversations: List<ChatConversation> = emptyList(),
    val isLoading: Boolean = false,
)

class ArchivedMessagesViewModel : ViewModel() {

    private val chatRepository = ChatRepository()

    private val _uiState = MutableStateFlow(
        ArchivedMessagesUiState(
            allConversations = ChatRepository.archivedConversationsFlow.value,
            filteredConversations = ChatRepository.archivedConversationsFlow.value,
        )
    )
    val uiState: StateFlow<ArchivedMessagesUiState> = _uiState

    init {
        observeArchivedConversations()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (_uiState.value.allConversations.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }
            chatRepository.refreshRemoteMessages()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun observeArchivedConversations() {
        viewModelScope.launch {
            ChatRepository.archivedConversationsFlow.collectLatest { list ->
                _uiState.value = _uiState.value.copy(allConversations = list)
                filterList(_uiState.value.query)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        filterList(newQuery)
    }

    private fun filterList(query: String) {
        val q = query.trim().lowercase()
        val list = _uiState.value.allConversations.filter { conv ->
            q.isBlank() ||
                conv.contactName.lowercase().contains(q) ||
                conv.propertyName.lowercase().contains(q) ||
                conv.lastMessage.lowercase().contains(q)
        }
        _uiState.value = _uiState.value.copy(filteredConversations = list)
    }

    fun unarchiveConversation(chatId: String) {
        viewModelScope.launch {
            chatRepository.unarchiveChat(chatId)
        }
    }

    fun deleteConversation(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteChatLocally(chatId)
        }
    }
}
