package com.mobin.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobin.app.data.model.ChatConversation
import com.mobin.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class MessagesUiState(
    val query: String = "",
    val selectedTab: String = "All", // "All", "Unread"
    val allConversations: List<ChatConversation> = emptyList(),
    val filteredConversations: List<ChatConversation> = emptyList(),
    val isLoading: Boolean = false,
)

class MessagesViewModel : ViewModel() {

    private val chatRepository = ChatRepository()

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState

    init {
        observeConversations()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            chatRepository.refreshRemoteMessages()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun observeConversations() {
        viewModelScope.launch {
            ChatRepository.conversationsFlow.collectLatest { list ->
                _uiState.value = _uiState.value.copy(allConversations = list)
                filterList(_uiState.value.query, _uiState.value.selectedTab)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        filterList(newQuery, _uiState.value.selectedTab)
    }

    fun selectTab(tab: String) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        filterList(_uiState.value.query, tab)
    }

    private fun filterList(query: String, tab: String) {
        val q = query.trim().lowercase()
        val list = _uiState.value.allConversations.filter { conv ->
            val matchesQuery = q.isBlank() ||
                    conv.contactName.lowercase().contains(q) ||
                    conv.propertyName.lowercase().contains(q) ||
                    conv.lastMessage.lowercase().contains(q)

            val matchesTab = when (tab) {
                "Unread" -> conv.unreadCount > 0
                else -> true
            }

            matchesQuery && matchesTab
        }

        _uiState.value = _uiState.value.copy(filteredConversations = list)
    }
}
