package com.turjaun.cookiesuploader.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.turjaun.cookiesuploader.data.AccountRepository
import com.turjaun.cookiesuploader.data.model.Account
import com.turjaun.cookiesuploader.data.model.LogEntry
import com.turjaun.cookiesuploader.network.WebhookService
import com.turjaun.cookiesuploader.util.FileHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AccountRepository(application)
    private val webhookService = WebhookService()
    private val fileHelper = FileHelper(application)

    val accounts = repository.accounts
    val logs = repository.logs
    
    private val _webhookUrl = MutableStateFlow("")
    val webhookUrl: StateFlow<String> = _webhookUrl.asStateFlow()
    
    private val _jsonFilename = MutableStateFlow("")
    val jsonFilename: StateFlow<String> = _jsonFilename.asStateFlow()
    
    private val _currentPassword = MutableStateFlow("")
    val currentPassword: StateFlow<String> = _currentPassword.asStateFlow()
    
    private val _serverStatus = MutableStateFlow("Check")
    val serverStatus: StateFlow<String> = _serverStatus.asStateFlow()
    
    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()
    
    // ✅ NEW: FCM Token StateFlow
    private val _fcmToken = MutableStateFlow<String>("")
    val fcmToken: StateFlow<String> = _fcmToken.asStateFlow()

    init {
        viewModelScope.launch {
            repository.webhookUrl.collect { _webhookUrl.value = it }
        }
        viewModelScope.launch {
            repository.jsonFilename.collect { _jsonFilename.value = it }
        }
        viewModelScope.launch {
            repository.currentPassword.collect { 
                _currentPassword.value = it
                if (it.isEmpty()) generatePassword()
            }
        }
        
        // ✅ NEW: Fetch FCM Token on init
        fetchFcmToken()
    }
    
    // ✅ NEW: Helper function to fetch FCM token
    private fun fetchFcmToken() {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                _fcmToken.value = token
                repository.addLog("FCM", "Token fetched successfully")
            } catch (e: Exception) {
                _fcmToken.value = "Error: ${e.localizedMessage ?: "Unknown error"}"
                repository.addLog("FCM", "Token fetch failed: ${e.localizedMessage}")
            }
        }
    }

    fun generatePassword() {
        viewModelScope.launch {
            val pwd = repository.generatePassword()
            _currentPassword.value = pwd
        }
    }

    fun copyPassword() {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Password", _currentPassword.value)
        clipboard.setPrimaryClip(clip)
        
        viewModelScope.launch {
            repository.addLog("System", "Password copied successfully")
        }
    }
    
    // ✅ NEW: Copy FCM Token function (optional helper)
    fun copyFcmToken() {
        val token = _fcmToken.value
        if (token.isNotBlank() && !token.startsWith("Error")) {
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("FCM Token", token)
            clipboard.setPrimaryClip(clip)
            
            viewModelScope.launch {
                repository.addLog("FCM", "Token copied to clipboard")
            }
        }
    }

    fun checkServerStatus() {
        if (_webhookUrl.value.isEmpty() || !_webhookUrl.value.startsWith("http")) {
            viewModelScope.launch {
                repository.addLog("Error", "Invalid webhook URL")
            }
            return
        }

        viewModelScope.launch {
            _isChecking.value = true
            _serverStatus.value = "Checking..."
            
            val result = webhookService.checkServerStatus(_webhookUrl.value)
            
            _serverStatus.value = if (result.success) "ON" else "OFF"
            _isChecking.value = false
            
            repository.addLog(
                if (result.success) "System" else "Error", 
                "Server check: ${result.message}"
            )
        }
    }

    fun submitAccount(username: String, password: String, authCode: String) {
        if (username.isBlank() || password.isBlank()) {
            viewModelScope.launch {
                repository.addLog("Error", "Username and Password required")
            }
            return
        }

        viewModelScope.launch {
            repository.addAccount(username, password, authCode)
            
            val result = webhookService.submitAccount(_webhookUrl.value, username, password, authCode)
            
            val style = if (result.failedCount == 0 && result.successCount > 0) "bold" else "normal"
            repository.addLog(
                if (result.success) "Webhook" else "Error",
                "(${if (result.success) 200 else 400}) ${result.message}",
                style
            )
            
            // Generate new password after submission
            generatePassword()
        }
    }

    fun deleteAccount(index: Int) {
        viewModelScope.launch {
            repository.deleteAccount(index)
            repository.addLog("System", "Account deleted")
        }
    }

    fun clearAccounts() {
        viewModelScope.launch {
            repository.clearAccounts()
            repository.addLog("System", "All accounts cleared")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun saveSettings(webhook: String, filename: String) {
        viewModelScope.launch {
            repository.saveSettings(webhook, filename)
            _webhookUrl.value = webhook
            _jsonFilename.value = filename
            repository.addLog("System", "Settings saved")
        }
    }

    fun downloadEncryptedFile(): Boolean {
        return try {
            viewModelScope.launch {
                val password = _currentPassword.value
                val (pwd, encrypted) = repository.exportEncryptedData(password)
                val content = "$pwd\n$encrypted"
                
                val dir = fileHelper.getDownloadDirectory()
                if (!fileHelper.ensureDirectoryExists(dir)) {
                    repository.addLog("Error", "Failed to create directory")
                    return@launch
                }
                
                val file = File(dir, _jsonFilename.value)
                file.writeText(content)
                
                repository.addLog("System", "File saved to ${file.absolutePath}")
            }
            true
        } catch (e: Exception) {
            viewModelScope.launch {
                repository.addLog("Error", "Download failed: ${e.message}")
            }
            false
        }
    }

    fun importFromFile(): Boolean {
        return try {
            viewModelScope.launch {
                val dir = fileHelper.getDownloadDirectory()
                val file = File(dir, _jsonFilename.value)
                
                if (!file.exists()) {
                    repository.addLog("Error", "No backup file found at ${file.path}")
                    return@launch
                }
                
                val content = file.readText()
                val password = _currentPassword.value
                
                if (repository.importEncryptedData(content, password)) {
                    repository.addLog("Success", "Imported accounts from secure backup")
                } else {
                    repository.addLog("Error", "Import failed: Wrong password?")
                }
            }
            true
        } catch (e: Exception) {
            viewModelScope.launch {
                repository.addLog("Error", "Import failed: ${e.message}")
            }
            false
        }
    }
    
    // ✅ Optional: Refresh FCM token manually (e.g., from Settings)
    fun refreshFcmToken() {
        _fcmToken.value = "Refreshing..."
        fetchFcmToken()
    }
}