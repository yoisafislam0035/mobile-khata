package com.example.ui.screens.admin

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
fun BusinessProfileScreen(viewModel: MobiKhataViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val language by viewModel.language.collectAsState()
    val business by viewModel.currentBusiness.collectAsState()

    BackHandler {
        viewModel.navigateTo(AppScreen.ADMIN_SETTINGS)
    }

    if (business == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Business not found")
        }
        return
    }

    val currentBusiness = business!!
    var name by remember(currentBusiness.id) { mutableStateOf(currentBusiness.name) }
    var ownerName by remember(currentBusiness.id) { mutableStateOf(currentBusiness.ownerName) }
    var phone by remember(currentBusiness.id) { mutableStateOf(currentBusiness.phone) }
    var whatsapp by remember(currentBusiness.id) { mutableStateOf(currentBusiness.whatsapp) }
    var address by remember(currentBusiness.id) { mutableStateOf(currentBusiness.address) }
    var city by remember(currentBusiness.id) { mutableStateOf(currentBusiness.city) }
    var category by remember(currentBusiness.id) { mutableStateOf(currentBusiness.category) }
    var currency by remember(currentBusiness.id) { mutableStateOf(currentBusiness.currency) }
    var logoUrl by remember(currentBusiness.id) { mutableStateOf(currentBusiness.logoUrl) }
    var isSaving by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val savedLocalUri = ImageStorageUtils.saveImagePermanently(context, uri, "biz_logo_${currentBusiness.id}")
                if (savedLocalUri != null) {
                    logoUrl = savedLocalUri
                    Toast.makeText(context, "Logo updated! Tap Save to keep changes.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        Localization.get("business_profile", language),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.ADMIN_SETTINGS) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (name.isBlank()) {
                                Toast.makeText(context, "Business name cannot be empty", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            isSaving = true
                            viewModel.updateBusinessProfile(
                                name = name.trim(),
                                ownerName = ownerName.trim(),
                                phone = phone.trim(),
                                whatsapp = whatsapp.trim(),
                                address = address.trim(),
                                city = city.trim(),
                                category = category.trim(),
                                currency = currency.trim(),
                                logoUrl = logoUrl
                            ) { success ->
                                isSaving = false
                                if (success) {
                                    Toast.makeText(context, "Business profile saved successfully!", Toast.LENGTH_SHORT).show()
                                    viewModel.navigateTo(AppScreen.ADMIN_SETTINGS)
                                } else {
                                    Toast.makeText(context, "Failed to save profile.", Toast.LENGTH_SHORT).show()
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
            // Header Card with Business Logo
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
                            if (logoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = logoUrl,
                                    contentDescription = name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    logoPickerLauncher.launch(
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
                                    contentDescription = "Change Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = name.ifEmpty { "My Business" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${ownerName.ifEmpty { "Owner" }} • $city",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                logoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(Localization.get("change_logo", language), fontSize = 12.sp)
                        }
                        if (logoUrl.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        ImageStorageUtils.deleteImageIfExists(logoUrl)
                                        logoUrl = ""
                                    }
                                }
                            ) {
                                Text(Localization.get("remove_logo", language), color = JamaaRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Business Information Card
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
                        "Business Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(Localization.get("business_name", language) + " *") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text(Localization.get("owner_name", language)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(Localization.get("business_phone", language)) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text(Localization.get("business_whatsapp", language)) },
                        leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(Localization.get("business_category", language)) },
                        placeholder = { Text("e.g. General Store, Wholesaler, Electronics, Pharmacy") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Location Card
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
                        "Shop Address",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(Localization.get("address", language)) },
                        placeholder = { Text("Shop #, Market / Bazaar name") },
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

            // Financial & Currency Card
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
                        "Currency & Statement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("PKR", "USD", "AED", "SAR", "GBP").forEach { cur ->
                            FilterChip(
                                selected = currency == cur,
                                onClick = { currency = cur },
                                label = { Text(cur, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Business name cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    viewModel.updateBusinessProfile(
                        name = name.trim(),
                        ownerName = ownerName.trim(),
                        phone = phone.trim(),
                        whatsapp = whatsapp.trim(),
                        address = address.trim(),
                        city = city.trim(),
                        category = category.trim(),
                        currency = currency.trim(),
                        logoUrl = logoUrl
                    ) { success ->
                        isSaving = false
                        if (success) {
                            Toast.makeText(context, "Business profile saved successfully!", Toast.LENGTH_SHORT).show()
                            viewModel.navigateTo(AppScreen.ADMIN_SETTINGS)
                        } else {
                            Toast.makeText(context, "Failed to save profile.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(Localization.get("save", language), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}
