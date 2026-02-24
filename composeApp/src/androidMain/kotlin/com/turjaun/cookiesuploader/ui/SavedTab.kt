package com.turjaun.cookiesuploader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turjaun.cookiesuploader.data.model.Account
import com.turjaun.cookiesuploader.ui.theme.DarkColorScheme

@Composable
fun SavedTab(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    
    // Count duplicates
    val usernameCounts = accounts.groupingBy { it.username }.eachCount()
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkColorScheme.surface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Saved Accounts: ${accounts.size}",
                color = Color.White,
                fontSize = 16.sp
            )
            
            Row {
                IconButton(onClick = { viewModel.downloadEncryptedFile() }) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color(0xFF22c55e)
                    )
                }
                IconButton(onClick = { showDeleteAllDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete All",
                        tint = Color.Red
                    )
                }
            }
        }
        
        // Accounts List
        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No saved accounts", color = Color(0xFF475569))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                itemsIndexed(accounts) { index, account ->
                    val isDuplicate = (usernameCounts[account.username] ?: 0) > 1
                    AccountCard(
                        account = account,
                        isDuplicate = isDuplicate,
                        onDelete = { viewModel.deleteAccount(index) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
    
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            containerColor = DarkColorScheme.surface,
            title = { Text("Delete All Accounts", color = Color.White) },
            text = { Text("Are you sure you want to delete ALL saved accounts?", color = Color(0xFFe5e7eb)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAccounts()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Delete All", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel", color = Color(0xFF94a3b8))
                }
            }
        )
    }
}

@Composable
fun AccountCard(
    account: Account,
    isDuplicate: Boolean,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val cardColor = if (isDuplicate) Color(0xFF7f1d1d) else DarkColorScheme.surface
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDuplicate) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Duplicate",
                            tint = Color(0xFFfbbf24),
                            modifier = Modifier.padding(end = 5.dp)
                        )
                    }
                    Text(
                        account.username,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.padding(0.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Password: ${account.password}",
                color = Color(0xFF94a3b8),
                fontSize = 13.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            val cookieDisplay = if (account.authCode.length > 30) {
                account.authCode.substring(0, 30) + "..."
            } else {
                account.authCode.ifEmpty { "None" }
            }
            Text(
                "Cookies: $cookieDisplay",
                color = Color(0xFF94a3b8),
                fontSize = 11.sp
            )
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = DarkColorScheme.surface,
            title = { Text("Delete Account", color = Color.White) },
            text = { Text("Are you sure?", color = Color(0xFFe5e7eb)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color(0xFF94a3b8))
                }
            }
        )
    }
}