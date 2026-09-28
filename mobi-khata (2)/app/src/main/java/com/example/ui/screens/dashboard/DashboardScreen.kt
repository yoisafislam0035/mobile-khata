package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.CustomerLedgerEntry
import com.example.sync.SyncStatus
import com.example.ui.screens.sync.InternetSyncDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.CurrencyUtils
import com.example.util.Localization

@Composable
fun DashboardScreen(
    viewModel: MobiKhataViewModel,
    onOpenSearch: () -> Unit,
    onOpenQuickEntry: (Boolean) -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val business by viewModel.currentBusiness.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val allCustomerLedger by viewModel.allCustomerLedger.collectAsState()
    var showSyncDialog by remember { mutableStateOf(false) }

    val totalReceivables = remember(customers) {
        customers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
    }
    val totalPayables = remember(suppliers) {
        suppliers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
    }
    val cashBalance = remember(accounts) {
        accounts.find { it.type == "CASH" }?.balance ?: accounts.sumOf { it.balance }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Profile Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable { viewModel.navigateTo(AppScreen.BUSINESS_PROFILE) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        val logo = business?.logoUrl ?: ""
                        if (logo.isNotBlank()) {
                            AsyncImage(
                                model = logo,
                                contentDescription = business?.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = business?.name?.take(1)?.uppercase() ?: "M",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = business?.name ?: Localization.get("app_name", lang),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = Localization.get("edit", lang),
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = "${business?.ownerName ?: ""} • ${business?.city ?: ""}".trim().trim('•').trim(),
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    // Sync Pill
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier.clickable { showSyncDialog = true },
                        color = when (syncStatus) {
                            SyncStatus.SYNCED -> Color(0xFF10B981)
                            SyncStatus.SYNCING -> Color(0xFFF59E0B)
                            SyncStatus.OFFLINE -> Color(0xFF6B7280)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (syncStatus) {
                                    SyncStatus.SYNCED -> Icons.Default.CloudDone
                                    SyncStatus.SYNCING -> Icons.Default.Sync
                                    SyncStatus.OFFLINE -> Icons.Default.CloudOff
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = when (syncStatus) {
                                    SyncStatus.SYNCED -> Localization.get("cloud_synced", lang)
                                    SyncStatus.SYNCING -> Localization.get("syncing", lang)
                                    SyncStatus.OFFLINE -> Localization.get("offline", lang)
                                },
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Khata Main KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Receivables (Lene Hain)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(AppScreen.CUSTOMERS) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = JamaaRedBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JamaaRed.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(JamaaRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowOutward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                Localization.get("total_receivables", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = JamaaRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            CurrencyUtils.format(totalReceivables),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = JamaaRed
                        )
                        Text(
                            "${customers.count { it.currentBalance > 0 }} ${Localization.get("customers_have_udhaar", lang)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }

                // Total Payables (Dene Hain)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(AppScreen.SUPPLIERS) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = UdhaarGreenBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, UdhaarGreen.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(UdhaarGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CallReceived,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                Localization.get("total_payables", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = UdhaarGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            CurrencyUtils.format(totalPayables),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = UdhaarGreen
                        )
                        Text(
                            "${suppliers.count { it.currentBalance > 0 }} ${Localization.get("suppliers_to_pay", lang)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // Secondary Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(AppScreen.ACCOUNTS) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                Localization.get("cash_balance", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                CurrencyUtils.format(cashBalance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(AppScreen.CUSTOMERS) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                Localization.get("total_accounts", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                "${customers.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Grid
        item {
            Text(
                Localization.get("quick_actions", lang),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Default.PersonAdd,
                    label = Localization.get("quick_new_customer", lang),
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) }
                )
                QuickActionButton(
                    icon = Icons.Default.ArrowOutward,
                    label = Localization.get("you_gave_udhaar", lang),
                    color = JamaaRed,
                    onClick = { onOpenQuickEntry(true) }
                )
                QuickActionButton(
                    icon = Icons.Default.CallReceived,
                    label = Localization.get("you_got_payment", lang),
                    color = UdhaarGreen,
                    onClick = { onOpenQuickEntry(false) }
                )
                QuickActionButton(
                    icon = Icons.Default.People,
                    label = Localization.get("nav_customers", lang),
                    color = Color(0xFF2563EB),
                    onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Default.LocalShipping,
                    label = Localization.get("nav_suppliers", lang),
                    color = Color(0xFF7C3AED),
                    onClick = { viewModel.navigateTo(AppScreen.SUPPLIERS) }
                )
                QuickActionButton(
                    icon = Icons.Default.MenuBook,
                    label = Localization.get("nav_daybook", lang),
                    color = Color(0xFFD97706),
                    onClick = { viewModel.navigateTo(AppScreen.DAYBOOK) }
                )
                QuickActionButton(
                    icon = Icons.Default.ReceiptLong,
                    label = Localization.get("expenses", lang),
                    color = Color(0xFFDC2626),
                    onClick = { viewModel.navigateTo(AppScreen.EXPENSES) }
                )
                QuickActionButton(
                    icon = Icons.Default.Assessment,
                    label = Localization.get("nav_reports", lang),
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.navigateTo(AppScreen.REPORTS) }
                )
            }
        }

        // Recent Customer Transactions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    Localization.get("recent_transactions", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) }) {
                    Text(Localization.get("view_customers", lang))
                }
            }
        }

        if (allCustomerLedger.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(Localization.get("no_transactions_yet", lang), color = Color.Gray, fontWeight = FontWeight.Medium)
                            Text(Localization.get("no_transactions_sub", lang), fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        } else {
            val recentEntries = allCustomerLedger.take(7)
            items(recentEntries, key = { it.id }) { entry ->
                val customer = customers.find { it.id == entry.customerId }
                DashboardLedgerRow(
                    entry = entry,
                    customerName = customer?.name ?: "Customer",
                    customerPhone = customer?.phone ?: "",
                    profileImageUrl = customer?.profileImageUrl ?: "",
                    language = lang,
                    onClick = {
                        if (customer != null) {
                            viewModel.selectCustomer(customer)
                        }
                    }
                )
            }
        }

        item {
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showSyncDialog) {
        InternetSyncDialog(
            viewModel = viewModel,
            onDismiss = { showSyncDialog = false }
        )
    }
}

@Composable
fun DashboardLedgerRow(
    entry: CustomerLedgerEntry,
    customerName: String,
    customerPhone: String,
    profileImageUrl: String,
    language: com.example.util.AppLanguage,
    onClick: () -> Unit
) {
    val isGave = entry.type == "GAVE_UDHAAR" || entry.type == "SALE"
    val color = if (isGave) JamaaRed else UdhaarGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
                color = color.copy(alpha = 0.12f)
            ) {
                if (profileImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = customerName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = customerName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = color,
                            fontSize = 17.sp
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (entry.description.isNotBlank()) {
                    Text(
                        entry.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        maxLines = 1
                    )
                }
                Text(
                    text = "${CurrencyUtils.formatDate(entry.date)} • ${if (isGave) Localization.get("you_gave_udhaar", language) else Localization.get("you_got_payment", language)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isGave) "- " else "+ ") + CurrencyUtils.format(entry.amount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = color
                )
                Text(
                    text = "${Localization.get("running_balance", language)}: ${CurrencyUtils.format(entry.balanceAfter)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(color.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(25.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}
