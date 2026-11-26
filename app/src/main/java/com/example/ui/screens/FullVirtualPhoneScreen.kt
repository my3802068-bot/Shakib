package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CloudNoteEntity
import com.example.data.model.CloudPhoneEntity
import com.example.data.model.CustomCloudAppEntity
import com.example.data.model.DeviceBrand
import com.example.data.model.InstalledVirtualAppEntity
import com.example.data.model.PerformanceProfile
import com.example.data.model.PlayStoreCatalogItem
import com.example.data.model.VirtualAppId
import com.example.ui.components.VirtualPhoneViewport
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.InfinixGreen
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.RealmeAmber
import com.example.util.HostMemoryTelemetry
import com.example.util.InstalledHostApp
import kotlinx.coroutines.flow.Flow

@Composable
fun FullVirtualPhoneScreen(
    phone: CloudPhoneEntity,
    customCloudApps: List<CustomCloudAppEntity>,
    installedAppsFlow: Flow<List<InstalledVirtualAppEntity>>,
    hostApps: List<InstalledHostApp>,
    memoryTelemetry: HostMemoryTelemetry,
    notesFlow: Flow<List<CloudNoteEntity>>,
    onBackToFleet: () -> Unit,
    onMinimizeToMiniDesktop: () -> Unit,
    onFloatOutsideApp: () -> Unit,
    onPinToHomeScreen: (String?, String?, String?) -> Unit,
    onSelectApp: (VirtualAppId) -> Unit,
    onLaunchUrl: (String) -> Unit,
    onBrowserUrlChanged: (String) -> Unit,
    onInstallPlayApp: (PlayStoreCatalogItem) -> Unit,
    onUninstallPlayApp: (String) -> Unit,
    onChangePerf: (PerformanceProfile) -> Unit,
    onTurboBoost: () -> Unit,
    onUpdateWallpaper: (String, String?) -> Unit,
    onAddNote: (String, String) -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    val brand = DeviceBrand.fromId(phone.brand)

    BackHandler(onBack = onBackToFleet)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Host Control Strip
        Surface(
            color = ObsidianSurface,
            border = BorderStroke(1.dp, brand.accentColor.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBackToFleet,
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("full_phone_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit to Fleet",
                            tint = Color.White
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = phone.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "State auto-saved",
                                tint = InfinixGreen,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = "${phone.modelName} • Auto-Saved ✓",
                            style = MaterialTheme.typography.labelSmall,
                            color = brand.accentColor,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = InfinixGreen.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, InfinixGreen.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onFloatOutsideApp)
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .testTag("full_phone_float_outside_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.PictureInPictureAlt,
                                contentDescription = "Float Outside App",
                                tint = InfinixGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Float",
                                style = MaterialTheme.typography.labelMedium,
                                color = InfinixGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = { onPinToHomeScreen(phone.activeAppId, phone.name, null) },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pin to Home Screen",
                            tint = RealmeAmber
                        )
                    }

                    IconButton(
                        onClick = onMinimizeToMiniDesktop,
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("minimize_to_mini_desktop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DesktopWindows,
                            contentDescription = "Minimize to Mini Desktop",
                            tint = CyberCyan
                        )
                    }
                }
            }
        }

        // Full Virtual Phone OS Viewport
        VirtualPhoneViewport(
            phone = phone,
            customCloudApps = customCloudApps,
            installedAppsFlow = installedAppsFlow,
            hostApps = hostApps,
            memoryTelemetry = memoryTelemetry,
            notesFlow = notesFlow,
            isCompactMini = false,
            onSelectApp = onSelectApp,
            onLaunchUrl = onLaunchUrl,
            onBrowserUrlChanged = onBrowserUrlChanged,
            onInstallPlayApp = onInstallPlayApp,
            onUninstallPlayApp = onUninstallPlayApp,
            onChangePerf = onChangePerf,
            onTurboBoost = onTurboBoost,
            onUpdateWallpaper = onUpdateWallpaper,
            onAddNote = onAddNote,
            onDeleteNote = onDeleteNote,
            onPinAppToHome = { appId, label, url -> onPinToHomeScreen(appId, label, url) },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}
