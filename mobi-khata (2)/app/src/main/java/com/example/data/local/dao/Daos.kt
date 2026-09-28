package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses WHERE ownerUserId = :ownerId ORDER BY createdAt ASC")
    fun getBusinessesForUser(ownerId: String): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
    suspend fun getBusinessById(id: String): BusinessEntity?

    @Query("SELECT * FROM businesses ORDER BY createdAt ASC LIMIT 1")
    suspend fun getFirstBusiness(): BusinessEntity?

    @Query("SELECT COUNT(*) FROM businesses WHERE ownerUserId = :ownerId")
    suspend fun getBusinessCountForUser(ownerId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    @Query("DELETE FROM businesses WHERE id = :id")
    suspend fun deleteBusinessById(id: String)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE businessId = :businessId ORDER BY updatedAt DESC")
    fun getCustomers(businessId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE businessId = :businessId AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%')")
    fun searchCustomers(businessId: String, query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE businessId = :businessId AND phone = :phone LIMIT 1")
    suspend fun findCustomerByPhone(businessId: String, phone: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET currentBalance = currentBalance + :amountDiff, updatedAt = :updatedAt WHERE id = :customerId")
    suspend fun adjustCustomerBalance(customerId: String, amountDiff: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: String)
}

@Dao
interface CustomerLedgerDao {
    @Query("SELECT * FROM customer_ledger WHERE customerId = :customerId ORDER BY date DESC, createdAt DESC")
    fun getLedgerForCustomer(customerId: String): Flow<List<CustomerLedgerEntry>>

    @Query("SELECT * FROM customer_ledger WHERE businessId = :businessId ORDER BY date DESC, createdAt DESC")
    fun getAllLedgerForBusiness(businessId: String): Flow<List<CustomerLedgerEntry>>

    @Query("SELECT * FROM customer_ledger WHERE id = :id LIMIT 1")
    suspend fun getLedgerEntryById(id: String): CustomerLedgerEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: CustomerLedgerEntry)

    @Update
    suspend fun updateLedgerEntry(entry: CustomerLedgerEntry)

    @Query("DELETE FROM customer_ledger WHERE id = :id")
    suspend fun deleteLedgerEntry(id: String)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers WHERE businessId = :businessId ORDER BY updatedAt DESC")
    fun getSuppliers(businessId: String): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE businessId = :businessId AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%')")
    fun searchSuppliers(businessId: String, query: String): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getSupplierById(id: String): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Query("UPDATE suppliers SET currentBalance = currentBalance + :amountDiff, updatedAt = :updatedAt WHERE id = :supplierId")
    suspend fun adjustSupplierBalance(supplierId: String, amountDiff: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteSupplierById(id: String)
}

@Dao
interface SupplierLedgerDao {
    @Query("SELECT * FROM supplier_ledger WHERE supplierId = :supplierId ORDER BY date DESC, createdAt DESC")
    fun getLedgerForSupplier(supplierId: String): Flow<List<SupplierLedgerEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: SupplierLedgerEntry)

    @Query("DELETE FROM supplier_ledger WHERE id = :id")
    suspend fun deleteLedgerEntry(id: String)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE businessId = :businessId ORDER BY name ASC")
    fun getProducts(businessId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE businessId = :businessId AND (name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%')")
    fun searchProducts(businessId: String, query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE businessId = :businessId AND barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(businessId: String, barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE businessId = :businessId AND currentStock <= minStockLevel")
    fun getLowStockProducts(businessId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET currentStock = currentStock + :delta, updatedAt = :timestamp WHERE id = :productId")
    suspend fun updateStock(productId: String, delta: Double, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)
}

@Dao
interface InventoryMovementDao {
    @Query("SELECT * FROM inventory_movements WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getMovements(businessId: String): Flow<List<InventoryMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: InventoryMovementEntity)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales WHERE businessId = :businessId ORDER BY date DESC, createdAt DESC")
    fun getSales(businessId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE businessId = :businessId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSalesBetweenDates(businessId: String, startDate: Long, endDate: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleById(id: String): SaleEntity?

    @Query("SELECT COUNT(*) FROM sales WHERE businessId = :businessId")
    suspend fun getSaleCount(businessId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: String): List<SaleItemEntity>

    @Query("UPDATE sales SET status = 'RETURNED' WHERE id = :saleId")
    suspend fun markSaleReturned(saleId: String)
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases WHERE businessId = :businessId ORDER BY date DESC, createdAt DESC")
    fun getPurchases(businessId: String): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE businessId = :businessId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getPurchasesBetweenDates(businessId: String, startDate: Long, endDate: Long): Flow<List<PurchaseEntity>>

    @Query("SELECT COUNT(*) FROM purchases WHERE businessId = :businessId")
    suspend fun getPurchaseCount(businessId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getPurchaseItems(purchaseId: String): List<PurchaseItemEntity>
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE businessId = :businessId ORDER BY isDefault DESC, name ASC")
    fun getAccounts(businessId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE businessId = :businessId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultAccount(businessId: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = balance + :amountDiff WHERE id = :accountId")
    suspend fun updateBalance(accountId: String, amountDiff: Double)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE businessId = :businessId ORDER BY date DESC, createdAt DESC")
    fun getPayments(businessId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE businessId = :businessId ORDER BY date DESC, createdAt DESC")
    fun getExpenses(businessId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE businessId = :businessId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesBetweenDates(businessId: String, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: String)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs WHERE businessId = :businessId ORDER BY timestamp DESC LIMIT 200")
    fun getAuditLogs(businessId: String): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingSyncItems(): Flow<List<SyncQueueEntity>>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = :status, retryCount = retryCount + 1 WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
    suspend fun clearSynced()
}
