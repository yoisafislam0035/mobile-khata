package com.example.ui.screens.khata

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.local.entity.CustomerLedgerEntry
import com.example.ui.components.MobiKhataAmountInputField
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JamaaRed
import com.example.ui.theme.JamaaRedBg
import com.example.ui.theme.UdhaarGreen
import com.example.ui.theme.UdhaarGreenBg
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.CurrencyUtils
import com.example.util.ImageStorageUtils
import com.example.util.Localization
import com.example.util.PdfGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(viewModel: MobiKhataViewModel) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val customer by viewModel.selectedCustomer.collectAsState()
    val ledger by viewModel.customerLedger.collectAsState()
    val business by viewModel.currentBusiness.collectAsState()

    var showEntryDialog by remember { mutableStateOf(false) }
    var isGaveUdhaar by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var viewingBillImageUri by remember { mutableStateOf<String?>(null) }

    BackHandler {
        viewModel.navigateTo(AppScreen.CUSTOMERS)
    }

    if (customer == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(Localization.get("no_customers_found", language))
        }
        return
    }

    val currentCustomer = customer!!
    val filteredLedger = remember(ledger, selectedFilter) {
        when (selectedFilter) {
            "UDHAAR" -> ledger.filter { it.type == "GAVE_UDHAAR" || it.type == "SALE" }
            "PAYMENT" -> ledger.filter { it.type == "GOT_PAYMENT" || it.type == "RETURN" }
            else -> ledger
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentCustomer.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = Localization.get("cancel", language))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.navigateTo(AppScreen.CUSTOMER_PROFILE_SETTINGS)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = Localization.get("profile_settings", language),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            isGaveUdhaar = true
                            showEntryDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JamaaRed),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            Localization.get("you_gave_udhaar", language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Button(
                        onClick = {
                            isGaveUdhaar = false
                            showEntryDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UdhaarGreen),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.CallReceived, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            Localization.get("you_got_payment", language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Card (Clean & professional display without clutter)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            if (currentCustomer.profileImageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = currentCustomer.profileImageUrl,
                                    contentDescription = currentCustomer.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initial = currentCustomer.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "#"
                                    Text(
                                        text = initial,
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = currentCustomer.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentCustomer.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        if (currentCustomer.address.isNotBlank() || currentCustomer.city.isNotBlank()) {
                            Text(
                                text = listOf(currentCustomer.address, currentCustomer.city).filter { it.isNotBlank() }.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        // Current Balance Banner
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                currentCustomer.currentBalance > 0 -> JamaaRedBg
                                currentCustomer.currentBalance < 0 -> UdhaarGreenBg
                                else -> MaterialTheme.colorScheme.surface
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when {
                                    currentCustomer.currentBalance > 0 -> JamaaRed.copy(alpha = 0.3f)
                                    currentCustomer.currentBalance < 0 -> UdhaarGreen.copy(alpha = 0.3f)
                                    else -> Color.LightGray
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when {
                                        currentCustomer.currentBalance > 0 -> Localization.get("you_will_get", language)
                                        currentCustomer.currentBalance < 0 -> Localization.get("you_will_give", language)
                                        else -> Localization.get("settled_balance", language)
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        currentCustomer.currentBalance > 0 -> JamaaRed
                                        currentCustomer.currentBalance < 0 -> UdhaarGreen
                                        else -> Color.DarkGray
                                    }
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = CurrencyUtils.format(Math.abs(currentCustomer.currentBalance)),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when {
                                        currentCustomer.currentBalance > 0 -> JamaaRed
                                        currentCustomer.currentBalance < 0 -> UdhaarGreen
                                        else -> Color.DarkGray
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        // Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileActionButton(
                                icon = Icons.Default.Phone,
                                label = Localization.get("call_customer", language),
                                color = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${currentCustomer.phone}"))
                                    context.startActivity(intent)
                                }
                            )
                            ProfileActionButton(
                                icon = Icons.Default.Chat,
                                label = Localization.get("whatsapp_customer", language),
                                color = Color(0xFF25D366),
                                onClick = {
                                    viewModel.sendWhatsAppReminder(context, currentCustomer)
                                }
                            )
                            ProfileActionButton(
                                icon = Icons.Default.NotificationsActive,
                                label = Localization.get("reminder", language),
                                color = Color(0xFFF59E0B),
                                onClick = {
                                    viewModel.sendWhatsAppReminder(context, currentCustomer)
                                }
                            )
                            ProfileActionButton(
                                icon = Icons.Default.Description,
                                label = Localization.get("statement", language),
                                color = Color(0xFF3B82F6),
                                onClick = {
                                    if (business != null) {
                                        val pdf = PdfGenerator.generateCustomerStatementPdf(context, business!!, currentCustomer, ledger)
                                        if (pdf != null) {
                                            PdfGenerator.sharePdf(context, pdf, "Statement ${currentCustomer.name}")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Ledger Section Title & Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        Localization.get("customer_ledger", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("${Localization.get("all", language)} (${ledger.size})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == "UDHAAR",
                            onClick = { selectedFilter = "UDHAAR" },
                            label = { Text(Localization.get("filter_udhaar", language), fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == "PAYMENT",
                            onClick = { selectedFilter = "PAYMENT" },
                            label = { Text(Localization.get("filter_payment", language), fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Ledger Entries List
            if (filteredLedger.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(42.dp), tint = Color.Gray)
                                Spacer(Modifier.height(8.dp))
                                Text(Localization.get("no_ledger_entries", language), color = Color.Gray, fontWeight = FontWeight.Medium)
                                Text(Localization.get("use_bottom_buttons", language), fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                items(filteredLedger, key = { it.id }) { entry ->
                    CustomerLedgerItem(
                        entry = entry,
                        language = language,
                        onViewBill = { billUrl ->
                            viewingBillImageUri = billUrl
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    // Add Transaction Dialog (with In-App Calculator and Add Bill Camera + Gallery)
    if (showEntryDialog) {
        RecordEntryDialog(
            customerId = currentCustomer.id,
            isGaveUdhaar = isGaveUdhaar,
            language = language,
            onDismiss = { showEntryDialog = false },
            onSave = { amount, description, paymentMethod, billImageUrl ->
                viewModel.recordCustomerEntry(
                    customerId = currentCustomer.id,
                    isGaveUdhaar = isGaveUdhaar,
                    amount = amount,
                    description = description,
                    paymentMethod = paymentMethod,
                    billImageUrl = billImageUrl
                )
                showEntryDialog = false
            }
        )
    }

    // View Bill Full-Screen Dialog
    if (viewingBillImageUri != null) {
        Dialog(
            onDismissRequest = { viewingBillImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.92f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = viewingBillImageUri,
                        contentDescription = "Bill Receipt",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = { viewingBillImageUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}

@Composable
fun CustomerLedgerItem(
    entry: CustomerLedgerEntry,
    language: com.example.util.AppLanguage,
    onViewBill: (String) -> Unit
) {
    val isGave = entry.type == "GAVE_UDHAAR" || entry.type == "SALE"
    val color = if (isGave) JamaaRed else UdhaarGreen

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGave) Icons.Default.ArrowOutward else Icons.Default.CallReceived,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Localization.get(if (isGave) "you_gave_udhaar" else "you_got_payment", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = color
                    )
                    if (entry.billImageUrl.isNotBlank()) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { onViewBill(entry.billImageUrl) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = Localization.get("view_bill", language),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    Localization.get("bill_attached", language),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                if (entry.description.isNotBlank()) {
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
                Text(
                    text = "${CurrencyUtils.formatDate(entry.date)} • ${entry.paymentMethod}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isGave) "- " else "+ ") + CurrencyUtils.format(entry.amount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = color
                )
                Text(
                    text = "${Localization.get("running_balance", language)}: ${CurrencyUtils.format(entry.balanceAfter)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                if (entry.billImageUrl.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { onViewBill(entry.billImageUrl) }
                    ) {
                        AsyncImage(
                            model = entry.billImageUrl,
                            contentDescription = "Bill Thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

/**
 * Record Entry Dialog supporting:
 * - In-app Calculator for Amount
 * - Add Bill via Camera + Gallery with preview, replace, remove
 */
@Composable
fun RecordEntryDialog(
    customerId: String,
    isGaveUdhaar: Boolean,
    language: com.example.util.AppLanguage,
    onDismiss: () -> Unit,
    onSave: (amount: Double, description: String, paymentMethod: String, billImageUrl: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var billImageUrl by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showAddBillChoice by remember { mutableStateOf(false) }

    // Camera capture state
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            coroutineScope.launch {
                val savedUri = ImageStorageUtils.saveImagePermanently(context, tempCameraUri!!, "bill_${customerId}")
                if (savedUri != null) {
                    billImageUrl = savedUri
                    Toast.makeText(context, Localization.get("bill_attached", language), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val pair = ImageStorageUtils.createTempImageUri(context, "bill_${customerId}")
            if (pair != null) {
                tempCameraUri = pair.first
                cameraLauncher.launch(pair.first)
            }
        } else {
            Toast.makeText(context, Localization.get("camera_permission_needed", language), Toast.LENGTH_LONG).show()
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val savedUri = ImageStorageUtils.saveImagePermanently(context, uri, "bill_${customerId}")
                if (savedUri != null) {
                    billImageUrl = savedUri
                    Toast.makeText(context, Localization.get("bill_attached", language), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dialog Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isGaveUdhaar) JamaaRed.copy(alpha = 0.15f) else UdhaarGreen.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isGaveUdhaar) Icons.Default.ArrowOutward else Icons.Default.CallReceived,
                            contentDescription = null,
                            tint = if (isGaveUdhaar) JamaaRed else UdhaarGreen
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isGaveUdhaar) Localization.get("you_gave_udhaar", language) else Localization.get("receive_payment", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isGaveUdhaar) JamaaRed else UdhaarGreen
                    )
                }

                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // Amount Field with In-App Calculator Integration
                MobiKhataAmountInputField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMsg = null
                    },
                    label = Localization.get("amount", language) + " *"
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(Localization.get("description", language)) },
                    placeholder = { Text(Localization.get("description", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Payment Mode Selection
                Column {
                    Text(Localization.get("payment_mode", language), fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Cash", "Online / Bank", "JazzCash / EasyPaisa").forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Add Bill / Bill Attachment Section
                Column {
                    Text("Bill / Receipt Attachment", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(6.dp))

                    if (billImageUrl.isBlank()) {
                        OutlinedButton(
                            onClick = { showAddBillChoice = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(Localization.get("add_bill", language), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Attached bill preview card with replace and remove options
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = billImageUrl,
                                        contentDescription = "Bill Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        Localization.get("bill_attached", language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        TextButton(
                                            onClick = { showAddBillChoice = true },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(Localization.get("replace_bill", language), fontSize = 11.sp)
                                        }
                                        TextButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    ImageStorageUtils.deleteImageIfExists(billImageUrl)
                                                    billImageUrl = ""
                                                }
                                            },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(Localization.get("remove_bill", language), fontSize = 11.sp, color = JamaaRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
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
                            val amt = amountText.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                errorMsg = Localization.get("amount_required", language)
                                return@Button
                            }
                            onSave(amt, description.trim(), paymentMethod, billImageUrl)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGaveUdhaar) JamaaRed else UdhaarGreen
                        )
                    ) {
                        Text(Localization.get("save", language), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Add Bill Choice Dialog (Camera vs Gallery)
    if (showAddBillChoice) {
        AlertDialog(
            onDismissRequest = { showAddBillChoice = false },
            title = {
                Text(
                    text = Localization.get("add_bill", language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Option 1: Camera
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddBillChoice = false
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    val pair = ImageStorageUtils.createTempImageUri(context, "bill_${customerId}")
                                    if (pair != null) {
                                        tempCameraUri = pair.first
                                        cameraLauncher.launch(pair.first)
                                    }
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
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
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    Localization.get("take_photo", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Capture receipt or invoice using camera",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Option 2: Gallery
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddBillChoice = false
                                galleryPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
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
                                    .background(Color(0xFF2563EB), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    Localization.get("choose_gallery", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Choose existing receipt image from gallery",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddBillChoice = false }) {
                    Text(Localization.get("cancel", language))
                }
            }
        )
    }
}
