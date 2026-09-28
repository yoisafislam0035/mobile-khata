package com.example.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.MobiKhataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSetupScreen(viewModel: MobiKhataViewModel) {
    val user by viewModel.currentUser.collectAsState()
    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf(user?.name ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var city by remember { mutableStateOf("Lahore") }
    var category by remember { mutableStateOf("General Store") }

    val categories = listOf(
        "General Store & Karyana",
        "Medical Store & Pharmacy",
        "Electronics & Mobile",
        "Cloth & Garments",
        "Hardware & Sanitary",
        "Wholesale & Distribution",
        "Other Retail"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Business Setup Wizard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Setup your shop profile to start managing your khata and ledger",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Shop / Business Name *") },
                        placeholder = { Text("e.g. Madina Karyana Store") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner / Proprietor Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Shop Phone / WhatsApp *") },
                        placeholder = { Text("03001234567") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City *") },
                        placeholder = { Text("e.g. Lahore, Karachi, Rawalpindi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Business Category:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    Column {
                        categories.take(4).forEach { cat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = category == cat,
                                    onClick = { category = cat }
                                )
                                Text(cat, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (businessName.isNotBlank() && ownerName.isNotBlank()) {
                                viewModel.createNewBusiness(
                                    businessName.trim(),
                                    ownerName.trim(),
                                    phone.trim(),
                                    city.trim(),
                                    category
                                )
                            }
                        },
                        enabled = businessName.isNotBlank() && ownerName.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Create Shop & Open Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
