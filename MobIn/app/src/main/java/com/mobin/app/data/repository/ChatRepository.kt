package com.mobin.app.data.repository

import com.mobin.app.data.model.ChatConversation
import com.mobin.app.data.model.ChatMessage
import com.mobin.app.data.model.SupabaseMessageDto
import com.mobin.app.data.model.SupabaseMessageInsert
import com.mobin.app.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatRepository {

    private val supabase = SupabaseClient.client
    private val propertyRepository = PropertyRepository()

    companion object {
        private val _conversationsFlow = MutableStateFlow<List<ChatConversation>>(emptyList())
        val conversationsFlow: StateFlow<List<ChatConversation>> = _conversationsFlow

        private val _messagesFlow = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
        val messagesFlow: StateFlow<Map<String, List<ChatMessage>>> = _messagesFlow

        private var isPollerStarted = false
    }

    init {
        startLivePoller()
    }

    private fun getCurrentUserEmail(): String {
        return supabase.auth.currentUserOrNull()?.email ?: "renter@mobin.app"
    }

    private fun getCurrentUserName(): String {
        return supabase.auth.currentUserOrNull()?.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
            ?: getCurrentUserEmail().substringBefore("@")
    }

    private fun startLivePoller() {
        if (isPollerStarted) return
        isPollerStarted = true

        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    refreshRemoteMessages()
                } catch (e: Exception) {
                    android.util.Log.d("ChatRepository", "Poller note: ${e.message}")
                }
                delay(3000) // Poll every 3 seconds for live message updates
            }
        }
    }

    suspend fun refreshRemoteMessages() = withContext(Dispatchers.IO) {
        try {
            val userEmail = getCurrentUserEmail()
            val dtos = supabase.from("messages")
                .select()
                .decodeList<SupabaseMessageDto>()

            if (dtos.isEmpty()) return@withContext

            val conversationMap = mutableMapOf<String, MutableList<ChatMessage>>()
            val conversationHeaders = mutableMapOf<String, ChatConversation>()

            val timeFormatter = SimpleDateFormat("h:mm a", Locale.US)
            val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            for (dto in dtos) {
                val chatId = dto.propertyId?.ifBlank { null } ?: dto.id
                val isFromUser = dto.senderEmail.equals(userEmail, ignoreCase = true) ||
                        dto.senderName.equals(getCurrentUserName(), ignoreCase = true)

                val formattedTime = try {
                    val date = if (!dto.createdAt.isNullOrBlank()) {
                        val cleanDateStr = if (dto.createdAt.length >= 19) dto.createdAt.substring(0, 19) else dto.createdAt
                        isoParser.parse(cleanDateStr) ?: Date()
                    } else Date()
                    timeFormatter.format(date)
                } catch (e: Exception) {
                    "10:30 AM"
                }

                val msg = ChatMessage(
                    id = dto.id,
                    chatId = chatId,
                    senderId = if (isFromUser) "user" else (dto.landlordId ?: "landlord"),
                    text = dto.message,
                    timestamp = formattedTime,
                    isFromCurrentUser = isFromUser,
                )

                val list = conversationMap.getOrPut(chatId) { mutableListOf() }
                list.add(msg)

                conversationHeaders[chatId] = ChatConversation(
                    id = chatId,
                    propertyId = dto.propertyId ?: chatId,
                    contactName = if (dto.senderEmail != userEmail && !dto.senderName.isNullOrBlank()) dto.senderName else (dto.propertyName ?: "Mary Ann Dasalo"),
                    contactEmail = dto.landlordEmail ?: "",
                    propertyName = dto.propertyName ?: "Accommodation",
                    lastMessage = dto.message,
                    lastTimestamp = formattedTime,
                    isOnline = true,
                    unreadCount = if (!isFromUser) 1 else 0,
                )
            }

            _messagesFlow.value = conversationMap.mapValues { it.value.toList() }
            _conversationsFlow.value = conversationHeaders.values.toList()
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Failed to refresh remote messages: ${e.message}", e)
        }
    }

    suspend fun getConversations(): Result<List<ChatConversation>> = withContext(Dispatchers.IO) {
        runCatching {
            refreshRemoteMessages()
            val list = _conversationsFlow.value
            if (list.isNotEmpty()) {
                list
            } else {
                listOf(
                    ChatConversation(
                        id = "1",
                        propertyId = "1",
                        contactName = "Mary Ann Dasalo",
                        propertyName = "Casa Urgello",
                        lastMessage = "Hi! I'm interested in this property. Is it available?",
                        lastTimestamp = "10:33 AM",
                        isOnline = true,
                        unreadCount = 0,
                    )
                )
            }
        }
    }

    suspend fun getConversation(chatId: String): ChatConversation? = withContext(Dispatchers.IO) {
        val found = _conversationsFlow.value.find { it.id == chatId }
        if (found != null) return@withContext found

        val property = propertyRepository.getPropertyById(chatId)
        ChatConversation(
            id = chatId,
            propertyId = chatId,
            contactName = property?.ownerName ?: "Mary Ann Dasalo",
            contactEmail = "landlord@mobin.app",
            propertyName = property?.title ?: "Casa Urgello",
            lastMessage = "Start a conversation",
            lastTimestamp = "Just now",
            isOnline = true,
            unreadCount = 0,
        )
    }

    suspend fun getMessages(chatId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        _messagesFlow.value[chatId] ?: emptyList()
    }

    suspend fun sendMessage(chatId: String, text: String): ChatMessage = withContext(Dispatchers.IO) {
        val userEmail = getCurrentUserEmail()
        val userName = getCurrentUserName()
        val property = propertyRepository.getPropertyById(chatId)
        val time = SimpleDateFormat("h:mm a", Locale.US).format(Date())

        val newMessage = ChatMessage(
            id = "m_${System.currentTimeMillis()}",
            chatId = chatId,
            senderId = "user",
            text = text.trim(),
            timestamp = time,
            isFromCurrentUser = true,
        )

        // Local instant update
        val currentList = (_messagesFlow.value[chatId] ?: emptyList()).toMutableList()
        currentList.add(newMessage)
        val updatedMap = _messagesFlow.value.toMutableMap()
        updatedMap[chatId] = currentList
        _messagesFlow.value = updatedMap

        // Remote Supabase insert
        try {
            val insert = SupabaseMessageInsert(
                landlordId = null,
                landlordEmail = "landlord@mobin.ph",
                senderName = userName,
                senderEmail = userEmail,
                propertyId = chatId,
                propertyName = property?.title ?: "Casa Urgello",
                message = text.trim(),
            )
            supabase.from("messages").insert(insert)
            android.util.Log.d("ChatRepository", "Inserted live message to Supabase for property $chatId")
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Remote message insert note: ${e.message}", e)
        }

        newMessage
    }
}
