package com.turjaun.cookiesuploader.ui.screens

import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.turjaun.cookiesuploader.App
import com.turjaun.cookiesuploader.data.models.Account
import com.turjaun.cookiesuploader.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SavedTab(viewModel: MainViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val jsonFilename by viewModel.jsonFilename.collectAsState()
    val currentPassword by viewModel.currentPassword.collectAsState()
    
    val context = LocalContext.current
    val secureVault = App.container.secureVault
    
    // Count duplicates for warning indicator
    val usernameCounts = remember(accounts) {
        accounts.groupingBy { it.username }.eachCount()
    }
    
    // Permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),        onResult = { isGranted ->
            if (isGranted) {
                downloadEncryptedFile(viewModel, context, secureVault, jsonFilename, currentPassword)
            } else {
                viewModel.addLog("Error", "Storage permission denied")
                Toast.makeText(context, "Permission required for download", Toast.LENGTH_SHORT).show()
            }
        }
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with count and actions
        Surface(
            color = Color(0xFF111827),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Accounts: ${accounts.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Download Button
                    IconButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                downloadEncryptedFile(viewModel, context, secureVault, jsonFilename, currentPassword)
                            } else {
                                permissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                        },
                        content = { Icon(Icons.Default.Download, contentDescription = "Download", tint = Color(0xFF22c55e)) }
                    )
                    
                    // Import Button
                    IconButton(
                        onClick = { importFromFile(viewModel, context, secureVault, jsonFilename, currentPassword) },
                        content = { Icon(Icons.Default.Upload, contentDescription = "Import", tint = Color(0xFF3b82f6)) }
                    )                    
                    // Delete All Button
                    IconButton(
                        onClick = {
                            viewModel.addLog("System", "All accounts cleared")
                            viewModel.clearAccounts()
                        },
                        content = { Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = Color.Red) }
                    )
                }
            }
        }
        
        // Accounts List
        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No saved accounts", color = Color(0xFF475569))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(accounts) { index, account ->
                    val isDuplicate = usernameCounts[account.username] ?: 0 > 1
                    val cardColor = if (isDuplicate) Color(0xFF7f1d1d) else Color(0xFF1f2937)
                    
                    AccountCard(
                        account = account,
                        isDuplicate = isDuplicate,
                        onDelete = { viewModel.removeAccount(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun AccountCard(
    account: Account,
    isDuplicate: Boolean,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isDuplicate) Color(0xFF7f1d1d) else Color(0xFF1f2937)),
        shape = RoundedCornerShape(8.dp),        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Username + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isDuplicate) {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = "Duplicate",
                            tint = Color(0xFFf97316),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = account.username,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Password
            Text(
                text = "Password: ${account.password}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94a3b8)
            )
            
            // Cookies (truncated)
            val cookiesPreview = if (account.auth_code.isEmpty()) {
                "None"            } else {
                val end = minOf(30, account.auth_code.length)
                "${account.auth_code.substring(0, end)}..."
            }
            Text(
                text = "Cookies: $cookiesPreview",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94a3b8)
            )
        }
    }
}

// --- Download Encrypted File ---
private fun downloadEncryptedFile(
    viewModel: MainViewModel,
    context: android.content.Context,
    secureVault: com.turjaun.cookiesuploader.security.SecureVault,
    filename: String,
    password: String
) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            val accounts = viewModel.accounts.value
            val json = kotlinx.serialization.json.Json.encodeToString(
                kotlinx.serialization.serializer<List<com.turjaun.cookiesuploader.data.models.Account>>(),
                accounts
            )
            val encrypted = secureVault.pack(json, password)
            val fileContent = "$password\n$encrypted"
            
            // Get Downloads directory
            val downloadsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            }
            
            val saveDir = File(downloadsDir, "insta_saver").apply { mkdirs() }
            val saveFile = File(saveDir, filename)
            saveFile.writeText(fileContent)
            
            withContext(Dispatchers.Main) {
                viewModel.addLog("System", "File saved to ${saveFile.absolutePath}")
                Toast.makeText(context, "Downloaded to ${saveFile.absolutePath}", Toast.LENGTH_LONG).show()
                
                // Optional: Trigger media scan for visibility in file managers
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    context.sendBroadcast(
                        android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)                            .setData(android.net.Uri.fromFile(saveFile))
                    )
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                viewModel.addLog("Error", "Download failed: ${e.message}")
                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// --- Import from File ---
private fun importFromFile(
    viewModel: MainViewModel,
    context: android.content.Context,
    secureVault: com.turjaun.cookiesuploader.security.SecureVault,
    filename: String,
    currentPassword: String
) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            val downloadsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            }
            
            val file = File(File(downloadsDir, "insta_saver"), filename)
            
            if (!file.exists()) {
                withContext(Dispatchers.Main) {
                    viewModel.addLog("Error", "No backup file found at ${file.absolutePath}")
                    Toast.makeText(context, "Backup file not found", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            
            val fileContent = file.readText()
            val newlineIdx = fileContent.indexOf('\n')
            
            val (filePassword, encryptedContent) = if (newlineIdx != -1) {
                fileContent.substring(0, newlineIdx).trim() to fileContent.substring(newlineIdx + 1).trim()
            } else {
                // Legacy format without password line
                currentPassword to fileContent
            }
            
            val decrypted = secureVault.unpack(encryptedContent, filePassword)            
            if (decrypted.startsWith("ERROR:")) {
                withContext(Dispatchers.Main) {
                    viewModel.addLog("Error", "Decryption Failed: Wrong password or tampered file")
                    Toast.makeText(context, "Import Failed: Wrong Password?", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            
            val accounts = kotlinx.serialization.json.Json.decodeFromString<List<com.turjaun.cookiesuploader.data.models.Account>>(decrypted)
            
            withContext(Dispatchers.Main) {
                // Replace or merge accounts - here we replace
                viewModel.clearAccounts()
                accounts.forEach { viewModel.addAccount(it) }
                viewModel.addLog("Success", "Imported ${accounts.size} accounts from secure backup")
                Toast.makeText(context, "Imported ${accounts.size} accounts", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                viewModel.addLog("Error", "Import failed: ${e.message}")
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}