package com.mobin.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mobin_prefs")

object DataStoreManager {

    private lateinit var dataStore: DataStore<Preferences>

    private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")

    fun init(context: Context) {
        dataStore = context.dataStore
    }

    suspend fun setOnboardingComplete() {
        dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETE] = true }
    }

    fun isOnboardingComplete(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[ONBOARDING_COMPLETE] ?: false }
}
