package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.local.CloudPhoneDatabase
import com.example.data.local.WorkspacePrefs
import com.example.data.model.DeviceBrand
import com.example.data.model.PerformanceProfile
import com.example.data.model.VirtualAppId
import com.example.data.repository.CloudPhoneRepository
import com.example.ui.components.VirtualPhoneViewport
import com.example.ui.theme.CloudPhoneTheme
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.util.HostTelemetryHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FloatingMiniPhoneService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    private lateinit var windowManager: WindowManager
    private lateinit var overlayParams: WindowManager.LayoutParams
    private var composeView: ComposeView? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var repository: CloudPhoneRepository
    private var currentPhoneId: Long = -1L

    override fun onCreate() {
        super.onCreate()
        savedStateController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val db = CloudPhoneDatabase.getInstance(this)
        repository = CloudPhoneRepository(
            phoneDao = db.cloudPhoneDao(),
            noteDao = db.cloudNoteDao(),
            appDao = db.customCloudAppDao(),
            installedAppDao = db.installedVirtualAppDao(),
            workspacePrefs = WorkspacePrefs(this)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        val requestedId = intent?.getLongExtra(EXTRA_PHONE_ID, -1L) ?: -1L
        if (requestedId > 0L) {
            currentPhoneId = requestedId
        }

        startForegroundNotification()
        if (composeView == null) {
            attachOverlayWindow()
        } else {
            // Re-render for the newly requested phoneId
            detachOverlayWindow()
            attachOverlayWindow()
        }
        return START_STICKY
    }

    private fun startForegroundNotification() {
        val channelId = "cloudphone_floating_mini"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                "Floating Mini Cloud Phone",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, FloatingMiniPhoneService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("CloudPhone Mini Desktop Active")
            .setContentText("Floating mobile window running outside app")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openAppIntent)
            .addAction(0, "Close Mini", stopIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun attachOverlayWindow() {
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 140
        }

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingMiniPhoneService)
            setViewTreeViewModelStoreOwner(this@FloatingMiniPhoneService)
            setViewTreeSavedStateRegistryOwner(this@FloatingMiniPhoneService)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                CloudPhoneTheme {
                    FloatingMiniOverlayContent(
                        phoneId = currentPhoneId,
                        repository = repository,
                        onDrag = { dx, dy ->
                            overlayParams.x += dx.roundToInt()
                            overlayParams.y += dy.roundToInt()
                            runCatching { windowManager.updateViewLayout(this, overlayParams) }
                        },
                        onToggleKeyboardFocus = { focusable ->
                            overlayParams.flags = if (focusable) {
                                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            } else {
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            }
                            runCatching { windowManager.updateViewLayout(this, overlayParams) }
                        },
                        onExpandToMainApp = { phoneId ->
                            val launch = Intent(this@FloatingMiniPhoneService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra(com.example.util.HomeShortcutHelper.EXTRA_PHONE_ID, phoneId)
                                putExtra(com.example.util.HomeShortcutHelper.EXTRA_OPEN_MINI_DESKTOP, false)
                            }
                            startActivity(launch)
                            stopSelf()
                        },
                        onClose = { stopSelf() }
                    )
                }
            }
        }

        composeView = view
        windowManager.addView(view, overlayParams)
    }

    private fun detachOverlayWindow() {
        composeView?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        composeView = null
    }

    override fun onDestroy() {
        detachOverlayWindow()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_PHONE_ID = "extra_floating_phone_id"
        const val ACTION_STOP = "com.example.action.STOP_FLOATING_MINI_PHONE"
        private const val NOTIFICATION_ID = 1405

        fun start(context: Context, phoneId: Long) {
            val intent = Intent(context, FloatingMiniPhoneService::class.java).apply {
                putExtra(EXTRA_PHONE_ID, phoneId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingMiniPhoneService::class.java))
        }
    }
}

