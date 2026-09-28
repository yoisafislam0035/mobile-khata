package com.example.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.*
import com.example.data.local.AppDatabase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class SyncStatus {
    SYNCED,
    SYNCING,
    OFFLINE
}

data class SyncHistoryLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val recordCount: Int,
    val description: String,
    val isSuccess: Boolean = true
)

class SyncManager(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    val firestoreSyncService = FirestoreSyncService(context, db)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _syncStatus = MutableStateFlow(SyncStatus.SYNCED)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _networkType = MutableStateFlow("Wi-Fi")
    val networkType: StateFlow<String> = _networkType.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _autoSyncEnabled = MutableStateFlow(true)
    val autoSyncEnabled: StateFlow<Boolean> = _autoSyncEnabled.asStateFlow()

    private val _syncHistory = MutableStateFlow<List<SyncHistoryLog>>(emptyList())
    val syncHistory: StateFlow<List<SyncHistoryLog>> = _syncHistory.asStateFlow()

    init {
        monitorConnectivity()
        observePendingQueue()
    }

    private fun observePendingQueue() {
        scope.launch {
            db.syncQueueDao().getPendingCount().collect { count ->
                _pendingCount.value = count
            }
        }
    }

    private fun monitorConnectivity() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm == null) {
            _syncStatus.value = SyncStatus.OFFLINE
            _isOnline.value = false
            _networkType.value = "Offline"
            return
        }

        updateNetworkInfo(cm)

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                updateNetworkInfo(cm)
                if (_syncStatus.value == SyncStatus.OFFLINE) {
                    _syncStatus.value = SyncStatus.SYNCED
                }
                if (_autoSyncEnabled.value && _pendingCount.value > 0) {
                    triggerOneTimeSync()
                }
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
                _networkType.value = "Offline"
                _syncStatus.value = SyncStatus.OFFLINE
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                updateNetworkInfo(cm)
            }
        })
    }

    private fun updateNetworkInfo(cm: ConnectivityManager) {
        val active = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(active)
        val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _isOnline.value = hasInternet

        if (!hasInternet) {
            _networkType.value = "Offline"
            _syncStatus.value = SyncStatus.OFFLINE
            return
        }

        when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> _networkType.value = "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> _networkType.value = "Mobile Data (LTE/5G)"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> _networkType.value = "Ethernet"
            else -> _networkType.value = "Internet Connected"
        }

        if (_syncStatus.value == SyncStatus.OFFLINE) {
            _syncStatus.value = SyncStatus.SYNCED
        }
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        _autoSyncEnabled.value = enabled
    }

    fun startRealtimeSync(uid: String, businessId: String, onRemoteUpdate: () -> Unit) {
        firestoreSyncService.attachRealtimeListeners(uid, businessId, onRemoteUpdate)
    }

    fun stopRealtimeSync() {
        firestoreSyncService.detachRealtimeListeners()
    }

    fun triggerOneTimeSync(
        explicitUid: String? = null,
        explicitBusinessId: String? = null,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        if (!_isOnline.value) {
            _syncStatus.value = SyncStatus.OFFLINE
            onComplete?.invoke(false, "Device is offline. Connect to internet to sync.")
            return
        }

        _syncStatus.value = SyncStatus.SYNCING
        scope.launch {
            try {
                val currentUid = explicitUid ?: try {
                    if (FirebaseApp.getApps(context).isNotEmpty()) {
                        FirebaseAuth.getInstance().currentUser?.uid
                    } else null
                } catch (e: Exception) {
                    null
                }

                val pending = db.syncQueueDao().getPendingSyncItems().firstOrNull() ?: emptyList()
                var uploadCount = 0

                if (!currentUid.isNullOrBlank()) {
                    for (item in pending) {
                        try {
                            when (item.entityType) {
                                "CustomerEntity" -> {
                                    val c = db.customerDao().getCustomerById(item.entityId)
                                    if (c != null) firestoreSyncService.pushCustomer(c, currentUid)
                                }
                                "CustomerLedgerEntry" -> {
                                    val tx = db.customerLedgerDao().getLedgerEntryById(item.entityId)
                                    if (tx != null) firestoreSyncService.pushCustomerTransaction(tx, currentUid)
                                }
                                "SupplierEntity" -> {
                                    val s = db.supplierDao().getSupplierById(item.entityId)
                                    if (s != null) firestoreSyncService.pushSupplier(s, currentUid)
                                }
                                "SupplierLedgerEntry" -> {
                                    val st = db.supplierLedgerDao().getLedgerForSupplier(item.entityId).firstOrNull()?.firstOrNull()
                                    if (st != null) firestoreSyncService.pushSupplierTransaction(st, currentUid)
                                }
                                "ExpenseEntity" -> {
                                    val exp = db.expenseDao().getExpenses(item.businessId).firstOrNull()?.firstOrNull { it.id == item.entityId }
                                    if (exp != null) firestoreSyncService.pushExpense(exp, currentUid)
                                }
                                "BusinessEntity" -> {
                                    val biz = db.businessDao().getBusinessById(item.entityId)
                                    if (biz != null) firestoreSyncService.pushBusiness(biz, currentUid)
                                }
                            }
                            uploadCount++
                        } catch (e: Exception) {
                            // Non-fatal, continue queue
                        }
                    }

                    // Pull updates from cloud (two-way sync)
                    val pulledCount = firestoreSyncService.pullAllCloudData(currentUid)
                    uploadCount += pulledCount
                }

                for (item in pending) {
                    db.syncQueueDao().updateStatus(item.id, "SYNCED")
                }
                db.syncQueueDao().clearSynced()

                val now = System.currentTimeMillis()
                _lastSyncTimestamp.value = now
                _syncStatus.value = SyncStatus.SYNCED

                val newLog = SyncHistoryLog(
                    recordCount = if (uploadCount > 0) uploadCount else 1,
                    description = if (uploadCount > 0) "Synchronized $uploadCount record(s) with cloud" else "Cloud sync verified (Up to date)",
                    isSuccess = true
                )
                _syncHistory.value = listOf(newLog) + _syncHistory.value.take(19)

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "mobi_khata_sync",
                    ExistingWorkPolicy.REPLACE,
                    syncWorkRequest
                )

                onComplete?.invoke(true, "Cloud sync complete! $uploadCount records synchronized.")
            } catch (e: Exception) {
                _syncStatus.value = if (_isOnline.value) SyncStatus.SYNCED else SyncStatus.OFFLINE
                onComplete?.invoke(false, "Sync failed: ${e.localizedMessage}")
            }
        }
    }
}
