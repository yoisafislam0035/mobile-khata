package com.example.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JamaaRed
import com.example.ui.theme.UdhaarGreen
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.CurrencyUtils

@Composable
fun CashAccountsScreen(viewModel: MobiKhataViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    var showReconciliationDialog by remember { mutableStateOf(false) }

    val totalBalance = remember(accounts) { accounts.sumOf { it.balance } }
    val cashAccount = accounts.find { it.type == "CASH" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Total Funds Banner
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Total Liquid Funds", color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(4.dp))
                Text(
                    CurrencyUtils.format(totalBalance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showReconciliationDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Physical Cash Reconciliation", color = Color.White)
                }
            }
        }

        Text("Accounts & Wallets (${accounts.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))

        if (accounts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No accounts created yet", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(accounts, key = { it.id }) { acc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (acc.type) {
                                        "BANK" -> Icons.Default.AccountBalance
                                        "WALLET" -> Icons.Default.Smartphone
                                        else -> Icons.Default.Payments
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(acc.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                if (acc.accountNumber.isNotBlank()) {
                                    Text("A/C: ${acc.accountNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Text(acc.type, style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                            }
                            Text(
                                CurrencyUtils.format(acc.balance),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (acc.balance >= 0) UdhaarGreen else JamaaRed
                            )
                        }
                    }
                }
            }
        }
    }

    if (showReconciliationDialog && cashAccount != null) {
        CashReconciliationDialog(
            appCashBalance = cashAccount.balance,
            onDismiss = { showReconciliationDialog = false }
        )
    }
}

@Composable
fun CashReconciliationDialog(
    appCashBalance: Double,
    onDismiss: () -> Unit
) {
    var c5000 by remember { mutableStateOf("") }
    var c1000 by remember { mutableStateOf("") }
    var c500 by remember { mutableStateOf("") }
    var c100 by remember { mutableStateOf("") }
    var c50 by remember { mutableStateOf("") }
    var c20 by remember { mutableStateOf("") }
    var c10 by remember { mutableStateOf("") }

    val totalPhysical = remember(c5000, c1000, c500, c100, c50, c20, c10) {
        val n5000 = (c5000.toIntOrNull() ?: 0) * 5000
        val n1000 = (c1000.toIntOrNull() ?: 0) * 1000
        val n500 = (c500.toIntOrNull() ?: 0) * 500
        val n100 = (c100.toIntOrNull() ?: 0) * 100
        val n50 = (c50.toIntOrNull() ?: 0) * 50
        val n20 = (c20.toIntOrNull() ?: 0) * 20
        val n10 = (c10.toIntOrNull() ?: 0) * 10
        (n5000 + n1000 + n500 + n100 + n50 + n20 + n10).toDouble()
    }

    val difference = totalPhysical - appCashBalance

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Cash Reconciliation") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("App Cash Balance: ${CurrencyUtils.format(appCashBalance)}", fontWeight = FontWeight.Bold)
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                }
                item { DenominationRow(denom = "5000", value = c5000, onValueChange = { c5000 = it }) }
                item { DenominationRow(denom = "1000", value = c1000, onValueChange = { c1000 = it }) }
                item { DenominationRow(denom = "500", value = c500, onValueChange = { c500 = it }) }
                item { DenominationRow(denom = "100", value = c100, onValueChange = { c100 = it }) }
                item { DenominationRow(denom = "50", value = c50, onValueChange = { c50 = it }) }
                item { DenominationRow(denom = "20", value = c20, onValueChange = { c20 = it }) }
                item { DenominationRow(denom = "10", value = c10, onValueChange = { c10 = it }) }
                item {
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    Text("Physical Cash Total: ${CurrencyUtils.format(totalPhysical)}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = if (difference == 0.0) "Exact Match"
                        else if (difference > 0) "Excess: + ${CurrencyUtils.format(difference)}"
                        else "Shortage: - ${CurrencyUtils.format(Math.abs(difference))}",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (difference == 0.0) UdhaarGreen else if (difference > 0) MaterialTheme.colorScheme.primary else JamaaRed
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun DenominationRow(
    denom: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Rs. $denom x", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(80.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("0") },
            singleLine = true,
            modifier = Modifier.width(90.dp)
        )
        val noteTotal = (value.toIntOrNull() ?: 0) * denom.toInt()
        Text("= Rs. $noteTotal", fontSize = 12.sp, modifier = Modifier.width(90.dp))
    }
}
