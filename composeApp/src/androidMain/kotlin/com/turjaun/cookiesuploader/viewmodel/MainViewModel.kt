package com.turjaun.cookiesuploader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turjaun.cookiesuploader.data.AppContainer
import com.turjaun.cookiesuploader.data.models.Account
import com.turjaun.cookiesuploader.data.models.LogEntry
import com.turjaun.cookiesuploader.network.SubmitResponse
import com.turjaun.cookiesuploader.network.WebhookClient
import com.turjaun.cookiesuploader.security.SecureVault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class MainViewModel(private val container: AppContainer) : ViewModel() {
    
    private val _webhookUrl = MutableStateFlow("")
    val webhookUrl: StateFlow<String> = _webhookUrl.asStateFlow()
    
    private val _jsonFilename = MutableStateFlow("accounts.json")
    val jsonFilename: StateFlow<String> = _jsonFilename.asStateFlow()
    
    private val _currentPassword = MutableStateFlow("")
    val currentPassword: StateFlow<String> = _currentPassword.asStateFlow()
    
    private val _deviceToken = MutableStateFlow("")
    val deviceToken: StateFlow<String> = _deviceToken.asStateFlow()
    
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()
    
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()
    
    private val _serverStatus = MutableStateFlow("Check")
    val serverStatus: StateFlow<String> = _serverStatus.asStateFlow()
    
    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val dataStore = container.dataStoreRepository    private val webhookClient: WebhookClient = container.webhookClient
    private val secureVault: SecureVault = container.secureVault
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            combine(
                dataStore.webhookUrlFlow,
                dataStore.jsonFilenameFlow,
                dataStore.currentPasswordFlow,
                dataStore.deviceTokenFlow,
                dataStore.accountsFlow,
                dataStore.logsFlow
            ) { webhook, filename, password, token, accounts, logs ->
                _webhookUrl.value = webhook
                _jsonFilename.value = filename
                _currentPassword.value = password
                _deviceToken.value = token
                _accounts.value = accounts
                _logs.value = logs
                
                // Generate password if empty
                if (password.isEmpty()) {
                    generatePassword()
                }
            }.collect {}
        }
    }
    
    private fun saveData() {
        viewModelScope.launch {
            dataStore.saveWebhookUrl(_webhookUrl.value)
            dataStore.saveJsonFilename(_jsonFilename.value)
            dataStore.saveCurrentPassword(_currentPassword.value)
            dataStore.saveAccounts(_accounts.value)
            dataStore.saveLogs(_logs.value)
        }
    }
    
    fun updateWebhookUrl(url: String) {
        _webhookUrl.value = url
        saveData()
    }
    
    fun updateJsonFilename(filename: String) {
        _jsonFilename.value = filename
        saveData()    }
    
    fun updateDeviceToken(token: String) {
        _deviceToken.value = token
        viewModelScope.launch {
            dataStore.saveDeviceToken(token)
        }
    }
    
    fun generatePassword() {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val dateStr = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
        val random = Random()
        val length = random.nextInt(5) + 8
        val letterCount = length - dateStr.length
        
        val result = (1..letterCount).map { chars.random() }.joinToString("")
        val newPassword = result + dateStr
        
        _currentPassword.value = newPassword
        saveData()
    }
    
    fun copyPassword(): String {
        return _currentPassword.value
    }
    
    fun checkServerStatus() {
        if (_webhookUrl.value.isEmpty() || !_webhookUrl.value.startsWith("http")) {
            addLog("Error", "Please set a valid Webhook URL in Settings first")
            return
        }
        
        viewModelScope.launch {
            _isChecking.value = true
            _serverStatus.value = "Checking..."
            
            val response = webhookClient.checkServerStatus(_webhookUrl.value)
            
            _serverStatus.value = response.status
            _isChecking.value = false
        }
    }
    
    fun submitData(username: String, password: String, cookies: String) {
        if (username.isEmpty() || password.isEmpty()) {
            addLog("Error", "Username and Password required")
            return
        }
                // Update current password
        _currentPassword.value = password
        
        val newEntry = Account(
            email = "",
            username = username,
            password = password,
            auth_code = cookies
        )
        
        _accounts.value = listOf(newEntry) + _accounts.value
        saveData()
        
        viewModelScope.launch {
            _isLoading.value = true
            
            val response = webhookClient.submitData(
                webhookUrl = _webhookUrl.value,
                username = username,
                password = password,
                cookies = cookies
            )
            
            val logStatus = if (response.success) "Webhook" else "Error"
            val logStyle = if (response.failedCount == 0 && response.successCount > 0) "bold" else "normal"
            val logMessage = if (response.successCount > 0 || response.failedCount > 0) {
                "Success: ${response.successCount} | Failed: ${response.failedCount}"
            } else {
                response.message
            }
            
            addLog(logStatus, "(${response.statusCode}) $logMessage", logStyle)
            _isLoading.value = false
        }
    }
    
    fun addAccount(account: Account) {
        _accounts.value = listOf(account) + _accounts.value
        saveData()
    }
    
    fun removeAccount(index: Int) {
        _accounts.value = _accounts.value.toMutableList().apply { removeAt(index) }
        saveData()
        addLog("System", "Account deleted")
    }
    
    fun clearAccounts() {
        _accounts.value = emptyList()
        saveData()        addLog("System", "All accounts cleared")
    }
    
    fun addLog(status: String, message: String, style: String = "normal") {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val newLog = LogEntry(status = status, message = message, time = time, style = style)
        _logs.value = listOf(newLog) + _logs.value
        saveData()
    }
    
    fun clearLogs() {
        _logs.value = emptyList()
        saveData()
    }
    
    fun encryptAndSave(accounts: List<Account>, password: String): String {
        val json = kotlinx.serialization.json.Json.encodeToString(
            kotlinx.serialization.serializer<List<Account>>(), 
            accounts
        )
        return secureVault.pack(json, password)
    }
    
    fun decryptAndLoad(encryptedData: String, password: String): Result<List<Account>> {
        return try {
            val decrypted = secureVault.unpack(encryptedData, password)
            if (decrypted.startsWith("ERROR:")) {
                Result.failure(Exception(decrypted))
            } else {
                val accounts = kotlinx.serialization.json.Json.decodeFromString<List<Account>>(decrypted)
                Result.success(accounts)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}