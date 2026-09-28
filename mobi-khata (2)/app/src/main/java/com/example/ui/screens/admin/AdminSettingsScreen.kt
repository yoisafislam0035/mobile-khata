package com.example.ui.screens.admin

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sync.SyncStatus
import com.example.ui.screens.sync.InternetSyncDialog
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JamaaRed
import com.example.ui.theme.PRESET_ACCENT_COLORS
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.UdhaarGreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.AppLanguage
import com.example.util.Localization
import kotlinx.coroutines.launch

@Composable
fun AdminSettingsScreen(viewModel: MobiKhataViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lang by viewModel.language.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val customColor by viewModel.customPrimaryColor.collectAsState()

    val business by viewModel.currentBusiness.collectAsState()
    val businesses by viewModel.businesses.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val pendingCount by viewModel.pendingSyncCount.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var showBusinessSwitcher by remember { mutableStateOf(false) }
    var showCreateBusinessDialog by remember { mutableStateOf(false) }
    var showAuditDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Language Selection Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = Localization.get("select_language", lang),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val languageOptions = listOf(
                            AppLanguage.ENGLISH to "English",
                            AppLanguage.ROMAN_URDU to "Roman Urdu",
                            AppLanguage.URDU to "اردو"
                        )
                        languageOptions.forEach { (languageItem, labelText) ->
                            FilterChip(
                                selected = lang == languageItem,
                                onClick = { viewModel.setLanguage(languageItem) },
                                label = {
                                    Text(
                                        labelText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Appearance & Quick Theme Switcher Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Theme & Custom Colors",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    // Theme Mode Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            ThemeMode.LIGHT to Localization.get("light_theme", lang),
                            ThemeMode.DARK to Localization.get("dark_theme", lang),
                            ThemeMode.CUSTOM to Localization.get("custom_theme", lang)
                        ).forEach { (mode, label) ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = Localization.get("choose_accent_color", lang),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PRESET_ACCENT_COLORS) { preset ->
                            val isChosen = (customColor == preset.color)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.setCustomPrimaryColor(preset.color)
                                        viewModel.setThemeMode(ThemeMode.CUSTOM)
                                    }
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(preset.color)
                                        .border(
                                            width = if (isChosen) 3.dp else 1.dp,
                                            color = if (isChosen) MaterialTheme.colorScheme.onSurface else Color.LightGray,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(preset.name.substringBefore(" "), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // User Account Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = when (user?.authProvider) {
                                    "GOOGLE" -> Color(0xFF4285F4)
                                    "PHONE" -> UdhaarGreen
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (user?.authProvider) {
                                            "GOOGLE" -> Icons.Default.AccountCircle
                                            "PHONE" -> Icons.Default.PhoneAndroid
                                            else -> Icons.Default.Email
                                        },
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    user?.name ?: "Store Owner",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    user?.email ?: "",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                                if (!user?.phone.isNullOrBlank()) {
                                    Text(
                                        user?.phone ?: "",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (user?.authProvider) {
                                "GOOGLE" -> Color(0xFFEFF6FF)
                                "PHONE" -> Color(0xFFECFDF5)
                                else -> Color(0xFFF3F4F6)
                            }
                        ) {
                            Text(
                                text = when (user?.authProvider) {
                                    "GOOGLE" -> "Google Verified"
                                    "PHONE" -> "Phone Verified"
                                    else -> "Email Verified"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (user?.authProvider) {
                                    "GOOGLE" -> Color(0xFF2563EB)
                                    "PHONE" -> UdhaarGreen
                                    else -> Color.DarkGray
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UID: ${user?.id?.take(16)}...",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = { viewModel.signOut() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JamaaRed),
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(Localization.get("sign_out", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Business Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                business?.name ?: Localization.get("app_name", lang),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "${business?.category ?: "General"} • ${business?.city ?: "Lahore"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                            Text(
                                "${Localization.get("owner_name", lang)}: ${business?.ownerName ?: "Owner"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                        Button(
                            onClick = { showBusinessSwitcher = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(Localization.get("switch_shop", lang))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.BUSINESS_PROFILE) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(Localization.get("my_business_profile", lang), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Cloud & Sync Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Localization.get("cloud_sync_section", lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOnline) Color(0xFFF0FDF4) else Color(0xFFF3F4F6)
                        ) {
                            Text(
                                if (isOnline) "• ${Localization.get("online", lang)}" else "• ${Localization.get("offline", lang)}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) UdhaarGreen else Color.DarkGray
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${Localization.get("sync_status_label", lang)}: ${when (syncStatus) {
                            SyncStatus.SYNCED -> Localization.get("sync_all_backed_up", lang)
                            SyncStatus.SYNCING -> Localization.get("sync_syncing", lang)
                            SyncStatus.OFFLINE -> Localization.get("sync_offline_mode", lang)
                        }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    if (pendingCount > 0) {
                        Text(
                            "$pendingCount changes pending sync",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD97706),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showSyncDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(Localization.get("sync_center", lang), fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.syncNow() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(Localization.get("sync_now", lang), fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Audit Trail
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${Localization.get("audit_trail", lang)} (${auditLogs.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(Localization.get("audit_trail_sub", lang), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showAuditDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(Localization.get("view_audit_log", lang))
                    }
                }
            }
        }

        // Backup & JSON Export
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(Localization.get("backup_export", lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val bizId = business?.id ?: return@OutlinedButton
                            coroutineScope.launch {
                                val json = viewModel.repository.exportBusinessDataAsJson(bizId)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, json)
                                    type = "application/json"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export JSON Backup"))
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(Localization.get("export_backup_json", lang))
                    }
                }
            }
        }

        // Sign Out Button
        item {
            Button(
                onClick = { viewModel.signOut() },
                colors = ButtonDefaults.buttonColors(containerColor = JamaaRed),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(Localization.get("sign_out", lang), fontWeight = FontWeight.Bold)
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }

    if (showBusinessSwitcher) {
        AlertDialog(
            onDismissRequest = { showBusinessSwitcher = false },
            title = { Text(Localization.get("switch_shop", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    businesses.forEach { b ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchBusiness(b)
                                    showBusinessSwitcher = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (b.id == business?.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(b.name, fontWeight = FontWeight.Bold)
                                Text("${b.city} • ${b.category}", fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showBusinessSwitcher = false
                            showCreateBusinessDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(Localization.get("your_business", lang))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBusinessSwitcher = false }) {
                    Text(Localization.get("cancel", lang))
                }
            }
        )
    }

    if (showSyncDialog) {
        InternetSyncDialog(
            viewModel = viewModel,
            onDismiss = { showSyncDialog = false }
        )
    }

    if (showAuditDialog) {
        AlertDialog(
            onDismissRequest = { showAuditDialog = false },
            title = { Text(Localization.get("audit_trail", lang)) },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(auditLogs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(log.details, fontSize = 11.sp)
                                Text("${log.userName} • ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))}", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAuditDialog = false }) {
                    Text(Localization.get("cancel", lang))
                }
            }
        )
    }
}
