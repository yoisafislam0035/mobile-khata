package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PRESET_ACCENT_COLORS
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickThemeSwitchDialog(
    viewModel: MobiKhataViewModel,
    onDismiss: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val currentMode by viewModel.themeMode.collectAsState()
    val customColor by viewModel.customPrimaryColor.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = Localization.get("theme", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Mode Selector Cards
                Text(
                    text = "Select Display Mode:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Light Mode
                    ThemeOptionCard(
                        title = Localization.get("light_theme", language),
                        icon = Icons.Default.LightMode,
                        isSelected = currentMode == ThemeMode.LIGHT,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                    )

                    // Dark Mode
                    ThemeOptionCard(
                        title = Localization.get("dark_theme", language),
                        icon = Icons.Default.DarkMode,
                        isSelected = currentMode == ThemeMode.DARK,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                    )

                    // Custom Mode
                    ThemeOptionCard(
                        title = Localization.get("custom_theme", language),
                        icon = Icons.Default.ColorLens,
                        isSelected = currentMode == ThemeMode.CUSTOM,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setThemeMode(ThemeMode.CUSTOM) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Custom Accent Color Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.get("choose_accent_color", language),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(customColor)
                                .border(1.dp, Color.LightGray, CircleShape)
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PRESET_ACCENT_COLORS) { preset ->
                            val isChosen = (customColor == preset.color)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.setCustomPrimaryColor(preset.color)
                                        viewModel.setThemeMode(ThemeMode.CUSTOM)
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(preset.color)
                                        .border(
                                            width = if (isChosen) 3.dp else 1.dp,
                                            color = if (isChosen) MaterialTheme.colorScheme.onSurface else Color.LightGray,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = preset.name.substringBefore(" "),
                                    fontSize = 11.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(Localization.get("done", language))
            }
        }
    )
}

@Composable
private fun ThemeOptionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.DarkGray,
                maxLines = 1
            )
        }
    }
}
