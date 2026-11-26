package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CloudPhoneEntity
import com.example.data.model.DeviceBrand
import com.example.data.model.DeviceCatalog
import com.example.data.model.PerformanceProfile
import com.example.data.model.VirtualAppId
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.InfinixGreen
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.RealmeAmber
import com.example.ui.theme.TextSecondary
import com.example.util.HostMemoryTelemetry

@Composable
fun FleetDashboardScreen(
    phones: List<CloudPhoneEntity>,
    miniDesktopCount: Int,
    brandFilter: DeviceBrand?,
    memoryTelemetry: HostMemoryTelemetry,
    onSelectBrandFilter: (DeviceBrand?) -> Unit,
    onOpenCreateSheet: () -> Unit,
    onBootFullScreen: (Long) -> Unit,
    onOpenMiniDesktop: (Long) -> Unit,
    onFloatOutsideApp: (Long) -> Unit,
    onPinToHomeScreen: (CloudPhoneEntity) -> Unit,
    onDeletePhone: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredPhones = if (brandFilter == null) {
        phones
    } else {
        phones.filter { it.brand.equals(brandFilter.id, ignoreCase = true) }
    }

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
            // Hero Command Banner
            item {
                HeroFleetBanner(
                    totalPhones = phones.size,
                    miniDesktopCount = miniDesktopCount,
                    memoryTelemetry = memoryTelemetry,
                    onCreateClick = onOpenCreateSheet
                )
            }

            // Brand Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = brandFilter == null,
                            onClick = { onSelectBrandFilter(null) },
                            label = { Text("All Fleet (${phones.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                    items(DeviceBrand.entries) { brand ->
                        val count = phones.count { it.brand == brand.id }
                        FilterChip(
                            selected = brandFilter == brand,
                            onClick = {
                                onSelectBrandFilter(if (brandFilter == brand) null else brand)
                            },
                            label = { Text("${brand.displayName} 14 ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = brand.accentColor.copy(alpha = 0.2f),
                                selectedLabelColor = brand.accentColor
                            )
                        )
                    }
                }
            }

            if (filteredPhones.isEmpty()) {
                item {
                    EmptyFleetCard(onOpenCreateSheet = onOpenCreateSheet)
                }
            } else {
                items(filteredPhones, key = { it.id }) { phone ->
                    CloudPhoneFleetCard(
                        phone = phone,
                        onBootFullScreen = { onBootFullScreen(phone.id) },
                        onOpenMiniDesktop = { onOpenMiniDesktop(phone.id) },
                        onFloatOutsideApp = { onFloatOutsideApp(phone.id) },
                        onPinToHomeScreen = { onPinToHomeScreen(phone) },
                        onDelete = { onDeletePhone(phone.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroFleetBanner(
    totalPhones: Int,
    miniDesktopCount: Int,
    memoryTelemetry: HostMemoryTelemetry,
    onCreateClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ObsidianCardBorder),
        color = ObsidianSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner_1795454507625),
                contentDescription = "CloudPhone Virtual Fleet Command Center",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                ObsidianBg.copy(alpha = 0.65f),
                                ObsidianBg.copy(alpha = 0.92f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CLOUDPHONE 14",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan
                        )
                        Text(
                            text = "Unlimited Virtual Nodes",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Infinix XOS 14 & Realme UI 5.0 • Smooth App Engine",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Telemetry Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBadge(
                        label = "Fleet",
                        value = "$totalPhones ∞",
                        accent = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        label = "Mini Desktop",
                        value = "$miniDesktopCount / ${DeviceCatalog.MAX_ACTIVE_MINI_DESKTOPS}",
                        accent = InfinixGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        label = "Free RAM",
                        value = "${memoryTelemetry.availableRamMb}MB",
                        accent = RealmeAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = onCreateClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("hero_create_phones_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = ObsidianBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create 1–5 Cloud Phones (Infinix / Realme 14)",
                        color = ObsidianBg,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianSurfaceVariant.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CloudPhoneFleetCard(
    phone: CloudPhoneEntity,
    onBootFullScreen: () -> Unit,
    onOpenMiniDesktop: () -> Unit,
    onFloatOutsideApp: () -> Unit,
    onPinToHomeScreen: () -> Unit,
    onDelete: () -> Unit
) {
    val brand = DeviceBrand.fromId(phone.brand)
    val perf = PerformanceProfile.fromId(phone.performanceMode)
    val activeApp = VirtualAppId.fromId(phone.activeAppId)

    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (phone.isMiniDesktopOpen) brand.accentColor.copy(alpha = 0.7f) else ObsidianCardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("phone_card_${phone.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(brand.accentColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhoneAndroid,
                            contentDescription = brand.displayName,
                            tint = brand.accentColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = phone.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (phone.isRunning) InfinixGreen else Color.Gray)
                            )
                        }
                        Text(
                            text = "${phone.modelName} • ${phone.osSkin}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = brand.accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete ${phone.name}",
                        tint = TextSecondary
                    )
                }
            }

            // Spec Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecTag(icon = Icons.Filled.Memory, text = "${phone.ramGb}GB RAM")
                SpecTag(icon = Icons.Filled.Speed, text = "${perf.refreshRateHz}Hz")
                SpecTag(
                    icon = Icons.Filled.DesktopWindows,
                    text = "Saved • ${activeApp.label}"
                )
            }

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBootFullScreen,
                    colors = ButtonDefaults.buttonColors(containerColor = brand.accentColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier
                        .weight(1.05f)
                        .height(48.dp)
                        .testTag("boot_phone_${phone.id}")
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Boot OS", color = ObsidianBg, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onOpenMiniDesktop,
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(48.dp)
                        .testTag("mini_desktop_${phone.id}")
                ) {
                    Icon(Icons.Filled.DesktopWindows, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mini",
                        color = CyberCyan,
                        maxLines = 1
                    )
                }

                Surface(
                    color = ObsidianSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, InfinixGreen.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onFloatOutsideApp)
                        .testTag("float_outside_${phone.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PictureInPictureAlt,
                            contentDescription = "Float ${phone.name} Outside App",
                            tint = InfinixGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    color = ObsidianSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RealmeAmber.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onPinToHomeScreen)
                        .testTag("pin_home_${phone.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pin ${phone.name} to Android Home Screen",
                            tint = RealmeAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecTag(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        color = ObsidianSurfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

@Composable
private fun EmptyFleetCard(onOpenCreateSheet: () -> Unit) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ObsidianCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PhoneAndroid,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = "No Cloud Phones in this filter",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = "Create 1 to 5 Infinix or Realme Android 14 virtual phones anytime.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Button(
                onClick = onOpenCreateSheet,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("Create Cloud Phones", color = ObsidianBg, fontWeight = FontWeight.Bold)
            }
        }
    }
}
