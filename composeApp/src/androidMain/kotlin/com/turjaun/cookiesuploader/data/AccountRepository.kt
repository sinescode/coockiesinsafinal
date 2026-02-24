package com.turjaun.cookiesuploader.data

import android.content.Context
import com.turjaun.cookiesuploader.data.model.Account
import com.turjaun.cookiesuploader.data.model.LogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class AccountRepository(private val context: Context) {
    private val dataStore = SettingsDataStore(context)
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    val accounts: Flow<List<Account>> = dataStore.accounts
    val logs: Flow<List<LogEntry>> = dataStore.logs
    val webhookUrl: Flow<String> = dataStore.webhookUrl
    val jsonFilename: Flow<String> = dataStore.jsonFilename
    val currentPassword: Flow<String> = dataStore.currentPassword
    val fcmToken: Flow<String> = dataStore.fcmToken

    suspend fun generatePassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val dateStr = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
        val random = Random()
        val length = random.nextInt(5) + 8
        val letterCount = length - dateStr.length

        var result = ""
        repeat(letterCount) {
            result += chars[random.nextInt(chars.length)]
        }
        val password = result + dateStr
        dataStore.saveCurrentPassword(password)
        return password
    }

    suspend fun addAccount(username: String, password: String, authCode: String): Account {
        val account = Account(
            username = username,
            password = password,
            authCode = authCode
        )
        val current = accounts.first().toMutableList()
        current.add(0, account)
        dataStore.saveAccounts(current)
        return account
    }

    suspend fun deleteAccount(index: Int) {
        val current = accounts.first().toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            dataStore.saveAccounts(current)
        }
    }

    suspend fun clearAccounts() {
        dataStore.saveAccounts(emptyList())
    }

    suspend fun addLog(status: String, message: String, style: String = "normal") {
        val log = LogEntry(
            status = status,
            message = message,
            time = dateFormat.format(Date()),
            style = style
        )
        val current = logs.first().toMutableList()
        current.add(0, log)
        // Keep only last 100 logs
        if (current.size > 100) current.removeAt(current.lastIndex)
        dataStore.saveLogs(current)
    }

    suspend fun clearLogs() {
        dataStore.saveLogs(emptyList())
    }

    suspend fun saveSettings(webhookUrl: String, filename: String) {
        dataStore.saveWebhookUrl(webhookUrl)
        dataStore.saveJsonFilename(filename)
    }

    suspend fun saveFcmToken(token: String) {
        dataStore.saveFcmToken(token)
    }

    suspend fun exportEncryptedData(password: String): Pair<String, String> {
        val accountsList = accounts.first()
        val json = kotlinx.serialization.json.Json.encodeToString(accountsList)
        val encrypted = SecureVault.pack(json, password)
        return password to encrypted
    }

    suspend fun importEncryptedData(fileContent: String, password: String): Boolean {
        val newlineIdx = fileContent.indexOf('\n')
        val encryptedContent = if (newlineIdx != -1) {
            fileContent.substring(newlineIdx + 1).trim()
        } else {
            fileContent
        }

        val decrypted = SecureVault.unpack(encryptedContent, password)
        if (decrypted.startsWith("ERROR:")) return false

        val imported = kotlinx.serialization.json.Json.decodeFromString<List<Account>>(decrypted)
        dataStore.saveAccounts(imported)
        return true
    }
}