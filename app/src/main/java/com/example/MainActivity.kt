package com.example

import android.Manifest
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.VirtualAppId
import com.example.service.FloatingMiniPhoneService
import com.example.ui.CloudPhoneViewModel
import com.example.ui.MainTab
import com.example.ui.screens.CloudAppsHubScreen
import com.example.ui.screens.CreateCloudPhonesSheet
import com.example.ui.screens.FleetDashboardScreen
import com.example.ui.screens.FullVirtualPhoneScreen
import com.example.ui.screens.MiniDesktopScreen
import com.example.ui.theme.CloudPhoneTheme
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.InfinixGreen
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: CloudPhoneViewModel by viewModels()
    private val isInPipMode = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.handleShortcutIntent(intent)

        setContent {
            CloudPhoneTheme {
                CloudPhoneAppRoot(
                    viewModel = viewModel,
                    isInPipMode = isInPipMode.value,
                    onEnterPipMode = { enterMobilePipMode() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleShortcutIntent(intent)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode.value = isInPictureInPictureMode
    }

    private fun enterMobilePipMode(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        ) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(9, 16))
                .build()
            return enterPictureInPictureMode(params)
        }
        return false
    }
}

@Composable
fun CloudPhoneAppRoot(
    viewModel: CloudPhoneViewModel,
    isInPipMode: Boolean,
    onEnterPipMode: () -> Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val phones by viewModel.phones.collectAsStateWithLifecycle()
    val miniPhones by viewModel.miniDesktopPhones.collectAsStateWithLifecycle()
    val customApps by viewModel.customCloudApps.collectAsStateWithLifecycle()
    val hostApps by viewModel.hostApps.collectAsStateWithLifecycle()
    val memoryTelemetry by viewModel.memoryTelemetry.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val brandFilter by viewModel.brandFilter.collectAsStateWithLifecycle()
    val fullScreenPhoneId by viewModel.fullScreenPhoneId.collectAsStateWithLifecycle()
    val focusedMiniPhoneId by viewModel.focusedMiniPhoneId.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateSheet by remember { mutableStateOf(false) }
    var pendingOverlayPhoneId by remember { mutableStateOf<Long?>(null) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    var showDeveloperBanner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showDeveloperBanner = true
        delay(5000L)
        showDeveloperBanner = false
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        pendingOverlayPhoneId?.let { targetId ->
            FloatingMiniPhoneService.start(context, targetId)
            scope.launch {
                snackbarHostState.showSnackbar("Mini Phone floating outside app!")
            }
            pendingOverlayPhoneId = null
        }
    }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val targetId = pendingOverlayPhoneId
        if (targetId != null && Settings.canDrawOverlays(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                FloatingMiniPhoneService.start(context, targetId)
                scope.launch {
                    snackbarHostState.showSnackbar("Mini Phone floating outside app!")
                }
                pendingOverlayPhoneId = null
            }
        }
    }

    fun triggerFloatOutsideApp(phoneId: Long) {
        if (Settings.canDrawOverlays(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                pendingOverlayPhoneId = phoneId
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                FloatingMiniPhoneService.start(context, phoneId)
                scope.launch {
                    snackbarHostState.showSnackbar("Mini Phone is now floating outside the app! Minimize app to use it anywhere.")
                }
            }
        } else {
            pendingOverlayPhoneId = phoneId
            showOverlayPermissionDialog = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (selectedTab != MainTab.FLEET && fullScreenPhoneId == null) {
        BackHandler {
            viewModel.selectTab(MainTab.FLEET)
        }
    }

    val fullScreenPhone = remember(phones, fullScreenPhoneId) {
        fullScreenPhoneId?.let { id -> phones.firstOrNull { it.id == id } }
    }

    // If Android PiP is active, render only the live phone viewport cleanly
    if (isInPipMode) {
        val pipPhone = fullScreenPhone
            ?: phones.firstOrNull { it.id == focusedMiniPhoneId }
            ?: phones.firstOrNull()
        if (pipPhone != null) {
            com.example.ui.components.VirtualPhoneViewport(
                phone = pipPhone,
                customCloudApps = customApps,
                installedAppsFlow = viewModel.observeInstalledApps(pipPhone.id),
                hostApps = hostApps,
                memoryTelemetry = memoryTelemetry,
                notesFlow = viewModel.observeNotes(pipPhone.id),
                isCompactMini = true,
                onSelectApp = { app -> viewModel.launchVirtualApp(pipPhone.id, app) },
                onLaunchUrl = { url -> viewModel.launchUrlInPhone(pipPhone.id, url) },
                onBrowserUrlChanged = { url -> viewModel.updateBrowserUrlOnly(pipPhone.id, url) },
                onInstallPlayApp = { item -> viewModel.installPlayApp(pipPhone.id, item) },
                onUninstallPlayApp = { key -> viewModel.uninstallPlayApp(pipPhone.id, key) },
                onChangePerf = { perf -> viewModel.updatePerformanceMode(pipPhone.id, perf) },
                onTurboBoost = { viewModel.runTurboBoost(pipPhone.name) },
                onUpdateWallpaper = { key, uri -> viewModel.updateWallpaper(pipPhone.id, key, uri) },
                onAddNote = { title, body -> viewModel.addNote(pipPhone.id, title, body) },
                onDeleteNote = viewModel::deleteNote,
                onPinAppToHome = { _, _, _ -> }
            )
            return
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = ObsidianSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = { TabIcon(tab = tab, selected = selected, miniCount = miniPhones.size) },
                                label = { Text(tabLabel(tab)) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    indicatorColor = CyberCyan.copy(alpha = 0.16f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.weight(1f),
                    containerColor = ObsidianBg,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    floatingActionButton = {
                        if (selectedTab == MainTab.FLEET) {
                            ExtendedFloatingActionButton(
                                onClick = { showCreateSheet = true },
                                containerColor = CyberCyan,
                                contentColor = ObsidianBg,
                                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                                text = { Text("Create 1–5") },
                                modifier = Modifier.testTag("fab_create_phones")
                            )
                        }
                    },
                    bottomBar = {
                        if (!useNavigationRail) {
                            NavigationBar(
                                containerColor = ObsidianSurface
                            ) {
                                MainTab.entries.forEach { tab ->
                                    val selected = selectedTab == tab
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(tab) },
                                        icon = {
                                            TabIcon(
                                                tab = tab,
                                                selected = selected,
                                                miniCount = miniPhones.size
                                            )
                                        },
                                        label = { Text(tabLabel(tab)) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = CyberCyan,
                                            selectedTextColor = CyberCyan,
                                            indicatorColor = CyberCyan.copy(alpha = 0.16f),
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        ),
                                        modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            MainTab.FLEET -> FleetDashboardScreen(
                                phones = phones,
                                miniDesktopCount = miniPhones.size,
                                brandFilter = brandFilter,
                                memoryTelemetry = memoryTelemetry,
                                onSelectBrandFilter = viewModel::setBrandFilter,
                                onOpenCreateSheet = { showCreateSheet = true },
                                onBootFullScreen = viewModel::bootFullScreen,
                                onOpenMiniDesktop = { id -> viewModel.openInMiniDesktop(id, switchTab = true) },
                                onFloatOutsideApp = ::triggerFloatOutsideApp,
                                onPinToHomeScreen = { phone ->
                                    viewModel.pinToHomeScreen(
                                        context = context,
                                        phone = phone,
                                        openAsMiniDesktop = true
                                    )
                                },
                                onDeletePhone = viewModel::deletePhone
                            )

                            MainTab.MINI_DESKTOP -> MiniDesktopScreen(
                                allPhones = phones,
                                activeMiniPhones = miniPhones,
                                focusedPhoneId = focusedMiniPhoneId,
                                customCloudApps = customApps,
                                hostApps = hostApps,
                                memoryTelemetry = memoryTelemetry,
                                observeNotes = viewModel::observeNotes,
                                observeInstalledApps = viewModel::observeInstalledApps,
                                onFocusWindow = viewModel::focusMiniWindow,
                                onOpenMiniWindow = { id -> viewModel.openInMiniDesktop(id, switchTab = false) },
                                onCloseMiniWindow = viewModel::closeMiniDesktop,
                                onExpandFullScreen = viewModel::bootFullScreen,
                                onFloatOutsideApp = ::triggerFloatOutsideApp,
                                onUpdateMiniPosition = viewModel::updateMiniPosition,
                                onArrangeGrid = viewModel::arrangeMiniDesktopGrid,
                                onCascade = viewModel::cascadeMiniDesktop,
                                onSelectApp = viewModel::launchVirtualApp,
                                onLaunchUrl = viewModel::launchUrlInPhone,
                                onBrowserUrlChanged = viewModel::updateBrowserUrlOnly,
                                onInstallPlayApp = viewModel::installPlayApp,
                                onUninstallPlayApp = viewModel::uninstallPlayApp,
                                onChangePerf = viewModel::updatePerformanceMode,
                                onTurboBoost = viewModel::runTurboBoost,
                                onUpdateWallpaper = viewModel::updateWallpaper,
                                onAddNote = viewModel::addNote,
                                onDeleteNote = viewModel::deleteNote,
                                onPinToHomeScreen = { phone, appId, label, url ->
                                    viewModel.pinToHomeScreen(
                                        context = context,
                                        phone = phone,
                                        openAsMiniDesktop = true,
                                        appId = appId,
                                        appLabel = label,
                                        appUrl = url
                                    )
                                },
                                onOpenCreateSheet = { showCreateSheet = true }
                            )

                            MainTab.CLOUD_APPS -> CloudAppsHubScreen(
                                phones = phones,
                                customCloudApps = customApps,
                                hostApps = hostApps,
                                onAddCustomApp = viewModel::addCustomCloudApp,
                                onDeleteCustomApp = viewModel::deleteCustomCloudApp,
                                onLaunchInMiniDesktop = viewModel::launchCustomAppInMiniDesktop,
                                onPinAppToHomeScreen = { phone, app ->
                                    viewModel.pinToHomeScreen(
                                        context = context,
                                        phone = phone,
                                        openAsMiniDesktop = true,
                                        appId = VirtualAppId.BROWSER.id,
                                        appLabel = app.name,
                                        appUrl = app.url
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Full-screen Virtual Phone Overlay
        AnimatedVisibility(
            visible = fullScreenPhone != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            fullScreenPhone?.let { phone ->
                FullVirtualPhoneScreen(
                    phone = phone,
                    customCloudApps = customApps,
                    installedAppsFlow = viewModel.observeInstalledApps(phone.id),
                    hostApps = hostApps,
                    memoryTelemetry = memoryTelemetry,
                    notesFlow = viewModel.observeNotes(phone.id),
                    onBackToFleet = viewModel::exitFullScreen,
                    onMinimizeToMiniDesktop = { viewModel.minimizeToMiniDesktop(phone.id) },
                    onFloatOutsideApp = { triggerFloatOutsideApp(phone.id) },
                    onPinToHomeScreen = { appId, label, url ->
                        viewModel.pinToHomeScreen(
                            context = context,
                            phone = phone,
                            openAsMiniDesktop = false,
                            appId = appId,
                            appLabel = label,
                            appUrl = url
                        )
                    },
                    onSelectApp = { app -> viewModel.launchVirtualApp(phone.id, app) },
                    onLaunchUrl = { url -> viewModel.launchUrlInPhone(phone.id, url) },
                    onBrowserUrlChanged = { url -> viewModel.updateBrowserUrlOnly(phone.id, url) },
                    onInstallPlayApp = { item -> viewModel.installPlayApp(phone.id, item) },
                    onUninstallPlayApp = { key -> viewModel.uninstallPlayApp(phone.id, key) },
                    onChangePerf = { perf -> viewModel.updatePerformanceMode(phone.id, perf) },
                    onTurboBoost = { viewModel.runTurboBoost(phone.name) },
                    onUpdateWallpaper = { key, uri -> viewModel.updateWallpaper(phone.id, key, uri) },
                    onAddNote = { title, body -> viewModel.addNote(phone.id, title, body) },
                    onDeleteNote = viewModel::deleteNote
                )
            }
        }

        if (showCreateSheet) {
            CreateCloudPhonesSheet(
                totalFleetCount = phones.size,
                onDismiss = { showCreateSheet = false },
                onCreateBatch = viewModel::createBatch
            )
        }

        if (showOverlayPermissionDialog) {
            AlertDialog(
                onDismissRequest = {
                    showOverlayPermissionDialog = false
                    pendingOverlayPhoneId = null
                },
                containerColor = ObsidianSurface,
                title = { Text("Float Mini Phone Outside App") },
                text = {
                    Text(
                        "To use your Mini Desktop phone on top of your Home Screen and other apps, allow \"Display over other apps\", or use Picture-in-Picture mode.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showOverlayPermissionDialog = false
                            val permIntent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            overlayPermissionLauncher.launch(permIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = InfinixGreen)
                    ) {
                        Text("Allow Overlay", color = ObsidianBg)
                    }
                },
                dismissButton = {
                    Row {
                        OutlinedButton(
                            onClick = {
                                showOverlayPermissionDialog = false
                                pendingOverlayPhoneId = null
                                onEnterPipMode()
                            }
                        ) {
                            Text("Use PiP", color = CyberCyan)
                        }
                        TextButton(
                            onClick = {
                                showOverlayPermissionDialog = false
                                pendingOverlayPhoneId = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }
        // 5-second Dynamic Developer Banner at Bottom on Launch
        AnimatedVisibility(
            visible = showDeveloperBanner,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it }
            ) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            DeveloperDynamicBanner(
                onTelegramClick = {
                    val tgIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://t.me/shakib_Ahmed_007")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(tgIntent) }
                },
                onDismiss = { showDeveloperBanner = false }
            )
        }
    }
}

@Composable
private fun DeveloperDynamicBanner(
    onTelegramClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val progress = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
        )
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = ObsidianSurface.copy(alpha = 0.96f),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(listOf(CyberCyan, InfinixGreen))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onTelegramClick)
            .testTag("developer_dynamic_banner")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        CyberCyan.copy(alpha = 0.25f),
                                        InfinixGreen.copy(alpha = 0.25f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Verified Developer",
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "App Developed by Shakib",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Telegram",
                                tint = InfinixGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Telegram: @shakib_Ahmed_007",
                                style = MaterialTheme.typography.labelMedium,
                                color = InfinixGreen
                            )
                        }
                    }
                }

                Surface(
                    color = ObsidianSurfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${(progress.value * 5).toInt() + 1}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = CyberCyan,
                trackColor = ObsidianBg
            )
        }
    }
}

@Composable
private fun TabIcon(tab: MainTab, selected: Boolean, miniCount: Int) {
    when (tab) {
        MainTab.FLEET -> Icon(
            imageVector = if (selected) Icons.Filled.PhoneAndroid else Icons.Outlined.PhoneAndroid,
            contentDescription = stringResource(R.string.nav_fleet)
        )
        MainTab.MINI_DESKTOP -> BadgedBox(
            badge = {
                if (miniCount > 0) {
                    Badge(containerColor = CyberCyan, contentColor = ObsidianBg) {
                        Text("$miniCount")
                    }
                }
            }
        ) {
            Icon(
                imageVector = if (selected) Icons.Filled.DesktopWindows else Icons.Outlined.DesktopWindows,
                contentDescription = stringResource(R.string.nav_mini_desktop)
            )
        }
        MainTab.CLOUD_APPS -> Icon(
            imageVector = if (selected) Icons.Filled.Apps else Icons.Outlined.Apps,
            contentDescription = stringResource(R.string.nav_apps)
        )
    }
}

@Composable
private fun tabLabel(tab: MainTab): String = when (tab) {
    MainTab.FLEET -> stringResource(R.string.nav_fleet)
    MainTab.MINI_DESKTOP -> stringResource(R.string.nav_mini_desktop)
    MainTab.CLOUD_APPS -> stringResource(R.string.nav_apps)
}
