package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.GoogleAuthManager
import com.example.auth.GoogleAuthResult
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.MobiKhataRepository
import com.example.sync.SyncManager
import com.example.sync.SyncStatus
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ThemeMode
import com.example.util.AppLanguage
import com.example.util.CurrencyUtils
import com.example.util.Localization
import com.example.util.PdfGenerator
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

enum class AppScreen {
    AUTH,
    BUSINESS_SETUP,
    DASHBOARD,
    CUSTOMERS,
    CUSTOMER_DETAIL,
    CUSTOMER_PROFILE_SETTINGS,
    BUSINESS_PROFILE,
    SUPPLIERS,
    ACCOUNTS,
    EXPENSES,
    DAYBOOK,
    REPORTS,
    ADMIN_SETTINGS
}

data class CartItem(
    val product: ProductEntity,
    var quantity: Double = 1.0,
    var unitPrice: Double = product.salePrice,
    var discount: Double = 0.0
) {
    val total: Double get() = (quantity * unitPrice) - discount
}

class MobiKhataViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    val repository = MobiKhataRepository(db)
    val syncManager = SyncManager(application)
    val googleAuthManager = GoogleAuthManager(application)

    val syncStatus: StateFlow<SyncStatus> = syncManager.syncStatus
    val isOnline: StateFlow<Boolean> = syncManager.isOnline
    val networkType: StateFlow<String> = syncManager.networkType
    val pendingSyncCount: StateFlow<Int> = syncManager.pendingCount
    val lastSyncTimestamp: StateFlow<Long> = syncManager.lastSyncTimestamp
    val autoSyncEnabled: StateFlow<Boolean> = syncManager.autoSyncEnabled
    val syncHistory: StateFlow<List<com.example.sync.SyncHistoryLog>> = syncManager.syncHistory

    private val prefs = application.getSharedPreferences("mobi_khata_prefs", Context.MODE_PRIVATE)

    // Navigation and screen stack
    private val _currentScreen = MutableStateFlow(AppScreen.AUTH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Language preference persisted
    private val _language = MutableStateFlow(
        run {
            val code = prefs.getString("app_language", AppLanguage.ENGLISH.code)
            AppLanguage.values().firstOrNull { it.code == code } ?: AppLanguage.ENGLISH
        }
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Theme Mode & Custom Accent Color persisted
    private val _themeMode = MutableStateFlow(
        run {
            val mode = prefs.getString("app_theme_mode", ThemeMode.LIGHT.name)
            try {
                ThemeMode.valueOf(mode ?: ThemeMode.LIGHT.name)
            } catch (e: Exception) {
                ThemeMode.LIGHT
            }
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _customPrimaryColor = MutableStateFlow(
        run {
            val colorLong = prefs.getLong("app_custom_color", 0xFF0D5C46)
            Color(colorLong)
        }
    )
    val customPrimaryColor: StateFlow<Color> = _customPrimaryColor.asStateFlow()

    // Auth & Business
    data class EmailVerificationData(
        val email: String,
        val name: String = "",
        val phone: String = ""
    )
    private val _emailVerificationState = MutableStateFlow<EmailVerificationData?>(null)
    val emailVerificationState: StateFlow<EmailVerificationData?> = _emailVerificationState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentBusiness = MutableStateFlow<BusinessEntity?>(null)
    val currentBusiness: StateFlow<BusinessEntity?> = _currentBusiness.asStateFlow()

    private val _businesses = MutableStateFlow<List<BusinessEntity>>(emptyList())
    val businesses: StateFlow<List<BusinessEntity>> = _businesses.asStateFlow()

    // Selected details
    val selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedSupplier = MutableStateFlow<SupplierEntity?>(null)

    // Data lists observed reactively
    val customers = MutableStateFlow<List<CustomerEntity>>(emptyList())
    val customerLedger = MutableStateFlow<List<CustomerLedgerEntry>>(emptyList())
    val allCustomerLedger = MutableStateFlow<List<CustomerLedgerEntry>>(emptyList())
    val suppliers = MutableStateFlow<List<SupplierEntity>>(emptyList())
    val supplierLedger = MutableStateFlow<List<SupplierLedgerEntry>>(emptyList())
    val products = MutableStateFlow<List<ProductEntity>>(emptyList())
    val lowStockProducts = MutableStateFlow<List<ProductEntity>>(emptyList())
    val sales = MutableStateFlow<List<SaleEntity>>(emptyList())
    val purchases = MutableStateFlow<List<PurchaseEntity>>(emptyList())
    val accounts = MutableStateFlow<List<AccountEntity>>(emptyList())
    val expenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val auditLogs = MutableStateFlow<List<AuditLogEntity>>(emptyList())

    // Firebase Phone Auth State
    var phoneVerificationId: String? = null
    var phoneResendToken: PhoneAuthProvider.ForceResendingToken? = null

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            // First check Firebase Auth state
            val firebaseUser = googleAuthManager.getCurrentFirebaseUser()
            if (firebaseUser != null) {
                // If account was created with email and is not yet verified, require verification
                val isPhoneOrGoogle = firebaseUser.phoneNumber != null || firebaseUser.providerData.any { it.providerId == "google.com" }
                if (!isPhoneOrGoogle && !firebaseUser.isEmailVerified) {
                    _emailVerificationState.value = EmailVerificationData(
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: ""
                    )
                    _currentUser.value = null
                    _currentBusiness.value = null
                    _currentScreen.value = AppScreen.AUTH
                    return@launch
                }

                val existingUser = repository.userDao.getUserById(firebaseUser.uid)
                    ?: repository.userDao.getUserByEmail(firebaseUser.email ?: "")
                if (existingUser != null) {
                    onUserAuthenticated(existingUser)
                    return@launch
                } else {
                    // Create user entity matching Firebase user
                    val newUser = UserEntity(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Store Owner",
                        email = firebaseUser.email ?: "${firebaseUser.uid}@mobikhata.pk",
                        phone = firebaseUser.phoneNumber ?: "",
                        authProvider = if (firebaseUser.phoneNumber != null) "PHONE" else if (firebaseUser.providerData.any { it.providerId == "google.com" }) "GOOGLE" else "EMAIL",
                        isEmailVerified = firebaseUser.isEmailVerified,
                        isPhoneVerified = firebaseUser.phoneNumber != null
                    )
                    repository.userDao.insertUser(newUser)
                    onUserAuthenticated(newUser)
                    return@launch
                }
            }

            // Check if local signed in user exists
            val (user, business) = repository.getInitialUserAndBusiness()
            if (user != null && business != null) {
                _currentUser.value = user
                _currentBusiness.value = business
                observeBusinessData(business.id)
                observeUserBusinesses(user.id)
                _currentScreen.value = AppScreen.DASHBOARD
            } else {
                // Production requirement: NO demo seeding on startup! Unauthenticated users see Login!
                _currentUser.value = null
                _currentBusiness.value = null
                _currentScreen.value = AppScreen.AUTH
            }
        }
    }

    private fun observeUserBusinesses(userId: String) {
        viewModelScope.launch {
            repository.businessDao.getBusinessesForUser(userId).collect { list ->
                _businesses.value = list
            }
        }
    }

    fun observeBusinessData(businessId: String) {
        viewModelScope.launch {
            repository.customerDao.getCustomers(businessId).collect { customers.value = it }
        }
        viewModelScope.launch {
            repository.customerLedgerDao.getAllLedgerForBusiness(businessId).collect { allCustomerLedger.value = it }
        }
        viewModelScope.launch {
            repository.supplierDao.getSuppliers(businessId).collect { suppliers.value = it }
        }
        viewModelScope.launch {
            repository.productDao.getProducts(businessId).collect { products.value = it }
        }
        viewModelScope.launch {
            repository.productDao.getLowStockProducts(businessId).collect { lowStockProducts.value = it }
        }
        viewModelScope.launch {
            repository.saleDao.getSales(businessId).collect { sales.value = it }
        }
        viewModelScope.launch {
            repository.purchaseDao.getPurchases(businessId).collect { purchases.value = it }
        }
        viewModelScope.launch {
            repository.accountDao.getAccounts(businessId).collect { accounts.value = it }
        }
        viewModelScope.launch {
            repository.expenseDao.getExpenses(businessId).collect { expenses.value = it }
        }
        viewModelScope.launch {
            repository.auditLogDao.getAuditLogs(businessId).collect { auditLogs.value = it }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        prefs.edit().putString("app_language", lang.code).apply()
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("app_theme_mode", mode.name).apply()
    }

    fun setCustomPrimaryColor(color: Color) {
        _customPrimaryColor.value = color
        val colorLong = (color.value.toLong() shr 32)
        // Store ARGB as long
        val argb = ((color.alpha * 255.0f + 0.5f).toInt() shl 24) or
                ((color.red * 255.0f + 0.5f).toInt() shl 16) or
                ((color.green * 255.0f + 0.5f).toInt() shl 8) or
                (color.blue * 255.0f + 0.5f).toInt()
        prefs.edit().putLong("app_custom_color", argb.toLong() and 0xFFFFFFFFL).apply()
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectCustomer(customer: CustomerEntity) {
        selectedCustomer.value = customer
        viewModelScope.launch {
            repository.customerLedgerDao.getLedgerForCustomer(customer.id).collect {
                customerLedger.value = it
            }
        }
        _currentScreen.value = AppScreen.CUSTOMER_DETAIL
    }

    fun selectSupplier(supplier: SupplierEntity) {
        selectedSupplier.value = supplier
        viewModelScope.launch {
            repository.supplierLedgerDao.getLedgerForSupplier(supplier.id).collect {
                supplierLedger.value = it
            }
        }
        _currentScreen.value = AppScreen.SUPPLIERS
    }

    fun switchBusiness(business: BusinessEntity) {
        val user = _currentUser.value ?: return
        _currentBusiness.value = business
        observeBusinessData(business.id)
        syncManager.stopRealtimeSync()
        syncManager.startRealtimeSync(user.id, business.id) {
            observeBusinessData(business.id)
        }
        Toast.makeText(getApplication(), "Switched to ${business.name}", Toast.LENGTH_SHORT).show()
    }

    fun createNewBusiness(name: String, ownerName: String, phone: String, city: String, category: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val newBiz = repository.createBusiness(user.id, name, ownerName, phone, city, category)
            _currentBusiness.value = newBiz
            syncManager.firestoreSyncService.pushBusiness(newBiz, user.id)
            observeBusinessData(newBiz.id)
            observeUserBusinesses(user.id)
            syncManager.startRealtimeSync(user.id, newBiz.id) {
                observeBusinessData(newBiz.id)
            }
            _currentScreen.value = AppScreen.DASHBOARD
            Toast.makeText(getApplication(), "Business created: $name", Toast.LENGTH_SHORT).show()
        }
    }

    fun loginWithEmail(email: String, pin: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            try {
                if (FirebaseApp.getApps(getApplication()).isEmpty()) {
                    onResult(false, "Firebase is not initialized.")
                    return@launch
                }
                val auth = FirebaseAuth.getInstance()
                val authResult = auth.signInWithEmailAndPassword(cleanEmail, pin).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    if (!firebaseUser.isEmailVerified) {
                        try {
                            firebaseUser.sendEmailVerification().await()
                        } catch (_: Exception) {}
                        _emailVerificationState.value = EmailVerificationData(
                            email = cleanEmail,
                            name = firebaseUser.displayName ?: cleanEmail.substringBefore("@")
                        )
                        onResult(false, "Please verify your email address to continue. We sent a verification email to $cleanEmail.")
                        return@launch
                    }

                    val existingUser = repository.userDao.getUserById(firebaseUser.uid)
                    val user = if (existingUser != null) {
                        val updated = existingUser.copy(isEmailVerified = true)
                        repository.userDao.updateUser(updated)
                        updated
                    } else {
                        val newUser = UserEntity(
                            id = firebaseUser.uid,
                            name = firebaseUser.displayName ?: cleanEmail.substringBefore("@"),
                            email = cleanEmail,
                            authProvider = "EMAIL",
                            isEmailVerified = true
                        )
                        repository.userDao.insertUser(newUser)
                        newUser
                    }
                    onUserAuthenticated(user)
                    onResult(true, null)
                } else {
                    onResult(false, "Authentication failed.")
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                onResult(false, "No account found with this email. Please create an account.")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                onResult(false, "Incorrect email or password. Please try again.")
            } catch (e: FirebaseTooManyRequestsException) {
                onResult(false, "Too many failed attempts. Please wait a few minutes and try again.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Sign in failed.")
            }
        }
    }

    fun registerWithEmail(name: String, email: String, phone: String, pin: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            try {
                if (FirebaseApp.getApps(getApplication()).isEmpty()) {
                    onResult(false, "Firebase is not initialized.")
                    return@launch
                }
                val auth = FirebaseAuth.getInstance()
                val authResult = auth.createUserWithEmailAndPassword(cleanEmail, pin).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    try {
                        firebaseUser.sendEmailVerification().await()
                    } catch (e: Exception) {
                        android.util.Log.w("Auth", "Failed to send verification email: ${e.message}")
                    }

                    val newUser = UserEntity(
                        id = firebaseUser.uid,
                        name = name.trim().ifEmpty { cleanEmail.substringBefore("@") },
                        email = cleanEmail,
                        phone = phone.trim(),
                        authProvider = "EMAIL",
                        isEmailVerified = firebaseUser.isEmailVerified
                    )
                    repository.userDao.insertUser(newUser)

                    if (!firebaseUser.isEmailVerified) {
                        _emailVerificationState.value = EmailVerificationData(
                            email = cleanEmail,
                            name = name,
                            phone = phone
                        )
                        onResult(true, null)
                    } else {
                        onUserAuthenticated(newUser)
                        onResult(true, null)
                    }
                } else {
                    onResult(false, "Failed to create account.")
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                onResult(false, "An account with this email already exists. Please sign in.")
            } catch (e: FirebaseAuthWeakPasswordException) {
                onResult(false, "Password must be at least 6 characters.")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                onResult(false, "Please enter a valid email address.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Registration failed.")
            }
        }
    }

    fun checkEmailVerificationStatus(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                if (user == null) {
                    onResult(false, "No active sign-in session found.")
                    return@launch
                }
                user.reload().await()
                if (user.isEmailVerified) {
                    _emailVerificationState.value = null
                    val existingUser = repository.userDao.getUserById(user.uid)
                    val updatedUser = if (existingUser != null) {
                        val u = existingUser.copy(isEmailVerified = true)
                        repository.userDao.updateUser(u)
                        u
                    } else {
                        val u = UserEntity(
                            id = user.uid,
                            name = user.displayName ?: user.email?.substringBefore("@") ?: "Store Owner",
                            email = user.email ?: "",
                            authProvider = "EMAIL",
                            isEmailVerified = true
                        )
                        repository.userDao.insertUser(u)
                        u
                    }
                    onUserAuthenticated(updatedUser)
                    onResult(true, null)
                } else {
                    onResult(false, "Email is not verified yet. Please check your inbox and tap the link.")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to verify status.")
            }
        }
    }

    fun resendVerificationEmail(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    user.sendEmailVerification().await()
                    onResult(true, "Verification email resent successfully!")
                } else {
                    onResult(false, "No active registration session.")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to resend email.")
            }
        }
    }

    fun cancelEmailVerification() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}
        _emailVerificationState.value = null
    }

    fun loginWithGoogle(email: String, name: String, photoUrl: String? = null, uid: String? = null) {
        viewModelScope.launch {
            val user = repository.loginWithGoogle(email, name, photoUrl, uid)
            onUserAuthenticated(user)
            Toast.makeText(getApplication(), "Signed in with Google ($email)", Toast.LENGTH_SHORT).show()
        }
    }

    fun signInWithGoogleViaCredentialManager(
        context: Context,
        onResult: (GoogleAuthResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = googleAuthManager.signIn(context)
            if (result is GoogleAuthResult.Success) {
                val user = repository.loginWithGoogle(
                    email = result.email,
                    name = result.displayName,
                    photoUrl = result.photoUrl,
                    uid = result.firebaseUid
                )
                onUserAuthenticated(user)
                Toast.makeText(getApplication(), "Signed in as ${result.displayName} (${result.email})", Toast.LENGTH_SHORT).show()
            }
            onResult(result)
        }
    }

    /**
     * Real Firebase Phone OTP request
     */
    fun sendPhoneVerificationCode(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (FirebaseApp.getApps(activity).isEmpty()) {
                onError("Firebase is not initialized.")
                return
            }
            val auth = FirebaseAuth.getInstance()
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // Instant auto-retrieval
                        signInWithPhoneCredential(credential, phoneNumber, "") { success, error ->
                            if (!success && error != null) {
                                onError(error)
                            }
                        }
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        val message = when (e) {
                            is FirebaseAuthInvalidCredentialsException -> "Invalid phone number format."
                            is FirebaseTooManyRequestsException -> "Too many requests. Please try again later."
                            else -> e.localizedMessage ?: "Verification failed."
                        }
                        onError(message)
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        phoneVerificationId = verificationId
                        phoneResendToken = token
                        onCodeSent()
                    }
                })
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Failed to send code")
        }
    }

    /**
     * Real Firebase Phone OTP verification
     */
    fun verifyPhoneOtpWithFirebase(
        enteredOtp: String,
        phone: String,
        ownerName: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val verificationId = phoneVerificationId
        if (verificationId.isNullOrBlank()) {
            onResult(false, "Verification session expired. Please request a new code.")
            return
        }

        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, enteredOtp.trim())
            signInWithPhoneCredential(credential, phone, ownerName, onResult)
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Invalid verification code.")
        }
    }

    private fun signInWithPhoneCredential(
        credential: PhoneAuthCredential,
        phone: String,
        ownerName: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUid = authResult.user?.uid
                val user = repository.loginWithPhone(phone, ownerName, firebaseUid)
                onUserAuthenticated(user)
                onResult(true, null)
            } catch (e: Exception) {
                val msg = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect OTP entered. Please try again."
                    else -> e.localizedMessage ?: "Authentication failed."
                }
                onResult(false, msg)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            syncManager.stopRealtimeSync()
            googleAuthManager.signOut()
            _emailVerificationState.value = null
            // Clear all user and sensitive cached data
            _currentUser.value = null
            _currentBusiness.value = null
            _businesses.value = emptyList()
            selectedCustomer.value = null
            selectedSupplier.value = null
            customers.value = emptyList()
            customerLedger.value = emptyList()
            allCustomerLedger.value = emptyList()
            suppliers.value = emptyList()
            supplierLedger.value = emptyList()
            products.value = emptyList()
            sales.value = emptyList()
            purchases.value = emptyList()
            accounts.value = emptyList()
            expenses.value = emptyList()
            auditLogs.value = emptyList()
            _currentScreen.value = AppScreen.AUTH
        }
    }

    fun resetPassword(email: String): String {
        try {
            if (FirebaseApp.getApps(getApplication()).isNotEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim())
            }
        } catch (_: Exception) {}
        return "Password reset link sent to $email. Follow instructions in your email."
    }

    private suspend fun onUserAuthenticated(user: UserEntity) {
        _currentUser.value = user
        // Push user identity to Firestore
        syncManager.firestoreSyncService.pushUser(user)

        // Pull any existing cloud data for this user (Multi-device support)
        syncManager.firestoreSyncService.pullAllCloudData(user.id)

        val existing = repository.businessDao.getBusinessesForUser(user.id).firstOrNull()
        if (existing != null && existing.isNotEmpty()) {
            val activeBiz = existing.first()
            _currentBusiness.value = activeBiz
            observeBusinessData(activeBiz.id)
            observeUserBusinesses(user.id)
            syncManager.startRealtimeSync(user.id, activeBiz.id) {
                observeBusinessData(activeBiz.id)
            }
            _currentScreen.value = AppScreen.DASHBOARD
        } else {
            _currentScreen.value = AppScreen.BUSINESS_SETUP
        }
        syncManager.triggerOneTimeSync(user.id, _currentBusiness.value?.id)
    }

    fun setAutoSync(enabled: Boolean) {
        syncManager.setAutoSyncEnabled(enabled)
    }

    fun syncNow(onComplete: ((Boolean, String) -> Unit)? = null) {
        val uid = _currentUser.value?.id
        val bizId = _currentBusiness.value?.id
        syncManager.triggerOneTimeSync(uid, bizId, onComplete)
    }

    fun restoreFromCloudJson(json: String, onResult: (Boolean) -> Unit) {
        val biz = _currentBusiness.value ?: return
        viewModelScope.launch {
            val success = repository.restoreBusinessDataFromJson(biz.id, json)
            if (success) {
                observeBusinessData(biz.id)
            }
            onResult(success)
        }
    }

    // Customer Actions
    fun addCustomer(name: String, phone: String, openingBalance: Double, address: String) {
        val biz = _currentBusiness.value ?: return
        viewModelScope.launch {
            val customer = CustomerEntity(
                businessId = biz.id,
                name = name,
                phone = phone,
                whatsapp = phone,
                address = address,
                openingBalance = openingBalance,
                currentBalance = openingBalance
            )
            repository.customerDao.insertCustomer(customer)
            val uid = _currentUser.value?.id
            if (!uid.isNullOrBlank()) {
                syncManager.firestoreSyncService.pushCustomer(customer, uid)
            }
            if (openingBalance != 0.0) {
                val opEntry = CustomerLedgerEntry(
                    businessId = biz.id,
                    customerId = customer.id,
                    type = if (openingBalance > 0) "GAVE_UDHAAR" else "GOT_PAYMENT",
                    amount = Math.abs(openingBalance),
                    balanceAfter = openingBalance,
                    description = "Opening Balance"
                )
                repository.customerLedgerDao.insertLedgerEntry(opEntry)
                if (!uid.isNullOrBlank()) {
                    syncManager.firestoreSyncService.pushCustomerTransaction(opEntry, uid)
                }
            }
            repository.logAudit(biz.id, "Owner", "Owner", "CUSTOMER_ADDED", "Added customer: $name ($phone)")
            Toast.makeText(getApplication(), "Customer added: $name", Toast.LENGTH_SHORT).show()
        }
    }

    fun addOrOpenCustomerFromContact(
        contactName: String,
        phoneNumber: String,
        photoUri: String?,
        onResult: (isNew: Boolean, customer: CustomerEntity) -> Unit
    ) {
        val biz = _currentBusiness.value ?: return
        viewModelScope.launch {
            val cleanPhone = phoneNumber.replace("-", "").replace(" ", "").trim()
            val existing = repository.customerDao.findCustomerByPhone(biz.id, cleanPhone)
                ?: repository.customerDao.findCustomerByPhone(biz.id, phoneNumber.trim())
            if (existing != null) {
                selectedCustomer.value = existing
                repository.customerLedgerDao.getLedgerForCustomer(existing.id).collect {
                    customerLedger.value = it
                }
                _currentScreen.value = AppScreen.CUSTOMER_DETAIL
                onResult(false, existing)
            } else {
                val newCustomer = CustomerEntity(
                    businessId = biz.id,
                    name = contactName.trim().ifEmpty { cleanPhone },
                    phone = cleanPhone,
                    whatsapp = cleanPhone,
                    profileImageUrl = photoUri ?: "",
                    openingBalance = 0.0,
                    currentBalance = 0.0
                )
                repository.customerDao.insertCustomer(newCustomer)
                repository.logAudit(biz.id, "Owner", "Owner", "CUSTOMER_ADDED", "Imported contact: ${newCustomer.name} ($cleanPhone)")
                selectedCustomer.value = newCustomer
                customerLedger.value = emptyList()
                _currentScreen.value = AppScreen.CUSTOMER_DETAIL
                onResult(true, newCustomer)
            }
        }
    }

    fun addCustomerManual(
        name: String,
        phone: String,
        altPhone: String,
        address: String,
        city: String,
        openingBalance: Double,
        notes: String,
        onResult: (isNew: Boolean, customer: CustomerEntity) -> Unit
    ) {
        val biz = _currentBusiness.value ?: return
        viewModelScope.launch {
            val cleanPhone = phone.replace("-", "").replace(" ", "").trim()
            val existing = repository.customerDao.findCustomerByPhone(biz.id, cleanPhone)
                ?: repository.customerDao.findCustomerByPhone(biz.id, phone.trim())
            if (existing != null) {
                selectedCustomer.value = existing
                repository.customerLedgerDao.getLedgerForCustomer(existing.id).collect {
                    customerLedger.value = it
                }
                _currentScreen.value = AppScreen.CUSTOMER_DETAIL
                onResult(false, existing)
            } else {
                val newCustomer = CustomerEntity(
                    businessId = biz.id,
                    name = name.trim(),
                    phone = cleanPhone,
                    alternatePhone = altPhone.trim(),
                    whatsapp = cleanPhone,
                    address = address.trim(),
                    city = city.trim(),
                    notes = notes.trim(),
                    openingBalance = openingBalance,
                    currentBalance = openingBalance
                )
                repository.customerDao.insertCustomer(newCustomer)
                if (openingBalance != 0.0) {
                    repository.customerLedgerDao.insertLedgerEntry(
                        CustomerLedgerEntry(
                            businessId = biz.id,
                            customerId = newCustomer.id,
                            type = if (openingBalance > 0) "GAVE_UDHAAR" else "GOT_PAYMENT",
                            amount = Math.abs(openingBalance),
                            balanceAfter = openingBalance,
                            description = "Opening Balance"
                        )
                    )
                }
                repository.logAudit(biz.id, "Owner", "Owner", "CUSTOMER_ADDED", "Added customer: $name ($cleanPhone)")
                selectedCustomer.value = newCustomer
                _currentScreen.value = AppScreen.CUSTOMER_DETAIL
                onResult(true, newCustomer)
            }
        }
    }

    fun updateCustomerProfile(
        customer: CustomerEntity,
        newName: String,
        newPhone: String,
        newAltPhone: String,
        newAddress: String,
        newCity: String,
        newNotes: String,
        newReminderSchedule: String,
        newProfileImageUrl: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val biz = _currentBusiness.value ?: run {
            onResult(false, "No active business selected")
            return
        }
        viewModelScope.launch {
            val cleanPhone = newPhone.replace("-", "").replace(" ", "").trim()
            if (cleanPhone != customer.phone && newPhone.trim() != customer.phone) {
                val existing = repository.customerDao.findCustomerByPhone(biz.id, cleanPhone)
                    ?: repository.customerDao.findCustomerByPhone(biz.id, newPhone.trim())
                if (existing != null && existing.id != customer.id) {
                    onResult(false, "Another customer already exists with phone $newPhone")
                    return@launch
                }
            }
            val updated = customer.copy(
                name = newName.trim(),
                phone = cleanPhone,
                alternatePhone = newAltPhone.trim(),
                whatsapp = cleanPhone,
                address = newAddress.trim(),
                city = newCity.trim(),
                notes = newNotes.trim(),
                reminderSchedule = newReminderSchedule,
                profileImageUrl = newProfileImageUrl,
                updatedAt = System.currentTimeMillis()
            )
            repository.customerDao.updateCustomer(updated)
            selectedCustomer.value = updated
            repository.logAudit(biz.id, "Owner", "Owner", "CUSTOMER_UPDATED", "Updated profile: ${updated.name} (${updated.phone})")
            onResult(true, null)
        }
    }

    fun updateCustomerPhoto(customer: CustomerEntity, photoUrl: String) {
        viewModelScope.launch {
            val updated = customer.copy(profileImageUrl = photoUrl, updatedAt = System.currentTimeMillis())
            repository.customerDao.updateCustomer(updated)
            if (selectedCustomer.value?.id == customer.id) {
                selectedCustomer.value = updated
            }
        }
    }

    fun removeCustomerPhoto(customer: CustomerEntity) {
        viewModelScope.launch {
            val updated = customer.copy(profileImageUrl = "", updatedAt = System.currentTimeMillis())
            repository.customerDao.updateCustomer(updated)
            if (selectedCustomer.value?.id == customer.id) {
                selectedCustomer.value = updated
            }
        }
    }

    fun updateBusinessProfile(
        name: String,
        ownerName: String,
        phone: String,
        whatsapp: String,
        address: String,
        city: String,
        category: String,
        currency: String,
        logoUrl: String,
        onResult: (Boolean) -> Unit
    ) {
        val biz = _currentBusiness.value ?: run {
            onResult(false)
            return
        }
        viewModelScope.launch {
            val updated = biz.copy(
                name = name.trim(),
                ownerName = ownerName.trim(),
                phone = phone.trim(),
                whatsapp = whatsapp.trim().ifEmpty { phone.trim() },
                address = address.trim(),
                city = city.trim(),
                category = category.trim(),
                currency = currency.trim(),
                logoUrl = logoUrl,
                updatedAt = System.currentTimeMillis()
            )
            repository.businessDao.updateBusiness(updated)
            _currentBusiness.value = updated
            repository.logAudit(biz.id, "Owner", "Owner", "BUSINESS_PROFILE_UPDATED", "Updated business profile: $name")
            onResult(true)
        }
    }

    fun removeBusinessLogo() {
        val biz = _currentBusiness.value ?: return
        viewModelScope.launch {
            val updated = biz.copy(logoUrl = "", updatedAt = System.currentTimeMillis())
            repository.businessDao.updateBusiness(updated)
            _currentBusiness.value = updated
        }
    }

    fun recordCustomerEntry(
        customerId: String,
        isGaveUdhaar: Boolean,
        amount: Double,
        description: String,
        paymentMethod: String = "Cash",
        billImageUrl: String = ""
    ) {
        val biz = _currentBusiness.value ?: return
        val customer = customers.value.find { it.id == customerId } ?: selectedCustomer.value ?: return
        viewModelScope.launch {
            repository.recordCustomerEntry(
                businessId = biz.id,
                customerId = customerId,
                customerName = customer.name,
                isGaveUdhaar = isGaveUdhaar,
                amount = amount,
                description = description,
                paymentMethod = paymentMethod,
                billImageUrl = billImageUrl
            )
            val updated = repository.customerDao.getCustomerById(customerId)
            if (selectedCustomer.value?.id == customerId) {
                selectedCustomer.value = updated
            }
            val uid = _currentUser.value?.id
            if (!uid.isNullOrBlank()) {
                if (updated != null) {
                    syncManager.firestoreSyncService.pushCustomer(updated, uid)
                }
                val latestTx = repository.customerLedgerDao.getLedgerForCustomer(customerId).firstOrNull()?.firstOrNull()
                if (latestTx != null) {
                    syncManager.firestoreSyncService.pushCustomerTransaction(latestTx, uid)
                }
            }
            Toast.makeText(getApplication(), "Entry saved: Rs. $amount", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateTransactionBill(entryId: String, billImageUrl: String) {
        viewModelScope.launch {
            repository.updateTransactionBill(entryId, billImageUrl)
            val uid = _currentUser.value?.id
            val tx = repository.customerLedgerDao.getLedgerEntryById(entryId)
            if (!uid.isNullOrBlank() && tx != null) {
                syncManager.firestoreSyncService.pushCustomerTransaction(tx, uid)
            }
            Toast.makeText(getApplication(), "Bill attachment updated!", Toast.LENGTH_SHORT).show()
        }
    }

    // Supplier Actions
    fun addSupplier(name: String, phone: String, openingBalance: Double, address: String) {
        val biz = _currentBusiness.value ?: return
        viewModelScope.launch {
            val supplier = SupplierEntity(
                businessId = biz.id,
                name = name,
                phone = phone,
                whatsapp = phone,
                address = address,
                openingBalance = openingBalance,
                currentBalance = openingBalance
            )
            repository.supplierDao.insertSupplier(supplier)
            val uid = _currentUser.value?.id
            if (!uid.isNullOrBlank()) {
                syncManager.firestoreSyncService.pushSupplier(supplier, uid)
            }
            if (openingBalance != 0.0) {
                val opEntry = SupplierLedgerEntry(
                    businessId = biz.id,
                    supplierId = supplier.id,
                    type = if (openingBalance > 0) "PURCHASE" else "PAID_PAYMENT",
                    amount = Math.abs(openingBalance),
                    balanceAfter = openingBalance,
                    description = "Opening Balance"
                )
                repository.supplierLedgerDao.insertLedgerEntry(opEntry)
                if (!uid.isNullOrBlank()) {
                    syncManager.firestoreSyncService.pushSupplierTransaction(opEntry, uid)
                }
            }
            repository.logAudit(biz.id, "Owner", "Owner", "SUPPLIER_ADDED", "Added supplier: $name ($phone)")
            Toast.makeText(getApplication(), "Supplier added: $name", Toast.LENGTH_SHORT).show()
        }
    }

    fun recordSupplierEntry(supplierId: String, isPaidPayment: Boolean, amount: Double, description: String) {
        val biz = _currentBusiness.value ?: return
        val supplier = suppliers.value.find { it.id == supplierId } ?: return
        viewModelScope.launch {
            repository.recordSupplierEntry(biz.id, supplierId, supplier.name, isPaidPayment, amount, description)
            val updated = repository.supplierDao.getSupplierById(supplierId)
            if (selectedSupplier.value?.id == supplierId) {
                selectedSupplier.value = updated
            }
            val uid = _currentUser.value?.id
            if (!uid.isNullOrBlank()) {
                if (updated != null) {
                    syncManager.firestoreSyncService.pushSupplier(updated, uid)
                }
                val latestTx = repository.supplierLedgerDao.getLedgerForSupplier(supplierId).firstOrNull()?.firstOrNull()
                if (latestTx != null) {
                    syncManager.firestoreSyncService.pushSupplierTransaction(latestTx, uid)
                }
            }
            Toast.makeText(getApplication(), "Entry saved: Rs. $amount", Toast.LENGTH_SHORT).show()
        }
    }

    // Expense Actions
    fun addExpense(category: String, amount: Double, description: String, accountId: String?) {
        val biz = _currentBusiness.value ?: return
        viewModelScope.launch {
            repository.recordExpense(biz.id, category, amount, description, accountId)
            Toast.makeText(getApplication(), "Expense recorded: Rs. $amount", Toast.LENGTH_SHORT).show()
        }
    }

    // WhatsApp Reminder Helper
    fun sendWhatsAppReminder(context: Context, customer: CustomerEntity) {
        val biz = _currentBusiness.value ?: return
        val lang = _language.value
        val formattedAmount = CurrencyUtils.format(customer.currentBalance)
        val message = when (lang) {
            AppLanguage.URDU -> "محترم ${customer.name} صاحب! ${biz.name} کی طرف سے ادائیگی کی یاد دہانی: آپ کے کھاتے کا بقایا $formattedAmount ہے۔ براہ مہربانی جلد ادائیگی فرمائیں۔ شکریہ!"
            AppLanguage.ROMAN_URDU -> "Mohtaram ${customer.name} sahab! ${biz.name} ki taraf se adaigi ka yaad dihani paigham: Aap ke khatey ka baqaya $formattedAmount hai. Baraye meherbani jald adaigi farmayein. Shukriya!"
            AppLanguage.ENGLISH -> "Dear ${customer.name}, Reminder from ${biz.name}: Your outstanding balance is $formattedAmount. Please arrange payment at your earliest convenience. Thank you!"
        }
        try {
            val phoneClean = customer.phone.replace("-", "").replace(" ", "").trim()
            val fullPhone = if (phoneClean.startsWith("0")) "92" + phoneClean.substring(1) else phoneClean
            val url = "https://api.whatsapp.com/send?phone=$fullPhone&text=${URLEncoder.encode(message, "UTF-8")}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp. Opening share...", Toast.LENGTH_SHORT).show()
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share reminder"))
        }
    }
}
