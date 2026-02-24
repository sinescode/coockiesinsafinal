package com.turjaun.cookiesuploader.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.Base64

class WebhookClient {
    
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    
    suspend fun checkServerStatus(webhookUrl: String): ServerStatusResponse {
        return try {
            val randomUser = randomString(8)
            val randomPass = randomString(8)
            val randomCookie = randomString(10)
            
            val convertedStr = "$randomUser:$randomPass|||$randomCookie||"
            val payload = "accounts=${Base64.getEncoder().encodeToString(convertedStr.toByteArray(Charsets.UTF_8))}"
            
            val response: HttpResponse = client.post(webhookUrl) {
                contentType(ContentType.Text.Plain)
                setBody(payload)
            }
            
            if (response.bodyAsText().isNotEmpty()) {
                parseServerResponse(response.bodyAsText())
            } else {
                ServerStatusResponse(status = "OFF")
            }
        } catch (e: Exception) {
            ServerStatusResponse(status = "OFF", error = e.message)
        }
    }
    
    suspend fun submitData(
        webhookUrl: String,
        username: String,
        password: String,        cookies: String
    ): SubmitResponse {
        return try {
            val convertedStr = "$username:$password|||$cookies||"
            val payload = "accounts=${Base64.getEncoder().encodeToString(convertedStr.toByteArray(Charsets.UTF_8))}"
            
            val response: HttpResponse = client.post(webhookUrl) {
                contentType(ContentType.Text.Plain)
                setBody(payload)
            }
            
            val responseBody = response.bodyAsText()
            val statusCode = response.status.value
            
            if (responseBody.isNotEmpty()) {
                val parsed = parseSubmitResponse(responseBody, statusCode)
                SubmitResponse(
                    success = true,
                    statusCode = statusCode,
                    message = parsed.message,
                    successCount = parsed.successCount,
                    failedCount = parsed.failedCount,
                    style = parsed.style
                )
            } else {
                SubmitResponse(
                    success = statusCode in 200..299,
                    statusCode = statusCode,
                    message = if (statusCode in 200..299) "Success" else "Empty response"
                )
            }
        } catch (e: Exception) {
            SubmitResponse(success = false, message = "Connection error: ${e.message}")
        }
    }
    
    private fun randomString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyz1234567890"
        return (1..length).map { chars.random() }.joinToString("")
    }
    
    private fun parseServerResponse(responseBody: String): ServerStatusResponse {
        return try {
            // Simplified parsing - adapt based on your API response format
            ServerStatusResponse(status = "ON")
        } catch (e: Exception) {
            ServerStatusResponse(status = "OFF")
        }
    }
        private fun parseSubmitResponse(responseBody: String, statusCode: Int): ParsedSubmitResponse {
        return try {
            // Parse JSON response - adapt based on your actual API format
            ParsedSubmitResponse(
                message = "Success: 1 | Failed: 0",
                successCount = 1,
                failedCount = 0,
                style = "bold"
            )
        } catch (e: Exception) {
            ParsedSubmitResponse(message = responseBody.trim())
        }
    }
}

data class ServerStatusResponse(
    val status: String,
    val error: String? = null
)

data class SubmitResponse(
    val success: Boolean,
    val statusCode: Int = 0,
    val message: String = "",
    val successCount: Int = 0,
    val failedCount: Int = 0,
    val style: String = "normal"
)

data class ParsedSubmitResponse(
    val message: String = "",
    val successCount: Int = 0,
    val failedCount: Int = 0,
    val style: String = "normal"
)