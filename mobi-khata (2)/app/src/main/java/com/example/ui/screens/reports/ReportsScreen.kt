package com.example.ui.screens.reports

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JamaaRed
import com.example.ui.theme.UdhaarGreen
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.CurrencyUtils

@Composable
fun ReportsScreen(viewModel: MobiKhataViewModel) {
    val context = LocalContext.current
    val lang by viewModel.language.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val products by viewModel.products.collectAsState()

    val totalRevenue = remember(sales) { sales.sumOf { it.grandTotal } }
    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val estimatedCogs = totalRevenue * 0.82
    val grossProfit = (totalRevenue - estimatedCogs).coerceAtLeast(0.0)
    val netProfit = grossProfit - totalExpenses
    val totalReceivables = remember(customers) { customers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance } }
    val totalPayables = remember(suppliers) { suppliers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance } }
    val stockValuation = remember(products) { products.sumOf { it.currentStock * it.purchasePrice } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Financial Reports & Profit / Loss",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                    onClick = {
                        val reportText = """
                            Mobi Khata - Financial Summary Report
                            =====================================
                            Total Sales Revenue: ${CurrencyUtils.format(totalRevenue)}
                            Gross Profit: ${CurrencyUtils.format(grossProfit)}
                            Total Expenses: ${CurrencyUtils.format(totalExpenses)}
                            Estimated Net Profit: ${CurrencyUtils.format(netProfit)}
                            -------------------------------------
                            Total Receivables (Udhaar): ${CurrencyUtils.format(totalReceivables)}
                            Total Payables: ${CurrencyUtils.format(totalPayables)}
                            Current Stock Valuation: ${CurrencyUtils.format(stockValuation)}
                        """.trimIndent()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export Report"))
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share Summary")
                }
            }
        }

        // Net Profit Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (netProfit >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Estimated Net Profit",
                        fontWeight = FontWeight.Bold,
                        color = if (netProfit >= 0) UdhaarGreen else JamaaRed
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        CurrencyUtils.format(netProfit),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (netProfit >= 0) UdhaarGreen else JamaaRed
                    )
                    Text(
                        "Based on sales revenue minus estimated cost of goods & expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // P&L Breakdown Table Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Profit & Loss Breakdown", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    HorizontalDivider()
                    ReportRow("Sales Revenue (+)", CurrencyUtils.format(totalRevenue), UdhaarGreen)
                    ReportRow("Cost of Goods Sold (-)", CurrencyUtils.format(estimatedCogs), Color.DarkGray)
                    HorizontalDivider()
                    ReportRow("Gross Profit", CurrencyUtils.format(grossProfit), MaterialTheme.colorScheme.primary, isBold = true)
                    ReportRow("Business Expenses (-)", CurrencyUtils.format(totalExpenses), JamaaRed)
                    HorizontalDivider()
                    ReportRow("Estimated Net Profit", CurrencyUtils.format(netProfit), if (netProfit >= 0) UdhaarGreen else JamaaRed, isBold = true)
                }
            }
        }

        // Balance Sheet Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Balance Sheet & Working Capital", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    HorizontalDivider()
                    ReportRow("Total Customer Receivables (Udhaar)", CurrencyUtils.format(totalReceivables), UdhaarGreen)
                    ReportRow("Total Supplier Payables", CurrencyUtils.format(totalPayables), JamaaRed)
                    ReportRow("Warehouse Stock Valuation", CurrencyUtils.format(stockValuation), MaterialTheme.colorScheme.primary)
                }
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun ReportRow(
    title: String,
    value: String,
    color: Color = Color.Black,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = color
        )
    }
}
