package com.mobin.app.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object EmailJsService {
    private const val TAG = "EmailJsService"
    
    const val SERVICE_ID = "service_vr0gcbr"
    const val TEMPLATE_ID = "template_zfo5vn4"
    const val PUBLIC_KEY = "LBV7JYBqE6IsIz6R8"
    const val PRIVATE_KEY = "6W1MKfGp0-SP7jqxsu4sK"

    suspend fun sendOtpEmail(toEmail: String, otpCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d(TAG, "Sending OTP email to $toEmail via EmailJS...")
            val url = URL("https://api.emailjs.com/api/v1.0/email/send")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json, text/plain, */*")
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.doOutput = true
            conn.doInput = true

            val templateParams = JSONObject().apply {
                put("to_email", toEmail)
                put("otp_code", otpCode)
                put("email", toEmail)
                put("code", otpCode)
            }

            val payload = JSONObject().apply {
                put("service_id", SERVICE_ID)
                put("template_id", TEMPLATE_ID)
                put("user_id", PUBLIC_KEY)
                put("accessToken", PRIVATE_KEY)
                put("template_params", templateParams)
            }

            val jsonBytes = payload.toString().toByteArray(Charsets.UTF_8)
            conn.outputStream.use { os ->
                os.write(jsonBytes)
                os.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                Log.d(TAG, "EmailJS OTP sent successfully! Response code: $responseCode")
                Unit
            } else {
                val errorStream = conn.errorStream ?: conn.inputStream
                val errorText = errorStream?.let { stream ->
                    BufferedReader(InputStreamReader(stream)).use { it.readText() }
                } ?: "Unknown error"
                Log.e(TAG, "EmailJS error ($responseCode): $errorText")
                error("EmailJS error ($responseCode): $errorText")
            }
        }
    }
}
