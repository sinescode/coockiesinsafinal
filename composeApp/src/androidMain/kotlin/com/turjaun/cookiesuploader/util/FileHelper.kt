package com.turjaun.cookiesuploader.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class FileHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "FileHelper"
        const val TARGET_FOLDER_NAME = "insta_saver"
        private const val USE_LEGACY_EXTERNAL_STORAGE = false
    }
    
    fun getTargetDirectoryPath(): String = "/storage/emulated/0/Download/$TARGET_FOLDER_NAME"
    
    fun getDownloadDirectory(): File {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                File(publicDir, TARGET_FOLDER_NAME).apply { if (!exists()) mkdirs() }
            }
            USE_LEGACY_EXTERNAL_STORAGE || hasManageExternalStoragePermission() -> {
                val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                File(publicDir, TARGET_FOLDER_NAME).apply { if (!exists()) mkdirs() }
            }
            else -> {
                val appDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                File(appDir, TARGET_FOLDER_NAME).apply { if (!exists()) mkdirs() }
            }
        }
    }
    
    private fun hasManageExternalStoragePermission(): Boolean = 
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()
    
    fun ensureDirectoryExists(directory: File): Boolean = try {
        if (!directory.exists()) directory.mkdirs() else directory.canWrite() || directory.canRead()
    } catch (e: Exception) { Log.e(TAG, "Failed to ensure directory: ${e.message}"); false }
    
    fun writeFile(filename: String, content: String): Uri? {
        return try {
            val directory = getDownloadDirectory()
            val file = File(directory, filename)
            
            if (file.canWrite() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                FileOutputStream(file).use { fos -> fos.write(content.toByteArray(Charsets.UTF_8)) }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                writeViaMediaStore(filename, content)
            }
        } catch (e: Exception) { Log.e(TAG, "Failed to write file: ${e.message}"); null }
    }
    
    private fun writeViaMediaStore(filename: String, content: String): Uri? {
        return try {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/json")
                put(android.provider.MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/$TARGET_FOLDER_NAME")
                put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { os -> os.write(content.toByteArray(Charsets.UTF_8)) }
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
            uri
        } catch (e: Exception) { Log.e(TAG, "MediaStore write failed: ${e.message}"); null }
    }
    
    fun requiresStoragePermission(): Boolean = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> true
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> false
        USE_LEGACY_EXTERNAL_STORAGE || hasManageExternalStoragePermission() -> false
        else -> true
    }
    
    fun getRequiredPermission(): String? = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> null
        USE_LEGACY_EXTERNAL_STORAGE || hasManageExternalStoragePermission() -> null
        else -> android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
    }
}