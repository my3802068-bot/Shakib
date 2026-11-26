package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CloudNoteEntity
import com.example.data.model.CloudPhoneEntity
import com.example.data.model.CustomCloudAppEntity
import com.example.data.model.DeviceBrand
import com.example.data.model.DeviceCatalog
import com.example.data.model.InstalledVirtualAppEntity
import com.example.data.model.PerformanceProfile
import com.example.data.model.PlayStoreCatalogItem
import com.example.data.model.VirtualAppId
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.InfinixGreen
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.RealmeAmber
import com.example.util.HostMemoryTelemetry
import com.example.util.HostTelemetryHelper
import com.example.util.InstalledHostApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VirtualPhoneViewport(
    phone: CloudPhoneEntity,
    customCloudApps: List<CustomCloudAppEntity>,
    installedAppsFlow: Flow<List<InstalledVirtualAppEntity>>,
    hostApps: List<InstalledHostApp>,
    memoryTelemetry: HostMemoryTelemetry,
    notesFlow: Flow<List<CloudNoteEntity>>,
    isCompactMini: Boolean,
    onSelectApp: (VirtualAppId) -> Unit,
    onLaunchUrl: (String) -> Unit,
    onBrowserUrlChanged: (String) -> Unit,
    onInstallPlayApp: (PlayStoreCatalogItem) -> Unit,
    onUninstallPlayApp: (String) -> Unit,
    onChangePerf: (PerformanceProfile) -> Unit,
    onTurboBoost: () -> Unit,
    onUpdateWallpaper: (String, String?) -> Unit,
    onAddNote: (String, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onPinAppToHome: (String, String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val brand = remember(phone.brand) { DeviceBrand.fromId(phone.brand) }
    val template = remember(phone.modelId) { DeviceCatalog.findById(phone.modelId) }
    val perf = remember(phone.performanceMode) { PerformanceProfile.fromId(phone.performanceMode) }
    val activeApp = remember(phone.activeAppId) { VirtualAppId.fromId(phone.activeAppId) }
    val installedPlayApps by installedAppsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        WallpaperBackground(
            wallpaperKey = phone.wallpaperKey,
            customUri = phone.customWallpaperUri,
            brand = brand
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = if (activeApp == VirtualAppId.HOME) 0.22f else 0.84f)
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Authentic Android 14 Status Bar + Magic Ring / Mini Capsule
            VirtualStatusBar(
                brand = brand,
                perf = perf,
                isCompactMini = isCompactMini
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeApp) {
                    VirtualAppId.HOME -> VirtualHomeScreen(
                        phone = phone,
                        brand = brand,
                        perf = perf,
                        installedPlayApps = installedPlayApps,
                        customCloudApps = customCloudApps,
                        isCompactMini = isCompactMini,
                        onOpenApp = onSelectApp,
                        onLaunchUrl = onLaunchUrl,
                        onPinAppToHome = onPinAppToHome
                    )
                    VirtualAppId.PLAY_STORE -> VirtualPlayStoreApp(
                        brand = brand,
                        installedApps = installedPlayApps,
                        isCompactMini = isCompactMini,
                        onInstall = onInstallPlayApp,
                        onUninstall = onUninstallPlayApp,
                        onOpenAppUrl = onLaunchUrl
                    )
                    VirtualAppId.BROWSER -> VirtualBrowserApp(
                        phone = phone,
                        userAgent = template.userAgent,
                        isCompactMini = isCompactMini,
                        onUrlCommitted = onLaunchUrl,
                        onUrlVisited = onBrowserUrlChanged,
                        onPinCurrentSite = { title, url ->
                            onPinAppToHome(VirtualAppId.BROWSER.id, title, url)
                        }
                    )
                    VirtualAppId.DIALER -> VirtualDialerApp(
                        brand = brand,
                        isCompactMini = isCompactMini
                    )
                    VirtualAppId.APPS -> VirtualAppDrawer(
                        installedPlayApps = installedPlayApps,
                        customCloudApps = customCloudApps,
                        hostApps = hostApps,
                        isCompactMini = isCompactMini,
                        onOpenBuiltIn = onSelectApp,
                        onLaunchUrl = onLaunchUrl,
                        onPinWebApp = { name, url ->
                            onPinAppToHome(VirtualAppId.BROWSER.id, name, url)
                        }
                    )
                    VirtualAppId.NOTES -> VirtualNotesApp(
                        notesFlow = notesFlow,
                        brand = brand,
                        isCompactMini = isCompactMini,
                        onAddNote = onAddNote,
                        onDeleteNote = onDeleteNote
                    )
                    VirtualAppId.CALCULATOR -> VirtualCalculatorApp(
                        brand = brand,
                        isCompactMini = isCompactMini
                    )
                    VirtualAppId.BOOST -> VirtualSystem14App(
                        phone = phone,
                        brand = brand,
                        templateChipset = template.chipset,
                        perf = perf,
                        memoryTelemetry = memoryTelemetry,
                        isCompactMini = isCompactMini,
                        onChangePerf = onChangePerf,
                        onTurboBoost = onTurboBoost
                    )
                    VirtualAppId.WALLPAPER -> VirtualWallpaperApp(
                        phone = phone,
                        brand = brand,
                        isCompactMini = isCompactMini,
                        onUpdateWallpaper = onUpdateWallpaper
                    )
                }
            }

            // Realistic Android 14 Gesture / Navigation Bar
            VirtualNavBar(
                brand = brand,
                isCompactMini = isCompactMini,
                onBack = {
                    if (activeApp != VirtualAppId.HOME) {
                        onSelectApp(VirtualAppId.HOME)
                    }
                },
                onHome = { onSelectApp(VirtualAppId.HOME) },
                onRecents = { onSelectApp(VirtualAppId.APPS) }
            )
        }
    }
}

