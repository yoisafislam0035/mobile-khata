package com.example

import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.CustomerLedgerEntry
import com.example.data.local.entity.UserEntity
import com.example.ui.components.CalculatorEngine
import com.example.util.ContactsHelper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testCalculatorArithmetic() {
        // Requested test case in prompt: 1000 + 500 - 200 = 1300
        val result = CalculatorEngine.evaluate("1000 + 500 - 200")
        assertNotNull(result)
        assertEquals(1300.0, result!!, 0.001)
    }

    @Test
    fun testCalculatorMultiplyDividePrecedence() {
        val result = CalculatorEngine.evaluate("100 + 50 × 2")
        assertNotNull(result)
        assertEquals(200.0, result!!, 0.001)

        val divResult = CalculatorEngine.evaluate("500 ÷ 2 + 50")
        assertNotNull(divResult)
        assertEquals(300.0, divResult!!, 0.001)
    }

    @Test
    fun testCalculatorDecimals() {
        val result = CalculatorEngine.evaluate("10.50 + 20.25")
        assertNotNull(result)
        assertEquals(30.75, result!!, 0.001)
    }

    @Test
    fun testBillAttachmentInLedgerEntry() {
        val entry = CustomerLedgerEntry(
            id = "ledger_01",
            businessId = "biz_01",
            customerId = "cust_01",
            amount = 1500.0,
            balanceAfter = 1500.0,
            type = "GAVE_UDHAAR",
            description = "Rice and spices",
            paymentMethod = "Cash",
            billImageUrl = "file:///data/user/0/com.aistudio.mobikhata.mzkp/files/mobi_khata_images/bill_01.jpg"
        )
        assertEquals("file:///data/user/0/com.aistudio.mobikhata.mzkp/files/mobi_khata_images/bill_01.jpg", entry.billImageUrl)
        assertEquals(1500.0, entry.amount, 0.01)
    }

    @Test
    fun testCustomerProfileEditPreservesIdAndBalance() {
        val original = CustomerEntity(
            id = "cust_abc_123",
            businessId = "biz_001",
            name = "Ahmed Khan",
            phone = "03001234567",
            openingBalance = 5000.0,
            currentBalance = 3000.0,
            profileImageUrl = "file:///data/photo1.jpg"
        )

        val edited = original.copy(
            name = "Ahmed Khan Updated",
            phone = "03009999999",
            address = "Shop # 5, Anarkali",
            city = "Lahore",
            profileImageUrl = "file:///data/photo2.jpg",
            notes = "Preferred customer"
        )

        assertEquals(original.id, edited.id)
        assertEquals(original.businessId, edited.businessId)
        assertEquals(original.currentBalance, edited.currentBalance, 0.01)
        assertEquals("Ahmed Khan Updated", edited.name)
        assertEquals("03009999999", edited.phone)
    }

    @Test
    fun testPhoneNormalizationAndMatching() {
        assertTrue(ContactsHelper.arePhoneNumbersMatching("03001234567", "+923001234567"))
        assertTrue(ContactsHelper.arePhoneNumbersMatching("0300-1234567", "0300 1234567"))
        assertFalse(ContactsHelper.arePhoneNumbersMatching("03001234567", "03211234567"))
    }

    @Test
    fun testUserDataIsolationByFirebaseUid() {
        val userA = UserEntity(
            id = "firebase_uid_user_A",
            name = "Shop Owner A",
            email = "userA@example.com",
            authProvider = "EMAIL",
            isEmailVerified = true
        )
        val userB = UserEntity(
            id = "firebase_uid_user_B",
            name = "Shop Owner B",
            email = "userB@example.com",
            authProvider = "GOOGLE",
            isEmailVerified = true
        )

        assertNotEquals(userA.id, userB.id)

        val bizUserA = com.example.data.local.entity.BusinessEntity(
            id = "biz_user_A_001",
            ownerUserId = userA.id,
            name = "Al-Madina Store",
            ownerName = userA.name
        )
        val bizUserB = com.example.data.local.entity.BusinessEntity(
            id = "biz_user_B_001",
            ownerUserId = userB.id,
            name = "Bismillah Traders",
            ownerName = userB.name
        )

        assertEquals("firebase_uid_user_A", bizUserA.ownerUserId)
        assertEquals("firebase_uid_user_B", bizUserB.ownerUserId)
        assertNotEquals(bizUserA.ownerUserId, bizUserB.ownerUserId)
    }

    @Test
    fun testEmailVerificationGating() {
        val unverifiedUser = UserEntity(
            id = "uid_unverified",
            name = "New Owner",
            email = "newowner@test.com",
            authProvider = "EMAIL",
            isEmailVerified = false
        )
        assertFalse(unverifiedUser.isEmailVerified)

        val verifiedUser = unverifiedUser.copy(isEmailVerified = true)
        assertTrue(verifiedUser.isEmailVerified)
    }
}
