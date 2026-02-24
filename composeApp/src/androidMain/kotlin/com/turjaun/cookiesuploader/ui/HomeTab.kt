package com.turjaun.cookiesuploader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardTypeimport androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turjaun.cookiesuploader.data.model.LogEntry
import com.turjaun.cookiesuploader.ui.theme.AppColors

@Composable
fun HomeTab(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cookies by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val currentPassword by viewModel.currentPassword.collectAsState()
    val logs by viewModel.logs.collectAsState(initial = emptyList())
    val serverStatus by viewModel.serverStatus.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()

    LaunchedEffect(currentPassword) {
        if (password.isEmpty()) password = currentPassword
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(AppColors.background, AppColors.surface)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Account Manager", color = AppColors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Convert & upload cookies", color = AppColors.textSecondary, fontSize = 12.sp)
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),                    color = when (serverStatus) {
                        "ON" -> AppColors.success.copy(alpha = 0.15f)
                        "OFF" -> AppColors.error.copy(alpha = 0.15f)
                        else -> AppColors.surface
                    },
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = when (serverStatus) {
                                "ON" -> AppColors.success.copy(alpha = 0.5f)
                                "OFF" -> AppColors.error.copy(alpha = 0.5f)
                                else -> AppColors.outline
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { if (!isChecking) viewModel.checkServerStatus() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when (serverStatus) {
                                        "ON" -> AppColors.success
                                        "OFF" -> AppColors.error
                                        else -> AppColors.textSecondary
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            when (serverStatus) {
                                "ON" -> "Online"
                                "OFF" -> "Offline"
                                "Checking..." -> "Checking..."
                                else -> "Unknown"
                            },
                            color = when (serverStatus) {
                                "ON" -> AppColors.success
                                "OFF" -> AppColors.error
                                else -> AppColors.textSecondary
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (isChecking) {
                            Spacer(modifier = Modifier.width(6.dp))
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = AppColors.primary, strokeWidth = 1.5.dp)                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = AppColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface,
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.outline
                ),
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Password
            Text("Password", color = AppColors.textSecondary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface,
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.outline
                ),
                shape = RoundedCornerShape(10.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = AppColors.textSecondary
                        )                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { viewModel.copyPassword() }) { Text("Copy", color = AppColors.primary, fontSize = 11.sp) }
                TextButton(onClick = { viewModel.generatePassword() }) { Text("Regenerate", color = AppColors.info, fontSize = 11.sp) }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cookies
            OutlinedTextField(
                value = cookies,
                onValueChange = { cookies = it },
                label = { Text("Cookies / Auth Code", color = AppColors.textSecondary) },
                modifier = Modifier.fillMaxWidth().height(90.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface,
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.outline
                ),
                shape = RoundedCornerShape(10.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { username = ""; cookies = "" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.surface, contentColor = AppColors.textSecondary),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Clear", fontWeight = FontWeight.Medium, fontSize = 13.sp) }
                Button(
                    onClick = {
                        if (username.isNotBlank() && cookies.isNotBlank()) {
                            viewModel.submitAccount(username, password, cookies)
                            username = ""; cookies = ""
                        }
                    },
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.success, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),                    enabled = username.isNotBlank() && cookies.isNotBlank()
                ) { Text("Convert & Push", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Activity Log", color = AppColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                if (logs.isNotEmpty()) TextButton(onClick = { viewModel.clearLogs() }) { Text("Clear", color = AppColors.error, fontSize = 11.sp) }
            }
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f),
                color = AppColors.surface,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No activity yet", color = AppColors.textSecondary, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(10.dp)) {
                        items(logs) { log -> LogItemSimple(log) }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItemSimple(log: LogEntry) {
    val statusColor = when (log.status) {
        "Error" -> AppColors.error
        "Warning" -> AppColors.warning
        "Webhook" -> AppColors.info
        "Success", "System" -> AppColors.success
        else -> AppColors.textSecondary
    }
    val fontWeight = if (log.style == "bold") FontWeight.Bold else FontWeight.Normal
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        color = AppColors.surface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(log.time, color = AppColors.textSecondary, fontSize = 9.sp, modifier = Modifier.padding(end = 8.dp))            Column(modifier = Modifier.weight(1f)) {
                Text(log.status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(log.message, color = AppColors.textPrimary, fontSize = 12.sp, fontWeight = fontWeight, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}