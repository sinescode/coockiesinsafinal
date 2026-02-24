package com.turjaun.cookiesuploader.util

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File

class FileHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "FileHelper"
        const val TARGET_FOLDER_NAME = "insta_saver"
    }
    
    /**
     * Returns the target directory: /storage/emulated/0/Download/insta_saver
     * Handles Android 10+ scoped storage with fallback
     */
    fun getDownloadDirectory(): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+: Try public Downloads first, fallback to app-specific
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = File(publicDir, TARGET_FOLDER_NAME)
            
            // Try to create, if fails use app-specific
            if (targetDir.canWrite() || targetDir.mkdirs()) {
                targetDir
            } else {
                // Fallback to app-specific external files dir
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), TARGET_FOLDER_NAME)
            }
        } else {
            // Android 9 and below: Direct public access
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(publicDir, TARGET_FOLDER_NAME).apply { mkdirs() }
        }
    }

    /**
     * Ensures directory exists and is writable
     */
    fun ensureDirectoryExists(directory: File): Boolean {
        return try {
            if (!directory.exists()) {
                val created = directory.mkdirs()
                Log.d(TAG, "Directory created: ${directory.absolutePath}, success: $created")
                created
            } else {
                directory.canWrite()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to ensure directory: ${e.message}")
            false
        }
    }
    
    /**
     * Check if storage permission is needed (for Android < 10)
     */
    fun requiresStoragePermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    }
}