@Composable
private fun WallpaperBackground(
    wallpaperKey: String,
    customUri: String?,
    brand: DeviceBrand
) {
    if (!customUri.isNullOrBlank()) {
        AsyncImage(
            model = Uri.parse(customUri),
            contentDescription = "Custom virtual phone wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    when (wallpaperKey) {
        "xos_wave" -> Image(
            painter = painterResource(id = R.drawable.img_wallpaper_xos_1795454506891),
            contentDescription = "Infinix XOS 14 wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        "realme_prism" -> Image(
            painter = painterResource(id = R.drawable.img_wallpaper_realme_1795454506827),
            contentDescription = "Realme UI 5.0 wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        else -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF090E1D),
                            brand.accentColor.copy(alpha = 0.25f),
                            Color(0xFF0A1228)
                        )
                    )
                )
        )
    }
}

@Composable
private fun VirtualStatusBar(
    brand: DeviceBrand,
    perf: PerformanceProfile,
    isCompactMini: Boolean
) {
    val context = LocalContext.current
    var clockText by remember { mutableStateOf("12:00") }
    val batteryPct = remember(context) {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) (level * 100) / scale else 98
    }

    LaunchedEffect(Unit) {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            clockText = fmt.format(Date())
            delay(15_000L)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.48f))
            .padding(
                horizontal = if (isCompactMini) 8.dp else 14.dp,
                vertical = if (isCompactMini) 3.dp else 6.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = clockText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isCompactMini) 9.sp else 11.sp
            ),
            color = Color.White
        )

        // Punch-hole camera + Infinix Magic Ring / Realme Mini Capsule
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .height(if (isCompactMini) 12.dp else 18.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.92f))
                .border(0.75.dp, brand.accentColor.copy(alpha = 0.55f), CircleShape)
                .padding(horizontal = if (isCompactMini) 6.dp else 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompactMini) 5.dp else 7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
            )
            Spacer(modifier = Modifier.width(if (isCompactMini) 4.dp else 6.dp))
            Text(
                text = if (brand == DeviceBrand.INFINIX) "XOS 14 • ${perf.refreshRateHz}Hz" else "RUI 5 • ${perf.refreshRateHz}Hz",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = if (isCompactMini) 7.sp else 9.sp,
                    fontFamily = MonoFontFamily
                ),
                color = brand.accentColor
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(
                imageVector = Icons.Filled.SignalCellularAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(if (isCompactMini) 10.dp else 13.dp)
            )
            Icon(
                imageVector = Icons.Filled.Wifi,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(if (isCompactMini) 10.dp else 13.dp)
            )
            if (!isCompactMini) {
                Icon(
                    imageVector = Icons.Filled.BatteryChargingFull,
                    contentDescription = null,
                    tint = brand.accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = "$batteryPct%",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isCompactMini) 8.sp else 10.sp),
                color = Color.White
            )
        }
    }
}

