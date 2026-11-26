package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CloudPhoneEntity
import com.example.data.model.CustomCloudAppEntity
import com.example.data.model.DeviceBrand
import com.example.data.model.VirtualAppId
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.InfinixGreen
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.RealmeAmber
import com.example.ui.theme.TextSecondary
import com.example.util.HostTelemetryHelper
import com.example.util.InstalledHostApp

@Composable
fun CloudAppsHubScreen(
    phones: List<CloudPhoneEntity>,
    customCloudApps: List<CustomCloudAppEntity>,
    hostApps: List<InstalledHostApp>,
    onAddCustomApp: (String, String, String, Long) -> Unit,
    onDeleteCustomApp: (Long) -> Unit,
    onLaunchInMiniDesktop: (Long, CustomCloudAppEntity) -> Unit,
    onPinAppToHomeScreen: (CloudPhoneEntity, CustomCloudAppEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPhoneId by remember(phones) { mutableStateOf(phones.firstOrNull()?.id) }
    val targetPhone = remember(phones, selectedPhoneId) {
        phones.firstOrNull { it.id == selectedPhoneId } ?: phones.firstOrNull()
    }

    var newAppName by remember { mutableStateOf("") }
    var newAppUrl by remember { mutableStateOf("") }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Cloud Apps & Home Shortcuts",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Text(
                        text = "Run web apps smoothly inside Infinix / Realme 14 or pin any app onto your Android Home Screen as a Mini Desktop shortcut.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Target Cloud Phone Selector
            if (phones.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Target Cloud Phone",
                            style = MaterialTheme.typography.labelLarge,
                            color = CyberCyan
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(phones, key = { "target_${it.id}" }) { phone ->
                                val brand = DeviceBrand.fromId(phone.brand)
                                val selected = targetPhone?.id == phone.id
                                Surface(
                                    color = if (selected) brand.accentColor.copy(alpha = 0.2f) else ObsidianSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) brand.accentColor else ObsidianCardBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedPhoneId = phone.id }
                                ) {
                                    Text(
                                        text = "${phone.name} (${brand.displayName})",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) brand.accentColor else Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Add Custom Cloud Web App
            item {
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, ObsidianCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Add Cloud App Shortcut",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        OutlinedTextField(
                            value = newAppName,
                            onValueChange = { newAppName = it },
                            label = { Text("App Name (e.g. Facebook, Discord)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newAppUrl,
                            onValueChange = { newAppUrl = it },
                            label = { Text("App URL (e.g. m.facebook.com)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (newAppName.isNotBlank() && newAppUrl.isNotBlank()) {
                                    onAddCustomApp(newAppName, newAppUrl, "Cloud Web", 0xFF00E5FF)
                                    newAppName = ""
                                    newAppUrl = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("add_cloud_app_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = ObsidianBg)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add to Cloud Phone Apps", color = ObsidianBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Cloud Web Apps List
            item {
                Text(
                    text = "Cloud Apps (${customCloudApps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberCyan
                )
            }
            items(customCloudApps, key = { "hub_app_${it.id}" }) { app ->
                val badge = Color(app.badgeColorHex)
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ObsidianCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(badge.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.name.take(2).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = badge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = app.url,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (targetPhone != null) {
                            IconButton(
                                onClick = { onLaunchInMiniDesktop(targetPhone.id, app) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DesktopWindows,
                                    contentDescription = "Open ${app.name} in Mini Desktop",
                                    tint = CyberCyan
                                )
                            }
                            IconButton(
                                onClick = { onPinAppToHomeScreen(targetPhone, app) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = "Pin ${app.name} to Home Screen",
                                    tint = RealmeAmber
                                )
                            }
                        }
                        IconButton(
                            onClick = { onDeleteCustomApp(app.id) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Remove ${app.name}",
                                tint = TextSecondary
                            )
                        }
                    }
                }
            }

            // Host Installed Apps
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Installed Device Apps (${hostApps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = InfinixGreen
                )
            }
            items(hostApps.take(20), key = { "host_${it.packageName}" }) { hostApp ->
                Surface(
                    color = ObsidianSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { HostTelemetryHelper.launchHostApp(context, hostApp.packageName) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = InfinixGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(hostApp.label, color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text(hostApp.packageName, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                        Text("OPEN", style = MaterialTheme.typography.labelMedium, color = CyberCyan)
                    }
                }
            }
        }
    }
}
