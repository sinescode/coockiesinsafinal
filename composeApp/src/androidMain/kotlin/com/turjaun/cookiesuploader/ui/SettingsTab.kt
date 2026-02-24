package com.turjaun.cookiesuploader.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    var showSaved by remember { mutableStateOf(false) }

    LaunchedEffect(webhookUrl) { webhook = webhookUrl }
    LaunchedEffect(jsonFilename) { filename = jsonFilename }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(AppColors.background, AppColors.surface)))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Settings", color = AppColors.textPrimary, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Webhook URL", color = AppColors.textSecondary, fontSize = 12.sp)
        OutlinedTextField(value = webhook, onValueChange = { webhook = it }, modifier = Modifier.fillMaxWidth(), colors = settingsTextFieldColors(), shape = RoundedCornerShape(8.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Filename", color = AppColors.textSecondary, fontSize = 12.sp)
        OutlinedTextField(value = filename, onValueChange = { filename = it }, modifier = Modifier.fillMaxWidth(), colors = settingsTextFieldColors(), shape = RoundedCornerShape(8.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("FCM Token", color = AppColors.textSecondary, fontSize = 12.sp)
        Surface(modifier = Modifier.fillMaxWidth(), color = AppColors.surface, shape = RoundedCornerShape(8.dp)) {
            SelectionContainer {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(if (fcmToken.isNotBlank() && !fcmToken.startsWith("Error")) fcmToken else "Loading...", color = if (fcmToken.isNotBlank()) AppColors.success else AppColors.textSecondary, fontSize = 11.sp)
                    if (fcmToken.isNotBlank() && !fcmToken.startsWith("Error")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Token", fcmToken))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("Copy", color = Color.White)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { viewModel.saveSettings(webhook, filename); showSaved = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary), shape = RoundedCornerShape(8.dp)) { Text("Save Settings") }
        if (showSaved) { Text("Saved!", color = AppColors.success, modifier = Modifier.padding(top = 8.dp)); LaunchedEffect(Unit) { kotlinx.coroutines.delay(2000); showSaved = false } }
    }
}

@Composable
private fun settingsTextFieldColors() = OutlinedTextFieldDefaults.colors(    focusedTextColor = AppColors.textPrimary, unfocusedTextColor = AppColors.textPrimary,
    focusedContainerColor = AppColors.surface, unfocusedContainerColor = AppColors.surface,
    focusedBorderColor = AppColors.primary, unfocusedBorderColor = AppColors.outline
)