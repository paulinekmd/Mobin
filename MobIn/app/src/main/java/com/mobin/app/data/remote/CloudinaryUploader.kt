package com.mobin.app.data.remote

import com.mobin.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

@Serializable
data class CloudinaryUploadResponse(
    @SerialName("secure_url") val secureUrl: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("public_id") val publicId: String? = null,
    @SerialName("format") val format: String? = null,
)

object CloudinaryUploader {

    private val httpClient = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun uploadImage(
        imageBytes: ByteArray,
        fileName: String = "upload_${System.currentTimeMillis()}.jpg",
        folder: String = "mobin",
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME.ifBlank { "drjicvvih" }
            val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET.ifBlank { "mobin_mobile" }
            val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

            val mediaType = "image/jpeg".toMediaTypeOrNull()
            val fileBody = imageBytes.toRequestBody(mediaType)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .addFormDataPart("upload_preset", uploadPreset)
                .addFormDataPart("folder", folder)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    throw IOException("Cloudinary upload failed (HTTP ${response.code}): $bodyString")
                }

                val uploadResult = json.decodeFromString<CloudinaryUploadResponse>(bodyString)
                val imageUrl = uploadResult.secureUrl ?: uploadResult.url
                    ?: throw IOException("No URL returned from Cloudinary")

                android.util.Log.d("CloudinaryUploader", "Upload successful: $imageUrl")
                imageUrl
            }
        }
    }
}
