package com.turjaun.cookiesuploader.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class FileHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "FileHelper"
        const val TARGET_FOLDER_NAME = "insta_saver"
        
        /**
         * Legacy flag: Set to true ONLY if you have MANAGE_EXTERNAL_STORAGE permission
         * and Google Play approval for "All files access"
         */
        private const val USE_LEGACY_EXTERNAL_STORAGE = false
    }
    
    /**
     * Returns the target directory path as a String for display/logging
     */
    fun getTargetDirectoryPath(): String {
        return "/storage/emulated/0/Download/$TARGET_FOLDER_NAME"
    }
    
    /**
     * Returns a File object for the target directory.
     * 
     * ⚠️ On Android 10+ (API 29+), direct File access to public Downloads 
     * requires MANAGE_EXTERNAL_STORAGE permission. Without it, this returns
     * the app-specific directory as a fallback.
     */
    fun getDownloadDirectory(): File {
        return when {
            // Android 9 and below: Direct public access works
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                File(publicDir, TARGET_FOLDER_NAME).apply { 
                    if (!exists()) mkdirs() 
                }
            }
            
            // Android 10+: Check for legacy storage flag or MANAGE permission
            USE_LEGACY_EXTERNAL_STORAGE || hasManageExternalStoragePermission() -> {
                // Direct access with legacy flag or All Files permission
                val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                File(publicDir, TARGET_FOLDER_NAME).apply { 
                    if (!exists()) mkdirs() 
                }
            }
            
            // Default fallback: App-specific directory (scoped storage compliant)
            else -> {
                val appDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                File(appDir, TARGET_FOLDER_NAME).apply { 
                    if (!exists()) mkdirs() 
                }
            }
        }
    }
    
    /**
     * Checks if the app has MANAGE_EXTERNAL_STORAGE permission (Android 11+)
     */
    private fun hasManageExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            false
        }
    }
    
    /**
     * Ensures directory exists and is writable
     */
    fun ensureDirectoryExists(directory: File): Boolean {
        return try {
            if (!directory.exists()) {
                val created = directory.mkdirs()
                if (created) {
                    Log.d(TAG, "Directory created: ${directory.absolutePath}")
                } else {
                    Log.e(TAG, "Failed to create directory: ${directory.absolutePath}")
                }
                created
            } else {
                directory.canWrite() || directory.canRead()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for directory: ${directory.absolutePath}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to ensure directory: ${e.message}")
            false
        }
    }
    
    /**
     * Writes content to a file in the target directory using appropriate API
     * 
     * @param filename Name of the file to create
     * @param content String content to write
     * @return Uri of the created file, or null if failed
     */
    fun writeFile(filename: String, content: String): Uri? {
        return try {
            val directory = getDownloadDirectory()
            val file = File(directory, filename)
            
            // Try direct file write first
            if (file.canWrite() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                FileOutputStream(file).use { fos ->
                    fos.write(content.toByteArray(Charsets.UTF_8))
                }
                // Convert to Uri for sharing
                ContextCompat.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                // Fallback: Use MediaStore for Android 10+
                writeViaMediaStore(filename, content)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write file: ${e.message}")
            null
        }
    }
    
    /**
     * Fallback method using MediaStore API for Android 10+ scoped storage
     */
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
                resolver.openOutputStream(it)?.use { os ->
                    os.write(content.toByteArray(Charsets.UTF_8))
                }
                
                // Mark as complete
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
            
            uri
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore write failed: ${e.message}")
            null
        }
    }
    
    /**
     * Reads a file from the target directory
     */
    fun readFile(filename: String): String? {
        return try {
            val directory = getDownloadDirectory()
            val file = File(directory, filename)
            
            if (file.exists() && file.canRead()) {
                file.readText(Charsets.UTF_8)
            } else {
                // Try via content resolver for scoped storage
                readViaContentResolver(filename)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read file: ${e.message}")
            null
        }
    }
    
    /**
     * Fallback read using ContentResolver
     */
    private fun readViaContentResolver(filename: String): String? {
        return try {
            val resolver = context.contentResolver
            val projection = arrayOf(
                android.provider.MediaStore.Downloads._ID,
                android.provider.MediaStore.Downloads.DISPLAY_NAME
            )
            val selection = "${android.provider.MediaStore.Downloads.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(filename)
            
            resolver.query(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Downloads._ID)
                    val id = cursor.getLong(idIndex)
                    val uri = android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI.buildUpon()
                        .appendPath(id.toString())
                        .build()
                    
                    resolver.openInputStream(uri)?.use { inputStream ->
                        return inputStream.bufferedReader().use { it.readText() }
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "ContentResolver read failed: ${e.message}")
            null
        }
    }
    
    /**
     * Checks if storage permission is needed at runtime
     */
    fun requiresStoragePermission(): Boolean {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> true // READ/WRITE_EXTERNAL_STORAGE
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> false // No permission needed for app-specific dirs
            USE_LEGACY_EXTERNAL_STORAGE || hasManageExternalStoragePermission() -> false
            else -> true // Would need MANAGE_EXTERNAL_STORAGE for public Downloads
        }
    }
    
    /**
     * Returns the permission string needed for runtime request
     */
    fun getRequiredPermission(): String? {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                null // No runtime permission needed for app-specific dirs
            }
            USE_LEGACY_EXTERNAL_STORAGE || hasManageExternalStoragePermission() -> {
                null
            }
            else -> {
                // Would need MANAGE_EXTERNAL_STORAGE - requires special approval
                android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
            }
        }
    }
}