package com.example.util

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PhoneContact(
    val contactId: String,
    val name: String,
    val phoneNumbers: List<String>,
    val photoUri: String? = null
)

object ContactsHelper {
    /**
     * Reads all contacts with phone numbers from Android Contacts Provider
     */
    suspend fun getPhoneContacts(context: Context): List<PhoneContact> = withContext(Dispatchers.IO) {
        val contactsMap = linkedMapOf<String, PhoneContact>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )
            cursor?.let { c ->
                val idIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                while (c.moveToNext()) {
                    val contactId = if (idIdx >= 0) c.getString(idIdx) ?: "" else ""
                    val name = if (nameIdx >= 0) c.getString(nameIdx) ?: "Unknown" else "Unknown"
                    val rawNumber = if (numberIdx >= 0) c.getString(numberIdx) ?: "" else ""
                    val photoUri = if (photoIdx >= 0) c.getString(photoIdx) else null

                    val cleanNumber = rawNumber.trim()
                    if (cleanNumber.isNotEmpty()) {
                        val existing = contactsMap[contactId]
                        if (existing != null) {
                            if (!existing.phoneNumbers.contains(cleanNumber)) {
                                contactsMap[contactId] = existing.copy(
                                    phoneNumbers = existing.phoneNumbers + cleanNumber
                                )
                            }
                        } else {
                            contactsMap[contactId] = PhoneContact(
                                contactId = contactId,
                                name = name,
                                phoneNumbers = listOf(cleanNumber),
                                photoUri = photoUri
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        contactsMap.values.toList()
    }

    /**
     * Normalizes a phone number for comparison (removes spaces, dashes, parentheses)
     */
    fun normalizePhoneNumber(phone: String): String {
        return phone.replace("[^0-9+]".toRegex(), "")
    }

    /**
     * Checks if two phone numbers match, accounting for local vs international prefixes (+92 vs 0)
     */
    fun arePhoneNumbersMatching(p1: String, p2: String): Boolean {
        val n1 = normalizePhoneNumber(p1)
        val n2 = normalizePhoneNumber(p2)
        if (n1 == n2) return true
        if (n1.isEmpty() || n2.isEmpty()) return false
        val digits1 = n1.filter { it.isDigit() }
        val digits2 = n2.filter { it.isDigit() }
        if (digits1 == digits2) return true
        val suffixLen = minOf(9, minOf(digits1.length, digits2.length))
        if (suffixLen >= 8) {
            return digits1.takeLast(suffixLen) == digits2.takeLast(suffixLen)
        }
        return false
    }
}
