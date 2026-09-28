package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MobiKhataRepository(private val db: AppDatabase) {
    val userDao = db.userDao()
    val businessDao = db.businessDao()
    val customerDao = db.customerDao()
    val customerLedgerDao = db.customerLedgerDao()
    val supplierDao = db.supplierDao()
    val supplierLedgerDao = db.supplierLedgerDao()
    val productDao = db.productDao()
    val inventoryMovementDao = db.inventoryMovementDao()
    val saleDao = db.saleDao()
    val purchaseDao = db.purchaseDao()
    val accountDao = db.accountDao()
    val paymentDao = db.paymentDao()
    val expenseDao = db.expenseDao()
    val auditLogDao = db.auditLogDao()
    val syncQueueDao = db.syncQueueDao()

    suspend fun getInitialUserAndBusiness(): Pair<UserEntity?, BusinessEntity?> = withContext(Dispatchers.IO) {
        val user = userDao.getLatestUser()
        if (user != null) {
            val businesses = businessDao.getBusinessesForUser(user.id).firstOrNull()
            val business = businesses?.firstOrNull() ?: businessDao.getFirstBusiness()
            Pair(user, business)
        } else {
            Pair(null, null)
        }
    }

    suspend fun registerOrLoginUser(name: String, email: String, phone: String, pin: String = "", uid: String? = null): UserEntity = withContext(Dispatchers.IO) {
        val existing = if (uid != null) userDao.getUserById(uid) else userDao.getUserByEmail(email)
        if (existing != null) {
            existing
        } else {
            val newUser = UserEntity(
                id = uid ?: UUID.randomUUID().toString(),
                name = name,
                email = email,
                phone = phone,
                pinHash = pin
            )
            userDao.insertUser(newUser)
            newUser
        }
    }

    suspend fun loginWithEmail(email: String, pin: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email.trim().lowercase())
        if (user == null) {
            Result.failure(Exception("No account found with this email. Please create an account."))
        } else if (user.pinHash.isNotEmpty() && pin.isNotEmpty() && user.pinHash != pin) {
            Result.failure(Exception("Incorrect password or PIN."))
        } else {
            Result.success(user)
        }
    }

    suspend fun registerWithEmail(name: String, email: String, phone: String, pin: String, uid: String? = null): Result<UserEntity> = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        val existing = userDao.getUserByEmail(normalizedEmail)
        if (existing != null) {
            Result.failure(Exception("An account with this email already exists. Please sign in."))
        } else {
            val newUser = UserEntity(
                id = uid ?: UUID.randomUUID().toString(),
                name = name.trim().ifEmpty { normalizedEmail.substringBefore("@") },
                email = normalizedEmail,
                phone = phone.trim(),
                pinHash = pin,
                authProvider = "EMAIL",
                isEmailVerified = true
            )
            userDao.insertUser(newUser)
            Result.success(newUser)
        }
    }

    suspend fun loginWithGoogle(email: String, name: String, photoUrl: String? = null, uid: String? = null): UserEntity = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        val existing = if (uid != null) userDao.getUserById(uid) else userDao.getUserByEmail(normalizedEmail)
        if (existing != null) {
            val updated = existing.copy(
                authProvider = "GOOGLE",
                profileImageUrl = photoUrl ?: existing.profileImageUrl,
                isEmailVerified = true
            )
            userDao.updateUser(updated)
            updated
        } else {
            val newUser = UserEntity(
                id = uid ?: UUID.randomUUID().toString(),
                name = name.ifEmpty { normalizedEmail.substringBefore("@") },
                email = normalizedEmail,
                profileImageUrl = photoUrl ?: "",
                authProvider = "GOOGLE",
                isEmailVerified = true
            )
            userDao.insertUser(newUser)
            newUser
        }
    }

    suspend fun loginWithPhone(phone: String, name: String = "", uid: String? = null): UserEntity = withContext(Dispatchers.IO) {
        val normalizedPhone = phone.trim()
        val existing = if (uid != null) userDao.getUserById(uid) else userDao.getUserByPhone(normalizedPhone)
        if (existing != null) {
            val updated = existing.copy(
                authProvider = "PHONE",
                isPhoneVerified = true
            )
            userDao.updateUser(updated)
            updated
        } else {
            val newUser = UserEntity(
                id = uid ?: UUID.randomUUID().toString(),
                name = name.ifEmpty { "Store Owner (${normalizedPhone.takeLast(4)})" },
                email = "${normalizedPhone.filter { it.isDigit() }}@mobikhata.pk",
                phone = normalizedPhone,
                authProvider = "PHONE",
                isPhoneVerified = true
            )
            userDao.insertUser(newUser)
            newUser
        }
    }

    suspend fun createBusiness(
        ownerUserId: String,
        name: String,
        ownerName: String,
        phone: String,
        city: String,
        category: String,
        currency: String = "PKR"
    ): BusinessEntity = withContext(Dispatchers.IO) {
        val business = BusinessEntity(
            ownerUserId = ownerUserId,
            name = name,
            ownerName = ownerName,
            phone = phone,
            city = city,
            category = category,
            currency = currency
        )
        businessDao.insertBusiness(business)

        // Create initial default Cash and Bank accounts with 0 balance
        val cashAccount = AccountEntity(
            businessId = business.id,
            name = "Cash in Hand",
            type = "CASH",
            balance = 0.0,
            isDefault = true
        )
        val bankAccount = AccountEntity(
            businessId = business.id,
            name = "Bank Account",
            type = "BANK",
            balance = 0.0,
            isDefault = false
        )
        val walletAccount = AccountEntity(
            businessId = business.id,
            name = "JazzCash / EasyPaisa",
            type = "WALLET",
            balance = 0.0,
            isDefault = false
        )
        accountDao.insertAccount(cashAccount)
        accountDao.insertAccount(bankAccount)
        accountDao.insertAccount(walletAccount)

        logAudit(business.id, "Owner", ownerName, "BUSINESS_CREATED", "Business created: $name")
        enqueueSync(business.id, "BusinessEntity", business.id, "INSERT", JSONObject().apply {
            put("name", name)
            put("ownerName", ownerName)
            put("phone", phone)
            put("city", city)
        }.toString())

        business
    }

    suspend fun recordCustomerEntry(
        businessId: String,
        customerId: String,
        customerName: String,
        isGaveUdhaar: Boolean,
        amount: Double,
        description: String,
        paymentMethod: String = "Cash",
        billImageUrl: String = ""
    ) = withContext(Dispatchers.IO) {
        val customer = customerDao.getCustomerById(customerId) ?: return@withContext
        val balanceDelta = if (isGaveUdhaar) amount else -amount
        val newBalance = customer.currentBalance + balanceDelta
        customerDao.adjustCustomerBalance(customerId, balanceDelta)

        val entry = CustomerLedgerEntry(
            businessId = businessId,
            customerId = customerId,
            type = if (isGaveUdhaar) "GAVE_UDHAAR" else "GOT_PAYMENT",
            amount = amount,
            balanceAfter = newBalance,
            description = description,
            paymentMethod = paymentMethod,
            billImageUrl = billImageUrl
        )
        customerLedgerDao.insertLedgerEntry(entry)

        if (!isGaveUdhaar) {
            val defaultAcc = accountDao.getDefaultAccount(businessId)
            if (defaultAcc != null) {
                accountDao.updateBalance(defaultAcc.id, amount)
                paymentDao.insertPayment(
                    PaymentEntity(
                        businessId = businessId,
                        accountId = defaultAcc.id,
                        partyType = "CUSTOMER",
                        partyId = customerId,
                        partyName = customerName,
                        amount = amount,
                        type = "IN",
                        referenceType = "MANUAL",
                        referenceId = entry.id,
                        notes = "Khata payment: $description"
                    )
                )
            }
        }

        logAudit(
            businessId, "Owner", "Owner",
            if (isGaveUdhaar) "CUSTOMER_UDHAAR" else "CUSTOMER_PAYMENT",
            "Customer $customerName: Rs. $amount ($description)${if (billImageUrl.isNotBlank()) " [Bill Attached]" else ""}"
        )

        enqueueSync(businessId, "CustomerLedgerEntry", entry.id, "INSERT", JSONObject().apply {
            put("customerId", customerId)
            put("amount", amount)
            put("type", entry.type)
            put("billImageUrl", billImageUrl)
        }.toString())
    }

    suspend fun updateTransactionBill(entryId: String, billImageUrl: String) = withContext(Dispatchers.IO) {
        val entry = customerLedgerDao.getLedgerEntryById(entryId) ?: return@withContext
        val updated = entry.copy(billImageUrl = billImageUrl)
        customerLedgerDao.updateLedgerEntry(updated)
        enqueueSync(entry.businessId, "CustomerLedgerEntry", entry.id, "UPDATE", JSONObject().apply {
            put("billImageUrl", billImageUrl)
        }.toString())
    }

    suspend fun recordSupplierEntry(
        businessId: String,
        supplierId: String,
        supplierName: String,
        isPaidPayment: Boolean,
        amount: Double,
        description: String,
        paymentMethod: String = "Cash"
    ) = withContext(Dispatchers.IO) {
        val supplier = supplierDao.getSupplierById(supplierId) ?: return@withContext
        val balanceDelta = if (isPaidPayment) -amount else amount
        val newBalance = supplier.currentBalance + balanceDelta
        supplierDao.adjustSupplierBalance(supplierId, balanceDelta)

        val entry = SupplierLedgerEntry(
            businessId = businessId,
            supplierId = supplierId,
            type = if (isPaidPayment) "PAID_PAYMENT" else "PURCHASE",
            amount = amount,
            balanceAfter = newBalance,
            description = description,
            paymentMethod = paymentMethod
        )
        supplierLedgerDao.insertLedgerEntry(entry)

        if (isPaidPayment) {
            val defaultAcc = accountDao.getDefaultAccount(businessId)
            if (defaultAcc != null) {
                accountDao.updateBalance(defaultAcc.id, -amount)
                paymentDao.insertPayment(
                    PaymentEntity(
                        businessId = businessId,
                        accountId = defaultAcc.id,
                        partyType = "SUPPLIER",
                        partyId = supplierId,
                        partyName = supplierName,
                        amount = amount,
                        type = "OUT",
                        referenceType = "MANUAL",
                        referenceId = entry.id,
                        notes = "Supplier payment: $description"
                    )
                )
            }
        }

        logAudit(
            businessId, "Owner", "Owner",
            if (isPaidPayment) "SUPPLIER_PAYMENT" else "SUPPLIER_PURCHASE",
            "Supplier $supplierName: Rs. $amount ($description)"
        )
    }

    suspend fun completeSale(
        business: BusinessEntity,
        customerId: String?,
        customerName: String,
        items: List<SaleItemEntity>,
        subtotal: Double,
        discount: Double,
        tax: Double,
        grandTotal: Double,
        paidAmount: Double,
        paymentMethod: String,
        notes: String
    ): SaleEntity = withContext(Dispatchers.IO) {
        val count = saleDao.getSaleCount(business.id)
        val invoiceNo = "${business.invoicePrefix}${1001 + count}"
        val balanceAmount = (grandTotal - paidAmount).coerceAtLeast(0.0)

        val sale = SaleEntity(
            businessId = business.id,
            invoiceNo = invoiceNo,
            customerId = customerId ?: "",
            customerName = customerName,
            subtotal = subtotal,
            discount = discount,
            tax = tax,
            grandTotal = grandTotal,
            paidAmount = paidAmount,
            balanceAmount = balanceAmount,
            paymentMethod = paymentMethod,
            notes = notes
        )
        saleDao.insertSale(sale)

        val itemsWithSaleId = items.map { it.copy(saleId = sale.id) }
        saleDao.insertSaleItems(itemsWithSaleId)

        for (item in itemsWithSaleId) {
            productDao.updateStock(item.productId, -item.quantity)
            val currentProduct = productDao.getProductById(item.productId)
            inventoryMovementDao.insertMovement(
                InventoryMovementEntity(
                    businessId = business.id,
                    productId = item.productId,
                    type = "SALE",
                    quantityChange = -item.quantity,
                    quantityAfter = currentProduct?.currentStock ?: 0.0,
                    reason = "Sale Invoice # $invoiceNo",
                    referenceId = sale.id
                )
            )
        }

        if (paidAmount > 0) {
            val defaultAcc = accountDao.getDefaultAccount(business.id)
            if (defaultAcc != null) {
                accountDao.updateBalance(defaultAcc.id, paidAmount)
                paymentDao.insertPayment(
                    PaymentEntity(
                        businessId = business.id,
                        accountId = defaultAcc.id,
                        partyType = "CUSTOMER",
                        partyId = customerId ?: "",
                        partyName = customerName,
                        amount = paidAmount,
                        type = "IN",
                        referenceType = "SALE",
                        referenceId = sale.id,
                        notes = "Sale # $invoiceNo payment"
                    )
                )
            }
        }

        if (balanceAmount > 0 && !customerId.isNullOrBlank()) {
            val customer = customerDao.getCustomerById(customerId)
            if (customer != null) {
                val newBal = customer.currentBalance + balanceAmount
                customerDao.adjustCustomerBalance(customerId, balanceAmount)
                customerLedgerDao.insertLedgerEntry(
                    CustomerLedgerEntry(
                        businessId = business.id,
                        customerId = customerId,
                        type = "SALE",
                        amount = balanceAmount,
                        balanceAfter = newBal,
                        referenceNo = invoiceNo,
                        description = "Remaining balance from Sale # $invoiceNo"
                    )
                )
            }
        }

        logAudit(business.id, "Owner", "Owner", "SALE_CREATED", "Invoice $invoiceNo: Total Rs. $grandTotal, Paid Rs. $paidAmount")
        enqueueSync(business.id, "Sale", sale.id, "INSERT", JSONObject().apply {
            put("invoiceNo", invoiceNo)
            put("grandTotal", grandTotal)
            put("paidAmount", paidAmount)
        }.toString())

        sale
    }

    suspend fun completePurchase(
        business: BusinessEntity,
        supplierId: String?,
        supplierName: String,
        items: List<PurchaseItemEntity>,
        grandTotal: Double,
        paidAmount: Double,
        paymentMethod: String,
        notes: String
    ): PurchaseEntity = withContext(Dispatchers.IO) {
        val count = purchaseDao.getPurchaseCount(business.id)
        val billNo = "BILL-${5001 + count}"
        val balanceAmount = (grandTotal - paidAmount).coerceAtLeast(0.0)

        val purchase = PurchaseEntity(
            businessId = business.id,
            billNo = billNo,
            supplierId = supplierId ?: "",
            supplierName = supplierName,
            grandTotal = grandTotal,
            paidAmount = paidAmount,
            balanceAmount = balanceAmount,
            paymentMethod = paymentMethod,
            notes = notes
        )
        purchaseDao.insertPurchase(purchase)

        val itemsWithId = items.map { it.copy(purchaseId = purchase.id) }
        purchaseDao.insertPurchaseItems(itemsWithId)

        for (item in itemsWithId) {
            productDao.updateStock(item.productId, item.quantity)
            val currentProduct = productDao.getProductById(item.productId)
            inventoryMovementDao.insertMovement(
                InventoryMovementEntity(
                    businessId = business.id,
                    productId = item.productId,
                    type = "PURCHASE",
                    quantityChange = item.quantity,
                    quantityAfter = currentProduct?.currentStock ?: 0.0,
                    reason = "Purchase Bill # $billNo",
                    referenceId = purchase.id
                )
            )
        }

        if (paidAmount > 0) {
            val defaultAcc = accountDao.getDefaultAccount(business.id)
            if (defaultAcc != null) {
                accountDao.updateBalance(defaultAcc.id, -paidAmount)
                paymentDao.insertPayment(
                    PaymentEntity(
                        businessId = business.id,
                        accountId = defaultAcc.id,
                        partyType = "SUPPLIER",
                        partyId = supplierId ?: "",
                        partyName = supplierName,
                        amount = paidAmount,
                        type = "OUT",
                        referenceType = "PURCHASE",
                        referenceId = purchase.id,
                        notes = "Purchase # $billNo payment"
                    )
                )
            }
        }

        if (balanceAmount > 0 && !supplierId.isNullOrBlank()) {
            val supplier = supplierDao.getSupplierById(supplierId)
            if (supplier != null) {
                val newBal = supplier.currentBalance + balanceAmount
                supplierDao.adjustSupplierBalance(supplierId, balanceAmount)
                supplierLedgerDao.insertLedgerEntry(
                    SupplierLedgerEntry(
                        businessId = business.id,
                        supplierId = supplierId,
                        type = "PURCHASE",
                        amount = balanceAmount,
                        balanceAfter = newBal,
                        referenceNo = billNo,
                        description = "Remaining payable from Bill # $billNo"
                    )
                )
            }
        }

        logAudit(business.id, "Owner", "Owner", "PURCHASE_CREATED", "Purchase Bill $billNo: Rs. $grandTotal")
        purchase
    }

    suspend fun recordExpense(
        businessId: String,
        category: String,
        amount: Double,
        description: String,
        accountId: String?
    ) = withContext(Dispatchers.IO) {
        val targetAccId = accountId ?: accountDao.getDefaultAccount(businessId)?.id ?: ""
        val expense = ExpenseEntity(
            businessId = businessId,
            category = category,
            amount = amount,
            accountId = targetAccId,
            description = description
        )
        expenseDao.insertExpense(expense)

        if (targetAccId.isNotBlank()) {
            accountDao.updateBalance(targetAccId, -amount)
            paymentDao.insertPayment(
                PaymentEntity(
                    businessId = businessId,
                    accountId = targetAccId,
                    partyType = "OTHER",
                    partyName = "Expense: $category",
                    amount = amount,
                    type = "OUT",
                    referenceType = "EXPENSE",
                    referenceId = expense.id,
                    notes = description
                )
            )
        }
        logAudit(businessId, "Owner", "Owner", "EXPENSE_RECORDED", "$category: Rs. $amount ($description)")
    }

    suspend fun recordStockAdjustment(
        businessId: String,
        productId: String,
        newStock: Double,
        reason: String
    ) = withContext(Dispatchers.IO) {
        val product = productDao.getProductById(productId) ?: return@withContext
        val diff = newStock - product.currentStock
        productDao.updateProduct(product.copy(currentStock = newStock, updatedAt = System.currentTimeMillis()))
        inventoryMovementDao.insertMovement(
            InventoryMovementEntity(
                businessId = businessId,
                productId = productId,
                type = "ADJUSTMENT",
                quantityChange = diff,
                quantityAfter = newStock,
                reason = reason
            )
        )
        logAudit(businessId, "Owner", "Owner", "STOCK_ADJUSTMENT", "${product.name}: stock changed from ${product.currentStock} to $newStock ($reason)")
    }

    suspend fun logAudit(businessId: String, userId: String, userName: String, action: String, details: String) {
        withContext(Dispatchers.IO) {
            db.auditLogDao().insertAuditLog(
                AuditLogEntity(
                    businessId = businessId,
                    userId = userId,
                    userName = userName,
                    action = action,
                    details = details
                )
            )
        }
    }

    suspend fun enqueueSync(businessId: String, entityType: String, entityId: String, action: String, payload: String) {
        withContext(Dispatchers.IO) {
            db.syncQueueDao().enqueue(
                SyncQueueEntity(
                    businessId = businessId,
                    entityType = entityType,
                    entityId = entityId,
                    action = action,
                    payloadJson = payload
                )
            )
        }
    }

    suspend fun exportBusinessDataAsJson(businessId: String): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val business = businessDao.getBusinessById(businessId)
        val customers = customerDao.getCustomers(businessId).firstOrNull() ?: emptyList()
        val suppliers = supplierDao.getSuppliers(businessId).firstOrNull() ?: emptyList()
        val products = productDao.getProducts(businessId).firstOrNull() ?: emptyList()
        val sales = saleDao.getSales(businessId).firstOrNull() ?: emptyList()
        val expenses = expenseDao.getExpenses(businessId).firstOrNull() ?: emptyList()

        root.put("version", "1.0")
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("businessId", businessId)
        root.put("businessName", business?.name ?: "")
        root.put("customerCount", customers.size)
        root.put("supplierCount", suppliers.size)
        root.put("productCount", products.size)
        root.put("saleCount", sales.size)
        root.put("expenseCount", expenses.size)

        val customerArray = JSONArray()
        customers.forEach { c ->
            customerArray.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("balance", c.currentBalance)
                put("address", c.address)
            })
        }
        root.put("customers", customerArray)

        val productArray = JSONArray()
        products.forEach { p ->
            productArray.put(JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("barcode", p.barcode)
                put("purchasePrice", p.purchasePrice)
                put("salePrice", p.salePrice)
                put("stock", p.currentStock)
                put("unit", p.unit)
            })
        }
        root.put("products", productArray)
        root.toString(2)
    }

    suspend fun restoreBusinessDataFromJson(businessId: String, jsonStr: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonStr)
            if (root.has("customers")) {
                val customersArr = root.getJSONArray("customers")
                for (i in 0 until customersArr.length()) {
                    val obj = customersArr.getJSONObject(i)
                    val id = obj.optString("id", UUID.randomUUID().toString())
                    val name = obj.optString("name", "Restored Customer")
                    val phone = obj.optString("phone", "")
                    val balance = obj.optDouble("balance", 0.0)
                    val address = obj.optString("address", "")
                    customerDao.insertCustomer(
                        CustomerEntity(
                            id = id,
                            businessId = businessId,
                            name = name,
                            phone = phone,
                            whatsapp = phone,
                            address = address,
                            openingBalance = balance,
                            currentBalance = balance
                        )
                    )
                }
            }
            if (root.has("products")) {
                val prodArr = root.getJSONArray("products")
                for (i in 0 until prodArr.length()) {
                    val obj = prodArr.getJSONObject(i)
                    val id = obj.optString("id", UUID.randomUUID().toString())
                    val name = obj.optString("name", "Restored Product")
                    val barcode = obj.optString("barcode", "")
                    val purchasePrice = obj.optDouble("purchasePrice", 0.0)
                    val salePrice = obj.optDouble("salePrice", 0.0)
                    val stock = obj.optDouble("stock", 0.0)
                    val unit = obj.optString("unit", "Pcs")
                    productDao.insertProduct(
                        ProductEntity(
                            id = id,
                            businessId = businessId,
                            name = name,
                            barcode = barcode,
                            purchasePrice = purchasePrice,
                            salePrice = salePrice,
                            currentStock = stock,
                            unit = unit
                        )
                    )
                }
            }
            logAudit(businessId, "Owner", "Owner", "CLOUD_BACKUP_RESTORE", "Restored data from cloud backup JSON")
            true
        } catch (e: Exception) {
            false
        }
    }
}
