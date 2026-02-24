package com.turjaun.cookiesuploader.util

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File

class FileHelper(private val context: Context) {
    
    fun getDownloadDirectory(): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+, use app-specific directory
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "insta_saver")
        } else {
            // For older versions, use public Downloads
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "insta_saver")
        }
    }

    fun ensureDirectoryExists(directory: File): Boolean {
        return if (!directory.exists()) {
            directory.mkdirs()
        } else {
            true
        }
    }
}