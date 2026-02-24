package com.turjaun.cookiesuploader.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turjaun.cookiesuploader.ui.theme.DarkColorScheme

@Composable
fun SettingsTab(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val webhookUrl by viewModel.webhookUrl.collectAsState()
    val jsonFilename by viewModel.jsonFilename.collectAsState()
    val fcmToken by viewModel.fcmToken.collectAsState()
    
    var webhook by remember { mutableStateOf(webhookUrl) }
    var filename by remember { mutableStateOf(jsonFilename) }
    var showSavedMessage by remember { mutableStateOf(false) }
    
    // Update local state when flow changes
    LaunchedEffect(webhookUrl) { webhook = webhookUrl }
    LaunchedEffect(jsonFilename) { filename = jsonFilename }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Webhook URL Field
        OutlinedTextField(
            value = webhook,
            onValueChange = { webhook = it },
            label = { Text("Webhook URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = DarkColorScheme.surface,
                unfocusedContainerColor = DarkColorScheme.surface,
                focusedBorderColor = Color(0xFF3b82f6),
                unfocusedBorderColor = Color(0xFF1f2937)
            )
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // JSON Filename Field
        OutlinedTextField(
            value = filename,
            onValueChange = { filename = it },
            label = { Text("JSON Filename") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = DarkColorScheme.surface,
                unfocusedContainerColor = DarkColorScheme.surface,
                focusedBorderColor = Color(0xFF3b82f6),
                unfocusedBorderColor = Color(0xFF1f2937)
            )
        )
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // FCM Token Display (Read-only with Copy)
        Text(
            "FCM Token",
            color = Color(0xFF94a3b8),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display token in a read-only text field style container
            SelectionContainer(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = fcmToken.ifEmpty { "Generating token..." },
                    color = if (fcmToken.isEmpty()) Color(0xFF64748b) else Color(0xFF22c55e),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }
            
            // Copy Button
            Button(
                onClick = {
                    if (fcmToken.isNotEmpty()) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("FCM Token", fcmToken)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Token copied!", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = fcmToken.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3b82f6),
                    disabledContainerColor = Color(0xFF1e293b)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Copy", color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // Save Settings Button
        OutlinedButton(
            onClick = {
                viewModel.saveSettings(webhook, filename)
                showSavedMessage = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFf97316)
            )
        ) {
            Text("Save Settings")
        }
        
        if (showSavedMessage) {
            Text(
                "Settings Saved",
                color = Color(0xFF22c55e),
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}