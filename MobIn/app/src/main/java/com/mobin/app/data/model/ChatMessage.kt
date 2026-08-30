package com.mobin.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: String = "10:30 AM",
    val isFromCurrentUser: Boolean = false,
)

@Serializable
data class ChatConversation(
    val id: String = "",
    val propertyId: String = "",
    val contactName: String = "Mary Ann Dasalo",
    val propertyName: String = "Casa Urgello",
    val lastMessage: String = "Hi! I'm interested in the 3 bed room. Is this still available?",
    val lastTimestamp: String = "10:33 AM",
    val avatarUrl: String? = null,
    val isOnline: Boolean = true,
    val unreadCount: Int = 0,
) {
    val displayHeader: String
        get() = "$contactName | $propertyName"
}
