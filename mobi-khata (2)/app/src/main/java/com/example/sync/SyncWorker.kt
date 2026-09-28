package com.example.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(applicationContext)
            val currentUid = try {
                if (FirebaseApp.getApps(applicationContext).isNotEmpty()) {
                    FirebaseAuth.getInstance().currentUser?.uid
                } else null
            } catch (e: Exception) {
                null
            }

            if (!currentUid.isNullOrBlank()) {
                val syncService = FirestoreSyncService(applicationContext, db)
                val pending = db.syncQueueDao().getPendingSyncItems().firstOrNull() ?: emptyList()
                for (item in pending) {
                    try {
                        when (item.entityType) {
                            "CustomerEntity" -> {
                                val c = db.customerDao().getCustomerById(item.entityId)
                                if (c != null) syncService.pushCustomer(c, currentUid)
                            }
                            "CustomerLedgerEntry" -> {
                                val tx = db.customerLedgerDao().getLedgerEntryById(item.entityId)
                                if (tx != null) syncService.pushCustomerTransaction(tx, currentUid)
                            }
                            "SupplierEntity" -> {
                                val s = db.supplierDao().getSupplierById(item.entityId)
                                if (s != null) syncService.pushSupplier(s, currentUid)
                            }
                            "SupplierLedgerEntry" -> {
                                val st = db.supplierLedgerDao().getLedgerForSupplier(item.entityId).firstOrNull()?.firstOrNull()
                                if (st != null) syncService.pushSupplierTransaction(st, currentUid)
                            }
                            "ExpenseEntity" -> {
                                val exp = db.expenseDao().getExpenses(item.businessId).firstOrNull()?.firstOrNull { it.id == item.entityId }
                                if (exp != null) syncService.pushExpense(exp, currentUid)
                            }
                            "BusinessEntity" -> {
                                val biz = db.businessDao().getBusinessById(item.entityId)
                                if (biz != null) syncService.pushBusiness(biz, currentUid)
                            }
                        }
                    } catch (_: Exception) {}
                    db.syncQueueDao().updateStatus(item.id, "SYNCED")
                }
                syncService.pullAllCloudData(currentUid)
            } else {
                val pendingItems = db.syncQueueDao().getPendingSyncItems().firstOrNull() ?: emptyList()
                for (item in pendingItems) {
                    db.syncQueueDao().updateStatus(item.id, "SYNCED")
                }
            }
            db.syncQueueDao().clearSynced()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
