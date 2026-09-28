package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sync.SyncStatus
import com.example.ui.components.MobiKhataAmountInputField
import com.example.ui.components.QuickThemeSwitchDialog
import com.example.ui.screens.accounts.CashAccountsScreen
import com.example.ui.screens.admin.AdminSettingsScreen
import com.example.ui.screens.admin.BusinessProfileScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.daybook.DayBookScreen
import com.example.ui.screens.expenses.ExpensesScreen
import com.example.ui.screens.khata.CustomerDetailScreen
import com.example.ui.screens.khata.CustomerLedgerScreen
import com.example.ui.screens.khata.CustomerProfileSettingsScreen
import com.example.ui.screens.khata.SupplierLedgerScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.search.GlobalSearchDialog
import com.example.ui.screens.setup.BusinessSetupScreen
import com.example.ui.screens.sync.InternetSyncDialog
import com.example.ui.theme.MobiKhataTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.Localization

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MobiKhataViewModel = viewModel()
            val language by viewModel.language.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val customColor by viewModel.customPrimaryColor.collectAsState()

            val currentScreen by viewModel.currentScreen.collectAsState()
            val business by viewModel.currentBusiness.collectAsState()
            val customers by viewModel.customers.collectAsState()
            val syncStatus by viewModel.syncStatus.collectAsState()
            val isOnline by viewModel.isOnline.collectAsState()
            val pendingCount by viewModel.pendingSyncCount.collectAsState()

            var showSearchDialog by remember { mutableStateOf(false) }
            var showQuickEntryDialog by remember { mutableStateOf(false) }
            var quickEntryIsGave by remember { mutableStateOf(true) }
            var showMoreMenuSheet by remember { mutableStateOf(false) }
            var showSyncDialog by remember { mutableStateOf(false) }
            var showThemeSwitchDialog by remember { mutableStateOf(false) }

            val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MobiKhataTheme(
                    language = language,
                    themeMode = themeMode,
                    customPrimaryColor = customColor
                ) {
                    if (currentScreen == AppScreen.AUTH) {
                        AuthScreen(viewModel = viewModel)
                    } else if (currentScreen == AppScreen.BUSINESS_SETUP) {
                        BusinessSetupScreen(viewModel = viewModel)
                    } else {
                        // Back handling on sub-screens
                        if (currentScreen != AppScreen.DASHBOARD) {
                            BackHandler {
                                when (currentScreen) {
                                    AppScreen.CUSTOMER_PROFILE_SETTINGS -> viewModel.navigateTo(AppScreen.CUSTOMER_DETAIL)
                                    AppScreen.CUSTOMER_DETAIL -> viewModel.navigateTo(AppScreen.CUSTOMERS)
                                    AppScreen.BUSINESS_PROFILE -> viewModel.navigateTo(AppScreen.ADMIN_SETTINGS)
                                    else -> viewModel.navigateTo(AppScreen.DASHBOARD)
                                }
                            }
                        }

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                if (currentScreen != AppScreen.CUSTOMER_DETAIL &&
                                    currentScreen != AppScreen.CUSTOMER_PROFILE_SETTINGS &&
                                    currentScreen != AppScreen.BUSINESS_PROFILE
                                ) {
                                    TopAppBar(
                                        title = {
                                            Column {
                                                Text(
                                                    text = when (currentScreen) {
                                                        AppScreen.DASHBOARD -> business?.name ?: Localization.get("app_name", language)
                                                        AppScreen.CUSTOMERS -> Localization.get("nav_customers", language)
                                                        AppScreen.SUPPLIERS -> Localization.get("nav_suppliers", language)
                                                        AppScreen.ACCOUNTS -> Localization.get("cash_accounts", language)
                                                        AppScreen.EXPENSES -> Localization.get("expenses", language)
                                                        AppScreen.DAYBOOK -> Localization.get("nav_daybook", language)
                                                        AppScreen.REPORTS -> Localization.get("nav_reports", language)
                                                        AppScreen.ADMIN_SETTINGS -> Localization.get("nav_admin", language)
                                                        else -> Localization.get("app_name", language)
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                                if (currentScreen == AppScreen.DASHBOARD) {
                                                    Text(
                                                        Localization.get("tagline", language),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        },
                                        navigationIcon = {
                                            if (currentScreen != AppScreen.DASHBOARD) {
                                                IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                                }
                                            }
                                        },
                                        actions = {
                                            // Quick Theme Switch Icon in upper corner
                                            IconButton(onClick = { showThemeSwitchDialog = true }) {
                                                Icon(
                                                    imageVector = Icons.Default.Palette,
                                                    contentDescription = "Switch Theme",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            // Cloud Sync Icon
                                            IconButton(onClick = { showSyncDialog = true }) {
                                                BadgedBox(
                                                    badge = {
                                                        if (pendingCount > 0) {
                                                            Badge(containerColor = Color(0xFFD97706)) {
                                                                Text("$pendingCount")
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = when {
                                                            !isOnline -> Icons.Default.CloudOff
                                                            syncStatus == SyncStatus.SYNCING -> Icons.Default.Sync
                                                            else -> Icons.Default.CloudDone
                                                        },
                                                        contentDescription = "Cloud Sync",
                                                        tint = when {
                                                            !isOnline -> Color.Gray
                                                            syncStatus == SyncStatus.SYNCING -> Color(0xFFF59E0B)
                                                            else -> Color(0xFF10B981)
                                                        }
                                                    )
                                                }
                                            }

                                            // Search Icon
                                            IconButton(onClick = { showSearchDialog = true }) {
                                                Icon(Icons.Default.Search, contentDescription = "Search")
                                            }

                                            // Settings Icon
                                            IconButton(onClick = { viewModel.navigateTo(AppScreen.ADMIN_SETTINGS) }) {
                                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                                            }
                                        }
                                    )
                                }
                            },
                            bottomBar = {
                                NavigationBar(
                                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.DASHBOARD,
                                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                        label = { Text(Localization.get("nav_dashboard", language)) }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen in listOf(
                                            AppScreen.CUSTOMERS,
                                            AppScreen.CUSTOMER_DETAIL,
                                            AppScreen.CUSTOMER_PROFILE_SETTINGS
                                        ),
                                        onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) },
                                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                                        label = { Text(Localization.get("nav_customers", language)) }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.SUPPLIERS,
                                        onClick = { viewModel.navigateTo(AppScreen.SUPPLIERS) },
                                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                                        label = { Text(Localization.get("nav_suppliers", language)) }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.DAYBOOK,
                                        onClick = { viewModel.navigateTo(AppScreen.DAYBOOK) },
                                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                                        label = { Text(Localization.get("nav_daybook", language)) }
                                    )
                                    NavigationBarItem(
                                        selected = showMoreMenuSheet || currentScreen in listOf(
                                            AppScreen.ACCOUNTS, AppScreen.EXPENSES, AppScreen.REPORTS,
                                            AppScreen.BUSINESS_PROFILE, AppScreen.ADMIN_SETTINGS
                                        ),
                                        onClick = { showMoreMenuSheet = true },
                                        icon = { Icon(Icons.Default.MoreHoriz, contentDescription = null) },
                                        label = { Text(Localization.get("nav_more", language)) }
                                    )
                                }
                            }
                        ) { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                            ) {
                                when (currentScreen) {
                                    AppScreen.DASHBOARD -> DashboardScreen(
                                        viewModel = viewModel,
                                        onOpenSearch = { showSearchDialog = true },
                                        onOpenQuickEntry = { isGave ->
                                            quickEntryIsGave = isGave
                                            showQuickEntryDialog = true
                                        }
                                    )
                                    AppScreen.CUSTOMERS -> CustomerLedgerScreen(viewModel = viewModel)
                                    AppScreen.CUSTOMER_DETAIL -> CustomerDetailScreen(viewModel = viewModel)
                                    AppScreen.CUSTOMER_PROFILE_SETTINGS -> CustomerProfileSettingsScreen(viewModel = viewModel)
                                    AppScreen.BUSINESS_PROFILE -> BusinessProfileScreen(viewModel = viewModel)
                                    AppScreen.SUPPLIERS -> SupplierLedgerScreen(viewModel = viewModel)
                                    AppScreen.ACCOUNTS -> CashAccountsScreen(viewModel = viewModel)
                                    AppScreen.EXPENSES -> ExpensesScreen(viewModel = viewModel)
                                    AppScreen.DAYBOOK -> DayBookScreen(viewModel = viewModel)
                                    AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                                    AppScreen.ADMIN_SETTINGS -> AdminSettingsScreen(viewModel = viewModel)
                                    else -> DashboardScreen(
                                        viewModel = viewModel,
                                        onOpenSearch = { showSearchDialog = true },
                                        onOpenQuickEntry = { isGave ->
                                            quickEntryIsGave = isGave
                                            showQuickEntryDialog = true
                                        }
                                    )
                                }
                            }
                        }

                        // More Bottom Sheet Menu
                        if (showMoreMenuSheet) {
                            ModalBottomSheet(
                                onDismissRequest = { showMoreMenuSheet = false }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .navigationBarsPadding(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        Localization.get("more_features", language),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    ListItem(
                                        headlineContent = { Text(Localization.get("my_business_profile", language)) },
                                        supportingContent = { Text(business?.name ?: "Shop") },
                                        leadingContent = { Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            showMoreMenuSheet = false
                                            viewModel.navigateTo(AppScreen.BUSINESS_PROFILE)
                                        }
                                    )
                                    ListItem(
                                        headlineContent = { Text(Localization.get("theme", language)) },
                                        supportingContent = { Text("Light, Dark, Custom Color") },
                                        leadingContent = { Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            showMoreMenuSheet = false
                                            showThemeSwitchDialog = true
                                        }
                                    )
                                    ListItem(
                                        headlineContent = { Text(Localization.get("cash_accounts", language)) },
                                        leadingContent = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            showMoreMenuSheet = false
                                            viewModel.navigateTo(AppScreen.ACCOUNTS)
                                        }
                                    )
                                    ListItem(
                                        headlineContent = { Text(Localization.get("expenses", language)) },
                                        leadingContent = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            showMoreMenuSheet = false
                                            viewModel.navigateTo(AppScreen.EXPENSES)
                                        }
                                    )
                                    ListItem(
                                        headlineContent = { Text(Localization.get("nav_reports", language)) },
                                        leadingContent = { Icon(Icons.Default.Assessment, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            showMoreMenuSheet = false
                                            viewModel.navigateTo(AppScreen.REPORTS)
                                        }
                                    )
                                    ListItem(
                                        headlineContent = { Text(Localization.get("nav_admin", language)) },
                                        leadingContent = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            showMoreMenuSheet = false
                                            viewModel.navigateTo(AppScreen.ADMIN_SETTINGS)
                                        }
                                    )
                                    ListItem(
                                        headlineContent = { Text(Localization.get("cloud_sync_section", language)) },
                                        supportingContent = {
                                            Text(
                                                if (isOnline) "${Localization.get("online", language)} • ${if (pendingCount > 0) "$pendingCount pending" else Localization.get("sync_all_backed_up", language)}"
                                                else Localization.get("sync_offline_mode", language),
                                                fontSize = 11.sp,
                                                color = if (isOnline) Color(0xFF10B981) else Color.Gray
                                            )
                                        },
                                        leadingContent = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            showMoreMenuSheet = false
                                            showSyncDialog = true
                                        }
                                    )
                                }
                            }
                        }

                        // Quick Theme Switch Dialog
                        if (showThemeSwitchDialog) {
                            QuickThemeSwitchDialog(
                                viewModel = viewModel,
                                onDismiss = { showThemeSwitchDialog = false }
                            )
                        }

                        // Global Search Dialog
                        if (showSearchDialog) {
                            GlobalSearchDialog(
                                viewModel = viewModel,
                                onDismiss = { showSearchDialog = false }
                            )
                        }

                        // Quick Entry Dialog from Dashboard (With in-app Calculator)
                        if (showQuickEntryDialog) {
                            var selectedCustId by remember { mutableStateOf(customers.firstOrNull()?.id ?: "") }
                            var amountText by remember { mutableStateOf("") }
                            var descText by remember { mutableStateOf("") }

                            AlertDialog(
                                onDismissRequest = { showQuickEntryDialog = false },
                                title = {
                                    Text(
                                        if (quickEntryIsGave) Localization.get("quick_entry_gave_title", language)
                                        else Localization.get("quick_entry_got_title", language)
                                    )
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(Localization.get("select_customer", language), fontWeight = FontWeight.Bold)
                                        if (customers.isEmpty()) {
                                            Text("No customers found. Please add a customer first.", color = Color.Gray, fontSize = 12.sp)
                                        } else {
                                            customers.take(5).forEach { c ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().clickable { selectedCustId = c.id },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    RadioButton(
                                                        selected = selectedCustId == c.id,
                                                        onClick = { selectedCustId = c.id }
                                                    )
                                                    Text(c.name)
                                                }
                                            }
                                        }

                                        // In-App Calculator for Amount input
                                        MobiKhataAmountInputField(
                                            value = amountText,
                                            onValueChange = { amountText = it },
                                            label = Localization.get("amount", language) + " *"
                                        )

                                        OutlinedTextField(
                                            value = descText,
                                            onValueChange = { descText = it },
                                            label = { Text(Localization.get("description", language)) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            val amount = amountText.toDoubleOrNull()
                                            if (selectedCustId.isNotBlank() && amount != null && amount > 0) {
                                                viewModel.recordCustomerEntry(selectedCustId, quickEntryIsGave, amount, descText.trim())
                                                showQuickEntryDialog = false
                                            }
                                        },
                                        enabled = selectedCustId.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
                                    ) {
                                        Text(Localization.get("save", language))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showQuickEntryDialog = false }) {
                                        Text(Localization.get("cancel", language))
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
                    }
                }
            }
        }
    }
}
