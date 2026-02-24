package com.turjaun.cookiesuploader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.turjaun.cookiesuploader.viewmodel.MainViewModel

@Composable
fun HomeTab(viewModel: MainViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cookies by remember { mutableStateOf("") }
    
    val currentPassword by viewModel.currentPassword.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val clipboardManager = LocalClipboardManager.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Username Input
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username", color = Color(0xFF94a3b8)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color(0xFF0b1220),
                textColor = Color.White,
                focusedBorderColor = Color(0xFF3b82f6),                unfocusedBorderColor = Color(0xFF1f2937)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        
        // Password Input with Copy/Change buttons
        Column {
            Text("Password", color = Color(0xFF94a3b8), style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = password.ifEmpty { currentPassword },
                    onValueChange = { password = it },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFF0b1220),
                        textColor = Color.White,
                        focusedBorderColor = Color(0xFF3b82f6),
                        unfocusedBorderColor = Color(0xFF1f2937)
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                SmallButton("Copy") {
                    clipboardManager.setText(AnnotatedString(password.ifEmpty { currentPassword }))
                    viewModel.addLog("System", "Password copied successfully")
                }
                SmallButton("Change") { viewModel.generatePassword() }
            }
        }
        
        // Cookies Input
        OutlinedTextField(
            value = cookies,
            onValueChange = { cookies = it },
            label = { Text("Cookies", color = Color(0xFF94a3b8)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color(0xFF0b1220),
                textColor = Color.White,
                focusedBorderColor = Color(0xFF3b82f6),
                unfocusedBorderColor = Color(0xFF1f2937)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        
        // Action Buttons        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    username = ""
                    cookies = ""
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFf97316)
                )
            ) {
                Text("Clear Inputs")
            }
            Button(
                onClick = {
                    viewModel.submitData(username, password.ifEmpty { currentPassword }, cookies)
                    username = ""
                    cookies = ""
                },
                modifier = Modifier.weight(2f),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22c55e),
                    contentColor = Color(0xFF0b1220)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF0b1220)
                    )
                } else {
                    Text("Convert & Push")
                }
            }
        }
        
        // Logs Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Logs", style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = Color(0xFF94a3b8))
            TextButton(onClick = { viewModel.clearLogs() }) {
                Text("Clear Logs", color = Color.Red, style = MaterialTheme.typography.labelSmall)            }
        }
        
        // Logs List
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF0b1220), MaterialTheme.shapes.medium)
                .padding(8.dp)
        ) {
            if (logs.isEmpty()) {
                Text("No logs yet", color = Color(0xFF475569), style = MaterialTheme.typography.labelSmall)
            } else {
                LazyColumn {
                    items(logs.size) { index ->
                        val log = logs[index]
                        val logColor = when {
                            log.status == "Error" || log.status == "Warning" -> Color.Red
                            log.status == "Webhook" -> Color.Cyan
                            else -> Color.Green
                        }
                        val fontWeight = if (log.style == "bold") androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                        
                        ListItem(
                            headlineContent = {
                                Text(
                                    "${log.status}: ${log.message}",
                                    color = logColor,
                                    fontWeight = fontWeight,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                )
                            },
                            supportingContent = {
                                Text(log.time, color = Color(0xFF94a3b8), style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace))
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmallButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFF334155),        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}