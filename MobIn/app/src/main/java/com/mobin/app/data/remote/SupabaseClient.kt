package com.mobin.app.data.remote

import com.mobin.app.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Singleton Supabase client.
 * Add your credentials to local.properties:
 *   SUPABASE_URL=https://YOUR_PROJECT_ID.supabase.co
 *   SUPABASE_ANON_KEY=YOUR_ANON_KEY
 */
object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        install(Auth) {
            autoLoadFromStorage = true
            autoSaveToStorage = true
        }
        install(Postgrest)
        install(Storage)
    }
}
