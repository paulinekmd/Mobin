package com.mobin.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mobin_prefs")

object DataStoreManager {

    private lateinit var dataStore: DataStore<Preferences>

    private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    private val SAVED_PROPERTY_IDS = stringSetPreferencesKey("saved_property_ids")

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
            kotlinx.coroutines.flow.flowOf(emptySet())
        }
}
