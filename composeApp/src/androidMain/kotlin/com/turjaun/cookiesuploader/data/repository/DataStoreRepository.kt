package com.turjaun.cookiesuploader.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.turjaun.cookiesuploader.data.models.Account
import com.turjaun.cookiesuploader.data.models.LogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreRepository(private val context: Context) {
    
    companion object {
        private val WEBHOOK_URL_KEY = stringPreferencesKey("webhook_url")
        private val JSON_FILENAME_KEY = stringPreferencesKey("json_filename")
        private val CURRENT_PASSWORD_KEY = stringPreferencesKey("current_password")
        private val ACCOUNTS_LIST_KEY = stringPreferencesKey("accounts_list")
        private val LOGS_LIST_KEY = stringPreferencesKey("logs_list")
        private val DEVICE_TOKEN_KEY = stringPreferencesKey("device_token")
        
        private val json = Json { ignoreUnknownKeys = true }
    }
    
    // Webhook URL
    val webhookUrlFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[WEBHOOK_URL_KEY] ?: "http://43.135.182.151/api/api/v1/webhook/nRlmI2-8T7x2DAWe1hWxi97qGA1FcCxrNcyCtLTO_Cw/account-push"
        }
    
    suspend fun saveWebhookUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[WEBHOOK_URL_KEY] = url
        }
    }
    
    // JSON Filename    val jsonFilenameFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[JSON_FILENAME_KEY] ?: "accounts.json"
        }
    
    suspend fun saveJsonFilename(filename: String) {
        context.dataStore.edit { preferences ->
            preferences[JSON_FILENAME_KEY] = filename
        }
    }
    
    // Current Password
    val currentPasswordFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[CURRENT_PASSWORD_KEY] ?: ""
        }
    
    suspend fun saveCurrentPassword(password: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_PASSWORD_KEY] = password
        }
    }
    
    // Device Token (FCM)
    val deviceTokenFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[DEVICE_TOKEN_KEY] ?: ""
        }
    
    suspend fun saveDeviceToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[DEVICE_TOKEN_KEY] = token
        }
    }
    
    // Accounts List
    val accountsFlow: Flow<List<Account>> = context.dataStore.data
        .catch { exception ->            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            val accountsStr = preferences[ACCOUNTS_LIST_KEY]
            if (!accountsStr.isNullOrEmpty()) {
                try {
                    json.decodeFromString<List<Account>>(accountsStr)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    
    suspend fun saveAccounts(accounts: List<Account>) {
        context.dataStore.edit { preferences ->
            preferences[ACCOUNTS_LIST_KEY] = json.encodeToString(accounts)
        }
    }
    
    // Logs List
    val logsFlow: Flow<List<LogEntry>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            val logsStr = preferences[LOGS_LIST_KEY]
            if (!logsStr.isNullOrEmpty()) {
                try {
                    json.decodeFromString<List<LogEntry>>(logsStr)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    
    suspend fun saveLogs(logs: List<LogEntry>) {
        context.dataStore.edit { preferences ->
            preferences[LOGS_LIST_KEY] = json.encodeToString(logs)
        }
    }
    
    // Clear all data
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->            preferences.clear()
        }
    }
}