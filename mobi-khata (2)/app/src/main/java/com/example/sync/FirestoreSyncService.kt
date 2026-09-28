package com.example.sync

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncService(private val context: Context, private val db: AppDatabase) {

    companion object {
        private const val TAG = "FirestoreSyncService"
    }

    private val activeListeners = mutableListOf<ListenerRegistration>()

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not available: ${e.message}")
            null
        }
    }

    // ==================== LOCAL TO CLOUD PUSH ====================

    suspend fun pushUser(user: UserEntity) = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext
        try {
            val userMap = hashMapOf<String, Any>(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "profileImageUrl" to user.profileImageUrl,
                "authProvider" to user.authProvider,
                "isEmailVerified" to user.isEmailVerified,
                "isPhoneVerified" to user.isPhoneVerified,
                "createdAt" to user.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(user.id)
                .set(userMap, SetOptions.merge())
                .await()
            Log.d(TAG, "Pushed user ${user.id} to Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push user to Firestore: ${e.message}")
        }
    }

    suspend fun pushBusiness(business: BusinessEntity, uid: String) = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext
        try {
            val map = hashMapOf<String, Any>(
                "id" to business.id,
                "ownerUserId" to uid,
                "name" to business.name,
                "ownerName" to business.ownerName,
                "phone" to business.phone,
                "whatsapp" to business.whatsapp,
                "address" to business.address,
                "city" to business.city,
                "category" to business.category,
                "currency" to business.currency,
                "invoicePrefix" to business.invoicePrefix,
                "financialYear" to business.financialYear,
                "logoUrl" to business.logoUrl,
                "preferredLanguage" to business.preferredLanguage,
                "createdAt" to business.createdAt,
                "updatedAt" to business.updatedAt
            )
            firestore.collection("users").document(uid)
                .collection("businesses").document(business.id)
                .set(map, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push business: ${e.message}")
        }
    }

    suspend fun pushCustomer(customer: CustomerEntity, uid: String) = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext
        try {
            val map = hashMapOf<String, Any>(
                "id" to customer.id,
                "businessId" to customer.businessId,
                "name" to customer.name,
                "phone" to customer.phone,
                "alternatePhone" to customer.alternatePhone,
                "whatsapp" to customer.whatsapp,
                "address" to customer.address,
                "city" to customer.city,
                "customerType" to customer.customerType,
                "profileImageUrl" to customer.profileImageUrl,
                "openingBalance" to customer.openingBalance,
                "currentBalance" to customer.currentBalance,
                "language" to customer.language,
                "notes" to customer.notes,
                "reminderSchedule" to customer.reminderSchedule,
                "createdAt" to customer.createdAt,
                "updatedAt" to customer.updatedAt
            )
            firestore.collection("users").document(uid)
                .collection("businesses").document(customer.businessId)
                .collection("customers").document(customer.id)
                .set(map, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push customer: ${e.message}")
        }
    }

    suspend fun pushCustomerTransaction(entry: CustomerLedgerEntry, uid: String) = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext
        try {
            val map = hashMapOf<String, Any>(
                "id" to entry.id,
                "businessId" to entry.businessId,
                "customerId" to entry.customerId,
                "date" to entry.date,
                "type" to entry.type,
                "amount" to entry.amount,
                "balanceAfter" to entry.balanceAfter,
                "referenceNo" to entry.referenceNo,
                "description" to entry.description,
                "paymentMethod" to entry.paymentMethod,
                "billImageUrl" to entry.billImageUrl,
                "createdAt" to entry.createdAt,
                "updatedAt" to entry.date
            )
            firestore.collection("users").document(uid)
                .collection("businesses").document(entry.businessId)
                .collection("transactions").document(entry.id)
                .set(map, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push transaction: ${e.message}")
        }
    }

    suspend fun pushSupplier(supplier: SupplierEntity, uid: String) = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext
        try {
            val map = hashMapOf<String, Any>(
                "id" to supplier.id,
                "businessId" to supplier.businessId,
                "name" to supplier.name,
                "phone" to supplier.phone,
                "whatsapp" to supplier.whatsapp,
                "address" to supplier.address,
                "openingBalance" to supplier.openingBalance,
                "currentBalance" to supplier.currentBalance,
                "notes" to supplier.notes,
                "createdAt" to supplier.createdAt,
                "updatedAt" to supplier.updatedAt
            )
            firestore.collection("users").document(uid)
                .collection("businesses").document(supplier.businessId)
                .collection("suppliers").document(supplier.id)
                .set(map, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push supplier: ${e.message}")
        }
    }

    suspend fun pushSupplierTransaction(entry: SupplierLedgerEntry, uid: String) = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext
        try {
            val map = hashMapOf<String, Any>(
                "id" to entry.id,
                "businessId" to entry.businessId,
                "supplierId" to entry.supplierId,
                "date" to entry.date,
                "type" to entry.type,
                "amount" to entry.amount,
                "balanceAfter" to entry.balanceAfter,
                "referenceNo" to entry.referenceNo,
                "description" to entry.description,
                "paymentMethod" to entry.paymentMethod,
                "createdAt" to entry.createdAt,
                "updatedAt" to entry.date
            )
            firestore.collection("users").document(uid)
                .collection("businesses").document(entry.businessId)
                .collection("supplier_transactions").document(entry.id)
                .set(map, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push supplier transaction: ${e.message}")
        }
    }

    suspend fun pushExpense(expense: ExpenseEntity, uid: String) = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext
        try {
            val map = hashMapOf<String, Any>(
                "id" to expense.id,
                "businessId" to expense.businessId,
                "category" to expense.category,
                "amount" to expense.amount,
                "accountId" to expense.accountId,
                "date" to expense.date,
                "description" to expense.description,
                "notes" to expense.notes,
                "createdAt" to expense.createdAt,
                "updatedAt" to expense.date
            )
            firestore.collection("users").document(uid)
                .collection("businesses").document(expense.businessId)
                .collection("expenses").document(expense.id)
                .set(map, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push expense: ${e.message}")
        }
    }

    suspend fun pushAccount(account: AccountEntity, uid: String) = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext
        try {
            val map = hashMapOf<String, Any>(
                "id" to account.id,
                "businessId" to account.businessId,
                "name" to account.name,
                "type" to account.type,
                "accountNumber" to account.accountNumber,
                "balance" to account.balance,
                "isDefault" to account.isDefault,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid)
                .collection("businesses").document(account.businessId)
                .collection("cash_accounts").document(account.id)
                .set(map, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push account: ${e.message}")
        }
    }

    suspend fun pushAuditLog(log: AuditLogEntity, uid: String) = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext
        try {
            val map = hashMapOf<String, Any>(
                "id" to log.id,
                "businessId" to log.businessId,
                "userId" to log.userId,
                "userName" to log.userName,
                "action" to log.action,
                "details" to log.details,
                "timestamp" to log.timestamp,
                "createdAt" to log.timestamp,
                "updatedAt" to log.timestamp
            )
            firestore.collection("users").document(uid)
                .collection("businesses").document(log.businessId)
                .collection("audit_logs").document(log.id)
                .set(map, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push audit log: ${e.message}")
        }
    }

    // ==================== CLOUD TO LOCAL PULL (MULTI-DEVICE SYNC) ====================

    suspend fun pullAllCloudData(uid: String): Int = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext 0
        var totalRecordsSynced = 0

        try {
            // 1. Pull user profile
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val name = userDoc.getString("name") ?: "Store Owner"
                val email = userDoc.getString("email") ?: ""
                val phone = userDoc.getString("phone") ?: ""
                val photo = userDoc.getString("profileImageUrl") ?: ""
                val authProvider = userDoc.getString("authProvider") ?: "EMAIL"
                val isEmailVerified = userDoc.getBoolean("isEmailVerified") ?: true
                val isPhoneVerified = userDoc.getBoolean("isPhoneVerified") ?: false

                val existingUser = db.userDao().getUserById(uid)
                val userEntity = UserEntity(
                    id = uid,
                    name = name,
                    email = email,
                    phone = phone,
                    profileImageUrl = photo,
                    authProvider = authProvider,
                    isEmailVerified = isEmailVerified,
                    isPhoneVerified = isPhoneVerified
                )
                if (existingUser != null) {
                    db.userDao().updateUser(userEntity)
                } else {
                    db.userDao().insertUser(userEntity)
                }
                totalRecordsSynced++
            }

            // 2. Pull businesses
            val businessesSnapshot = firestore.collection("users").document(uid)
                .collection("businesses").get().await()

            for (bDoc in businessesSnapshot.documents) {
                val bizId = bDoc.id
                val bizName = bDoc.getString("name") ?: "My Shop"
                val ownerName = bDoc.getString("ownerName") ?: "Owner"
                val phone = bDoc.getString("phone") ?: ""
                val whatsapp = bDoc.getString("whatsapp") ?: ""
                val address = bDoc.getString("address") ?: ""
                val city = bDoc.getString("city") ?: "Lahore"
                val category = bDoc.getString("category") ?: "General Store"
                val currency = bDoc.getString("currency") ?: "PKR"
                val invoicePrefix = bDoc.getString("invoicePrefix") ?: "MK-"
                val financialYear = bDoc.getString("financialYear") ?: "2026-2027"
                val logoUrl = bDoc.getString("logoUrl") ?: ""
                val preferredLanguage = bDoc.getString("preferredLanguage") ?: "en"
                val createdAt = bDoc.getLong("createdAt") ?: System.currentTimeMillis()
                val updatedAt = bDoc.getLong("updatedAt") ?: System.currentTimeMillis()

                val business = BusinessEntity(
                    id = bizId,
                    ownerUserId = uid,
                    name = bizName,
                    ownerName = ownerName,
                    phone = phone,
                    whatsapp = whatsapp,
                    address = address,
                    city = city,
                    category = category,
                    currency = currency,
                    invoicePrefix = invoicePrefix,
                    financialYear = financialYear,
                    logoUrl = logoUrl,
                    preferredLanguage = preferredLanguage,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
                db.businessDao().insertBusiness(business)
                totalRecordsSynced++

                val bizRef = bDoc.reference

                // 2a. Pull customers
                val custSnapshot = bizRef.collection("customers").get().await()
                for (cDoc in custSnapshot.documents) {
                    val cId = cDoc.id
                    val remoteUpdatedAt = cDoc.getLong("updatedAt") ?: 0L
                    val local = db.customerDao().getCustomerById(cId)
                    if (local == null || remoteUpdatedAt >= local.updatedAt) {
                        val customer = CustomerEntity(
                            id = cId,
                            businessId = bizId,
                            name = cDoc.getString("name") ?: "Customer",
                            phone = cDoc.getString("phone") ?: "",
                            alternatePhone = cDoc.getString("alternatePhone") ?: "",
                            whatsapp = cDoc.getString("whatsapp") ?: "",
                            address = cDoc.getString("address") ?: "",
                            city = cDoc.getString("city") ?: "",
                            customerType = cDoc.getString("customerType") ?: "Retail",
                            profileImageUrl = cDoc.getString("profileImageUrl") ?: "",
                            openingBalance = cDoc.getDouble("openingBalance") ?: 0.0,
                            currentBalance = cDoc.getDouble("currentBalance") ?: 0.0,
                            language = cDoc.getString("language") ?: "ur",
                            notes = cDoc.getString("notes") ?: "",
                            reminderSchedule = cDoc.getString("reminderSchedule") ?: "WEEKLY",
                            createdAt = cDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                            updatedAt = remoteUpdatedAt
                        )
                        db.customerDao().insertCustomer(customer)
                        totalRecordsSynced++
                    }
                }

                // 2b. Pull transactions (customer ledger)
                val txSnapshot = bizRef.collection("transactions").get().await()
                for (tDoc in txSnapshot.documents) {
                    val tId = tDoc.id
                    val existingTx = db.customerLedgerDao().getLedgerEntryById(tId)
                    if (existingTx == null) {
                        val entry = CustomerLedgerEntry(
                            id = tId,
                            businessId = bizId,
                            customerId = tDoc.getString("customerId") ?: "",
                            date = tDoc.getLong("date") ?: System.currentTimeMillis(),
                            type = tDoc.getString("type") ?: "GAVE_UDHAAR",
                            amount = tDoc.getDouble("amount") ?: 0.0,
                            balanceAfter = tDoc.getDouble("balanceAfter") ?: 0.0,
                            referenceNo = tDoc.getString("referenceNo") ?: "",
                            description = tDoc.getString("description") ?: "",
                            paymentMethod = tDoc.getString("paymentMethod") ?: "Cash",
                            billImageUrl = tDoc.getString("billImageUrl") ?: "",
                            createdAt = tDoc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                        db.customerLedgerDao().insertLedgerEntry(entry)
                        totalRecordsSynced++
                    }
                }

                // 2c. Pull suppliers
                val supSnapshot = bizRef.collection("suppliers").get().await()
                for (sDoc in supSnapshot.documents) {
                    val sId = sDoc.id
                    val remoteUpdatedAt = sDoc.getLong("updatedAt") ?: 0L
                    val localSup = db.supplierDao().getSupplierById(sId)
                    if (localSup == null || remoteUpdatedAt >= localSup.updatedAt) {
                        val supplier = SupplierEntity(
                            id = sId,
                            businessId = bizId,
                            name = sDoc.getString("name") ?: "Supplier",
                            phone = sDoc.getString("phone") ?: "",
                            whatsapp = sDoc.getString("whatsapp") ?: "",
                            address = sDoc.getString("address") ?: "",
                            openingBalance = sDoc.getDouble("openingBalance") ?: 0.0,
                            currentBalance = sDoc.getDouble("currentBalance") ?: 0.0,
                            notes = sDoc.getString("notes") ?: "",
                            createdAt = sDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                            updatedAt = remoteUpdatedAt
                        )
                        db.supplierDao().insertSupplier(supplier)
                        totalRecordsSynced++
                    }
                }

                // 2d. Pull supplier transactions
                val supTxSnapshot = bizRef.collection("supplier_transactions").get().await()
                for (stDoc in supTxSnapshot.documents) {
                    val stId = stDoc.id
                    val entry = SupplierLedgerEntry(
                        id = stId,
                        businessId = bizId,
                        supplierId = stDoc.getString("supplierId") ?: "",
                        date = stDoc.getLong("date") ?: System.currentTimeMillis(),
                        type = stDoc.getString("type") ?: "PURCHASE",
                        amount = stDoc.getDouble("amount") ?: 0.0,
                        balanceAfter = stDoc.getDouble("balanceAfter") ?: 0.0,
                        referenceNo = stDoc.getString("referenceNo") ?: "",
                        description = stDoc.getString("description") ?: "",
                        paymentMethod = stDoc.getString("paymentMethod") ?: "Cash",
                        createdAt = stDoc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                    db.supplierLedgerDao().insertLedgerEntry(entry)
                    totalRecordsSynced++
                }

                // 2e. Pull expenses
                val expSnapshot = bizRef.collection("expenses").get().await()
                for (eDoc in expSnapshot.documents) {
                    val eId = eDoc.id
                    val expense = ExpenseEntity(
                        id = eId,
                        businessId = bizId,
                        category = eDoc.getString("category") ?: "Other",
                        amount = eDoc.getDouble("amount") ?: 0.0,
                        accountId = eDoc.getString("accountId") ?: "",
                        date = eDoc.getLong("date") ?: System.currentTimeMillis(),
                        description = eDoc.getString("description") ?: "",
                        notes = eDoc.getString("notes") ?: "",
                        createdAt = eDoc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                    db.expenseDao().insertExpense(expense)
                    totalRecordsSynced++
                }

                // 2f. Pull cash accounts
                val accSnapshot = bizRef.collection("cash_accounts").get().await()
                for (aDoc in accSnapshot.documents) {
                    val aId = aDoc.id
                    val account = AccountEntity(
                        id = aId,
                        businessId = bizId,
                        name = aDoc.getString("name") ?: "Cash",
                        type = aDoc.getString("type") ?: "CASH",
                        accountNumber = aDoc.getString("accountNumber") ?: "",
                        balance = aDoc.getDouble("balance") ?: 0.0,
                        isDefault = aDoc.getBoolean("isDefault") ?: false
                    )
                    db.accountDao().insertAccount(account)
                    totalRecordsSynced++
                }

                // 2g. Pull audit logs
                val auditSnapshot = bizRef.collection("audit_logs").get().await()
                for (lDoc in auditSnapshot.documents) {
                    val lId = lDoc.id
                    val log = AuditLogEntity(
                        id = lId,
                        businessId = bizId,
                        userId = lDoc.getString("userId") ?: "Owner",
                        userName = lDoc.getString("userName") ?: "Owner",
                        action = lDoc.getString("action") ?: "ACTION",
                        details = lDoc.getString("details") ?: "",
                        timestamp = lDoc.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                    db.auditLogDao().insertAuditLog(log)
                    totalRecordsSynced++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling cloud data: ${e.message}", e)
        }

        totalRecordsSynced
    }

    // ==================== REAL-TIME LISTENERS (MULTI-DEVICE) ====================

    fun attachRealtimeListeners(
        uid: String,
        businessId: String,
        onRemoteUpdate: () -> Unit
    ) {
        val firestore = getFirestore() ?: return
        detachRealtimeListeners()

        try {
            val bizRef = firestore.collection("users").document(uid)
                .collection("businesses").document(businessId)

            // Customers listener
            val custListener = bizRef.collection("customers").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                if (!snapshot.metadata.hasPendingWrites()) {
                    // Update came from cloud
                    onRemoteUpdate()
                }
            }
            activeListeners.add(custListener)

            // Transactions listener
            val txListener = bizRef.collection("transactions").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                if (!snapshot.metadata.hasPendingWrites()) {
                    onRemoteUpdate()
                }
            }
            activeListeners.add(txListener)

            // Suppliers listener
            val supListener = bizRef.collection("suppliers").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                if (!snapshot.metadata.hasPendingWrites()) {
                    onRemoteUpdate()
                }
            }
            activeListeners.add(supListener)

            Log.d(TAG, "Attached real-time cloud listeners for business $businessId")
        } catch (e: Exception) {
            Log.w(TAG, "Could not attach realtime listeners: ${e.message}")
        }
    }

    fun detachRealtimeListeners() {
        for (listener in activeListeners) {
            try {
                listener.remove()
            } catch (_: Exception) {}
        }
        activeListeners.clear()
        Log.d(TAG, "Detached all Firestore realtime listeners")
    }
}
