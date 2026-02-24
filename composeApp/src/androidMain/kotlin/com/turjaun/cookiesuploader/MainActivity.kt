package com.turjaun.cookiesuploader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import com.turjaun.cookiesuploader.ui.MainScreen
import com.turjaun.cookiesuploader.ui.theme.CookiesUploaderTheme
import com.turjaun.cookiesuploader.util.FileHelper

class MainActivity : ComponentActivity() {
    
    private lateinit var fileHelper: FileHelper
    
    // ─────────────────────────────────────────
    // Permission Launchers
    // ─────────────────────────────────────────
    
    // Standard runtime permission launcher (POST_NOTIFICATIONS, WRITE_EXTERNAL_STORAGE)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted - proceed with file operations
            fileHelper.ensureDirectoryExists(fileHelper.getDownloadDirectory())
        } else {
            // Permission denied - show fallback message
            showPermissionFallbackToast()
        }
    }
    
    // Special launcher for MANAGE_EXTERNAL_STORAGE (Android 11+)
    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            // All Files Access granted
            fileHelper.ensureDirectoryExists(fileHelper.getDownloadDirectory())
        } else {
            // Denied or cancelled - fallback to app-specific directory
            showPermissionFallbackToast()
        }
    }
    
    // ─────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize FileHelper
        fileHelper = FileHelper(this)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Request required permissions
        checkAndRequestPermissions()
        
        // Set Compose content
        setContent {
            CookiesUploaderTheme {
                Surface {
                    MainScreen()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Re-check permissions in case user changed them in Settings
        if (fileHelper.requiresStoragePermission() && fileHelper.getRequiredPermission() == Manifest.permission.MANAGE_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                // Permission granted while app was backgrounded
                fileHelper.ensureDirectoryExists(fileHelper.getDownloadDirectory())
            }
        }
    }
    
    // ─────────────────────────────────────────
    // Permission Logic
    // ─────────────────────────────────────────
    
    private fun checkAndRequestPermissions() {
        // 1. Handle Notification Permission (Android 13+)
        handleNotificationPermission()
        
        // 2. Handle Storage Permission
        handleStoragePermission()
    }
    
    private fun handleNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, 
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request notification permission (non-blocking - app works without it)
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    private fun handleStoragePermission() {
        val requiredPermission = fileHelper.getRequiredPermission()
        
        when {
            // ✅ No permission needed (Android 13+ app-specific or legacy enabled)
            requiredPermission == null -> {
                // Ensure directory is ready
                fileHelper.ensureDirectoryExists(fileHelper.getDownloadDirectory())
            }
            
            // ⚠️ MANAGE_EXTERNAL_STORAGE - Special handling for Android 11+
            requiredPermission == Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // Already granted
                        fileHelper.ensureDirectoryExists(fileHelper.getDownloadDirectory())
                    } else {
                        // Request All Files Access - opens system settings
                        requestManageExternalStoragePermission()
                    }
                }
            }
            
            // 📁 Legacy storage permission (Android 9 and below)
            else -> {
                if (ContextCompat.checkSelfPermission(
                        this, 
                        requiredPermission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Request runtime permission
                    requestPermissionLauncher.launch(requiredPermission)
                } else {
                    // Already granted
                    fileHelper.ensureDirectoryExists(fileHelper.getDownloadDirectory())
                }
            }
        }
    }
    
    private fun requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                manageStorageLauncher.launch(intent)
            } catch (e: Exception) {
                // Fallback for devices that don't support the intent
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                manageStorageLauncher.launch(intent)
            }
        }
    }
    
    // ─────────────────────────────────────────
    // UI Helpers
    // ─────────────────────────────────────────
    
    private fun showPermissionFallbackToast() {
        Toast.makeText(
            this,
            "Using app-specific storage. Files saved in:\n${fileHelper.getDownloadDirectory()}",
            Toast.LENGTH_LONG
        ).show()
    }
    
    // ─────────────────────────────────────────
    // Public API for ViewModel/Other Components
    // ─────────────────────────────────────────
    
    /**
     * Call this before performing file operations to ensure permissions are ready.
     * Returns true if ready, false if permission flow was triggered.
     */
    fun ensureStorageReady(onReady: () -> Unit): Boolean {
        val requiredPermission = fileHelper.getRequiredPermission()
        
        return when {
            requiredPermission == null -> {
                onReady()
                true
            }
            requiredPermission == Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                    onReady()
                    true
                } else {
                    requestManageExternalStoragePermission()
                    false
                }
            }
            else -> {
                if (ContextCompat.checkSelfPermission(this, requiredPermission) == PackageManager.PERMISSION_GRANTED) {
                    onReady()
                    true
                } else {
                    requestPermissionLauncher.launch(requiredPermission)
                    false
                }
            }
        }
    }
    
    /**
     * Get the target directory path for display in UI
     */
    fun getStoragePathDisplay(): String {
        return fileHelper.getTargetDirectoryPath()
    }
}