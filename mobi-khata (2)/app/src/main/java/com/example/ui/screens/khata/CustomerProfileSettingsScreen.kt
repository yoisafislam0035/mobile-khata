package com.example.ui.screens.khata

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.JamaaRed
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.ImageStorageUtils
import com.example.util.Localization
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileSettingsScreen(
    viewModel: MobiKhataViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val language by viewModel.language.collectAsState()
    val customer by viewModel.selectedCustomer.collectAsState()

    BackHandler {
        viewModel.navigateTo(AppScreen.CUSTOMER_DETAIL)
    }

    if (customer == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(Localization.get("no_customers_found", language))
        }
        return
    }

    val currentCustomer = customer!!
    var name by remember(currentCustomer.id) { mutableStateOf(currentCustomer.name) }
    var phone by remember(currentCustomer.id) { mutableStateOf(currentCustomer.phone) }
    var alternatePhone by remember(currentCustomer.id) { mutableStateOf(currentCustomer.alternatePhone) }
    var address by remember(currentCustomer.id) { mutableStateOf(currentCustomer.address) }
    var city by remember(currentCustomer.id) { mutableStateOf(currentCustomer.city) }
    var notes by remember(currentCustomer.id) { mutableStateOf(currentCustomer.notes) }
    var reminderSchedule by remember(currentCustomer.id) { mutableStateOf(currentCustomer.reminderSchedule) }
    var profileImageUrl by remember(currentCustomer.id) { mutableStateOf(currentCustomer.profileImageUrl) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val savedLocalUri = ImageStorageUtils.saveImagePermanently(context, uri, "customer_${currentCustomer.id}")
                if (savedLocalUri != null) {
                    profileImageUrl = savedLocalUri
                    Toast.makeText(context, Localization.get("photo_updated", language), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        Localization.get("profile_settings", language),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.CUSTOMER_DETAIL) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = Localization.get("cancel", language))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            errorMessage = null
                            if (name.isBlank()) {
                                errorMessage = Localization.get("name_required", language)
                                return@TextButton
                            }
                            if (phone.isBlank()) {
                                errorMessage = Localization.get("phone_required", language)
                                return@TextButton
                            }
                            isSaving = true
                            viewModel.updateCustomerProfile(
                                customer = currentCustomer,
                                newName = name.trim(),
                                newPhone = phone.trim(),
                                newAltPhone = alternatePhone.trim(),
                                newAddress = address.trim(),
                                newCity = city.trim(),
                                newNotes = notes.trim(),
                                newReminderSchedule = reminderSchedule,
                                newProfileImageUrl = profileImageUrl
                            ) { success, error ->
                                isSaving = false
                                if (success) {
                                    Toast.makeText(context, Localization.get("profile_saved", language), Toast.LENGTH_SHORT).show()
                                    viewModel.navigateTo(AppScreen.CUSTOMER_DETAIL)
                                } else {
                                    errorMessage = error ?: "Failed to update profile."
                                }
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Text(
                            Localization.get("save", language),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Profile Photo Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.size(110.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            if (profileImageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = profileImageUrl,
                                    contentDescription = name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "#"
                                    Text(
                                        text = initial,
                                        fontSize = 42.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = Localization.get("change_photo", language),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(Localization.get("change_photo", language), fontSize = 12.sp)
                        }
                        if (profileImageUrl.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        ImageStorageUtils.deleteImageIfExists(profileImageUrl)
                                        profileImageUrl = ""
                                        Toast.makeText(context, Localization.get("photo_removed", language), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text(Localization.get("remove_photo", language), color = JamaaRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Contact Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        Localization.get("contact_info_section", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(Localization.get("customer_name", language) + " *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(Localization.get("phone_number", language) + " *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = alternatePhone,
                        onValueChange = { alternatePhone = it },
                        label = { Text(Localization.get("alt_phone_number", language)) },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Location Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        Localization.get("location_section", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(Localization.get("address", language)) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text(Localization.get("city", language)) },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Notes & Reminder Schedule
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        Localization.get("notes_section", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        Localization.get("reminder_frequency", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "WEEKLY" to Localization.get("remind_weekly", language),
                            "DAILY" to Localization.get("remind_daily", language),
                            "MONTHLY" to Localization.get("remind_monthly", language),
                            "NONE" to Localization.get("remind_none", language)
                        ).forEach { (code, labelText) ->
                            FilterChip(
                                selected = reminderSchedule == code,
                                onClick = { reminderSchedule = code },
                                label = { Text(labelText, fontSize = 12.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(Localization.get("notes", language)) },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Save and Cancel Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.CUSTOMER_DETAIL) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Localization.get("cancel", language))
                }
                Button(
                    onClick = {
                        errorMessage = null
                        if (name.isBlank()) {
                            errorMessage = Localization.get("name_required", language)
                            return@Button
                        }
                        if (phone.isBlank()) {
                            errorMessage = Localization.get("phone_required", language)
                            return@Button
                        }
                        isSaving = true
                        viewModel.updateCustomerProfile(
                            customer = currentCustomer,
                            newName = name.trim(),
                            newPhone = phone.trim(),
                            newAltPhone = alternatePhone.trim(),
                            newAddress = address.trim(),
                            newCity = city.trim(),
                            newNotes = notes.trim(),
                            newReminderSchedule = reminderSchedule,
                            newProfileImageUrl = profileImageUrl
                        ) { success, error ->
                            isSaving = false
                            if (success) {
                                Toast.makeText(context, Localization.get("profile_saved", language), Toast.LENGTH_SHORT).show()
                                viewModel.navigateTo(AppScreen.CUSTOMER_DETAIL)
                            } else {
                                errorMessage = error ?: "Failed to update profile."
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(Localization.get("save_profile", language), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}
