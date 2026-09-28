package com.example.util

enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean) {
    ENGLISH("en", "English", false),
    ROMAN_URDU("ur_roman", "Roman Urdu", false),
    URDU("ur", "اردو", true)
}

object Localization {
    private val translations: Map<String, Map<AppLanguage, String>> = mapOf(
        // General & Brand
        "app_name" to mapOf(
            AppLanguage.ENGLISH to "Mobi Khata",
            AppLanguage.ROMAN_URDU to "Mobi Khata",
            AppLanguage.URDU to "موبی کھاتہ"
        ),
        "tagline" to mapOf(
            AppLanguage.ENGLISH to "Customer Ledger & Digital Khata",
            AppLanguage.ROMAN_URDU to "Customer Ledger aur Digital Khata",
            AppLanguage.URDU to "ڈیجیٹل کھاتہ اور لیجر"
        ),
        "save" to mapOf(
            AppLanguage.ENGLISH to "Save",
            AppLanguage.ROMAN_URDU to "Mehfooz Karein",
            AppLanguage.URDU to "محفوظ کریں"
        ),
        "save_profile" to mapOf(
            AppLanguage.ENGLISH to "Save Profile",
            AppLanguage.ROMAN_URDU to "Profile Mehfooz Karein",
            AppLanguage.URDU to "پروفائل محفوظ کریں"
        ),
        "cancel" to mapOf(
            AppLanguage.ENGLISH to "Cancel",
            AppLanguage.ROMAN_URDU to "Mansookh",
            AppLanguage.URDU to "منسوخ"
        ),
        "delete" to mapOf(
            AppLanguage.ENGLISH to "Delete",
            AppLanguage.ROMAN_URDU to "Khatam Karein",
            AppLanguage.URDU to "حذف کریں"
        ),
        "edit" to mapOf(
            AppLanguage.ENGLISH to "Edit",
            AppLanguage.ROMAN_URDU to "Tabdeel Karein",
            AppLanguage.URDU to "تبدیل کریں"
        ),
        "done" to mapOf(
            AppLanguage.ENGLISH to "Done",
            AppLanguage.ROMAN_URDU to "Mukammal",
            AppLanguage.URDU to "مکمل"
        ),
        "clear" to mapOf(
            AppLanguage.ENGLISH to "Clear",
            AppLanguage.ROMAN_URDU to "Saaf Karein",
            AppLanguage.URDU to "صاف کریں"
        ),
        "search" to mapOf(
            AppLanguage.ENGLISH to "Search by name or phone...",
            AppLanguage.ROMAN_URDU to "Naam ya phone number se talash karein...",
            AppLanguage.URDU to "نام یا فون نمبر سے تلاش کریں..."
        ),
        "total" to mapOf(
            AppLanguage.ENGLISH to "Total",
            AppLanguage.ROMAN_URDU to "Kul Raqam",
            AppLanguage.URDU to "کل رقم"
        ),
        "online" to mapOf(
            AppLanguage.ENGLISH to "Online",
            AppLanguage.ROMAN_URDU to "Online (Rabta Mojood)",
            AppLanguage.URDU to "آن لائن"
        ),
        "offline" to mapOf(
            AppLanguage.ENGLISH to "Offline",
            AppLanguage.ROMAN_URDU to "Offline (Local DB)",
            AppLanguage.URDU to "آف لائن"
        ),
        "loading" to mapOf(
            AppLanguage.ENGLISH to "Loading...",
            AppLanguage.ROMAN_URDU to "Loading...",
            AppLanguage.URDU to "لوڈ ہو رہا ہے..."
        ),

        // Navigation
        "nav_dashboard" to mapOf(
            AppLanguage.ENGLISH to "Dashboard",
            AppLanguage.ROMAN_URDU to "Dashboard",
            AppLanguage.URDU to "ڈیش بورڈ"
        ),
        "nav_customers" to mapOf(
            AppLanguage.ENGLISH to "Customer Ledger",
            AppLanguage.ROMAN_URDU to "Customer ka Khata",
            AppLanguage.URDU to "کسٹمر کھاتہ"
        ),
        "nav_suppliers" to mapOf(
            AppLanguage.ENGLISH to "Suppliers",
            AppLanguage.ROMAN_URDU to "Suppliers ka Khata",
            AppLanguage.URDU to "سپلائرز کھاتہ"
        ),
        "nav_transactions" to mapOf(
            AppLanguage.ENGLISH to "Transactions",
            AppLanguage.ROMAN_URDU to "Len Den",
            AppLanguage.URDU to "لین دین"
        ),
        "nav_reminders" to mapOf(
            AppLanguage.ENGLISH to "Payment Reminder",
            AppLanguage.ROMAN_URDU to "Adaigi ka Yaad Dihani Paigham",
            AppLanguage.URDU to "یاد دہانی"
        ),
        "nav_daybook" to mapOf(
            AppLanguage.ENGLISH to "Daybook",
            AppLanguage.ROMAN_URDU to "Roznamcha (Daybook)",
            AppLanguage.URDU to "روزنامچہ"
        ),
        "nav_reports" to mapOf(
            AppLanguage.ENGLISH to "Reports",
            AppLanguage.ROMAN_URDU to "Maliyati Reports",
            AppLanguage.URDU to "رپورٹس"
        ),
        "nav_settings" to mapOf(
            AppLanguage.ENGLISH to "Settings",
            AppLanguage.ROMAN_URDU to "Settings",
            AppLanguage.URDU to "ترتیبات"
        ),
        "nav_admin" to mapOf(
            AppLanguage.ENGLISH to "Settings & Admin",
            AppLanguage.ROMAN_URDU to "Settings aur Admin",
            AppLanguage.URDU to "ترتیبات و ایڈمن"
        ),
        "nav_more" to mapOf(
            AppLanguage.ENGLISH to "More",
            AppLanguage.ROMAN_URDU to "Mazeed",
            AppLanguage.URDU to "مزید"
        ),

        // Dashboard & Totals
        "your_business" to mapOf(
            AppLanguage.ENGLISH to "Your Business",
            AppLanguage.ROMAN_URDU to "Aap ka Karobar",
            AppLanguage.URDU to "آپ کا کاروبار"
        ),
        "your_shop" to mapOf(
            AppLanguage.ENGLISH to "Your Shop",
            AppLanguage.ROMAN_URDU to "Aap ki Dukan",
            AppLanguage.URDU to "آپ کی دکان"
        ),
        "your_account" to mapOf(
            AppLanguage.ENGLISH to "Your Account",
            AppLanguage.ROMAN_URDU to "Aap ka Account",
            AppLanguage.URDU to "آپ کا اکاؤنٹ"
        ),
        "total_receivables" to mapOf(
            AppLanguage.ENGLISH to "Total Receivables",
            AppLanguage.ROMAN_URDU to "Kul Wasool Talab (Lene Hain)",
            AppLanguage.URDU to "وصول طلب (لینے ہیں)"
        ),
        "total_payables" to mapOf(
            AppLanguage.ENGLISH to "Total Payables",
            AppLanguage.ROMAN_URDU to "Kul Wajib ul Ada (Dene Hain)",
            AppLanguage.URDU to "واجب الادا (دینے ہیں)"
        ),
        "cash_balance" to mapOf(
            AppLanguage.ENGLISH to "Cash in Hand",
            AppLanguage.ROMAN_URDU to "Mojooda Naqd Raqam",
            AppLanguage.URDU to "نقد رقم"
        ),
        "total_accounts" to mapOf(
            AppLanguage.ENGLISH to "Total Accounts",
            AppLanguage.ROMAN_URDU to "Kul Khatey",
            AppLanguage.URDU to "کل کھاتے"
        ),
        "quick_actions" to mapOf(
            AppLanguage.ENGLISH to "Khata Quick Actions",
            AppLanguage.ROMAN_URDU to "Fauree Khata Actions",
            AppLanguage.URDU to "فوری ایکشنز"
        ),
        "quick_new_customer" to mapOf(
            AppLanguage.ENGLISH to "Add Customer",
            AppLanguage.ROMAN_URDU to "Customer Shamil Karein",
            AppLanguage.URDU to "کسٹمر شامل کریں"
        ),
        "recent_transactions" to mapOf(
            AppLanguage.ENGLISH to "Recent Customer Transactions",
            AppLanguage.ROMAN_URDU to "Haliya Khata Transactions",
            AppLanguage.URDU to "حالیہ لین دین"
        ),
        "no_transactions_yet" to mapOf(
            AppLanguage.ENGLISH to "No transactions recorded yet.",
            AppLanguage.ROMAN_URDU to "Abhi tak koi entry darj nahi hui.",
            AppLanguage.URDU to "ابھی کوئی انٹری درج نہیں ہوئی۔"
        ),
        "no_transactions_sub" to mapOf(
            AppLanguage.ENGLISH to "Record an Udhaar or Payment to see running ledger entries.",
            AppLanguage.ROMAN_URDU to "Udhaar ya wasooli darj karein taakay khata shuru ho sakey.",
            AppLanguage.URDU to "ادھار یا وصولی درج کریں تاکہ کھاتہ شروع ہو سکے۔"
        ),
        "customers_have_udhaar" to mapOf(
            AppLanguage.ENGLISH to "customers have Udhaar",
            AppLanguage.ROMAN_URDU to "customers ke zimmey udhaar hai",
            AppLanguage.URDU to "کسٹمرز کے ذمہ ادھار ہے"
        ),
        "suppliers_to_pay" to mapOf(
            AppLanguage.ENGLISH to "suppliers to pay",
            AppLanguage.ROMAN_URDU to "suppliers ko raqam deni hai",
            AppLanguage.URDU to "سپلائرز کو رقم دینی ہے"
        ),
        "view_customers" to mapOf(
            AppLanguage.ENGLISH to "View Customers",
            AppLanguage.ROMAN_URDU to "Customers Dekhein",
            AppLanguage.URDU to "کسٹمرز دیکھیں"
        ),
        "cloud_synced" to mapOf(
            AppLanguage.ENGLISH to "Cloud Synced",
            AppLanguage.ROMAN_URDU to "Cloud Synced",
            AppLanguage.URDU to "کلاؤڈ سنکڈ"
        ),
        "syncing" to mapOf(
            AppLanguage.ENGLISH to "Syncing...",
            AppLanguage.ROMAN_URDU to "Sync ho raha hai...",
            AppLanguage.URDU to "سنک ہو رہا ہے..."
        ),

        // Customer Flow & Contacts
        "add_customer" to mapOf(
            AppLanguage.ENGLISH to "Add Customer",
            AppLanguage.ROMAN_URDU to "Customer Shamil Karein",
            AppLanguage.URDU to "کسٹمر شامل کریں"
        ),
        "import_contacts" to mapOf(
            AppLanguage.ENGLISH to "Import from Contacts",
            AppLanguage.ROMAN_URDU to "Phone Contacts Se Chunein",
            AppLanguage.URDU to "فون سے منتخب کریں"
        ),
        "manual_customer" to mapOf(
            AppLanguage.ENGLISH to "Add Manually",
            AppLanguage.ROMAN_URDU to "Khud Darj Karein",
            AppLanguage.URDU to "خود درج کریں"
        ),
        "contacts_permission_title" to mapOf(
            AppLanguage.ENGLISH to "Contacts Access Required",
            AppLanguage.ROMAN_URDU to "Contacts Ki Ijazat Darkar Hai",
            AppLanguage.URDU to "رابطوں کی اجازت درکار ہے"
        ),
        "contacts_permission_desc" to mapOf(
            AppLanguage.ENGLISH to "Allow Mobi Khata to read your contacts so you can quickly add customers without manual typing.",
            AppLanguage.ROMAN_URDU to "Mobi Khata ko contacts parhne ki ijazat dein taakay aap ba-asani customer shamil kar sakein.",
            AppLanguage.URDU to "موبی کھاتہ کو رابطے پڑھنے کی اجازت دیں تاکہ کسٹمر باآسانی شامل ہو سکے۔"
        ),
        "grant_permission" to mapOf(
            AppLanguage.ENGLISH to "Grant Permission",
            AppLanguage.ROMAN_URDU to "Ijazat Dein",
            AppLanguage.URDU to "اجازت دیں"
        ),
        "open_settings" to mapOf(
            AppLanguage.ENGLISH to "Open App Settings",
            AppLanguage.ROMAN_URDU to "App Settings Kholein",
            AppLanguage.URDU to "سیٹنگز کھولیں"
        ),
        "select_contact" to mapOf(
            AppLanguage.ENGLISH to "Select Contact",
            AppLanguage.ROMAN_URDU to "Contact Select Karein",
            AppLanguage.URDU to "رابطہ منتخب کریں"
        ),
        "search_contacts" to mapOf(
            AppLanguage.ENGLISH to "Search contacts by name or number...",
            AppLanguage.ROMAN_URDU to "Naam ya number se contact talash karein...",
            AppLanguage.URDU to "نام یا نمبر سے رابطہ تلاش کریں..."
        ),
        "no_contacts_found" to mapOf(
            AppLanguage.ENGLISH to "No contacts found on device",
            AppLanguage.ROMAN_URDU to "Device mein koi contact nahi mila",
            AppLanguage.URDU to "کوئی رابطہ نہیں ملا"
        ),
        "select_number" to mapOf(
            AppLanguage.ENGLISH to "Choose Phone Number",
            AppLanguage.ROMAN_URDU to "Phone Number Chunein",
            AppLanguage.URDU to "فون نمبر منتخب کریں"
        ),
        "no_customers_found" to mapOf(
            AppLanguage.ENGLISH to "No customers found",
            AppLanguage.ROMAN_URDU to "Koi customer nahi mila",
            AppLanguage.URDU to "کوئی کسٹمر نہیں ملا"
        ),
        "tap_add_customer" to mapOf(
            AppLanguage.ENGLISH to "Tap '+ Add Customer' to import contacts or add manually.",
            AppLanguage.ROMAN_URDU to "Naya customer shamil karne ke liye button dabayein.",
            AppLanguage.URDU to "نیا کسٹمر شامل کرنے کے لیے بٹن دبائیں۔"
        ),

        // Customer Profile & Details
        "customer_profile" to mapOf(
            AppLanguage.ENGLISH to "Customer Profile",
            AppLanguage.ROMAN_URDU to "Customer ki Profile",
            AppLanguage.URDU to "کسٹمر پروفائل"
        ),
        "customer_ledger" to mapOf(
            AppLanguage.ENGLISH to "Customer Ledger",
            AppLanguage.ROMAN_URDU to "Customer ka Khata",
            AppLanguage.URDU to "کسٹمر کا کھاتہ"
        ),
        "current_balance" to mapOf(
            AppLanguage.ENGLISH to "Current Balance",
            AppLanguage.ROMAN_URDU to "Mojooda Baqaya",
            AppLanguage.URDU to "موجودہ بقایا"
        ),
        "you_will_get" to mapOf(
            AppLanguage.ENGLISH to "You'll Get (Udhaar / Receivable)",
            AppLanguage.ROMAN_URDU to "Aap ne Lene Hain (Udhaar)",
            AppLanguage.URDU to "آپ نے لینے ہیں (ادھار)"
        ),
        "you_will_give" to mapOf(
            AppLanguage.ENGLISH to "You'll Give (Advance / Payable)",
            AppLanguage.ROMAN_URDU to "Aap ne Dene Hain (Advance)",
            AppLanguage.URDU to "آپ نے دینے ہیں (پیشگی)"
        ),
        "settled_balance" to mapOf(
            AppLanguage.ENGLISH to "Settled (Nil Balance)",
            AppLanguage.ROMAN_URDU to "Khata Barabar Hai (Baqaya Sifar)",
            AppLanguage.URDU to "کھاتہ برابر ہے"
        ),
        "edit_profile" to mapOf(
            AppLanguage.ENGLISH to "Edit Profile",
            AppLanguage.ROMAN_URDU to "Profile ki Tabdeeli",
            AppLanguage.URDU to "پروفائل تبدیل کریں"
        ),
        "profile_settings" to mapOf(
            AppLanguage.ENGLISH to "Customer Profile Settings",
            AppLanguage.ROMAN_URDU to "Customer Profile Settings",
            AppLanguage.URDU to "کسٹمر پروفائل ترتیبات"
        ),
        "change_photo" to mapOf(
            AppLanguage.ENGLISH to "Change Profile Photo",
            AppLanguage.ROMAN_URDU to "Tasveer Badlein",
            AppLanguage.URDU to "تصویر تبدیل کریں"
        ),
        "remove_photo" to mapOf(
            AppLanguage.ENGLISH to "Remove Photo",
            AppLanguage.ROMAN_URDU to "Tasveer Hatayein",
            AppLanguage.URDU to "تصویر ہٹائیں"
        ),
        "select_from_gallery" to mapOf(
            AppLanguage.ENGLISH to "Select from Gallery",
            AppLanguage.ROMAN_URDU to "Gallery Se Tasveer Chunein",
            AppLanguage.URDU to "گیلری سے تصویر لیں"
        ),
        "customer_name" to mapOf(
            AppLanguage.ENGLISH to "Customer Name",
            AppLanguage.ROMAN_URDU to "Customer ka Naam",
            AppLanguage.URDU to "کسٹمر کا نام"
        ),
        "phone_number" to mapOf(
            AppLanguage.ENGLISH to "Phone Number",
            AppLanguage.ROMAN_URDU to "Phone Number",
            AppLanguage.URDU to "فون نمبر"
        ),
        "alt_phone_number" to mapOf(
            AppLanguage.ENGLISH to "Alternate Phone Number",
            AppLanguage.ROMAN_URDU to "Doosra Phone Number",
            AppLanguage.URDU to "دوسرا فون نمبر"
        ),
        "address" to mapOf(
            AppLanguage.ENGLISH to "Address",
            AppLanguage.ROMAN_URDU to "Pata",
            AppLanguage.URDU to "پتہ"
        ),
        "city" to mapOf(
            AppLanguage.ENGLISH to "City",
            AppLanguage.ROMAN_URDU to "Shehar",
            AppLanguage.URDU to "شہر"
        ),
        "notes" to mapOf(
            AppLanguage.ENGLISH to "Notes & Remarks",
            AppLanguage.ROMAN_URDU to "Notes aur Tafseelat",
            AppLanguage.URDU to "نوٹس اور تفصیلات"
        ),
        "reminder_frequency" to mapOf(
            AppLanguage.ENGLISH to "Reminder Schedule",
            AppLanguage.ROMAN_URDU to "Yaad Dihani ka Schedule",
            AppLanguage.URDU to "یاد دہانی کا شیڈول"
        ),
        "contact_info_section" to mapOf(
            AppLanguage.ENGLISH to "Contact Information",
            AppLanguage.ROMAN_URDU to "Rabita ki Tafseelat",
            AppLanguage.URDU to "رابطے کی تفصیلات"
        ),
        "location_section" to mapOf(
            AppLanguage.ENGLISH to "Location & Address",
            AppLanguage.ROMAN_URDU to "Pata aur Shehar",
            AppLanguage.URDU to "پتہ اور شہر"
        ),
        "notes_section" to mapOf(
            AppLanguage.ENGLISH to "Ledger Notes & Reminders",
            AppLanguage.ROMAN_URDU to "Khata Notes aur Yaad Dihani",
            AppLanguage.URDU to "نوٹس اور یاد دہانی"
        ),
        "remind_weekly" to mapOf(
            AppLanguage.ENGLISH to "Weekly",
            AppLanguage.ROMAN_URDU to "Haftawar",
            AppLanguage.URDU to "ہفتہ وار"
        ),
        "remind_daily" to mapOf(
            AppLanguage.ENGLISH to "Daily",
            AppLanguage.ROMAN_URDU to "Rozana",
            AppLanguage.URDU to "روزانہ"
        ),
        "remind_monthly" to mapOf(
            AppLanguage.ENGLISH to "Monthly",
            AppLanguage.ROMAN_URDU to "Mahana",
            AppLanguage.URDU to "ماہانہ"
        ),
        "remind_none" to mapOf(
            AppLanguage.ENGLISH to "None",
            AppLanguage.ROMAN_URDU to "Koi Nahi",
            AppLanguage.URDU to "کوئی نہیں"
        ),

        // Ledger & Transactions
        "you_gave_udhaar" to mapOf(
            AppLanguage.ENGLISH to "You Gave (-)",
            AppLanguage.ROMAN_URDU to "Aap ne Udhaar Diya (-)",
            AppLanguage.URDU to "آپ نے دیا (-)"
        ),
        "you_got_payment" to mapOf(
            AppLanguage.ENGLISH to "You Got (+)",
            AppLanguage.ROMAN_URDU to "Aap ne Wasool Kiya (+)",
            AppLanguage.URDU to "آپ نے وصول کیا (+)"
        ),
        "receive_payment" to mapOf(
            AppLanguage.ENGLISH to "Receive Payment",
            AppLanguage.ROMAN_URDU to "Raqam Wasool Karein",
            AppLanguage.URDU to "رقم وصول کریں"
        ),
        "record_entry" to mapOf(
            AppLanguage.ENGLISH to "Record Ledger Entry",
            AppLanguage.ROMAN_URDU to "Khatey mein Entry Karein",
            AppLanguage.URDU to "کھاتے میں انٹری کریں"
        ),
        "add_transaction" to mapOf(
            AppLanguage.ENGLISH to "Add Transaction",
            AppLanguage.ROMAN_URDU to "Entry Darj Karein",
            AppLanguage.URDU to "لین دین درج کریں"
        ),
        "transaction_history" to mapOf(
            AppLanguage.ENGLISH to "Transaction History",
            AppLanguage.ROMAN_URDU to "Khata ki Tafseel",
            AppLanguage.URDU to "لین دین کی تاریخ"
        ),
        "statement" to mapOf(
            AppLanguage.ENGLISH to "Statement",
            AppLanguage.ROMAN_URDU to "Khata Statement",
            AppLanguage.URDU to "اسٹیٹمنٹ"
        ),
        "payment_reminder" to mapOf(
            AppLanguage.ENGLISH to "Payment Reminder",
            AppLanguage.ROMAN_URDU to "Adaigi ka Yaad Dihani Paigham",
            AppLanguage.URDU to "یاد دہانی"
        ),
        "reminder" to mapOf(
            AppLanguage.ENGLISH to "Reminder",
            AppLanguage.ROMAN_URDU to "Yaad Dihani",
            AppLanguage.URDU to "یاد دہانی"
        ),
        "call_customer" to mapOf(
            AppLanguage.ENGLISH to "Call",
            AppLanguage.ROMAN_URDU to "Call Karein",
            AppLanguage.URDU to "کال کریں"
        ),
        "whatsapp_customer" to mapOf(
            AppLanguage.ENGLISH to "WhatsApp",
            AppLanguage.ROMAN_URDU to "WhatsApp Karein",
            AppLanguage.URDU to "واٹس ایپ"
        ),
        "amount" to mapOf(
            AppLanguage.ENGLISH to "Amount (Rs.)",
            AppLanguage.ROMAN_URDU to "Raqam (Rupees)",
            AppLanguage.URDU to "رقم (روپے)"
        ),
        "description" to mapOf(
            AppLanguage.ENGLISH to "Description / Details",
            AppLanguage.ROMAN_URDU to "Tafseel / Note",
            AppLanguage.URDU to "تفصیل / نوٹ"
        ),
        "payment_mode" to mapOf(
            AppLanguage.ENGLISH to "Payment Mode",
            AppLanguage.ROMAN_URDU to "Adaigi ka Tariqa",
            AppLanguage.URDU to "طریقہ ادائیگی"
        ),
        "all" to mapOf(
            AppLanguage.ENGLISH to "All",
            AppLanguage.ROMAN_URDU to "Sab",
            AppLanguage.URDU to "تمام"
        ),
        "filter_udhaar" to mapOf(
            AppLanguage.ENGLISH to "Udhaar (-)",
            AppLanguage.ROMAN_URDU to "Udhaar (-)",
            AppLanguage.URDU to "ادھار (-)"
        ),
        "filter_payment" to mapOf(
            AppLanguage.ENGLISH to "Payment (+)",
            AppLanguage.ROMAN_URDU to "Wasooli (+)",
            AppLanguage.URDU to "وصولی (+)"
        ),
        "filter_settled" to mapOf(
            AppLanguage.ENGLISH to "Settled",
            AppLanguage.ROMAN_URDU to "Barabar (Sifar)",
            AppLanguage.URDU to "برابر"
        ),
        "no_ledger_entries" to mapOf(
            AppLanguage.ENGLISH to "No ledger entries yet.",
            AppLanguage.ROMAN_URDU to "Khatey mein abhi koi entry nahi hai.",
            AppLanguage.URDU to "کھاتے میں ابھی کوئی انٹری نہیں ہے۔"
        ),
        "use_bottom_buttons" to mapOf(
            AppLanguage.ENGLISH to "Use the bottom buttons to record Udhaar or Payment.",
            AppLanguage.ROMAN_URDU to "Udhaar ya wasooli darj karne ke liye neechay diye gaye button istemal karein.",
            AppLanguage.URDU to "ادھار یا وصولی کے لیے نیچے دیے گئے بٹن استعمال کریں۔"
        ),
        "running_balance" to mapOf(
            AppLanguage.ENGLISH to "Bal",
            AppLanguage.ROMAN_URDU to "Baqaya",
            AppLanguage.URDU to "بقایا"
        ),

        // Add Bill & Bill Attachment
        "add_bill" to mapOf(
            AppLanguage.ENGLISH to "Add Bill",
            AppLanguage.ROMAN_URDU to "Bill Shamil Karein",
            AppLanguage.URDU to "بل شامل کریں"
        ),
        "bill_attached" to mapOf(
            AppLanguage.ENGLISH to "Bill Attached",
            AppLanguage.ROMAN_URDU to "Bill Munsalik Hai",
            AppLanguage.URDU to "بل منسلک ہے"
        ),
        "view_bill" to mapOf(
            AppLanguage.ENGLISH to "View Bill",
            AppLanguage.ROMAN_URDU to "Bill Dekhein",
            AppLanguage.URDU to "بل دیکھیں"
        ),
        "replace_bill" to mapOf(
            AppLanguage.ENGLISH to "Replace Bill",
            AppLanguage.ROMAN_URDU to "Bill Badlein",
            AppLanguage.URDU to "بل تبدیل کریں"
        ),
        "remove_bill" to mapOf(
            AppLanguage.ENGLISH to "Remove Bill",
            AppLanguage.ROMAN_URDU to "Bill Hatayein",
            AppLanguage.URDU to "بل ہٹائیں"
        ),
        "take_photo" to mapOf(
            AppLanguage.ENGLISH to "Camera (Take Photo)",
            AppLanguage.ROMAN_URDU to "Camera (Tasveer Kheinchein)",
            AppLanguage.URDU to "کیمرہ (تصویر لیں)"
        ),
        "choose_gallery" to mapOf(
            AppLanguage.ENGLISH to "Gallery (Choose Image)",
            AppLanguage.ROMAN_URDU to "Gallery (Tasveer Chunein)",
            AppLanguage.URDU to "گیلری (تصویر منتخب کریں)"
        ),
        "camera_permission_needed" to mapOf(
            AppLanguage.ENGLISH to "Camera permission is required to capture bill photos.",
            AppLanguage.ROMAN_URDU to "Bill ki tasveer kheenchnay ke liye camera ki ijazat darkar hai.",
            AppLanguage.URDU to "بل کی تصویر کے لیے کیمرے کی اجازت درکار ہے۔"
        ),

        // In-App Calculator
        "calculator" to mapOf(
            AppLanguage.ENGLISH to "Calculator",
            AppLanguage.ROMAN_URDU to "Calculator",
            AppLanguage.URDU to "کیلکولیٹر"
        ),
        "open_calculator" to mapOf(
            AppLanguage.ENGLISH to "Open Calculator",
            AppLanguage.ROMAN_URDU to "Calculator Kholein",
            AppLanguage.URDU to "کیلکولیٹر کھولیں"
        ),

        // Quick Theme Switch & Custom Color
        "theme" to mapOf(
            AppLanguage.ENGLISH to "Theme",
            AppLanguage.ROMAN_URDU to "Theme",
            AppLanguage.URDU to "تھیم"
        ),
        "light_theme" to mapOf(
            AppLanguage.ENGLISH to "Light Mode",
            AppLanguage.ROMAN_URDU to "Light Mode",
            AppLanguage.URDU to "لائٹ موڈ"
        ),
        "dark_theme" to mapOf(
            AppLanguage.ENGLISH to "Dark Mode",
            AppLanguage.ROMAN_URDU to "Dark Mode",
            AppLanguage.URDU to "ڈارک موڈ"
        ),
        "custom_theme" to mapOf(
            AppLanguage.ENGLISH to "Custom Color",
            AppLanguage.ROMAN_URDU to "Custom Rang",
            AppLanguage.URDU to "پسندیدہ رنگ"
        ),
        "choose_accent_color" to mapOf(
            AppLanguage.ENGLISH to "Choose App Accent Color",
            AppLanguage.ROMAN_URDU to "App ka Rang Chunein",
            AppLanguage.URDU to "ایپ کا رنگ منتخب کریں"
        ),

        // Business Profile Management
        "business_profile" to mapOf(
            AppLanguage.ENGLISH to "My Business Profile",
            AppLanguage.ROMAN_URDU to "Aap ka Karobar",
            AppLanguage.URDU to "کاروباری پروفائل"
        ),
        "business_name" to mapOf(
            AppLanguage.ENGLISH to "Shop / Business Name",
            AppLanguage.ROMAN_URDU to "Dukan ya Karobar ka Naam",
            AppLanguage.URDU to "دکان کا نام"
        ),
        "owner_name" to mapOf(
            AppLanguage.ENGLISH to "Proprietor / Owner Name",
            AppLanguage.ROMAN_URDU to "Malik ka Naam",
            AppLanguage.URDU to "مالک کا نام"
        ),
        "business_phone" to mapOf(
            AppLanguage.ENGLISH to "Business Phone",
            AppLanguage.ROMAN_URDU to "Karobari Phone Number",
            AppLanguage.URDU to "کاروباری فون"
        ),
        "business_whatsapp" to mapOf(
            AppLanguage.ENGLISH to "Business WhatsApp",
            AppLanguage.ROMAN_URDU to "Karobari WhatsApp",
            AppLanguage.URDU to "کاروباری واٹس ایپ"
        ),
        "business_category" to mapOf(
            AppLanguage.ENGLISH to "Business Category",
            AppLanguage.ROMAN_URDU to "Karobar ki Qisam",
            AppLanguage.URDU to "کاروبار کی قسم"
        ),
        "change_logo" to mapOf(
            AppLanguage.ENGLISH to "Change Business Logo",
            AppLanguage.ROMAN_URDU to "Logo Badlein",
            AppLanguage.URDU to "لوگو تبدیل کریں"
        ),
        "remove_logo" to mapOf(
            AppLanguage.ENGLISH to "Remove Logo",
            AppLanguage.ROMAN_URDU to "Logo Hatayein",
            AppLanguage.URDU to "لوگو ہٹائیں"
        ),
        "currency" to mapOf(
            AppLanguage.ENGLISH to "Currency",
            AppLanguage.ROMAN_URDU to "Currency",
            AppLanguage.URDU to "کرنسی"
        ),

        // Validation & Alerts
        "customer_exists_warning" to mapOf(
            AppLanguage.ENGLISH to "A customer with this phone number already exists. Opening existing ledger.",
            AppLanguage.ROMAN_URDU to "Is phone number ka customer pehle se mojood hai. Khata khola ja raha hai.",
            AppLanguage.URDU to "اس فون نمبر کا کسٹمر پہلے سے موجود ہے۔ کھاتہ کھولا جا رہا ہے۔"
        ),
        "name_required" to mapOf(
            AppLanguage.ENGLISH to "Customer name is required",
            AppLanguage.ROMAN_URDU to "Customer ka naam likhna zaroori hai",
            AppLanguage.URDU to "کسٹمر کا نام ضروری ہے"
        ),
        "phone_required" to mapOf(
            AppLanguage.ENGLISH to "Valid phone number is required",
            AppLanguage.ROMAN_URDU to "Sahi phone number likhna zaroori hai",
            AppLanguage.URDU to "صحیح فون نمبر درکار ہے"
        ),
        "amount_required" to mapOf(
            AppLanguage.ENGLISH to "Please enter a valid amount",
            AppLanguage.ROMAN_URDU to "Barahe meherbani sahi raqam darj karein",
            AppLanguage.URDU to "براہ مہربانی درست رقم درج کریں"
        ),
        "profile_saved" to mapOf(
            AppLanguage.ENGLISH to "Profile saved successfully!",
            AppLanguage.ROMAN_URDU to "Profile kamyabi se mehfooz ho gayi!",
            AppLanguage.URDU to "پروفائل کامیابی سے محفوظ ہوگئی!"
        ),
        "photo_updated" to mapOf(
            AppLanguage.ENGLISH to "Profile photo updated",
            AppLanguage.ROMAN_URDU to "Tasveer tabdeel kar di gayi hai",
            AppLanguage.URDU to "تصویر تبدیل کر دی گئی ہے"
        ),
        "photo_removed" to mapOf(
            AppLanguage.ENGLISH to "Profile photo removed",
            AppLanguage.ROMAN_URDU to "Tasveer hata di gayi hai",
            AppLanguage.URDU to "تصویر ہٹا دی گئی ہے"
        ),

        // Settings, Cloud & Admin
        "select_language" to mapOf(
            AppLanguage.ENGLISH to "Select App Language",
            AppLanguage.ROMAN_URDU to "App ki Zuban ka Intekhab",
            AppLanguage.URDU to "زبان کا انتخاب"
        ),
        "active_business" to mapOf(
            AppLanguage.ENGLISH to "Active Business",
            AppLanguage.ROMAN_URDU to "Mojooda Karobar",
            AppLanguage.URDU to "موجودہ کاروبار"
        ),
        "switch_shop" to mapOf(
            AppLanguage.ENGLISH to "Switch Shop",
            AppLanguage.ROMAN_URDU to "Dukan Badlein",
            AppLanguage.URDU to "دکان تبدیل کریں"
        ),
        "my_business_profile" to mapOf(
            AppLanguage.ENGLISH to "My Business Profile",
            AppLanguage.ROMAN_URDU to "Aap ki Dukan Profile",
            AppLanguage.URDU to "دکان کی پروفائل"
        ),
        "cloud_sync_section" to mapOf(
            AppLanguage.ENGLISH to "Cloud Synchronization",
            AppLanguage.ROMAN_URDU to "Cloud Synchronization",
            AppLanguage.URDU to "کلاؤڈ سنکرونائزیشن"
        ),
        "sync_status_label" to mapOf(
            AppLanguage.ENGLISH to "Sync Status",
            AppLanguage.ROMAN_URDU to "Sync ki Soorathal",
            AppLanguage.URDU to "سنک کی صورتحال"
        ),
        "sync_all_backed_up" to mapOf(
            AppLanguage.ENGLISH to "All data backed up & synced with cloud",
            AppLanguage.ROMAN_URDU to "Tamam data mehfooz aur cloud par synced hai",
            AppLanguage.URDU to "تمام ڈیٹا محفوظ اور سنکڈ ہے"
        ),
        "sync_syncing" to mapOf(
            AppLanguage.ENGLISH to "Synchronizing data with cloud...",
            AppLanguage.ROMAN_URDU to "Data cloud par sync ho raha hai...",
            AppLanguage.URDU to "ڈیٹا سنک ہو رہا ہے..."
        ),
        "sync_offline_mode" to mapOf(
            AppLanguage.ENGLISH to "Offline - saved securely in local Room DB",
            AppLanguage.ROMAN_URDU to "Offline - local database mein mehfooz hai",
            AppLanguage.URDU to "آف لائن - محفوظ ہے"
        ),
        "sync_now" to mapOf(
            AppLanguage.ENGLISH to "Sync Now",
            AppLanguage.ROMAN_URDU to "Abhi Sync Karein",
            AppLanguage.URDU to "ابھی سنک کریں"
        ),
        "sync_center" to mapOf(
            AppLanguage.ENGLISH to "Sync Center",
            AppLanguage.ROMAN_URDU to "Sync Center",
            AppLanguage.URDU to "سنک سینٹر"
        ),
        "audit_trail" to mapOf(
            AppLanguage.ENGLISH to "Audit Trail & Activity Log",
            AppLanguage.ROMAN_URDU to "Audit Record aur Tareekh",
            AppLanguage.URDU to "آڈٹ ریکارڈ"
        ),
        "audit_trail_sub" to mapOf(
            AppLanguage.ENGLISH to "Immutable audit record of all sales, payments & adjustments",
            AppLanguage.ROMAN_URDU to "Tamam len den aur adaaigiyon ka pakka record",
            AppLanguage.URDU to "تمام لین دین کا ریکارڈ"
        ),
        "view_audit_log" to mapOf(
            AppLanguage.ENGLISH to "View Complete Audit Log",
            AppLanguage.ROMAN_URDU to "Mukammal Audit Log Dekhein",
            AppLanguage.URDU to "آڈٹ لاگ دیکھیں"
        ),
        "backup_export" to mapOf(
            AppLanguage.ENGLISH to "Backup & Data Export",
            AppLanguage.ROMAN_URDU to "Backup aur Data Export",
            AppLanguage.URDU to "بیک اپ اور ایکسپورٹ"
        ),
        "export_backup_json" to mapOf(
            AppLanguage.ENGLISH to "Export Database Backup (JSON)",
            AppLanguage.ROMAN_URDU to "Database Backup File Nikalein (JSON)",
            AppLanguage.URDU to "ڈیٹا بیس بیک اپ (JSON)"
        ),
        "sign_out" to mapOf(
            AppLanguage.ENGLISH to "Sign Out",
            AppLanguage.ROMAN_URDU to "Sign Out Karein",
            AppLanguage.URDU to "لاگ آؤٹ"
        ),
        "more_features" to mapOf(
            AppLanguage.ENGLISH to "Business & Khata Features",
            AppLanguage.ROMAN_URDU to "Mazeed Karobari Features",
            AppLanguage.URDU to "مزید فیچرز"
        ),
        "cash_accounts" to mapOf(
            AppLanguage.ENGLISH to "Cash & Bank Accounts",
            AppLanguage.ROMAN_URDU to "Naqd aur Bank Khatey",
            AppLanguage.URDU to "کیش اور بینک اکاؤنٹس"
        ),
        "expenses" to mapOf(
            AppLanguage.ENGLISH to "Business Expenses",
            AppLanguage.ROMAN_URDU to "Karobari Ikhrajat",
            AppLanguage.URDU to "کاروباری اخراجات"
        ),
        "add_expense" to mapOf(
            AppLanguage.ENGLISH to "Add Expense",
            AppLanguage.ROMAN_URDU to "Kharcha Shamil Karein",
            AppLanguage.URDU to "خرچہ شامل کریں"
        ),
        "add_supplier" to mapOf(
            AppLanguage.ENGLISH to "Add Supplier",
            AppLanguage.ROMAN_URDU to "Supplier Shamil Karein",
            AppLanguage.URDU to "سپلائر شامل کریں"
        ),
        "quick_entry_gave_title" to mapOf(
            AppLanguage.ENGLISH to "Quick Entry: You Gave (-)",
            AppLanguage.ROMAN_URDU to "Fauree Entry: Aap ne Udhaar Diya (-)",
            AppLanguage.URDU to "فوری انٹری: آپ نے دیا (-)"
        ),
        "quick_entry_got_title" to mapOf(
            AppLanguage.ENGLISH to "Quick Entry: You Got (+)",
            AppLanguage.ROMAN_URDU to "Fauree Entry: Wasool Kiya (+)",
            AppLanguage.URDU to "فوری انٹری: آپ نے وصول کیا (+)"
        ),
        "select_customer" to mapOf(
            AppLanguage.ENGLISH to "Select Customer",
            AppLanguage.ROMAN_URDU to "Customer Chunein",
            AppLanguage.URDU to "کسٹمر منتخب کریں"
        )
    )

    fun get(key: String, language: AppLanguage): String {
        return translations[key]?.get(language)
            ?: translations[key]?.get(AppLanguage.ENGLISH)
            ?: key
    }
}
