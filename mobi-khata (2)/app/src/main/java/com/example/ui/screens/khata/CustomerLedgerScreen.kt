package com.example.ui.screens.khata

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.entity.CustomerEntity
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
fun CustomerLedgerScreen(
    viewModel: MobiKhataViewModel
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val customers by viewModel.customers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") }
    var showAddChoiceDialog by remember { mutableStateOf(false) }
    var showContactPicker by remember { mutableStateOf(false) }
    var showManualAddDialog by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val filteredCustomers = remember(customers, searchQuery, filterType) {
        customers.filter { c ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                c.name.contains(searchQuery, ignoreCase = true) || c.phone.contains(searchQuery)
            }
            val matchesFilter = when (filterType) {
                "RECEIVABLE" -> c.currentBalance > 0
                "SETTLED" -> c.currentBalance == 0.0
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    val totalUdhaar = remember(customers) {
        customers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddChoiceDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text(Localization.get("add_customer", language), fontWeight = FontWeight.Bold) },
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
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Localization.get("nav_customers", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${customers.size} total accounts",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Udhaar",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = CurrencyUtils.format(totalUdhaar),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = JamaaRed
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(Localization.get("search", language)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == "ALL",
                    onClick = { filterType = "ALL" },
                    label = { Text("All (${customers.size})") }
                )
                FilterChip(
                    selected = filterType == "RECEIVABLE",
                    onClick = { filterType = "RECEIVABLE" },
                    label = { Text("Udhaar (${customers.count { it.currentBalance > 0 }})") }
                )
                FilterChip(
                    selected = filterType == "SETTLED",
                    onClick = { filterType = "SETTLED" },
                    label = { Text("Settled (${customers.count { it.currentBalance == 0.0 }})") }
                )
            }

            // Customers List
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PeopleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(Localization.get("no_customers_found", language), color = Color.Gray, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            Localization.get("tap_add_customer", language),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerCardItem(
                            customer = customer,
                            language = language,
                            onClick = {
                                viewModel.selectCustomer(customer)
                            }
                        )
                    }
                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add Customer Choice Dialog (Contacts vs Manual)
    if (showAddChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showAddChoiceDialog = false },
            title = {
                Text(
                    text = Localization.get("add_customer", language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Option 1: Phone Contacts
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddChoiceDialog = false
                                showContactPicker = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF2563EB), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Contacts, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    Localization.get("import_contacts", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E40AF)
                                )
                                Text(
                                    "Fast 1-tap customer creation from contacts",
                                    fontSize = 11.sp,
                                    color = Color(0xFF3B82F6)
                                )
                            }
                        }
                    }

                    // Option 2: Manual Entry
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddChoiceDialog = false
                                showManualAddDialog = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.EditNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    Localization.get("manual_customer", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Type name, phone number, address manually",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddChoiceDialog = false }) {
                    Text(Localization.get("cancel", language))
                }
            }
        )
    }

    // Phone Contacts Picker Sheet
    if (showContactPicker) {
        PhoneContactPickerSheet(
            language = language,
            onContactSelected = { name, number, photoUri ->
                showContactPicker = false
                viewModel.addOrOpenCustomerFromContact(name, number, photoUri) { isNew, _ ->
                    if (!isNew) {
                        Toast.makeText(
                            context,
                            Localization.get("customer_exists_warning", language),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, "Added customer: $name", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onManualEntryRequested = {
                showContactPicker = false
                showManualAddDialog = true
            },
            onDismiss = { showContactPicker = false }
        )
    }

    // Manual Customer Entry Dialog with In-App Calculator for Opening Balance
    if (showManualAddDialog) {
        ManualAddCustomerDialog(
            language = language,
            onDismiss = { showManualAddDialog = false },
            onSave = { name, phone, altPhone, address, city, openingBalance, notes ->
                showManualAddDialog = false
                viewModel.addCustomerManual(
                    name = name,
                    phone = phone,
                    altPhone = altPhone,
                    address = address,
                    city = city,
                    openingBalance = openingBalance,
                    notes = notes
                ) { isNew, _ ->
                    if (!isNew) {
                        Toast.makeText(
                            context,
                            Localization.get("customer_exists_warning", language),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, "Customer added: $name", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

@Composable
fun CustomerCardItem(
    customer: CustomerEntity,
    language: com.example.util.AppLanguage,
    onClick: () -> Unit
) {
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
            if (customer.profileImageUrl.isNotBlank()) {
                AsyncImage(
                    model = customer.profileImageUrl,
                    contentDescription = customer.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                val initial = customer.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "#"
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Text(
                    text = customer.phone,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                if (customer.city.isNotBlank()) {
                    Text(
                        text = customer.city,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val isUdhaar = customer.currentBalance > 0
                val isAdvance = customer.currentBalance < 0
                val balanceColor = when {
                    isUdhaar -> JamaaRed
                    isAdvance -> UdhaarGreen
                    else -> Color.DarkGray
                }
                Text(
                    text = CurrencyUtils.format(Math.abs(customer.currentBalance)),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = balanceColor
                )
                Text(
                    text = when {
                        isUdhaar -> Localization.get("filter_udhaar", language)
                        isAdvance -> Localization.get("filter_payment", language)
                        else -> Localization.get("filter_settled", language)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
            }
        }
    }
}

@Composable
fun ManualAddCustomerDialog(
    language: com.example.util.AppLanguage,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        phone: String,
        altPhone: String,
        address: String,
        city: String,
        openingBalance: Double,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var altPhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var openingBalanceText by remember { mutableStateOf("") }
    var isOpeningBalanceUdhaar by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = Localization.get("manual_customer", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(Localization.get("customer_name", language) + " *") },
                            placeholder = { Text("e.g. Tariq Mehmood") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(Localization.get("phone_number", language) + " *") },
                            placeholder = { Text("03001234567") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = altPhone,
                            onValueChange = { altPhone = it },
                            label = { Text(Localization.get("alt_phone_number", language)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text(Localization.get("address", language)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text(Localization.get("city", language)) },
                            placeholder = { Text("e.g. Lahore") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    // Opening balance field using In-App Calculator
                    item {
                        MobiKhataAmountInputField(
                            value = openingBalanceText,
                            onValueChange = { openingBalanceText = it },
                            label = "Opening Balance (Rs.)",
                            placeholder = "0.00"
                        )
                        if (openingBalanceText.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = isOpeningBalanceUdhaar,
                                    onClick = { isOpeningBalanceUdhaar = true },
                                    label = { Text("Customer owes you (Udhaar)") }
                                )
                                FilterChip(
                                    selected = !isOpeningBalanceUdhaar,
                                    onClick = { isOpeningBalanceUdhaar = false },
                                    label = { Text("You owe customer (Advance)") }
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(Localization.get("notes", language)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(Localization.get("cancel", language))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMsg = Localization.get("name_required", language)
                                return@Button
                            }
                            if (phone.isBlank()) {
                                errorMsg = Localization.get("phone_required", language)
                                return@Button
                            }
                            val rawBalance = openingBalanceText.toDoubleOrNull() ?: 0.0
                            val finalBalance = if (isOpeningBalanceUdhaar) rawBalance else -rawBalance
                            onSave(
                                name.trim(),
                                phone.trim(),
                                altPhone.trim(),
                                address.trim(),
                                city.trim(),
                                finalBalance,
                                notes.trim()
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(Localization.get("save", language), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
