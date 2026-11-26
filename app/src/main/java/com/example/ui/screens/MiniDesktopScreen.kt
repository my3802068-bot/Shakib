package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.data.model.CloudNoteEntity
import com.example.data.model.CloudPhoneEntity
import com.example.data.model.CustomCloudAppEntity
import com.example.data.model.DeviceBrand
import com.example.data.model.DeviceCatalog
import com.example.data.model.InstalledVirtualAppEntity
import com.example.data.model.PerformanceProfile
import com.example.data.model.PlayStoreCatalogItem
import com.example.data.model.VirtualAppId
import com.example.ui.components.VirtualPhoneViewport
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.InfinixGreen
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.RealmeAmber
import com.example.ui.theme.TextSecondary
import com.example.util.HostMemoryTelemetry
import com.example.util.InstalledHostApp
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

@Composable
fun MiniDesktopScreen(
    allPhones: List<CloudPhoneEntity>,
    activeMiniPhones: List<CloudPhoneEntity>,
    focusedPhoneId: Long?,
    customCloudApps: List<CustomCloudAppEntity>,
    hostApps: List<InstalledHostApp>,
    memoryTelemetry: HostMemoryTelemetry,
    observeNotes: (Long) -> Flow<List<CloudNoteEntity>>,
    observeInstalledApps: (Long) -> Flow<List<InstalledVirtualAppEntity>>,
    onFocusWindow: (Long) -> Unit,
    onOpenMiniWindow: (Long) -> Unit,
    onCloseMiniWindow: (Long) -> Unit,
    onExpandFullScreen: (Long) -> Unit,
    onFloatOutsideApp: (Long) -> Unit,
    onUpdateMiniPosition: (Long, Float, Float) -> Unit,
    onArrangeGrid: (Float, Float, Float) -> Unit,
    onCascade: () -> Unit,
    onSelectApp: (Long, VirtualAppId) -> Unit,
    onLaunchUrl: (Long, String) -> Unit,
    onBrowserUrlChanged: (Long, String) -> Unit,
    onInstallPlayApp: (Long, PlayStoreCatalogItem) -> Unit,
    onUninstallPlayApp: (Long, String) -> Unit,
    onChangePerf: (Long, PerformanceProfile) -> Unit,
    onTurboBoost: (String) -> Unit,
    onUpdateWallpaper: (Long, String, String?) -> Unit,
    onAddNote: (Long, String, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onPinToHomeScreen: (CloudPhoneEntity, String?, String?, String?) -> Unit,
    onOpenCreateSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMenu by remember { mutableStateOf(false) }
    val availableToOpen = remember(allPhones, activeMiniPhones) {
        allPhones.filter { phone -> activeMiniPhones.none { it.id == phone.id } }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF101935), ObsidianBg)
                )
            )
    ) {
        val density = LocalDensity.current
        val canvasWidthDp = maxWidth.value
        val canvasHeightDp = maxHeight.value
        // True mini mobile-phone proportions (~9:18 portrait phone shape!)
        val windowWidthDp = (canvasWidthDp * 0.46f).coerceIn(174f, 206f)
        val windowHeightDp = (windowWidthDp * 1.98f).coerceAtMost((canvasHeightDp - 120f).coerceAtLeast(310f))

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Mini Desktop Toolbar
            Surface(
                color = ObsidianSurface.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, ObsidianCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mini Phone Desktop",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Active: ${activeMiniPhones.size}/${DeviceCatalog.MAX_ACTIVE_MINI_DESKTOPS} • Mobile Mini Size • Auto-Saved",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val activeTargetId = focusedPhoneId ?: activeMiniPhones.lastOrNull()?.id
                        if (activeTargetId != null) {
                            Surface(
                                color = InfinixGreen.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, InfinixGreen.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onFloatOutsideApp(activeTargetId) }
                                    .padding(horizontal = 8.dp, vertical = 7.dp)
                                    .testTag("float_outside_app_button")
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
                        }
                        IconButton(
                            onClick = { onArrangeGrid(canvasWidthDp, windowWidthDp, windowHeightDp) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GridView,
                                contentDescription = "Arrange Mini Phones Grid",
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onCascade,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Layers,
                                contentDescription = "Cascade Mini Phones",
                                tint = RealmeAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box {
                            Surface(
                                color = CyberCyan,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (availableToOpen.isEmpty()) {
                                            onOpenCreateSheet()
                                        } else {
                                            showAddMenu = true
                                        }
                                    }
                                    .padding(horizontal = 9.dp, vertical = 7.dp)
                                    .testTag("mini_desktop_add_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Add Mini Phone",
                                        tint = ObsidianBg,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Add",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ObsidianBg,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showAddMenu,
                                onDismissRequest = { showAddMenu = false }
                            ) {
                                availableToOpen.forEach { phone ->
                                    DropdownMenuItem(
                                        text = { Text("${phone.name} (${phone.modelName})") },
                                        onClick = {
                                            showAddMenu = false
                                            onOpenMiniWindow(phone.id)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("+ Create New 1–5 Phones") },
                                    onClick = {
                                        showAddMenu = false
                                        onOpenCreateSheet()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Floating Multi-Window Desktop Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (activeMiniPhones.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DesktopWindows,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(52.dp)
                        )
                        Text(
                            text = "Mini Phone Desktop Empty",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Open up to 5 mobile-sized Infinix or Realme Android 14 phones side-by-side or float one outside the app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Button(
                            onClick = {
                                val first = allPhones.firstOrNull()
                                if (first != null) onOpenMiniWindow(first.id) else onOpenCreateSheet()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Text(
                                text = if (allPhones.isNotEmpty()) "Launch Saved Mini Phone" else "Create Cloud Phones",
                                color = ObsidianBg,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    val maxOffsetXPx = with(density) { (canvasWidthDp - windowWidthDp).coerceAtLeast(0f).dp.toPx() }
                    val maxOffsetYPx = with(density) { (canvasHeightDp - windowHeightDp - 64f).coerceAtLeast(0f).dp.toPx() }

                    activeMiniPhones.forEachIndexed { idx, phone ->
                        val isFocused = focusedPhoneId == phone.id ||
                            (focusedPhoneId == null && idx == activeMiniPhones.lastIndex)

                        DraggableMiniPhoneWindow(
                            phone = phone,
                            windowWidthDp = windowWidthDp,
                            windowHeightDp = windowHeightDp,
                            maxOffsetXPx = maxOffsetXPx,
                            maxOffsetYPx = maxOffsetYPx,
                            zOrder = if (isFocused) 10f else idx.toFloat(),
                            isFocused = isFocused,
                            customCloudApps = customCloudApps,
                            hostApps = hostApps,
                            memoryTelemetry = memoryTelemetry,
                            notesFlow = observeNotes(phone.id),
                            installedAppsFlow = observeInstalledApps(phone.id),
                            onFocus = { onFocusWindow(phone.id) },
                            onDragEnd = { newX, newY -> onUpdateMiniPosition(phone.id, newX, newY) },
                            onExpand = { onExpandFullScreen(phone.id) },
                            onFloatOutside = { onFloatOutsideApp(phone.id) },
                            onClose = { onCloseMiniWindow(phone.id) },
                            onPinToHome = { appId, appLabel, appUrl ->
                                onPinToHomeScreen(phone, appId, appLabel, appUrl)
                            },
                            onSelectApp = { app -> onSelectApp(phone.id, app) },
                            onLaunchUrl = { url -> onLaunchUrl(phone.id, url) },
                            onBrowserUrlChanged = { url -> onBrowserUrlChanged(phone.id, url) },
                            onInstallPlayApp = { item -> onInstallPlayApp(phone.id, item) },
                            onUninstallPlayApp = { key -> onUninstallPlayApp(phone.id, key) },
                            onChangePerf = { perf -> onChangePerf(phone.id, perf) },
                            onTurboBoost = { onTurboBoost(phone.name) },
                            onUpdateWallpaper = { key, uri -> onUpdateWallpaper(phone.id, key, uri) },
                            onAddNote = { title, body -> onAddNote(phone.id, title, body) },
                            onDeleteNote = onDeleteNote
                        )
                    }
                }
            }

            // Bottom Mini Desktop Taskbar
            Surface(
                color = ObsidianSurface,
                border = BorderStroke(1.dp, ObsidianCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(activeMiniPhones, key = { "task_${it.id}" }) { phone ->
                        val brand = DeviceBrand.fromId(phone.brand)
                        val isFocused = focusedPhoneId == phone.id
                        Surface(
                            color = if (isFocused) brand.accentColor.copy(alpha = 0.2f) else ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isFocused) brand.accentColor else ObsidianCardBorder
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onFocusWindow(phone.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(brand.accentColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = phone.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableMiniPhoneWindow(
    phone: CloudPhoneEntity,
    windowWidthDp: Float,
    windowHeightDp: Float,
    maxOffsetXPx: Float,
    maxOffsetYPx: Float,
    zOrder: Float,
    isFocused: Boolean,
    customCloudApps: List<CustomCloudAppEntity>,
    hostApps: List<InstalledHostApp>,
    memoryTelemetry: HostMemoryTelemetry,
    notesFlow: Flow<List<CloudNoteEntity>>,
    installedAppsFlow: Flow<List<InstalledVirtualAppEntity>>,
    onFocus: () -> Unit,
    onDragEnd: (Float, Float) -> Unit,
    onExpand: () -> Unit,
    onFloatOutside: () -> Unit,
    onClose: () -> Unit,
    onPinToHome: (String?, String?, String?) -> Unit,
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
    val density = LocalDensity.current
    val brand = remember(phone.brand) { DeviceBrand.fromId(phone.brand) }

    var offsetXPx by remember(phone.id) {
        mutableFloatStateOf(with(density) { phone.miniX.dp.toPx() }.coerceIn(0f, maxOffsetXPx.coerceAtLeast(1f)))
    }
    var offsetYPx by remember(phone.id) {
        mutableFloatStateOf(with(density) { phone.miniY.dp.toPx() }.coerceIn(0f, maxOffsetYPx.coerceAtLeast(1f)))
    }

    LaunchedEffect(phone.miniX, phone.miniY) {
        offsetXPx = with(density) { phone.miniX.dp.toPx() }.coerceIn(0f, maxOffsetXPx.coerceAtLeast(1f))
        offsetYPx = with(density) { phone.miniY.dp.toPx() }.coerceIn(0f, maxOffsetYPx.coerceAtLeast(1f))
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF04070F),
        border = BorderStroke(
            width = if (isFocused) 2.dp else 1.25.dp,
            color = if (isFocused) brand.accentColor else ObsidianCardBorder
        ),
        modifier = Modifier
            .offset { IntOffset(offsetXPx.roundToInt(), offsetYPx.roundToInt()) }
            .zIndex(zOrder)
            .width(windowWidthDp.dp)
            .height(windowHeightDp.dp)
            .shadow(if (isFocused) 20.dp else 8.dp, RoundedCornerShape(22.dp))
            .clickable(onClick = onFocus)
            .testTag("mini_window_${phone.id}")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Draggable Phone Top Bezel Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isFocused) ObsidianSurfaceVariant else ObsidianSurface)
                    .pointerInput(phone.id, maxOffsetXPx, maxOffsetYPx) {
                        detectDragGestures(
                            onDragStart = { onFocus() },
                            onDragEnd = {
                                val dpX = with(density) { offsetXPx.toDp().value }
                                val dpY = with(density) { offsetYPx.toDp().value }
                                onDragEnd(dpX, dpY)
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            offsetXPx = (offsetXPx + dragAmount.x).coerceIn(0f, maxOffsetXPx.coerceAtLeast(0f))
                            offsetYPx = (offsetYPx + dragAmount.y).coerceIn(0f, maxOffsetYPx.coerceAtLeast(0f))
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(brand.accentColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = phone.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onFloatOutside,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PictureInPictureAlt,
                            contentDescription = "Float Outside App",
                            tint = InfinixGreen,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    IconButton(
                        onClick = { onPinToHome(phone.activeAppId, phone.name, null) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pin Mini Phone to Home Screen",
                            tint = RealmeAmber,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    IconButton(
                        onClick = onExpand,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInFull,
                            contentDescription = "Expand Full OS",
                            tint = CyberCyan,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Mini Window",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Live Interactive Mobile-Sized Virtual Phone Viewport
            VirtualPhoneViewport(
                phone = phone,
                customCloudApps = customCloudApps,
                installedAppsFlow = installedAppsFlow,
                hostApps = hostApps,
                memoryTelemetry = memoryTelemetry,
                notesFlow = notesFlow,
                isCompactMini = true,
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
                onPinAppToHome = { appId, label, url -> onPinToHome(appId, label, url) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}
