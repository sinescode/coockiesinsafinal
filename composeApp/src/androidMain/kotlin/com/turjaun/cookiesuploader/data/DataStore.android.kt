package com.turjaun.cookiesuploader.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        private val WEBHOOK_URL = stringPreferencesKey("webhook_url")
        private val JSON_FILENAME = stringPreferencesKey("json_filename")
        private val CURRENT_PASSWORD = stringPreferencesKey("current_password")
        private val ACCOUNTS_LIST = stringPreferencesKey("accounts_list")
        private val LOGS_LIST = stringPreferencesKey("logs_list")
        private val FCM_TOKEN = stringPreferencesKey("fcm_token")
        
        const val DEFAULT_WEBHOOK = "http://43.135.182.151/api/api/v1/webhook/nRlmI2-8T7x2DAWe1hWxi97qGA1FcCxrNcyCtLTO_Cw/account-push"
        const val DEFAULT_FILENAME = "accounts.json"
    }

    val webhookUrl: Flow<String> = context.dataStore.data.map { 
        it[WEBHOOK_URL] ?: DEFAULT_WEBHOOK 
    }
    
    val jsonFilename: Flow<String> = context.dataStore.data.map { 
        it[JSON_FILENAME] ?: DEFAULT_FILENAME 
    }
    
    val currentPassword: Flow<String> = context.dataStore.data.map { 
        it[CURRENT_PASSWORD] ?: "" 
    }
    
    val fcmToken: Flow<String> = context.dataStore.data.map {
        it[FCM_TOKEN] ?: ""
    }

    val accounts: Flow<List<com.turjaun.cookiesuploader.data.model.Account>> = context.dataStore.data.map {
        val json = it[ACCOUNTS_LIST] ?: "[]"
        try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val logs: Flow<List<com.turjaun.cookiesuploader.data.model.LogEntry>> = context.dataStore.data.map {
        val json = it[LOGS_LIST] ?: "[]"
        try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveWebhookUrl(url: String) {
        context.dataStore.edit { it[WEBHOOK_URL] = url }
    }

    suspend fun saveJsonFilename(filename: String) {
        context.dataStore.edit { it[JSON_FILENAME] = filename }
    }

    suspend fun saveCurrentPassword(password: String) {
        context.dataStore.edit { it[CURRENT_PASSWORD] = password }
    }

    suspend fun saveAccounts(accounts: List<com.turjaun.cookiesuploader.data.model.Account>) {
        context.dataStore.edit { 
            it[ACCOUNTS_LIST] = Json.encodeToString(accounts) 
        }
    }

    suspend fun saveLogs(logs: List<com.turjaun.cookiesuploader.data.model.LogEntry>) {
        context.dataStore.edit { 
            it[LOGS_LIST] = Json.encodeToString(logs) 
        }
    }

    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { it[FCM_TOKEN] = token }
    }
}