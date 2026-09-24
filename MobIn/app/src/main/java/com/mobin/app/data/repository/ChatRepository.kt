package com.mobin.app.data.repository

import com.mobin.app.data.model.ChatConversation
import com.mobin.app.data.model.ChatMessage
import com.mobin.app.data.model.SupabaseMessageDto
import com.mobin.app.data.model.SupabaseMessageInsert
import com.mobin.app.data.remote.SupabaseClient
import com.mobin.app.util.DataStoreManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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

        private val _readMessageIds = mutableMapOf<String, String>()
        private var isPollerStarted = false
        private var isObserverStarted = false
    }

    init {
        startReadStatusObserver()
        startLivePoller()
    }

    private fun startReadStatusObserver() {
        if (isObserverStarted) return
        isObserverStarted = true

        CoroutineScope(Dispatchers.IO).launch {
            DataStoreManager.getReadMessageMap().collectLatest { map ->
                synchronized(_readMessageIds) {
                    _readMessageIds.putAll(map)
                }
            }
        }
    }

    fun markAsRead(chatId: String) {
        val lastMsg = _messagesFlow.value[chatId]?.lastOrNull()
        if (lastMsg != null) {
            val mapToSave = synchronized(_readMessageIds) {
                _readMessageIds[chatId] = lastMsg.id
                _readMessageIds.toMap()
            }
            CoroutineScope(Dispatchers.IO).launch {
                DataStoreManager.saveReadMessageMap(mapToSave)
            }
        }
        val currentConversations = _conversationsFlow.value
        val index = currentConversations.indexOfFirst { it.id == chatId }
        if (index >= 0 && currentConversations[index].unreadCount > 0) {
            val updated = currentConversations.toMutableList()
            updated[index] = updated[index].copy(unreadCount = 0)
            _conversationsFlow.value = updated
        }
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
                delay(1500) // Fast 1.5s polling for true real-time chat updates
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

            val sortedDtos = dtos.sortedBy { it.createdAt ?: "" }
            val propertiesMap = propertyRepository.getCachedProperties().associateBy { it.id }
            val conversationMap = mutableMapOf<String, MutableList<ChatMessage>>()
            val latestDtoMap = mutableMapOf<String, SupabaseMessageDto>()

            val timeFormatter = SimpleDateFormat("h:mm a", Locale.US)
            val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            for (dto in sortedDtos) {
                val msgText = dto.message?.trim() ?: continue
                if (msgText.isBlank()) continue

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
                    text = msgText,
                    timestamp = formattedTime,
                    isFromCurrentUser = isFromUser,
                )

                val list = conversationMap.getOrPut(chatId) { mutableListOf() }
                list.add(msg)
                latestDtoMap[chatId] = dto
            }

            val currentReadMap = synchronized(_readMessageIds) {
                if (_readMessageIds.isEmpty()) {
                    null
                } else {
                    _readMessageIds.toMap()
                }
            } ?: run {
                val snapshot = DataStoreManager.getReadMessageMapSnapshot()
                synchronized(_readMessageIds) {
                    _readMessageIds.putAll(snapshot)
                    _readMessageIds.toMap()
                }
            }

            val conversationHeaders = mutableListOf<ChatConversation>()
            for ((chatId, msgs) in conversationMap) {
                val latestMsg = msgs.lastOrNull() ?: continue
                val latestDto = latestDtoMap[chatId]
                val prop = propertiesMap[chatId]
                val propTitle = latestDto?.propertyName?.ifBlank { null } ?: prop?.title ?: "Accommodation"
                val contactName = if (!latestMsg.isFromCurrentUser && !latestDto?.senderName.isNullOrBlank()) {
                    latestDto!!.senderName!!
                } else {
                    prop?.ownerName ?: "Landlord"
                }

                val lastReadId = currentReadMap[chatId]
                val isUnread = !latestMsg.isFromCurrentUser && (lastReadId == null || lastReadId != latestMsg.id)

                val displayLastMessage = if (latestMsg.text.trim().let {
                    it.contains("res.cloudinary.com") ||
                    it.lowercase().let { l -> l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".png") || l.endsWith(".gif") || l.endsWith(".webp") }
                }) "📷 Image" else latestMsg.text

                conversationHeaders.add(
                    ChatConversation(
                        id = chatId,
                        propertyId = latestDto?.propertyId ?: chatId,
                        contactName = contactName,
                        contactEmail = latestDto?.landlordEmail ?: "",
                        propertyName = propTitle,
                        lastMessage = displayLastMessage,
                        lastTimestamp = latestMsg.timestamp,
                        avatarUrl = prop?.imageUrl,
                        isOnline = true,
                        unreadCount = if (isUnread) 1 else 0,
                    )
                )
            }

            _messagesFlow.value = conversationMap.mapValues { it.value.toList() }
            _conversationsFlow.value = conversationHeaders.reversed()
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Failed to refresh remote messages: ${e.message}", e)
        }
    }

    suspend fun getConversations(): Result<List<ChatConversation>> = withContext(Dispatchers.IO) {
        runCatching {
            if (_conversationsFlow.value.isEmpty()) {
                refreshRemoteMessages()
            }
            _conversationsFlow.value
        }
    }

    suspend fun getConversation(chatId: String): ChatConversation? = withContext(Dispatchers.IO) {
        val found = _conversationsFlow.value.find { it.id == chatId }
        if (found != null) return@withContext found

        val property = propertyRepository.getPropertyById(chatId)
        ChatConversation(
            id = chatId,
            propertyId = chatId,
            contactName = property?.ownerName ?: "Landlord",
            contactEmail = "landlord@mobin.ph",
            propertyName = property?.title ?: "Accommodation",
            lastMessage = "Start a conversation",
            lastTimestamp = "Just now",
            avatarUrl = property?.imageUrl,
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

        // Local instant update for message history
        val currentList = (_messagesFlow.value[chatId] ?: emptyList()).toMutableList()
        currentList.add(newMessage)
        val updatedMap = _messagesFlow.value.toMutableMap()
        updatedMap[chatId] = currentList
        _messagesFlow.value = updatedMap

        // Local instant update for conversations tab
        val landlordName = property?.ownerName ?: "Landlord"
        val propTitle = property?.title ?: "Accommodation"
        val existingConversations = _conversationsFlow.value.toMutableList()
        val existingIndex = existingConversations.indexOfFirst { it.id == chatId }
        val updatedHeader = ChatConversation(
            id = chatId,
            propertyId = chatId,
            contactName = landlordName,
            contactEmail = "landlord@mobin.ph",
            propertyName = propTitle,
            lastMessage = text.trim(),
            lastTimestamp = time,
            avatarUrl = property?.imageUrl,
            isOnline = true,
            unreadCount = 0,
        )
        if (existingIndex >= 0) {
            existingConversations[existingIndex] = updatedHeader
        } else {
            existingConversations.add(0, updatedHeader)
        }
        _conversationsFlow.value = existingConversations

        // Remote Supabase insert
        try {
            val insert = SupabaseMessageInsert(
                landlordId = null,
                landlordEmail = "landlord@mobin.ph",
                senderName = userName,
                senderEmail = userEmail,
                propertyId = chatId,
                propertyName = property?.title ?: "Accommodation",
                message = text.trim(),
            )
            supabase.from("messages").insert(insert)
            android.util.Log.d("ChatRepository", "Inserted live message to Supabase for property $chatId")
            refreshRemoteMessages()
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Remote message insert note: ${e.message}", e)
        }

        newMessage
    }
}
