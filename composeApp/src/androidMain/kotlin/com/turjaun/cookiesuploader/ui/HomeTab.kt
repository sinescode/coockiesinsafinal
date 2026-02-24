package com.turjaun.cookiesuploader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turjaun.cookiesuploader.data.model.LogEntry
import com.turjaun.cookiesuploader.ui.theme.ModernColors

@OptIn(ExperimentalMaterial3Api::class)
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

    // Sync password with generated one
    LaunchedEffect(currentPassword) {
        if (password.isEmpty() || password == "••••••••") {
            password = currentPassword
        }
    }

    // Modern gradient background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ModernColors.gradientStart, ModernColors.gradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ─────────────────────────────────────────
            // Modern Header with Status
            // ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Account Manager",
                        color = ModernColors.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Convert & upload cookies",
                        color = ModernColors.textSecondary,
                        fontSize = 13.sp
                    )
                }
                
                // Server Status Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (serverStatus) {
                        "ON" -> ModernColors.success.copy(alpha = 0.15f)
                        "OFF" -> ModernColors.error.copy(alpha = 0.15f)
                        else -> ModernColors.surfaceVariant
                    },
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = when (serverStatus) {
                                "ON" -> ModernColors.success.copy(alpha = 0.5f)
                                "OFF" -> ModernColors.error.copy(alpha = 0.5f)
                                else -> ModernColors.border
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White.copy(alpha = 0.1f)),
                            onClick = { if (!isChecking) viewModel.checkServerStatus() }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (serverStatus) {
                                        "ON" -> ModernColors.success
                                        "OFF" -> ModernColors.error
                                        else -> ModernColors.textDisabled
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            when (serverStatus) {
                                "ON" -> "Online"
                                "OFF" -> "Offline"
                                "Checking..." -> "Checking..."
                                else -> "Unknown"
                            },
                            color = when (serverStatus) {
                                "ON" -> ModernColors.success
                                "OFF" -> ModernColors.error
                                else -> ModernColors.textSecondary
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (isChecking) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = ModernColors.primary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─────────────────────────────────────────
            // Modern Input Cards
            // ─────────────────────────────────────────
            ModernInputCard(title = "Username") {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("Enter username", color = ModernColors.textDisabled) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = modernTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ModernInputCard(title = "Password") {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Generated password", color = ModernColors.textDisabled) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = modernTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = ModernColors.textSecondary
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                
                // Password Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { viewModel.copyPassword() }) {
                        Text("Copy", color = ModernColors.primary, fontSize = 12.sp)
                    }
                    TextButton(onClick = { viewModel.generatePassword() }) {
                        Text("Regenerate", color = ModernColors.info, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ModernInputCard(title = "Cookies / Auth Code") {
                OutlinedTextField(
                    value = cookies,
                    onValueChange = { cookies = it },
                    placeholder = { Text("Paste cookies or auth code", color = ModernColors.textDisabled) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = modernTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─────────────────────────────────────────
            // Modern Action Buttons
            // ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clear Button
                Button(
                    onClick = { 
                        username = ""
                        cookies = ""
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ModernColors.surfaceVariant,
                        contentColor = ModernColors.textSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear", fontWeight = FontWeight.Medium)
                }
                
                // Submit Button - Primary Action
                Button(
                    onClick = {
                        if (username.isNotBlank() && cookies.isNotBlank()) {
                            viewModel.submitAccount(username, password, cookies)
                            username = ""
                            cookies = ""
                        }
                    },
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ModernColors.success,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = username.isNotBlank() && cookies.isNotBlank()
                ) {
                    Text("Convert & Push", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─────────────────────────────────────────
            // Modern Logs Section
            // ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Activity Log",
                    color = ModernColors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (logs.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearLogs() }) {
                        Text("Clear", color = ModernColors.error, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Logs Card with smooth scrolling
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
                color = ModernColors.surface,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                tint = ModernColors.textDisabled,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No activity yet", color = ModernColors.textDisabled, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs) { log ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(200)),
                                exit = fadeOut(animationSpec = tween(200))
                            ) {
                                LogItemModern(log)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────
// Modern Reusable Components
// ─────────────────────────────────────────

@Composable
fun ModernInputCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ModernColors.surface.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.ui.Modifier.border(
            width = 1.dp,
            color = ModernColors.border,
            shape = RoundedCornerShape(16.dp)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                color = ModernColors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun LogItemModern(log: LogEntry) {
    val (statusColor, icon) = when (log.status) {
        "Error" -> ModernColors.error to "⚠️"
        "Warning" -> ModernColors.warning to "⚡"
        "Webhook" -> ModernColors.info to "🔗"
        "Success", "System" -> ModernColors.success to "✓"
        else -> ModernColors.textSecondary to "•"
    }
    
    val fontWeight = if (log.style == "bold") FontWeight.Bold else FontWeight.Normal
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ModernColors.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time badge
            Surface(
                color = ModernColors.surface,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Text(
                    log.time,
                    color = ModernColors.textSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            // Log content
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = icon,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        log.status,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    log.message,
                    color = ModernColors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = fontWeight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Modern TextField Colors
@Composable
fun modernTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = ModernColors.textPrimary,
    unfocusedTextColor = ModernColors.textPrimary,
    disabledTextColor = ModernColors.textDisabled,
    focusedContainerColor = ModernColors.surface,
    unfocusedContainerColor = ModernColors.surface,
    disabledContainerColor = ModernColors.surface.copy(alpha = 0.5f),
    focusedBorderColor = ModernColors.borderFocused,
    unfocusedBorderColor = ModernColors.border,
    disabledBorderColor = ModernColors.border.copy(alpha = 0.3f),
    cursorColor = ModernColors.primary,
    errorBorderColor = ModernColors.error,
    focusedLabelColor = ModernColors.primary,
    unfocusedLabelColor = ModernColors.textSecondary
)