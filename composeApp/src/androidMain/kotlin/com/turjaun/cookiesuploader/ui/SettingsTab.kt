package com.turjaun.cookiesuploader.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turjaun.cookiesuploader.ui.theme.ModernColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val webhookUrl by viewModel.webhookUrl.collectAsState()
    val jsonFilename by viewModel.jsonFilename.collectAsState()
    val fcmToken by viewModel.fcmToken.collectAsState()
    
    var webhook by remember { mutableStateOf(webhookUrl) }
    var filename by remember { mutableStateOf(jsonFilename) }
    var showSavedMessage by remember { mutableStateOf(false) }
    var tokenVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(webhookUrl) { webhook = webhookUrl }
    LaunchedEffect(jsonFilename) { filename = jsonFilename }
    
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            Text(
                "Settings",
                color = ModernColors.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                "Configure your upload preferences",
                color = ModernColors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // ─────────────────────────────────────────
            // Webhook URL Card
            // ─────────────────────────────────────────
            SettingsCard(title = "Webhook Configuration", icon = "🔗") {
                Text(
                    "Endpoint URL for account submissions",
                    color = ModernColors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                OutlinedTextField(
                    value = webhook,
                    onValueChange = { webhook = it },
                    label = { Text("Webhook URL", color = ModernColors.textSecondary) },
                    placeholder = { Text("https://your-server.com/webhook", color = ModernColors.textDisabled) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = modernTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ─────────────────────────────────────────
            // Filename Card
            // ─────────────────────────────────────────
            SettingsCard(title = "Backup Settings", icon = "💾") {
                Text(
                    "Filename for encrypted backups",
                    color = ModernColors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                OutlinedTextField(
                    value = filename,
                    onValueChange = { filename = it },
                    label = { Text("JSON Filename", color = ModernColors.textSecondary) },
                    placeholder = { Text("accounts_backup.json", color = ModernColors.textDisabled) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = modernTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Text(
                    "Saved to: /Download/insta_saver/",
                    color = ModernColors.info,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ─────────────────────────────────────────
            // FCM Token Card
            // ─────────────────────────────────────────
            SettingsCard(title = "Push Notifications", icon = "🔔", isExpanded = true) {
                Text(
                    "Firebase Cloud Messaging token for remote commands",
                    color = ModernColors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Token Display Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = ModernColors.border,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    color = ModernColors.surface,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    SelectionContainer {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "FCM Token",
                                    color = ModernColors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                // Visibility Toggle
                                IconButton(onClick = { tokenVisible = !tokenVisible }) {
                                    Icon(
                                        imageVector = if (tokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (tokenVisible) "Hide token" else "Show token",
                                        tint = ModernColors.textSecondary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Token Text
                            val displayToken = when {
                                fcmToken.isBlank() || fcmToken.startsWith("Error") -> "Waiting for token..."
                                tokenVisible -> fcmToken
                                else -> fcmToken.take(20) + "..." + fcmToken.takeLast(8)
                            }
                            
                            Text(
                                text = displayToken,
                                color = if (fcmToken.isNotBlank() && !fcmToken.startsWith("Error")) 
                                    ModernColors.success 
                                else 
                                    ModernColors.textDisabled,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = if (tokenVisible) 4 else 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            // Copy Button
                            if (fcmToken.isNotBlank() && !fcmToken.startsWith("Error")) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("FCM Token", fcmToken)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Token copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ModernColors.primary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Token", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                
                // Refresh Token Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { viewModel.refreshFcmToken() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ModernColors.info
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh Token", fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // ─────────────────────────────────────────
            // Save Button
            // ─────────────────────────────────────────
            Button(
                onClick = {
                    viewModel.saveSettings(webhook, filename)
                    showSavedMessage = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ModernColors.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            
            // Success Message
            AnimatedVisibility(
                visible = showSavedMessage,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = ModernColors.success.copy(alpha = 0.15f),
                    border = androidx.compose.ui.Modifier.border(
                        width = 1.dp,
                        color = ModernColors.success.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Success",
                            tint = ModernColors.success,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "Settings saved successfully",
                            color = ModernColors.success,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                // Auto-hide after 2.5 seconds
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2500)
                    showSavedMessage = false
                }
            }
            
            // Bottom spacer for scroll
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Reusable Settings Card Component
@Composable
fun SettingsCard(
    title: String,
    icon: String,
    isExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ModernColors.surface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.ui.Modifier.border(
            width = 1.dp,
            color = ModernColors.border,
            shape = RoundedCornerShape(16.dp)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Card Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = if (isExpanded) 16.dp else 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ModernColors.primary.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = ModernColors.primary.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    title,
                    color = ModernColors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            if (isExpanded) {
                content()
            } else {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    content()
                }
            }
        }
    }
}