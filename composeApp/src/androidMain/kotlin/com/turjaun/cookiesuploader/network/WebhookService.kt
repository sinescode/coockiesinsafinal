package com.turjaun.cookiesuploader.network

import android.util.Base64
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout  // ✅ ADD THIS IMPORT
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout  // ✅ Keep this for timeout {} DSL
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Random

class WebhookService {
    
    // ✅ FIXED: HttpClient with HttpTimeout plugin installed
    private val client = HttpClient(OkHttp) {
        
        // ✅ INSTALL HttpTimeout plugin - Required for timeout {} DSL
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000   // Default: 15 seconds
            connectTimeoutMillis = 10_000   // Default: 10 seconds  
            socketTimeoutMillis = 15_000    // Default: 15 seconds
        }
        
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    data class WebhookResult(
        val success: Boolean,
        val successCount: Int = 0,
        val failedCount: Int = 0,
        val message: String = ""
    )

    suspend fun checkServerStatus(webhookUrl: String): WebhookResult = withContext(Dispatchers.IO) {
        try {
            val chars = "abcdefghijklmnopqrstuvwxyz1234567890"
            val random = Random()
            val randomUser = (1..8).map { chars[random.nextInt(chars.length)] }.joinToString("")
            val randomPass = (1..8).map { chars[random.nextInt(chars.length)] }.joinToString("")
            val randomCookie = (1..10).map { chars[random.nextInt(chars.length)] }.joinToString("")

            val convertedStr = "$randomUser:$randomPass|||$randomCookie||"
            val payload = "accounts=${Base64.encodeToString(convertedStr.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)}"

            val response: HttpResponse = client.post(webhookUrl) {
                contentType(ContentType.Text.Plain)
                setBody(payload)
                // ✅ Now this works because HttpTimeout plugin is installed above
                timeout { 
                    requestTimeoutMillis = 10_000  // Per-request override: 10 seconds
                }
            }

            parseResponse(response)
        } catch (e: Exception) {
            WebhookResult(
                success = false, 
                message = "Connection error: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
            )
        }
    }

    suspend fun submitAccount(
        webhookUrl: String, 
        username: String, 
        password: String, 
        authCode: String
    ): WebhookResult = withContext(Dispatchers.IO) {
        try {
            val convertedStr = "$username:$password|||$authCode||"
            val payload = "accounts=${Base64.encodeToString(convertedStr.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)}"

            val response: HttpResponse = client.post(webhookUrl) {
                contentType(ContentType.Text.Plain)
                setBody(payload)
                // ✅ Now this works because HttpTimeout plugin is installed
                timeout { 
                    requestTimeoutMillis = 10_000
                }
            }

            parseResponse(response, response.status.value)
        } catch (e: Exception) {
            WebhookResult(
                success = false, 
                message = "Connection error: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
            )
        }
    }

    private suspend fun parseResponse(response: HttpResponse, statusCode: Int = 200): WebhookResult {
        val bodyText = response.body<String>()
        
        return try {
            if (bodyText.isEmpty()) {
                return WebhookResult(success = false, message = "Empty response")
            }

            val json = Json.parseToJsonElement(bodyText).jsonObject
            val dataNode = if (json.containsKey("data")) json["data"]?.jsonObject else json
            
            val successCount = dataNode?.get("success_count")?.jsonPrimitive?.intOrNull ?: 0
            val failedCount = dataNode?.get("failed_count")?.jsonPrimitive?.intOrNull ?: 0
            
            val isSuccess = statusCode in 200..299 && (failedCount == 0 || successCount > 0)
            
            WebhookResult(
                success = isSuccess,
                successCount = successCount,
                failedCount = failedCount,
                message = "Success: $successCount | Failed: $failedCount"
            )
        } catch (e: Exception) {
            WebhookResult(success = false, message = "Invalid response format: ${e.localizedMessage}")
        }
    }
}