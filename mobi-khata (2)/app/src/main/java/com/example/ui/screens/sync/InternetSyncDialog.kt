package com.example.ui.screens.sync

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.sync.SyncStatus
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JamaaRed
import com.example.ui.theme.UdhaarGreen
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.CurrencyUtils
import kotlinx.coroutines.launch

@Composable
fun InternetSyncDialog(
    viewModel: MobiKhataViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val networkType by viewModel.networkType.collectAsState()
    val pendingCount by viewModel.pendingSyncCount.collectAsState()
    val lastSyncTime by viewModel.lastSyncTimestamp.collectAsState()
    val autoSyncEnabled by viewModel.autoSyncEnabled.collectAsState()
    val syncHistory by viewModel.syncHistory.collectAsState()
    val currentBusiness by viewModel.currentBusiness.collectAsState()

    var isManualSyncing by remember { mutableStateOf(false) }
    var syncFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isOnline) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isOnline) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Internet Sync & Cloud",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Firebase & Room Database",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Live Status Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOnline) Color(0xFFF0FDF4) else Color(0xFFF3F4F6)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(
                                                    if (isOnline) UdhaarGreen else Color.Gray,
                                                    CircleShape
                                                )
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (isOnline) "Internet Connected" else "Device Offline",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOnline) UdhaarGreen else Color.DarkGray,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isOnline) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = networkType,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isOnline) MaterialTheme.colorScheme.primary else Color.DarkGray
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Pending Changes", fontSize = 12.sp, color = Color.Gray)
                                        Text(
                                            if (pendingCount == 0) "0 (All Synced)" else "$pendingCount pending upload",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (pendingCount > 0) Color(0xFFD97706) else UdhaarGreen
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Last Synced", fontSize = 12.sp, color = Color.Gray)
                                        Text(
                                            CurrencyUtils.formatDate(lastSyncTime),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Sync Now Button
                    item {
                        Button(
                            onClick = {
                                isManualSyncing = true
                                syncFeedbackMessage = null
                                viewModel.syncNow { success, message ->
                                    isManualSyncing = false
                                    syncFeedbackMessage = message
                                }
                            },
                            enabled = isOnline && !isManualSyncing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .then(if (isManualSyncing || syncStatus == SyncStatus.SYNCING) Modifier.rotate(rotation) else Modifier)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isManualSyncing || syncStatus == SyncStatus.SYNCING) "Synchronizing with Cloud..." else "Sync Now with Cloud",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        if (syncFeedbackMessage != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = syncFeedbackMessage ?: "",
                                fontSize = 12.sp,
                                color = if (syncFeedbackMessage?.contains("complete", true) == true) UdhaarGreen else JamaaRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Auto-sync Toggle Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Automatic Background Sync", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "Sync newly created khata entries as soon as internet is available",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Switch(
                                    checked = autoSyncEnabled,
                                    onCheckedChange = { viewModel.setAutoSync(it) }
                                )
                            }
                        }
                    }

                    // Cloud Backup & Restore Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Cloud Backup & Restore", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Create offline snapshot of shop database or restore past backup", fontSize = 11.sp, color = Color.Gray)
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val bizId = currentBusiness?.id ?: return@OutlinedButton
                                            coroutineScope.launch {
                                                val json = viewModel.repository.exportBusinessDataAsJson(bizId)
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, json)
                                                    type = "application/json"
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "Share Cloud Backup (JSON)"))
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Export Backup", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { showRestoreDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Restore Data", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Sync History
                    item {
                        Text("Recent Sync History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    items(syncHistory) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (log.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (log.isSuccess) UdhaarGreen else JamaaRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(log.description, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text(CurrencyUtils.formatDate(log.timestamp), fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        "${log.recordCount} items",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRestoreDialog) {
        var jsonInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Business Data from JSON") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste JSON backup payload to restore customers and inventory into this shop:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text("{\n  \"customers\": [...],\n  \"products\": [...]\n}") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (jsonInput.isNotBlank()) {
                            viewModel.restoreFromCloudJson(jsonInput) { success ->
                                if (success) {
                                    Toast.makeText(context, "Data successfully restored!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to restore data. Check JSON format.", Toast.LENGTH_LONG).show()
                                }
                                showRestoreDialog = false
                            }
                        }
                    },
                    enabled = jsonInput.isNotBlank()
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text("Cancel") }
            }
        )
    }
}
