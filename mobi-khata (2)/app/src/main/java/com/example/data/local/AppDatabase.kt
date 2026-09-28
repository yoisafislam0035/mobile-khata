package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        BusinessEntity::class,
        CustomerEntity::class,
        CustomerLedgerEntry::class,
        SupplierEntity::class,
        SupplierLedgerEntry::class,
        CategoryEntity::class,
        ProductEntity::class,
        InventoryMovementEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        AccountEntity::class,
        PaymentEntity::class,
        ExpenseEntity::class,
        AuditLogEntity::class,
        SyncQueueEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun businessDao(): BusinessDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerLedgerDao(): CustomerLedgerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun supplierLedgerDao(): SupplierLedgerDao
    abstract fun productDao(): ProductDao
    abstract fun inventoryMovementDao(): InventoryMovementDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun accountDao(): AccountDao
    abstract fun paymentDao(): PaymentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mobi_khata.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
