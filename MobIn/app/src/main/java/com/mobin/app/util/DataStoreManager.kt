package com.mobin.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mobin_prefs")

object DataStoreManager {

    private lateinit var dataStore: DataStore<Preferences>

    private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    private val SAVED_PROPERTY_IDS = stringSetPreferencesKey("saved_property_ids")
    private val RECENT_SEARCHES = stringPreferencesKey("recent_searches_json")
    private val READ_MESSAGE_IDS = stringPreferencesKey("read_message_ids_json")
    private val DELETED_CHAT_IDS = stringSetPreferencesKey("deleted_chat_ids_set")
    private val ARCHIVED_CHAT_IDS = stringSetPreferencesKey("archived_chat_ids_set")
    private val DELETED_CHAT_LAST_MSG_MAP = stringPreferencesKey("deleted_chat_last_msg_map")
    private val ARCHIVED_CHAT_LAST_MSG_MAP = stringPreferencesKey("archived_chat_last_msg_map")

    fun init(context: Context) {
        dataStore = context.dataStore
    }

    suspend fun setOnboardingComplete() {
        dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETE] = true }
    }

    fun isOnboardingComplete(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[ONBOARDING_COMPLETE] ?: false }

    suspend fun setSavedPropertyIds(ids: Set<String>) {
        if (::dataStore.isInitialized) {
            dataStore.edit { prefs -> prefs[SAVED_PROPERTY_IDS] = ids }
        }
    }

    fun getSavedPropertyIds(): Flow<Set<String>> =
        if (::dataStore.isInitialized) {
            dataStore.data.map { prefs -> prefs[SAVED_PROPERTY_IDS] ?: emptySet() }
        } else {
            flowOf(emptySet())
        }

    suspend fun saveRecentSearches(searches: List<String>) {
        if (::dataStore.isInitialized) {
            dataStore.edit { prefs ->
                prefs[RECENT_SEARCHES] = Json.encodeToString(searches)
            }
        }
    }

    fun getRecentSearches(): Flow<List<String>> =
        if (::dataStore.isInitialized) {
            dataStore.data.map { prefs ->
                val jsonStr = prefs[RECENT_SEARCHES]
                if (jsonStr.isNullOrBlank()) {
                    emptyList()
                } else {
                    try {
                        Json.decodeFromString<List<String>>(jsonStr)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }
        } else {
            flowOf(emptyList())
        }

    suspend fun getReadMessageMapSnapshot(): Map<String, String> {
        return if (::dataStore.isInitialized) {
            try {
                val prefs = dataStore.data.first()
                val jsonStr = prefs[READ_MESSAGE_IDS]
                if (jsonStr.isNullOrBlank()) emptyMap() else Json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                emptyMap()
            }
        } else emptyMap()
    }

    fun getReadMessageMap(): Flow<Map<String, String>> =
        if (::dataStore.isInitialized) {
            dataStore.data.map { prefs ->
                val jsonStr = prefs[READ_MESSAGE_IDS]
                if (jsonStr.isNullOrBlank()) {
                    emptyMap()
                } else {
                    try {
                        Json.decodeFromString<Map<String, String>>(jsonStr)
                    } catch (e: Exception) {
                        emptyMap()
                    }
                }
            }
        } else {
            flowOf(emptyMap())
        }

    suspend fun saveReadMessageMap(map: Map<String, String>) {
        if (::dataStore.isInitialized) {
            dataStore.edit { prefs ->
                prefs[READ_MESSAGE_IDS] = Json.encodeToString(map)
            }
        }
    }

    suspend fun getArchivedChatLastMsgMapSnapshot(): Map<String, String> {
        return if (::dataStore.isInitialized) {
            try {
                val prefs = dataStore.data.first()
                val jsonStr = prefs[ARCHIVED_CHAT_LAST_MSG_MAP]
                if (jsonStr.isNullOrBlank()) emptyMap() else Json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                emptyMap()
            }
        } else emptyMap()
    }

    suspend fun archiveChatForUser(chatId: String, lastMsgId: String? = null) {
        if (::dataStore.isInitialized) {
            dataStore.edit { prefs ->
                val current = prefs[ARCHIVED_CHAT_IDS] ?: emptySet()
                prefs[ARCHIVED_CHAT_IDS] = current + chatId
                if (lastMsgId != null) {
                    val map = try {
                        val str = prefs[ARCHIVED_CHAT_LAST_MSG_MAP]
                        if (str.isNullOrBlank()) mutableMapOf() else Json.decodeFromString<MutableMap<String, String>>(str)
                    } catch (e: Exception) {
                        mutableMapOf()
                    }
                    map[chatId] = lastMsgId
                    prefs[ARCHIVED_CHAT_LAST_MSG_MAP] = Json.encodeToString(map)
                }
            }
        }
    }

    suspend fun unarchiveChatForUser(chatId: String) {
        if (::dataStore.isInitialized) {
            dataStore.edit { prefs ->
                val current = prefs[ARCHIVED_CHAT_IDS] ?: emptySet()
                prefs[ARCHIVED_CHAT_IDS] = current - chatId
                try {
                    val str = prefs[ARCHIVED_CHAT_LAST_MSG_MAP]
                    if (!str.isNullOrBlank()) {
                        val map = Json.decodeFromString<MutableMap<String, String>>(str)
                        if (map.remove(chatId) != null) {
                            prefs[ARCHIVED_CHAT_LAST_MSG_MAP] = Json.encodeToString(map)
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    fun getArchivedChatIds(): Flow<Set<String>> =
        if (::dataStore.isInitialized) {
            dataStore.data.map { prefs -> prefs[ARCHIVED_CHAT_IDS] ?: emptySet() }
        } else {
            flowOf(emptySet())
        }

    suspend fun getArchivedChatIdsSnapshot(): Set<String> {
        return if (::dataStore.isInitialized) {
            try {
                val prefs = dataStore.data.first()
                prefs[ARCHIVED_CHAT_IDS] ?: emptySet()
            } catch (e: Exception) {
                emptySet()
            }
        } else emptySet()
    }

    suspend fun getDeletedChatLastMsgMapSnapshot(): Map<String, String> {
        return if (::dataStore.isInitialized) {
            try {
                val prefs = dataStore.data.first()
                val jsonStr = prefs[DELETED_CHAT_LAST_MSG_MAP]
                if (jsonStr.isNullOrBlank()) emptyMap() else Json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                emptyMap()
            }
        } else emptyMap()
    }

    suspend fun deleteChatForUser(chatId: String, lastMsgId: String? = null) {
        if (::dataStore.isInitialized) {
            dataStore.edit { prefs ->
                val currentDeleted = prefs[DELETED_CHAT_IDS] ?: emptySet()
                prefs[DELETED_CHAT_IDS] = currentDeleted + chatId
                val currentArchived = prefs[ARCHIVED_CHAT_IDS] ?: emptySet()
                if (currentArchived.contains(chatId)) {
                    prefs[ARCHIVED_CHAT_IDS] = currentArchived - chatId
                }
                if (lastMsgId != null) {
                    val map = try {
                        val str = prefs[DELETED_CHAT_LAST_MSG_MAP]
                        if (str.isNullOrBlank()) mutableMapOf() else Json.decodeFromString<MutableMap<String, String>>(str)
                    } catch (e: Exception) {
                        mutableMapOf()
                    }
                    map[chatId] = lastMsgId
                    prefs[DELETED_CHAT_LAST_MSG_MAP] = Json.encodeToString(map)
                }
            }
        }
    }

    suspend fun undeleteChatForUser(chatId: String) {
        if (::dataStore.isInitialized) {
            dataStore.edit { prefs ->
                val current = prefs[DELETED_CHAT_IDS] ?: emptySet()
                prefs[DELETED_CHAT_IDS] = current - chatId
                try {
                    val str = prefs[DELETED_CHAT_LAST_MSG_MAP]
                    if (!str.isNullOrBlank()) {
                        val map = Json.decodeFromString<MutableMap<String, String>>(str)
                        if (map.remove(chatId) != null) {
                            prefs[DELETED_CHAT_LAST_MSG_MAP] = Json.encodeToString(map)
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    fun getDeletedChatIds(): Flow<Set<String>> =
        if (::dataStore.isInitialized) {
            dataStore.data.map { prefs -> prefs[DELETED_CHAT_IDS] ?: emptySet() }
        } else {
            flowOf(emptySet())
        }

    suspend fun getDeletedChatIdsSnapshot(): Set<String> {
        return if (::dataStore.isInitialized) {
            try {
                val prefs = dataStore.data.first()
                prefs[DELETED_CHAT_IDS] ?: emptySet()
            } catch (e: Exception) {
                emptySet()
            }
        } else emptySet()
    }
}
