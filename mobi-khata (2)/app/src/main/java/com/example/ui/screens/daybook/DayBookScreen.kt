package com.example.ui.screens.daybook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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

data class DayBookItem(
    val id: String,
    val date: Long,
    val type: String,
    val party: String,
    val reference: String,
    val amount: Double,
    val isPositive: Boolean
)

@Composable
fun DayBookScreen(viewModel: MobiKhataViewModel) {
    val sales by viewModel.sales.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    var filterType by remember { mutableStateOf("ALL") }

    val allEntries = remember(sales, purchases, expenses) {
        val list = mutableListOf<DayBookItem>()
        sales.forEach { s ->
            list.add(
                DayBookItem(
                    id = s.id,
                    date = s.date,
                    type = "SALE",
                    party = s.customerName,
                    reference = "Invoice #${s.invoiceNo}",
                    amount = s.paidAmount,
                    isPositive = true
                )
            )
        }
        purchases.forEach { p ->
            list.add(
                DayBookItem(
                    id = p.id,
                    date = p.date,
                    type = "PURCHASE",
                    party = p.supplierName,
                    reference = "Bill #${p.billNo}",
                    amount = p.paidAmount,
                    isPositive = false
                )
            )
        }
        expenses.forEach { e ->
            list.add(
                DayBookItem(
                    id = e.id,
                    date = e.date,
                    type = "EXPENSE",
                    party = e.category,
                    reference = e.description,
                    amount = e.amount,
                    isPositive = false
                )
            )
        }
        list.sortedByDescending { it.date }
    }

    val filtered = remember(allEntries, filterType) {
        when (filterType) {
            "INFLOW" -> allEntries.filter { it.isPositive }
            "OUTFLOW" -> allEntries.filter { !it.isPositive }
            else -> allEntries
        }
    }

    val totalInflow = remember(allEntries) { allEntries.filter { it.isPositive }.sumOf { it.amount } }
    val totalOutflow = remember(allEntries) { allEntries.filter { !it.isPositive }.sumOf { it.amount } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Cash In (+)", color = UdhaarGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(CurrencyUtils.format(totalInflow), fontWeight = FontWeight.ExtraBold, color = UdhaarGreen, fontSize = 16.sp)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Cash Out (-)", color = JamaaRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(CurrencyUtils.format(totalOutflow), fontWeight = FontWeight.ExtraBold, color = JamaaRed, fontSize = 16.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == "ALL",
                onClick = { filterType = "ALL" },
                label = { Text("All (${allEntries.size})") }
            )
            FilterChip(
                selected = filterType == "INFLOW",
                onClick = { filterType = "INFLOW" },
                label = { Text("Cash In (+)") }
            )
            FilterChip(
                selected = filterType == "OUTFLOW",
                onClick = { filterType = "OUTFLOW" },
                label = { Text("Cash Out (-)") }
            )
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions in Day Book", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (item.isPositive) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (item.isPositive) UdhaarGreen else JamaaRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(item.party, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${item.type} • ${item.reference}", fontSize = 12.sp, color = Color.DarkGray)
                                    Text(CurrencyUtils.formatDate(item.date), fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            Text(
                                text = "${if (item.isPositive) "+" else "-"} ${CurrencyUtils.format(item.amount)}",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.isPositive) UdhaarGreen else JamaaRed,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
