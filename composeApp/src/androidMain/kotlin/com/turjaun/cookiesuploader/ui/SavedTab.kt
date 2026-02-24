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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turjaun.cookiesuploader.data.model.Account
import com.turjaun.cookiesuploader.ui.theme.AppColors

@Composable
fun SavedTab(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(AppColors.background, AppColors.surface)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Saved: ${accounts.size}", color = AppColors.textPrimary, fontSize = 18.sp)
                Row {
                    IconButton(onClick = { viewModel.downloadEncryptedFile() }) { Icon(Icons.Default.Download, null, tint = AppColors.success) }
                    IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, null, tint = AppColors.error) }
                }
            }
            if (accounts.isEmpty()) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No accounts", color = AppColors.textSecondary) }
            else LazyColumn(modifier = Modifier.fillMaxSize().weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { account ->
                    Surface(modifier = Modifier.fillMaxWidth(), color = AppColors.surface, shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(account.username, color = AppColors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Text("Pass: ${account.password}", color = AppColors.textSecondary, fontSize = 12.sp)
                            Text("Auth: ${account.authCode.take(30)}${if (account.authCode.length > 30) "..." else ""}", color = AppColors.info, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        if (showDeleteDialog) AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AppColors.surface,
            title = { Text("Delete All?", color = AppColors.textPrimary) },
            text = { Text("Remove all saved accounts?", color = AppColors.textSecondary) },
            confirmButton = { Button(onClick = { viewModel.clearAccounts(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.error)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}