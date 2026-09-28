package com.example.ui.screens.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.MobiKhataAmountInputField
import com.example.ui.theme.JamaaRed
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.CurrencyUtils
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: MobiKhataViewModel) {
    val lang by viewModel.language.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(Localization.get("add_expense", lang)) },
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
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Expenses", color = JamaaRed, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        CurrencyUtils.format(totalExpenses),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = JamaaRed
                    )
                }
            }

            if (expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(50.dp), tint = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text("No expenses recorded", color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Text("Record First Expense")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(expenses, key = { it.id }) { expense ->
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
                                    Text(expense.category, fontWeight = FontWeight.Bold)
                                    if (expense.description.isNotBlank()) {
                                        Text(expense.description, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(CurrencyUtils.formatDate(expense.date), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                Text(
                                    "- ${CurrencyUtils.format(expense.amount)}",
                                    color = JamaaRed,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        val categories = listOf("Rent", "Electricity", "Salaries", "Transport", "Repairs", "Marketing", "Utilities", "Other")
        var selectedCat by remember { mutableStateOf(categories.first()) }
        var amountText by remember { mutableStateOf("") }
        var descText by remember { mutableStateOf("") }
        var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Record Business Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Category:", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ScrollableTabRow(
                            selectedTabIndex = categories.indexOf(selectedCat),
                            edgePadding = 0.dp
                        ) {
                            categories.forEach { cat ->
                                Tab(
                                    selected = selectedCat == cat,
                                    onClick = { selectedCat = cat },
                                    text = { Text(cat) }
                                )
                            }
                        }
                    }

                    // In-App Calculator Amount Field
                    MobiKhataAmountInputField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = "Amount (Rs.) *"
                    )

                    OutlinedTextField(
                        value = descText,
                        onValueChange = { descText = it },
                        label = { Text("Description / Details") },
                        placeholder = { Text("e.g. Shop electricity bill LESCO") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            viewModel.addExpense(selectedCat, amount, descText.trim(), selectedAccountId)
                            showAddDialog = false
                        }
                    },
                    enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text("Save Expense")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
