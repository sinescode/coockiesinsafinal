package com.turjaun.cookiesuploader.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.collapseVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rippleimport androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turjaun.cookiesuploader.data.model.Account
import com.turjaun.cookiesuploader.ui.theme.AppColors

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SavedTab(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    
    val usernameCounts = accounts.groupingBy { it.username }.eachCount()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(AppColors.background, AppColors.surface)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                color = AppColors.surface,
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Saved Accounts", color = AppColors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("${accounts.size} items", color = AppColors.textSecondary, fontSize = 13.sp)
                    }
                    
                    Row {
                        IconButton(
                            onClick = { viewModel.downloadEncryptedFile() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AppColors.success.copy(alpha = 0.15f))
                                .border(1.dp, AppColors.success.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = AppColors.success)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showImportDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AppColors.info.copy(alpha = 0.15f))
                                .border(1.dp, AppColors.info.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Import", tint = AppColors.info)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showDeleteAllDialog = true },
                            enabled = accounts.isNotEmpty(),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AppColors.error.copy(alpha = 0.15f))
                                .border(1.dp, AppColors.error.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete All", tint = AppColors.error)
                        }
                    }
                }
            }
            
            // Content
            AnimatedContent(
                targetState = accounts.isEmpty(),
                label = "EmptyStateToggle",
                modifier = Modifier.weight(1f)
            ) { isEmpty ->                if (isEmpty) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(modifier = Modifier.size(80.dp), color = AppColors.surfaceVariant, shape = CircleShape) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = AppColors.textSecondary, modifier = Modifier.size(40.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No saved accounts", color = AppColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Convert accounts to see them here", color = AppColors.textSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(accounts, key = { _, account -> account.username + account.password }) { index, account ->
                            val isDuplicate = (usernameCounts[account.username] ?: 0) > 1
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(300, index * 50)) + fadeIn(animationSpec = tween(300)),
                                exit = slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
                            ) {
                                AccountCardSimple(account = account, isDuplicate = isDuplicate, onDelete = { viewModel.deleteAccount(index) })
                            }
                        }
                    }
                }
            }
        }
        
        // Delete Dialog
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                containerColor = AppColors.surface,
                shape = RoundedCornerShape(20.dp),
                title = { Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AppColors.warning, modifier = Modifier.padding(end = 8.dp))
                    Text("Delete All Accounts", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
                }},
                text = { Text("This will permanently remove all ${accounts.size} saved accounts.", color = AppColors.textSecondary) },
                confirmButton = {
                    Button(onClick = { viewModel.clearAccounts(); showDeleteAllDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.error), shape = RoundedCornerShape(10.dp)) {
                        Text("Delete All", fontWeight = FontWeight.Medium)
                    }                },
                dismissButton = { TextButton(onClick = { showDeleteAllDialog = false }, shape = RoundedCornerShape(10.dp)) { Text("Cancel", color = AppColors.textSecondary) } }
            )
        }
        
        // Import Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                containerColor = AppColors.surface,
                shape = RoundedCornerShape(20.dp),
                title = { Text("Import Backup", color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("Import accounts from your encrypted backup file.", color = AppColors.textSecondary) },
                confirmButton = {
                    Button(onClick = { viewModel.importFromFile(); showImportDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary), shape = RoundedCornerShape(10.dp)) {
                        Text("Import", fontWeight = FontWeight.Medium)
                    }
                },
                dismissButton = { TextButton(onClick = { showImportDialog = false }, shape = RoundedCornerShape(10.dp)) { Text("Cancel", color = AppColors.textSecondary) } }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountCardSimple(account: Account, isDuplicate: Boolean, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    val cardBorderColor = if (isDuplicate) AppColors.warning.copy(alpha = 0.5f) else AppColors.outline
    val cardBackground = if (isDuplicate) 
        Brush.horizontalGradient(listOf(Color(0xFF7f1d1d), AppColors.surface)) 
    else 
        Brush.verticalGradient(listOf(AppColors.surface, AppColors.surfaceVariant.copy(alpha = 0.8f)))
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { expanded = !expanded },
                onLongClick = { showDeleteDialog = true },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.1f))
            )
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .border(width = 1.dp, color = cardBorderColor, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {        Column(modifier = Modifier.background(cardBackground)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDuplicate) {
                        Surface(color = AppColors.warning.copy(alpha = 0.2f), shape = CircleShape, modifier = Modifier.padding(end = 8.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = "Duplicate", tint = AppColors.warning, modifier = Modifier.padding(4.dp).size(16.dp))
                        }
                    }
                    Column {
                        Text(account.username, color = AppColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        if (isDuplicate) Text("Duplicate entry", color = AppColors.warning, fontSize = 11.sp)
                    }
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.clip(CircleShape).background(AppColors.error.copy(alpha = 0.1f))) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = AppColors.error, modifier = Modifier.size(18.dp))
                }
            }
            
            // Expandable Details - Simple conditional instead of complex animation
            if (expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).border(width = 1.dp, color = AppColors.outline.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp)).padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Password:", color = AppColors.textSecondary, fontSize = 12.sp)
                        Text(account.password, color = AppColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Auth/Cookies:", color = AppColors.textSecondary, fontSize = 12.sp)
                        Text(
                            if (account.authCode.length > 40) account.authCode.take(40) + "..." else account.authCode.ifEmpty { "None" },
                            color = AppColors.info, fontSize = 12.sp, fontFamily = FontFamily.Monospace, maxLines = 3, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            // Expand Indicator
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = AppColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    
    // Delete Confirmation    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AppColors.surface,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Delete Account", color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Remove \"${account.username}\"?", color = AppColors.textSecondary) },
            confirmButton = {
                Button(onClick = { onDelete(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.error), shape = RoundedCornerShape(10.dp)) {
                    Text("Delete", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }, shape = RoundedCornerShape(10.dp)) { Text("Cancel", color = AppColors.textSecondary) } }
        )
    }
}