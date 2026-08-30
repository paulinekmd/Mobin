package com.mobin.app.data.repository

import com.mobin.app.data.model.ChatConversation
import com.mobin.app.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatRepository {

    companion object {
        private val initialConversations = listOf(
            ChatConversation(
                id = "1",
                propertyId = "1",
                contactName = "Mary Ann Dasalo",
                propertyName = "Casa Urgello",
                lastMessage = "Hi! I'm interested in the 3 bed room. Is this still available?",
                lastTimestamp = "10:33 AM",
                isOnline = true,
                unreadCount = 0,
            ),
            ChatConversation(
                id = "2",
                propertyId = "2",
                contactName = "Maria Santos",
                propertyName = "Maria's Boarding",
                lastMessage = "Hi! I'm interested in the 3 bed room. Is this still available?",
                lastTimestamp = "10:33 AM",
                isOnline = true,
                unreadCount = 1,
            ),
            ChatConversation(
                id = "3",
                propertyId = "3",
                contactName = "John Green",
                propertyName = "Greenview Apartment",
                lastMessage = "Hi! I'm interested in the 3 bed room. Is this still available?",
                lastTimestamp = "10:33 AM",
                isOnline = false,
                unreadCount = 0,
            ),
            ChatConversation(
                id = "4",
                propertyId = "4",
                contactName = "Student Hub",
                propertyName = "Student Hub Dorm",
                lastMessage = "Hi! I'm interested in the 3 bed room. Is this still available?",
                lastTimestamp = "10:33 AM",
                isOnline = true,
                unreadCount = 0,
            ),
        )

        private val _conversationsFlow = MutableStateFlow(initialConversations)
        val conversationsFlow: StateFlow<List<ChatConversation>> = _conversationsFlow

        private val initialMessages = mutableMapOf<String, MutableList<ChatMessage>>(
            "1" to mutableListOf(
                ChatMessage(
                    id = "m1",
                    chatId = "1",
                    senderId = "user",
                    text = "Hi! I'm interested in the 3 bed room. Is this still available?",
                    timestamp = "10:30 AM",
                    isFromCurrentUser = true,
                ),
                ChatMessage(
                    id = "m2",
                    chatId = "1",
                    senderId = "owner",
                    text = "Hi! Thanks for reaching out about Casa Urgello.",
                    timestamp = "10:31 AM",
                    isFromCurrentUser = false,
                ),
                ChatMessage(
                    id = "m3",
                    chatId = "1",
                    senderId = "owner",
                    text = "Yes, it is available!",
                    timestamp = "10:32 AM",
                    isFromCurrentUser = false,
                ),
            )
        )

        private val _messagesFlow = MutableStateFlow<Map<String, List<ChatMessage>>>(initialMessages.mapValues { it.value.toList() })
        val messagesFlow: StateFlow<Map<String, List<ChatMessage>>> = _messagesFlow
    }

    suspend fun getConversations(): Result<List<ChatConversation>> = withContext(Dispatchers.IO) {
        runCatching { _conversationsFlow.value }
    }

    suspend fun getConversation(chatId: String): ChatConversation? = withContext(Dispatchers.IO) {
        _conversationsFlow.value.find { it.id == chatId } ?: _conversationsFlow.value.firstOrNull()
    }

    suspend fun getMessages(chatId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        _messagesFlow.value[chatId] ?: initialMessages["1"] ?: emptyList()
    }

    suspend fun sendMessage(chatId: String, text: String): ChatMessage = withContext(Dispatchers.IO) {
        val time = SimpleDateFormat("h:mm a", Locale.US).format(Date())
        val newMessage = ChatMessage(
            id = "m_${System.currentTimeMillis()}",
            chatId = chatId,
            senderId = "user",
            text = text.trim(),
            timestamp = time,
            isFromCurrentUser = true,
        )

        val currentList = (_messagesFlow.value[chatId] ?: emptyList()).toMutableList()
        currentList.add(newMessage)

        val updatedMap = _messagesFlow.value.toMutableMap()
        updatedMap[chatId] = currentList
        _messagesFlow.value = updatedMap

        // Update last message in conversation
        val updatedConversations = _conversationsFlow.value.map {
            if (it.id == chatId) {
                it.copy(lastMessage = text.trim(), lastTimestamp = time)
            } else it
        }
        _conversationsFlow.value = updatedConversations

        newMessage
    }
}
