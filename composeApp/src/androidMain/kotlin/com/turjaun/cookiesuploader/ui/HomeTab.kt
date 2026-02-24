package com.turjaun.cookiesuploader.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turjaun.cookiesuploader.data.model.LogEntryimport com.turjaun.cookiesuploader.ui.theme.AppColors

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

    LaunchedEffect(currentPassword) { if (password.isEmpty()) password = currentPassword }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(AppColors.background, AppColors.surface)))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Account Manager", color = AppColors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(serverStatus, color = if (serverStatus == "ON") AppColors.success else AppColors.error, fontSize = 12.sp)
                if (isChecking) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AppColors.primary)
                else IconButton(onClick = { viewModel.checkServerStatus() }) { Icon(Icons.Default.Refresh, null, tint = AppColors.textPrimary) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Inputs
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors(), shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors(), shape = RoundedCornerShape(8.dp), visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { viewModel.copyPassword() }) { Text("Copy", color = AppColors.primary) }
                TextButton(onClick = { viewModel.generatePassword() }) { Text("Change", color = AppColors.info) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = cookies, onValueChange = { cookies = it }, label = { Text("Cookies") }, modifier = Modifier.fillMaxWidth().height(80.dp), colors = textFieldColors(), shape = RoundedCornerShape(8.dp), maxLines = 3)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { username = ""; cookies = "" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.surface), shape = RoundedCornerShape(8.dp)) { Text("Clear") }
                Button(onClick = { if (username.isNotBlank() && cookies.isNotBlank()) { viewModel.submitAccount(username, password, cookies); username = ""; cookies = "" } }, modifier = Modifier.weight(2f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.success), shape = RoundedCornerShape(8.dp)) { Text("Convert & Push") }
            }
            Spacer(modifier = Modifier.height(16.dp))
                        // Logs
            Text("Logs", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(modifier = Modifier.fillMaxWidth().weight(1f), color = AppColors.surface, shape = RoundedCornerShape(8.dp)) {
                if (logs.isEmpty()) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No logs", color = AppColors.textSecondary) }
                else LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) { items(logs) { log -> Text("${log.time} ${log.status}: ${log.message}", color = if (log.status == "Error") AppColors.error else AppColors.textPrimary, modifier = Modifier.padding(vertical = 4.dp)) } }
            }
        }
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppColors.textPrimary, unfocusedTextColor = AppColors.textPrimary,
    focusedContainerColor = AppColors.surface, unfocusedContainerColor = AppColors.surface,
    focusedBorderColor = AppColors.primary, unfocusedBorderColor = AppColors.outline
)