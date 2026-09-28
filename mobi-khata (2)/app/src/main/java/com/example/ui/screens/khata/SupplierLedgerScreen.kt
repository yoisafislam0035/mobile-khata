package com.example.ui.screens.khata

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.SupplierLedgerEntry
import com.example.ui.components.MobiKhataAmountInputField
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JamaaRed
import com.example.ui.theme.UdhaarGreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.CurrencyUtils
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierLedgerScreen(
    viewModel: MobiKhataViewModel
) {
    val context = LocalContext.current
    val lang by viewModel.language.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddSupplierDialog by remember { mutableStateOf(false) }

    val filteredSuppliers = remember(suppliers, searchQuery) {
        suppliers.filter { s ->
            s.name.contains(searchQuery, ignoreCase = true) || s.phone.contains(searchQuery)
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSupplierDialog = true },
                icon = { Icon(Icons.Default.AddBusiness, contentDescription = null) },
                text = { Text(Localization.get("add_supplier", lang)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search suppliers by name or phone...") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredSuppliers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(54.dp), tint = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text("No suppliers found", color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { showAddSupplierDialog = true }) {
                            Text("Add First Supplier")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSuppliers, key = { it.id }) { supplier ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectSupplier(supplier) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color(0xFFE0E7FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF4338CA))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(supplier.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                                    Text(supplier.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val isPayable = supplier.currentBalance > 0
                                    Text(
                                        CurrencyUtils.format(supplier.currentBalance),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isPayable) JamaaRed else UdhaarGreen
                                    )
                                    Text(
                                        if (isPayable) "You'll Give" else "Settled",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isPayable) JamaaRed else UdhaarGreen
                                    )
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${supplier.phone}"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddSupplierDialog) {
        AddSupplierDialog(
            onDismiss = { showAddSupplierDialog = false },
            onAdd = { name, phone, bal, addr ->
                viewModel.addSupplier(name, phone, bal, addr)
                showAddSupplierDialog = false
            }
        )
    }
}

@Composable
fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, balance: Double, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Supplier") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Supplier / Company Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile / Phone *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                MobiKhataAmountInputField(
                    value = openingBalance,
                    onValueChange = { openingBalance = it },
                    label = "Opening Payable Balance (Rs.)"
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / City (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val amount = openingBalance.toDoubleOrNull() ?: 0.0
                        onAdd(name.trim(), phone.trim(), amount, address.trim())
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text("Save Supplier")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierDetailScreen(viewModel: MobiKhataViewModel) {
    val lang by viewModel.language.collectAsState()
    val supplier by viewModel.selectedSupplier.collectAsState()
    val ledger by viewModel.supplierLedger.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var isPaidPayment by remember { mutableStateOf(true) }

    BackHandler {
        viewModel.navigateTo(AppScreen.SUPPLIERS)
    }

    if (supplier == null) return
    val currentSupplier = supplier!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentSupplier.name, fontWeight = FontWeight.Bold)
                        Text(currentSupplier.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.SUPPLIERS) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            isPaidPayment = false
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JamaaRed),
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Purchase Bill (+)")
                    }
                    Button(
                        onClick = {
                            isPaidPayment = true
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UdhaarGreen),
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Pay Supplier (-)")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Total Payable to Supplier", color = JamaaRed, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        CurrencyUtils.format(currentSupplier.currentBalance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = JamaaRed
                    )
                }
            }

            Text("Ledger History (${ledger.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (ledger.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions yet for this supplier", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ledger, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        if (item.type == "PURCHASE") "Stock Purchase" else "Payment Paid",
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.type == "PURCHASE") JamaaRed else UdhaarGreen
                                    )
                                    Text(item.description, style = MaterialTheme.typography.bodySmall)
                                    Text(CurrencyUtils.formatDate(item.date), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        CurrencyUtils.format(item.amount),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("Bal: ${CurrencyUtils.format(item.balanceAfter)}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        SupplierEntryDialog(
            isPaidPayment = isPaidPayment,
            onDismiss = { showDialog = false },
            onSave = { amount, desc ->
                viewModel.recordSupplierEntry(currentSupplier.id, isPaidPayment, amount, desc)
                showDialog = false
            }
        )
    }
}

@Composable
fun SupplierEntryDialog(
    isPaidPayment: Boolean,
    onDismiss: () -> Unit,
    onSave: (amount: Double, desc: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isPaidPayment) "Pay Supplier (-)" else "Add Purchase Bill (+)")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MobiKhataAmountInputField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Amount (Rs.) *"
                )
                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    label = { Text("Details / Bill Reference") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        onSave(amt, descText.trim())
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaidPayment) UdhaarGreen else JamaaRed
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