@Composable
private fun FloatingMiniOverlayContent(
    phoneId: Long,
    repository: CloudPhoneRepository,
    onDrag: (Float, Float) -> Unit,
    onToggleKeyboardFocus: (Boolean) -> Unit,
    onExpandToMainApp: (Long) -> Unit,
    onClose: () -> Unit
) {
    val allPhones by repository.allPhones.collectAsState(initial = emptyList())
    val phone = remember(allPhones, phoneId) {
        allPhones.firstOrNull { it.id == phoneId } ?: allPhones.firstOrNull()
    }
    val customApps by repository.customCloudApps.collectAsState(initial = emptyList())
    val coroutineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val hostApps = remember { HostTelemetryHelper.loadLaunchableHostApps(context) }
    var telemetry by remember { mutableStateOf(HostTelemetryHelper.readMemoryTelemetry(context)) }

    var isCollapsedBubble by remember { mutableStateOf(false) }
    var isLargeMini by remember { mutableStateOf(false) }
    var keyboardEnabled by remember { mutableStateOf(false) }

    if (phone == null) {
        Surface(
            color = ObsidianSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CyberCyan)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("No saved phone", color = Color.White)
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
        return
    }

    val brand = remember(phone.brand) { DeviceBrand.fromId(phone.brand) }

    if (isCollapsedBubble) {
        Surface(
            color = ObsidianBg,
            shape = CircleShape,
            border = BorderStroke(2.dp, brand.accentColor),
            modifier = Modifier
                .size(56.dp)
                .shadow(12.dp, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
                .clickable { isCollapsedBubble = false }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.PhoneAndroid,
                    contentDescription = "Expand Mini Phone",
                    tint = brand.accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        return
    }

    // Authentic mobile phone proportions (9:18 mini size!)
    val widthDp = if (isLargeMini) 232.dp else 192.dp
    val heightDp = if (isLargeMini) 464.dp else 384.dp

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF050811),
        border = BorderStroke(2.dp, brand.accentColor.copy(alpha = 0.85f)),
        modifier = Modifier
            .width(widthDp)
            .height(heightDp)
            .shadow(18.dp, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Draggable Phone Top Bezel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurfaceVariant)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
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
                        onClick = {
                            keyboardEnabled = !keyboardEnabled
                            onToggleKeyboardFocus(keyboardEnabled)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Keyboard,
                            contentDescription = "Toggle Keyboard Input",
                            tint = if (keyboardEnabled) brand.accentColor else Color.LightGray,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    IconButton(
                        onClick = { isLargeMini = !isLargeMini },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AspectRatio,
                            contentDescription = "Resize Mini Phone",
                            tint = CyberCyan,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    IconButton(
                        onClick = { isCollapsedBubble = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Minimize to Bubble",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    IconButton(
                        onClick = { onExpandToMainApp(phone.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInFull,
                            contentDescription = "Open Full App",
                            tint = brand.accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Floating Mini Phone",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Live Virtual Phone Screen
            VirtualPhoneViewport(
                phone = phone,
                customCloudApps = customApps,
                installedAppsFlow = repository.observeInstalledApps(phone.id),
                hostApps = hostApps,
                memoryTelemetry = telemetry,
                notesFlow = repository.observeNotes(phone.id),
                isCompactMini = true,
                onSelectApp = { app ->
                    coroutineScope.launch { repository.updateActiveApp(phone.id, app.id) }
                },
                onLaunchUrl = { url ->
                    coroutineScope.launch { repository.launchBrowserUrl(phone.id, url) }
                },
                onBrowserUrlChanged = { url ->
                    coroutineScope.launch { repository.updateBrowserUrlOnly(phone.id, url) }
                },
                onInstallPlayApp = { item ->
                    coroutineScope.launch { repository.installPlayStoreApp(phone.id, item) }
                },
                onUninstallPlayApp = { appKey ->
                    coroutineScope.launch { repository.uninstallPlayStoreApp(phone.id, appKey) }
                },
                onChangePerf = { perf ->
                    coroutineScope.launch { repository.updatePerformanceMode(phone.id, perf) }
                },
                onTurboBoost = {
                    System.gc()
                    telemetry = HostTelemetryHelper.readMemoryTelemetry(context)
                },
                onUpdateWallpaper = { key, uri ->
                    coroutineScope.launch { repository.updateWallpaper(phone.id, key, uri) }
                },
                onAddNote = { title, body ->
                    coroutineScope.launch { repository.addNote(phone.id, title, body) }
                },
                onDeleteNote = { noteId ->
                    coroutineScope.launch { repository.deleteNote(noteId) }
                },
                onPinAppToHome = { appId, label, url ->
                    com.example.util.HomeShortcutHelper.pinCloudPhoneShortcut(
                        context = context,
                        phone = phone,
                        openAsMiniDesktop = true,
                        appId = appId,
                        appLabel = label,
                        appUrl = url
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}
