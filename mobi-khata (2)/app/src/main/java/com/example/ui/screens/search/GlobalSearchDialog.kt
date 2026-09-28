package com.example.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JamaaRed
import com.example.ui.theme.UdhaarGreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchDialog(
    viewModel: MobiKhataViewModel,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val ledgerEntries by viewModel.allCustomerLedger.collectAsState()

    val filteredCustomers = remember(query, customers) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim()
            val digits = query.filter { it.isDigit() }
            customers.filter { c ->
                c.name.contains(q, ignoreCase = true) ||
                c.phone.contains(q) ||
                (digits.isNotEmpty() && c.phone.filter { it.isDigit() }.contains(digits)) ||
                c.city.contains(q, ignoreCase = true) ||
                c.address.contains(q, ignoreCase = true)
            }
        }
    }

    val filteredSuppliers = remember(query, suppliers) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim()
            suppliers.filter { s ->
                s.name.contains(q, ignoreCase = true) ||
                s.phone.contains(q) ||
                s.address.contains(q, ignoreCase = true)
            }
        }
    }

    val filteredEntries = remember(query, ledgerEntries, customers) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim()
            ledgerEntries.filter { e ->
                e.description.contains(q, ignoreCase = true) ||
                e.paymentMethod.contains(q, ignoreCase = true)
            }.take(10)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search customer name, phone, city...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (query.isBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Type customer name, mobile number, or city to search",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (filteredCustomers.isEmpty() && filteredSuppliers.isEmpty() && filteredEntries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matching records found for \"$query\"", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (filteredCustomers.isNotEmpty()) {
                            item {
                                Text(
                                    "CUSTOMERS (${filteredCustomers.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(filteredCustomers, key = { "c_${it.id}" }) { c ->
                                ListItem(
                                    headlineContent = { Text(c.name, fontWeight = FontWeight.SemiBold) },
                                    supportingContent = {
                                        Text("${c.phone}${if (c.city.isNotBlank()) " • ${c.city}" else ""}")
                                    },
                                    trailingContent = {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                CurrencyUtils.format(Math.abs(c.currentBalance)),
                                                fontWeight = FontWeight.Bold,
                                                color = if (c.currentBalance > 0) JamaaRed else if (c.currentBalance < 0) UdhaarGreen else Color.Gray
                                            )
                                            Text(
                                                if (c.currentBalance > 0) "Udhaar" else if (c.currentBalance < 0) "Advance" else "Settled",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    },
                                    leadingContent = {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    },
                                    modifier = Modifier.clickable {
                                        viewModel.selectCustomer(c)
                                        onDismiss()
                                    }
                                )
                            }
                        }

                        if (filteredSuppliers.isNotEmpty()) {
                            item {
                                Text(
                                    "SUPPLIERS (${filteredSuppliers.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C3AED),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(filteredSuppliers, key = { "s_${it.id}" }) { s ->
                                ListItem(
                                    headlineContent = { Text(s.name, fontWeight = FontWeight.SemiBold) },
                                    supportingContent = { Text(s.phone) },
                                    trailingContent = {
                                        Text(
                                            CurrencyUtils.format(s.currentBalance),
                                            fontWeight = FontWeight.Bold,
                                            color = UdhaarGreen
                                        )
                                    },
                                    leadingContent = {
                                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF7C3AED))
                                    },
                                    modifier = Modifier.clickable {
                                        viewModel.selectSupplier(s)
                                        onDismiss()
                                    }
                                )
                            }
                        }

                        if (filteredEntries.isNotEmpty()) {
                            item {
                                Text(
                                    "TRANSACTION NOTES (${filteredEntries.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(filteredEntries, key = { "e_${it.id}" }) { entry ->
                                val cust = customers.find { it.id == entry.customerId }
                                ListItem(
                                    headlineContent = { Text(entry.description, fontWeight = FontWeight.SemiBold) },
                                    supportingContent = {
                                        Text("${cust?.name ?: "Customer"} • ${CurrencyUtils.formatDate(entry.date)}")
                                    },
                                    trailingContent = {
                                        Text(
                                            CurrencyUtils.format(entry.amount),
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    leadingContent = {
                                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF2563EB))
                                    },
                                    modifier = Modifier.clickable {
                                        if (cust != null) {
                                            viewModel.selectCustomer(cust)
                                            onDismiss()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
