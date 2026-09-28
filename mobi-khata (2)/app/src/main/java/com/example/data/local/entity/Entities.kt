package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(), // Firebase UID or local UUID
    val name: String,
    val email: String,
    val phone: String = "",
    val profileImageUrl: String = "",
    val pinHash: String = "",
    val authProvider: String = "EMAIL", // "EMAIL", "GOOGLE", "PHONE"
    val isPhoneVerified: Boolean = false,
    val isEmailVerified: Boolean = false,
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "businesses",
    indices = [Index(value = ["ownerUserId"])]
)
data class BusinessEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val ownerUserId: String,
    val name: String,
    val ownerName: String,
    val phone: String = "",
    val whatsapp: String = "",
    val address: String = "",
    val city: String = "Lahore",
    val category: String = "General Store",
    val currency: String = "PKR",
    val invoicePrefix: String = "MK-",
    val financialYear: String = "2026-2027",
    val logoUrl: String = "",
    val preferredLanguage: String = "en",
    val negativeInventoryAllowed: Boolean = false,
    val defaultTaxRate: Double = 0.0,
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "customers",
    indices = [Index(value = ["businessId", "phone"])]
)
data class CustomerEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val name: String,
    val phone: String,
    val alternatePhone: String = "",
    val whatsapp: String = "",
    val address: String = "",
    val city: String = "",
    val customerType: String = "Retail",
    val profileImageUrl: String = "",
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0, // Positive = Customer owes you (Udhaar/Receivable), Negative = You owe customer (Advance/Payable)
    val language: String = "ur",
    val notes: String = "",
    val reminderSchedule: String = "WEEKLY", // "NONE", "DAILY", "WEEKLY", "MONTHLY"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "customer_ledger",
    indices = [Index(value = ["businessId", "customerId"])]
)
data class CustomerLedgerEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val customerId: String,
    val date: Long = System.currentTimeMillis(),
    val type: String, // "GAVE_UDHAAR", "GOT_PAYMENT", "SALE", "RETURN", "ADJUSTMENT"
    val amount: Double,
    val balanceAfter: Double,
    val referenceNo: String = "",
    val description: String = "",
    val paymentMethod: String = "Cash",
    val billImageUrl: String = "", // Attached bill / receipt image URI or path
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "suppliers",
    indices = [Index(value = ["businessId", "phone"])]
)
data class SupplierEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val name: String,
    val phone: String,
    val whatsapp: String = "",
    val address: String = "",
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0, // Positive = You owe supplier (Payable), Negative = Supplier owes you
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "supplier_ledger",
    indices = [Index(value = ["businessId", "supplierId"])]
)
data class SupplierLedgerEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val supplierId: String,
    val date: Long = System.currentTimeMillis(),
    val type: String, // "PURCHASE", "PAID_PAYMENT", "RETURN", "ADJUSTMENT"
    val amount: Double,
    val balanceAfter: Double,
    val referenceNo: String = "",
    val description: String = "",
    val paymentMethod: String = "Cash",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["businessId"])]
)
data class CategoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val name: String
)

@Entity(
    tableName = "products",
    indices = [Index(value = ["businessId", "barcode"])]
)
data class ProductEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val categoryId: String = "",
    val categoryName: String = "General",
    val unit: String = "Pcs",
    val purchasePrice: Double,
    val salePrice: Double,
    val currentStock: Double,
    val minStockLevel: Double = 5.0,
    val supplierId: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "inventory_movements",
    indices = [Index(value = ["businessId", "productId"])]
)
data class InventoryMovementEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val productId: String,
    val type: String, // "SALE", "PURCHASE", "RETURN_IN", "RETURN_OUT", "DAMAGE", "EXPIRED", "ADJUSTMENT"
    val quantityChange: Double,
    val quantityAfter: Double,
    val reason: String = "",
    val referenceId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val createdBy: String = "Owner"
)

@Entity(
    tableName = "sales",
    indices = [Index(value = ["businessId", "invoiceNo"])]
)
data class SaleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val invoiceNo: String,
    val customerId: String = "",
    val customerName: String = "Walk-in Cash Customer",
    val date: Long = System.currentTimeMillis(),
    val subtotal: Double,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double,
    val balanceAmount: Double = 0.0,
    val paymentMethod: String = "Cash", // Cash, Bank, JazzCash, EasyPaisa, Credit
    val status: String = "COMPLETED", // DRAFT, COMPLETED, RETURNED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sale_items",
    indices = [Index(value = ["saleId"])]
)
data class SaleItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val saleId: String,
    val productId: String,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val purchasePrice: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double
)

@Entity(
    tableName = "purchases",
    indices = [Index(value = ["businessId", "billNo"])]
)
data class PurchaseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val billNo: String,
    val supplierId: String = "",
    val supplierName: String = "Walk-in Supplier",
    val date: Long = System.currentTimeMillis(),
    val grandTotal: Double,
    val paidAmount: Double,
    val balanceAmount: Double = 0.0,
    val paymentMethod: String = "Cash",
    val status: String = "COMPLETED",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "purchase_items",
    indices = [Index(value = ["purchaseId"])]
)
data class PurchaseItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val purchaseId: String,
    val productId: String,
    val productName: String,
    val quantity: Double,
    val purchasePrice: Double,
    val total: Double
)

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["businessId"])]
)
data class AccountEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val name: String,
    val type: String = "CASH", // "CASH", "BANK", "WALLET" (JazzCash/EasyPaisa)
    val accountNumber: String = "",
    val balance: Double = 0.0,
    val isDefault: Boolean = false
)

@Entity(
    tableName = "payments",
    indices = [Index(value = ["businessId", "date"])]
)
data class PaymentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val accountId: String = "",
    val partyType: String = "CUSTOMER", // "CUSTOMER", "SUPPLIER", "OTHER"
    val partyId: String = "",
    val partyName: String = "",
    val amount: Double,
    val type: String, // "IN", "OUT"
    val referenceType: String = "MANUAL", // "SALE", "PURCHASE", "EXPENSE", "MANUAL"
    val referenceId: String = "",
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["businessId", "date"])]
)
data class ExpenseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val category: String, // Rent, Electricity, Salaries, Transport, Repairs, Marketing, Utilities, Other
    val amount: Double,
    val accountId: String = "",
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "audit_logs",
    indices = [Index(value = ["businessId", "timestamp"])]
)
data class AuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val userId: String = "Owner",
    val userName: String = "Owner",
    val action: String, // "LOGIN", "SALE_CREATED", "PAYMENT_RECEIVED", "PRICE_CHANGED", etc.
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sync_queue",
    indices = [Index(value = ["status"])]
)
data class SyncQueueEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val entityType: String,
    val entityId: String,
    val action: String, // "INSERT", "UPDATE", "DELETE"
    val payloadJson: String,
    val status: String = "PENDING", // "PENDING", "IN_PROGRESS", "SYNCED", "FAILED"
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
