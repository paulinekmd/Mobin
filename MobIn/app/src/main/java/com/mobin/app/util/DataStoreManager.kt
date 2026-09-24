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
}