@Composable
private fun VirtualHomeScreen(
    phone: CloudPhoneEntity,
    brand: DeviceBrand,
    perf: PerformanceProfile,
    installedPlayApps: List<InstalledVirtualAppEntity>,
    customCloudApps: List<CustomCloudAppEntity>,
    isCompactMini: Boolean,
    onOpenApp: (VirtualAppId) -> Unit,
    onLaunchUrl: (String) -> Unit,
    onPinAppToHome: (String, String, String?) -> Unit
) {
    val dateLabel = remember {
        SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isCompactMini) 7.dp else 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Real Phone At-A-Glance + Search Pill
        Column(verticalArrangement = Arrangement.spacedBy(if (isCompactMini) 5.dp else 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateLabel,
                        style = if (isCompactMini) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${phone.modelName} • ${phone.androidVersion}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isCompactMini) 8.sp else 10.sp),
                        color = brand.accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.52f),
                    shape = CircleShape,
                    border = BorderStroke(0.75.dp, brand.accentColor.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onOpenApp(VirtualAppId.BOOST) }
                ) {
                    Text(
                        text = "${phone.ramGb}G • ${perf.refreshRateHz}Hz",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isCompactMini) 8.sp else 10.sp),
                        color = brand.accentColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            // Google-style quick search pill -> launches Chrome browser inside virtual phone
            Surface(
                color = Color(0xFF101729).copy(alpha = 0.85f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCompactMini) 26.dp else 40.dp)
                    .clip(CircleShape)
                    .clickable { onOpenApp(VirtualAppId.BROWSER) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = if (isCompactMini) 10.dp else 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search web",
                        tint = brand.accentColor,
                        modifier = Modifier.size(if (isCompactMini) 13.dp else 18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Search Google or URL...",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isCompactMini) 9.sp else 12.sp),
                        color = Color.LightGray,
                        maxLines = 1
                    )
                }
            }
        }

        // Core Real Phone Apps + Play Store Installed Apps Grid
        val coreIcons = remember {
            listOf(
                Triple(VirtualAppId.PLAY_STORE, "Play Store", Icons.Filled.Shop),
                Triple(VirtualAppId.BROWSER, "Chrome", Icons.Filled.Language),
                Triple(VirtualAppId.BOOST, "Settings", Icons.Filled.Settings),
                Triple(VirtualAppId.NOTES, "Notes", Icons.Filled.EditNote),
                Triple(VirtualAppId.CALCULATOR, "Calc", Icons.Filled.Calculate),
                Triple(VirtualAppId.WALLPAPER, "Themes", Icons.Filled.Wallpaper)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isCompactMini) 3 else 4),
            modifier = Modifier
                .weight(1f)
                .padding(top = if (isCompactMini) 6.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (isCompactMini) 5.dp else 10.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isCompactMini) 4.dp else 8.dp)
        ) {
            items(coreIcons, key = { it.first.id }) { (appId, label, icon) ->
                val iconTint = if (appId == VirtualAppId.PLAY_STORE) Color(0xFF00E676) else brand.accentColor
                VirtualAppShortcutTile(
                    label = label,
                    icon = icon,
                    tint = iconTint,
                    isCompactMini = isCompactMini,
                    onClick = { onOpenApp(appId) },
                    onPin = { onPinAppToHome(appId.id, label, null) }
                )
            }

            // Apps installed from this phone's Play Store
            items(installedPlayApps, key = { "play_${it.id}" }) { app ->
                VirtualWebShortcutTile(
                    name = app.name,
                    badgeColorHex = app.badgeColorHex,
                    isCompactMini = isCompactMini,
                    onClick = { onLaunchUrl(app.url) }
                )
            }

            // Custom Cloud Apps
            items(
                customCloudApps.filter { custom ->
                    installedPlayApps.none { it.name.equals(custom.name, ignoreCase = true) }
                }.take(if (isCompactMini) 2 else 4),
                key = { "custom_${it.id}" }
            ) { webApp ->
                VirtualWebShortcutTile(
                    name = webApp.name,
                    badgeColorHex = webApp.badgeColorHex,
                    isCompactMini = isCompactMini,
                    onClick = { onLaunchUrl(webApp.url) }
                )
            }
        }

        // Authentic Android 14 Dock (Phone, Chrome, Play Store, Apps)
        Surface(
            color = Color.Black.copy(alpha = 0.62f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = if (isCompactMini) 3.dp else 7.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockActionIcon(Icons.Filled.Call, "Phone", InfinixGreen, isCompactMini) {
                    onOpenApp(VirtualAppId.DIALER)
                }
                DockActionIcon(Icons.Filled.Language, "Chrome", CyberCyan, isCompactMini) {
                    onOpenApp(VirtualAppId.BROWSER)
                }
                DockActionIcon(Icons.Filled.Shop, "Play Store", Color(0xFF4ADE80), isCompactMini) {
                    onOpenApp(VirtualAppId.PLAY_STORE)
                }
                DockActionIcon(Icons.Filled.Apps, "All Apps", brand.accentColor, isCompactMini) {
                    onOpenApp(VirtualAppId.APPS)
                }
            }
        }
    }
}

