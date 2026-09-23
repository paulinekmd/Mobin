package com.mobin.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PropertyReport(
    val id: String = "",
    @SerialName("property_id") val propertyId: String = "",
    @SerialName("property_name") val propertyName: String? = null,
    @SerialName("reporter_email") val reporterEmail: String? = null,
    @SerialName("reporter_name") val reporterName: String? = null,
    val reason: String = "",
    val details: String? = null,
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class PropertyReportInsert(
    @SerialName("property_id") val propertyId: String,
    @SerialName("property_name") val propertyName: String?,
    @SerialName("reporter_email") val reporterEmail: String?,
    @SerialName("reporter_name") val reporterName: String?,
    val reason: String,
    val details: String? = null,
    val status: String = "pending",
)
