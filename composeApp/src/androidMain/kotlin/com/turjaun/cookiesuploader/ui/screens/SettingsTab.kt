package com.turjaun.cookiesuploader.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.turjaun.cookiesuploader.viewmodel.MainViewModel

@Composable
fun SettingsTab(viewModel: MainViewModel) {
    var webhookUrl by remember { mutableStateOf("") }
    var jsonFilename by remember { mutableStateOf("") }
    var deviceToken by remember { mutableStateOf("") }
    
    // Collect current values
    val savedWebhook by viewModel.webhookUrl.collectAsState()
    val savedFilename by viewModel.jsonFilename.collectAsState()
    val savedToken by viewModel.deviceToken.collectAsState()
    
    // Initialize with saved values
    LaunchedEffect(Unit) {
        webhookUrl = savedWebhook
        jsonFilename = savedFilename
        deviceToken = savedToken
    }
    
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)    ) {
        // Webhook URL Setting
        SettingsSection(title = "Webhook Configuration") {
            OutlinedTextField(
                value = webhookUrl,
                onValueChange = { webhookUrl = it },
                label = { Text("Webhook URL", color = Color(0xFF94a3b8)) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color(0xFF0b1220),
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3b82f6),
                    unfocusedBorderColor = Color(0xFF1f2937)
                ),
                keyboardOptions = KeyboardOptions(autoCorrect = false),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(webhookUrl))
                        Toast.makeText(context, "Webhook URL copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF94a3b8))
                    }
                }
            )
        }
        
        // JSON Filename Setting
        SettingsSection(title = "Backup Settings") {
            OutlinedTextField(
                value = jsonFilename,
                onValueChange = { jsonFilename = it },
                label = { Text("JSON Filename", color = Color(0xFF94a3b8)) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color(0xFF0b1220),
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3b82f6),
                    unfocusedBorderColor = Color(0xFF1f2937)
                ),
                keyboardOptions = KeyboardOptions(autoCorrect = false),
                singleLine = true,
                supportingText = { Text("File will be saved in /Download/insta_saver/", color = Color(0xFF64748b)) }
            )
        }
        
        // Device Token (FCM) - Read Only with Copy
        SettingsSection(title = "Firebase Cloud Messaging") {
            OutlinedTextField(
                value = deviceToken,                onValueChange = { /* Read-only */ },
                label = { Text("Device Token", color = Color(0xFF94a3b8)) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color(0xFF0b1220),
                    textColor = Color(0xFF94a3b8),
                    focusedBorderColor = Color(0xFF1f2937),
                    unfocusedBorderColor = Color(0xFF1f2937)
                ),
                readOnly = true,
                keyboardOptions = KeyboardOptions(autoCorrect = false),
                maxLines = 3,
                trailingIcon = {
                    if (deviceToken.isNotEmpty()) {
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(deviceToken))
                            Toast.makeText(context, "Device token copied", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF94a3b8))
                        }
                    }
                },
                supportingText = { 
                    Text(
                        if (deviceToken.isEmpty()) "Token will appear after app restarts..." 
                        else "Used for push notifications", 
                        color = Color(0xFF64748b)
                    ) 
                }
            )
            
            // Refresh Token Button
            Button(
                onClick = {
                    // Force token refresh
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val newToken = task.result
                                viewModel.updateDeviceToken(newToken)
                                deviceToken = newToken
                                Toast.makeText(context, "Token refreshed", Toast.LENGTH_SHORT).show()
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3b82f6),
                    contentColor = Color.White
                )            ) {
                Text("Refresh Device Token")
            }
        }
        
        // Password Section (Read-Only Display)
        SettingsSection(title = "Encryption Password") {
            var currentPassword by remember { mutableStateOf("") }
            val savedPassword by viewModel.currentPassword.collectAsState()
            
            LaunchedEffect(savedPassword) {
                currentPassword = savedPassword
            }
            
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { /* Read-only */ },
                label = { Text("Current Password", color = Color(0xFF94a3b8)) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color(0xFF0b1220),
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF1f2937),
                    unfocusedBorderColor = Color(0xFF1f2937)
                ),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(currentPassword))
                        Toast.makeText(context, "Password copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF94a3b8))
                    }
                }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.generatePassword() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFf97316)
                    )
                ) {
                    Text("Generate New")
                }
            }        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Save Settings Button
        Button(
            onClick = {
                viewModel.updateWebhookUrl(webhookUrl)
                viewModel.updateJsonFilename(jsonFilename)
                // Device token is saved automatically via ViewModel
                Toast.makeText(context, "Settings Saved", Toast.LENGTH_SHORT).show()
                viewModel.addLog("System", "Settings updated")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22c55e),
                contentColor = Color(0xFF0b1220)
            )
        ) {
            Text("Save Settings", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF94a3b8)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111827), RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )    }
}