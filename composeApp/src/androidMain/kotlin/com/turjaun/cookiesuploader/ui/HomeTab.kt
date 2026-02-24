package com.turjaun.cookiesuploader.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turjaun.cookiesuploader.data.model.LogEntry
import com.turjaun.cookiesuploader.ui.theme.DarkColorScheme
import androidx.compose.foundation.layout.width 

@Composable
fun HomeTab(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cookies by remember { mutableStateOf("") }
    
    val currentPassword by viewModel.currentPassword.collectAsState()
    val logs by viewModel.logs.collectAsState(initial = emptyList())
    val serverStatus by viewModel.serverStatus.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()

    // Update password field when generated
    password = currentPassword

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Server Status Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Account Manager",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            when (serverStatus) {
                                "ON" -> Color(0xFF22c55e).copy(alpha = 0.2f)
                                "OFF" -> Color(0xFFef4444).copy(alpha = 0.2f)
                                else -> Color.Gray.copy(alpha = 0.1f)
                        },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        serverStatus,
                        color = when (serverStatus) {
                            "ON" -> Color(0xFF4ade80)
                            "OFF" -> Color(0xFFf87171)
                            else -> Color.Gray
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp).padding(start = 8.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = { viewModel.checkServerStatus() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Username Input
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
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

        Spacer(modifier = Modifier.height(15.dp))

        // Password Input with Actions
        Column {
            Text(
                "Password",
                color = Color(0xFF94a3b8),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Row {
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = DarkColorScheme.surface,
                        unfocusedContainerColor = DarkColorScheme.surface,
                        focusedBorderColor = Color(0xFF3b82f6),
                        unfocusedBorderColor = Color(0xFF1f2937)
                    )
                )
                
                Button(
                    onClick = { viewModel.copyPassword() },
                    modifier = Modifier.padding(start = 5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Text("Copy", fontSize = 12.sp)
                }
                
                Button(
                    onClick = { viewModel.generatePassword() },
                    modifier = Modifier.padding(start = 5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Text("Change", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Cookies Input
        OutlinedTextField(
            value = cookies,
            onValueChange = { cookies = it },
            label = { Text("Cookies") },
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

        // Action Buttons
        Row {
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
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Button(
                onClick = {
                    viewModel.submitAccount(username, password, cookies)
                    username = ""
                    cookies = ""
                },
                modifier = Modifier.weight(2f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22c55e))
            ) {
                Text("Convert & Push", color = Color(0xFF0b1220))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Logs Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Logs",
                color = Color(0xFF94a3b8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { viewModel.clearLogs() }) {
                Text("Clear Logs", fontSize = 12.sp, color = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Logs List
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = DarkColorScheme.surface),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No logs yet", color = Color(0xFF475569), fontSize = 12.sp)
                }
            } else {
                LazyColumn {
                    items(logs) { log ->
                        LogItem(log)
                    }
                }
            }
        }
    }
}

@Composable
fun LogItem(log: LogEntry) {
    val color = when (log.status) {
        "Error", "Warning" -> Color(0xFFf87171)
        "Webhook" -> Color(0xFF22d3ee)
        else -> Color(0xFF4ade80)
    }
    
    val fontWeight = if (log.style == "bold") FontWeight.Bold else FontWeight.Normal
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            log.time,
            color = Color(0xFF94a3b8),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            "${log.status}: ${log.message}",
            color = color,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = fontWeight,
            modifier = Modifier.weight(1f)
        )
    }
}