@Composable
private fun VirtualPlayStoreApp(
    brand: DeviceBrand,
    installedApps: List<InstalledVirtualAppEntity>,
    isCompactMini: Boolean,
    onInstall: (PlayStoreCatalogItem) -> Unit,
    onUninstall: (String) -> Unit,
    onOpenAppUrl: (String) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Social", "Video", "Games", "Productivity")

    val installedKeys = remember(installedApps) { installedApps.map { it.appKey }.toSet() }
    val filtered = remember(query, selectedCategory) {
        DeviceCatalog.playStoreCatalog.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.name.contains(query, ignoreCase = true) ||
                item.category.contains(query, ignoreCase = true)
            val matchesCat = selectedCategory == "All" ||
                item.category.equals(selectedCategory, ignoreCase = true)
            matchesQuery && matchesCat
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B111E))
            .padding(if (isCompactMini) 7.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (isCompactMini) 6.dp else 10.dp)
    ) {
        // Play Store Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Shop,
                    contentDescription = "Play Store",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(if (isCompactMini) 16.dp else 22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Play Store",
                    style = if (isCompactMini) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                color = ObsidianSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCompactMini) "Store" else "Google Play",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open Google Play",
                        tint = CyberCyan,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        // Search input
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = {
                Text(
                    "Search apps & games...",
                    fontSize = if (isCompactMini) 10.sp else 13.sp
                )
            },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("play_store_search")
        )

        // Category pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories) { cat ->
                val selected = selectedCategory == cat
                Surface(
                    color = if (selected) Color(0xFF00E676) else ObsidianSurfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) ObsidianBg else Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // App list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(if (isCompactMini) 6.dp else 8.dp)
        ) {
            items(filtered, key = { it.appKey }) { item ->
                val isInstalled = installedKeys.contains(item.appKey)
                val badge = Color(item.badgeColorHex)
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isInstalled) Color(0xFF00E676).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(if (isCompactMini) 7.dp else 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isCompactMini) 32.dp else 44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(badge.copy(alpha = 0.22f))
                                .border(1.dp, badge.copy(alpha = 0.7f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.name.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = if (isCompactMini) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                color = Color.White,
                                style = if (isCompactMini) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.category} • ${item.rating} • ${item.sizeLabel}",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isCompactMini) 8.sp else 10.sp),
                                maxLines = 1
                            )
                        }
                        if (isInstalled) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    color = brand.accentColor,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onOpenAppUrl(item.url) }
                                ) {
                                    Text(
                                        text = "Open",
                                        color = ObsidianBg,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                                if (!isCompactMini) {
                                    IconButton(
                                        onClick = { onUninstall(item.appKey) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Uninstall ${item.name}",
                                            tint = Color(0xFFFF6B6B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                color = Color(0xFF00E676),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onInstall(item) }
                                    .testTag("install_${item.appKey}")
                            ) {
                                Text(
                                    text = "Install",
                                    color = ObsidianBg,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
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
private fun VirtualDialerApp(
    brand: DeviceBrand,
    isCompactMini: Boolean
) {
    val context = LocalContext.current
    var number by remember { mutableStateOf("") }
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("*", "0", "#")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isCompactMini) 8.dp else 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            color = ObsidianSurface,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = if (isCompactMini) 8.dp else 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = number.ifEmpty { "Enter number" },
                    style = if (isCompactMini) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                    color = if (number.isEmpty()) Color.Gray else Color.White,
                    maxLines = 1
                )
                IconButton(
                    onClick = { if (number.isNotEmpty()) number = number.dropLast(1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(if (isCompactMini) 4.dp else 8.dp)) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isCompactMini) 5.dp else 10.dp)
                ) {
                    row.forEach { digit ->
                        Surface(
                            color = ObsidianSurface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isCompactMini) 32.dp else 50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { if (number.length < 15) number += digit }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = digit,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching { context.startActivity(dialIntent) }
            },
            colors = ButtonDefaults.buttonColors(containerColor = InfinixGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isCompactMini) 34.dp else 48.dp)
        ) {
            Icon(Icons.Filled.Call, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Call", color = ObsidianBg, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VirtualAppShortcutTile(
    label: String,
    icon: ImageVector,
    tint: Color,
    isCompactMini: Boolean,
    onClick: () -> Unit,
    onPin: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = if (isCompactMini) 2.dp else 4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(if (isCompactMini) 34.dp else 50.dp)
                    .clip(RoundedCornerShape(if (isCompactMini) 10.dp else 15.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(tint.copy(alpha = 0.28f), Color(0xFF151E36))
                        )
                    )
                    .border(
                        1.dp,
                        tint.copy(alpha = 0.55f),
                        RoundedCornerShape(if (isCompactMini) 10.dp else 15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(if (isCompactMini) 17.dp else 25.dp)
                )
            }
            if (!isCompactMini) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(ObsidianBg.copy(alpha = 0.9f))
                        .clickable(onClick = onPin),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Pin $label to Home",
                        tint = tint,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isCompactMini) 8.sp else 11.sp),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun VirtualWebShortcutTile(
    name: String,
    badgeColorHex: Long,
    isCompactMini: Boolean,
    onClick: () -> Unit
) {
    val badgeColor = Color(badgeColorHex)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = if (isCompactMini) 2.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (isCompactMini) 34.dp else 50.dp)
                .clip(RoundedCornerShape(if (isCompactMini) 10.dp else 15.dp))
                .background(badgeColor.copy(alpha = 0.24f))
                .border(
                    1.dp,
                    badgeColor.copy(alpha = 0.72f),
                    RoundedCornerShape(if (isCompactMini) 10.dp else 15.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2).uppercase(),
                style = if (isCompactMini) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isCompactMini) 8.sp else 11.sp),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DockActionIcon(
    icon: ImageVector,
    description: String,
    tint: Color,
    isCompactMini: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(if (isCompactMini) 30.dp else 46.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(if (isCompactMini) 16.dp else 23.dp)
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun VirtualBrowserApp(
    phone: CloudPhoneEntity,
    userAgent: String,
    isCompactMini: Boolean,
    onUrlCommitted: (String) -> Unit,
    onUrlVisited: (String) -> Unit,
    onPinCurrentSite: (String, String) -> Unit
) {
    val context = LocalContext.current
    var urlInput by remember(phone.id) { mutableStateOf(phone.browserUrl) }
    var currentTitle by remember(phone.id) { mutableStateOf("Chrome") }
    var loadProgress by remember(phone.id) { mutableIntStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    val webView = remember(phone.id) {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                userAgentString = userAgent
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val target = request?.url?.toString() ?: return false
                    return if (target.startsWith("http://") || target.startsWith("https://")) {
                        false
                    } else {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(target))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                        true
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (!url.isNullOrBlank()) {
                        urlInput = url
                        onUrlVisited(url)
                    }
                    canGoBack = view?.canGoBack() == true
                    canGoForward = view?.canGoForward() == true
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    loadProgress = newProgress
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    if (!title.isNullOrBlank()) currentTitle = title
                }
            }
            loadUrl(phone.browserUrl)
        }
    }

    LaunchedEffect(phone.browserUrl) {
        val loaded = webView.url
        if (loaded != null && loaded != phone.browserUrl && phone.browserUrl.startsWith("http")) {
            urlInput = phone.browserUrl
            webView.loadUrl(phone.browserUrl)
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ObsidianSurface)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (webView.canGoBack()) webView.goBack() },
                enabled = canGoBack,
                modifier = Modifier.size(if (isCompactMini) 24.dp else 38.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (canGoBack) Color.White else Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
            }
            if (!isCompactMini) {
                IconButton(
                    onClick = { if (webView.canGoForward()) webView.goForward() },
                    enabled = canGoForward,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (canGoForward) Color.White else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (isCompactMini) 24.dp else 34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianSurfaceVariant)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontSize = if (isCompactMini) 10.sp else 13.sp
                    ),
                    cursorBrush = SolidColor(CyberCyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            val target = if (urlInput.startsWith("http")) urlInput else "https://$urlInput"
                            webView.loadUrl(target)
                            onUrlCommitted(target)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(
                onClick = { webView.reload() },
                modifier = Modifier.size(if (isCompactMini) 24.dp else 38.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reload",
                    tint = CyberCyan,
                    modifier = Modifier.size(14.dp)
                )
            }
            IconButton(
                onClick = { onPinCurrentSite(currentTitle.take(14), urlInput) },
                modifier = Modifier.size(if (isCompactMini) 24.dp else 38.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PushPin,
                    contentDescription = "Pin site to Home Screen",
                    tint = RealmeAmber,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        if (loadProgress in 1..99) {
            LinearProgressIndicator(
                progress = { loadProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = CyberCyan,
                trackColor = ObsidianSurfaceVariant
            )
        }

        AndroidView(
            factory = { webView },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun VirtualAppDrawer(
    installedPlayApps: List<InstalledVirtualAppEntity>,
    customCloudApps: List<CustomCloudAppEntity>,
    hostApps: List<InstalledHostApp>,
    isCompactMini: Boolean,
    onOpenBuiltIn: (VirtualAppId) -> Unit,
    onLaunchUrl: (String) -> Unit,
    onPinWebApp: (String, String) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(if (isCompactMini) 8.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Installed Apps (${installedPlayApps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF00E676)
                )
                OutlinedButton(
                    onClick = { onOpenBuiltIn(VirtualAppId.PLAY_STORE) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.Shop, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play Store", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E676))
                }
            }
        }

        items(installedPlayApps, key = { "drawer_play_${it.id}" }) { app ->
            Surface(
                color = ObsidianSurface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLaunchUrl(app.url) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(app.badgeColorHex).copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.name, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        Text(app.category, color = Color.Gray, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                    IconButton(
                        onClick = { onPinWebApp(app.name, app.url) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pin ${app.name} to Home",
                            tint = RealmeAmber,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        if (customCloudApps.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Custom Web Apps",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberCyan
                )
            }
            items(customCloudApps, key = { "drawer_web_${it.id}" }) { app ->
                Surface(
                    color = ObsidianSurface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLaunchUrl(app.url) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(Color(app.badgeColorHex).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.name.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(app.name, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            Text(app.category, color = Color.Gray, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                        IconButton(
                            onClick = { onPinWebApp(app.name, app.url) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pin ${app.name} to Home",
                                tint = RealmeAmber,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Host Device Apps (${hostApps.size})",
                style = MaterialTheme.typography.titleMedium,
                color = InfinixGreen
            )
        }
        items(hostApps.take(15), key = { it.packageName }) { hostApp ->
            Surface(
                color = ObsidianSurfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { HostTelemetryHelper.launchHostApp(context, hostApp.packageName) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = InfinixGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = hostApp.label,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "LAUNCH",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun VirtualNotesApp(
    notesFlow: Flow<List<CloudNoteEntity>>,
    brand: DeviceBrand,
    isCompactMini: Boolean,
    onAddNote: (String, String) -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    val notes by notesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isCompactMini) 8.dp else 14.dp)
    ) {
        Text(
            text = "Saved Phone Notes",
            style = MaterialTheme.typography.titleMedium,
            color = brand.accentColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Note title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                placeholder = { Text("Write note...") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    onAddNote(title, body)
                    title = ""
                    body = ""
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(brand.accentColor, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Save note", tint = ObsidianBg)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(notes, key = { it.id }) { note ->
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(note.title, color = Color.White, style = MaterialTheme.typography.labelLarge)
                            if (note.content.isNotBlank()) {
                                Text(note.content, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        IconButton(onClick = { onDeleteNote(note.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete note", tint = Color(0xFFFF6B6B))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VirtualCalculatorApp(
    brand: DeviceBrand,
    isCompactMini: Boolean
) {
    var display by remember { mutableStateOf("0") }
    var storedValue by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var resetNext by remember { mutableStateOf(false) }

    fun applyOp(a: Double, b: Double, op: String): Double = when (op) {
        "+" -> a + b
        "-" -> a - b
        "×" -> a * b
        "÷" -> if (b != 0.0) a / b else 0.0
        else -> b
    }

    fun formatNum(value: Double): String {
        return if (value % 1.0 == 0.0) value.toLong().toString() else "%.4f".format(value).trimEnd('0').trimEnd('.')
    }

    fun onKey(key: String) {
        when (key) {
            "C" -> {
                display = "0"
                storedValue = null
                pendingOp = null
                resetNext = false
            }
            "+", "-", "×", "÷" -> {
                val curr = display.toDoubleOrNull() ?: 0.0
                if (storedValue != null && pendingOp != null && !resetNext) {
                    val res = applyOp(storedValue!!, curr, pendingOp!!)
                    storedValue = res
                    display = formatNum(res)
                } else {
                    storedValue = curr
                }
                pendingOp = key
                resetNext = true
            }
            "=" -> {
                val curr = display.toDoubleOrNull() ?: 0.0
                if (storedValue != null && pendingOp != null) {
                    val res = applyOp(storedValue!!, curr, pendingOp!!)
                    display = formatNum(res)
                    storedValue = null
                    pendingOp = null
                    resetNext = true
                }
            }
            else -> {
                display = if (resetNext || display == "0") {
                    resetNext = false
                    if (key == ".") "0." else key
                } else {
                    if (key == "." && display.contains(".")) display else display + key
                }
            }
        }
    }

    val rows = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"),
        listOf("C", "0", "=", "+")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isCompactMini) 7.dp else 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            color = ObsidianSurface,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = display,
                style = if (isCompactMini) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displaySmall,
                color = Color.White,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isCompactMini) 8.dp else 18.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(if (isCompactMini) 4.dp else 10.dp)) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isCompactMini) 4.dp else 10.dp)
                ) {
                    row.forEach { key ->
                        val isOp = key in listOf("+", "-", "×", "÷", "=", "C")
                        Surface(
                            color = if (key == "=") brand.accentColor else if (isOp) ObsidianSurfaceVariant else ObsidianSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isCompactMini) 30.dp else 54.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onKey(key) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = key,
                                    color = if (key == "=") ObsidianBg else if (isOp) brand.accentColor else Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
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
private fun VirtualSystem14App(
    phone: CloudPhoneEntity,
    brand: DeviceBrand,
    templateChipset: String,
    perf: PerformanceProfile,
    memoryTelemetry: HostMemoryTelemetry,
    isCompactMini: Boolean,
    onChangePerf: (PerformanceProfile) -> Unit,
    onTurboBoost: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(if (isCompactMini) 8.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, brand.accentColor.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = InfinixGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "About Phone • ${brand.osSkin}",
                            style = MaterialTheme.typography.titleMedium,
                            color = brand.accentColor
                        )
                    }
                    Text(
                        text = "${phone.modelName} (${phone.androidVersion})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Build: ${brand.buildId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                    Text(
                        text = "$templateChipset • ${phone.ramGb}GB+8GB • ${phone.storageGb}GB",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                    Text(
                        text = "State Auto-Saved ✓ Resumes exactly here on reopen",
                        style = MaterialTheme.typography.labelSmall,
                        color = InfinixGreen
                    )
                }
            }
        }

        item {
            Surface(
                color = ObsidianSurfaceVariant,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Memory, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Host RAM Engine", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                        Text(
                            text = "${memoryTelemetry.availableRamMb}MB free",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { memoryTelemetry.usedPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = brand.accentColor,
                        trackColor = ObsidianBg
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onTurboBoost,
                        colors = ButtonDefaults.buttonColors(containerColor = brand.accentColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("turbo_boost_button")
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, tint = ObsidianBg)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Smooth 120Hz Turbo Boost", color = ObsidianBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Display Refresh Rate",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PerformanceProfile.entries) { option ->
                    val selected = option == perf
                    Surface(
                        color = if (selected) brand.accentColor else ObsidianSurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onChangePerf(option) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Speed,
                                contentDescription = null,
                                tint = if (selected) ObsidianBg else brand.accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = option.label,
                                color = if (selected) ObsidianBg else Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VirtualWallpaperApp(
    phone: CloudPhoneEntity,
    brand: DeviceBrand,
    isCompactMini: Boolean,
    onUpdateWallpaper: (String, String?) -> Unit
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdateWallpaper("custom", uri.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isCompactMini) 8.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Android 14 Themes & Wallpapers",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WallpaperChoiceCard(
                title = "Infinix XOS 14",
                selected = phone.wallpaperKey == "xos_wave" && phone.customWallpaperUri == null,
                accent = InfinixGreen,
                modifier = Modifier.weight(1f),
                onClick = { onUpdateWallpaper("xos_wave", null) }
            )
            WallpaperChoiceCard(
                title = "Realme UI 5.0",
                selected = phone.wallpaperKey == "realme_prism" && phone.customWallpaperUri == null,
                accent = RealmeAmber,
                modifier = Modifier.weight(1f),
                onClick = { onUpdateWallpaper("realme_prism", null) }
            )
        }
        Button(
            onClick = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = brand.accentColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = ObsidianBg)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pick Gallery Wallpaper", color = ObsidianBg, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WallpaperChoiceCard(
    title: String,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) accent else Color.Gray.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .height(74.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                color = if (selected) accent else Color.White,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VirtualNavBar(
    brand: DeviceBrand,
    isCompactMini: Boolean,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onRecents: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.72f))
            .padding(vertical = if (isCompactMini) 2.dp else 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(if (isCompactMini) 26.dp else 44.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Virtual Back",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(if (isCompactMini) 14.dp else 20.dp)
            )
        }
        IconButton(
            onClick = onHome,
            modifier = Modifier.size(if (isCompactMini) 26.dp else 44.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Virtual Home",
                tint = brand.accentColor,
                modifier = Modifier.size(if (isCompactMini) 15.dp else 22.dp)
            )
        }
        IconButton(
            onClick = onRecents,
            modifier = Modifier.size(if (isCompactMini) 26.dp else 44.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Apps,
                contentDescription = "Virtual App Drawer",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(if (isCompactMini) 14.dp else 20.dp)
            )
        }
    }
}
