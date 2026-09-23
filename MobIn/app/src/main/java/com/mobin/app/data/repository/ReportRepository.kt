package com.mobin.app.data.repository

import com.mobin.app.data.model.PropertyReportInsert
import com.mobin.app.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportRepository {

    private val supabase = SupabaseClient.client

    suspend fun submitReport(
        propertyId: String,
        propertyName: String?,
        reason: String,
        details: String? = null,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val user = supabase.auth.currentUserOrNull()
            val userEmail = user?.email ?: "renter@mobin.app"
            val userName = user?.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
                ?: userEmail.substringBefore("@")

            val insert = PropertyReportInsert(
                propertyId = propertyId,
                propertyName = propertyName,
                reporterEmail = userEmail,
                reporterName = userName,
                reason = reason.trim(),
                details = details?.trim()?.ifBlank { null },
                status = "pending",
            )

            supabase.from("reports").insert(insert)
            android.util.Log.d("ReportRepository", "Submitted report for property $propertyId")
            Unit
        }
    }
}
