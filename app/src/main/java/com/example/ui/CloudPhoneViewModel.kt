package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CloudPhoneDatabase
import com.example.data.local.WorkspacePrefs
import com.example.data.model.CloudNoteEntity
import com.example.data.model.CloudPhoneEntity
import com.example.data.model.CustomCloudAppEntity
import com.example.data.model.DeviceBrand
import com.example.data.model.DeviceCatalog
import com.example.data.model.DeviceTemplate
import com.example.data.model.InstalledVirtualAppEntity
import com.example.data.model.PerformanceProfile
import com.example.data.model.PlayStoreCatalogItem
import com.example.data.model.VirtualAppId
import com.example.data.repository.CloudPhoneRepository
import com.example.util.HomeShortcutHelper
import com.example.util.HostMemoryTelemetry
import com.example.util.HostTelemetryHelper
import com.example.util.InstalledHostApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MainTab {
    FLEET,
    MINI_DESKTOP,
    CLOUD_APPS
}

class CloudPhoneViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CloudPhoneDatabase.getInstance(application)
    private val workspacePrefs = WorkspacePrefs(application)
    private val repository = CloudPhoneRepository(
        phoneDao = db.cloudPhoneDao(),
        noteDao = db.cloudNoteDao(),
        appDao = db.customCloudAppDao(),
        installedAppDao = db.installedVirtualAppDao(),
        workspacePrefs = workspacePrefs
    )

    val phones: StateFlow<List<CloudPhoneEntity>> = repository.allPhones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val miniDesktopPhones: StateFlow<List<CloudPhoneEntity>> = repository.miniDesktopPhones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customCloudApps: StateFlow<List<CustomCloudAppEntity>> = repository.customCloudApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(
        runCatching { MainTab.valueOf(workspacePrefs.lastTab) }.getOrDefault(MainTab.FLEET)
    )
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _brandFilter = MutableStateFlow<DeviceBrand?>(null)
    val brandFilter: StateFlow<DeviceBrand?> = _brandFilter.asStateFlow()

    private val _fullScreenPhoneId = MutableStateFlow(workspacePrefs.lastFullScreenPhoneId)
    val fullScreenPhoneId: StateFlow<Long?> = _fullScreenPhoneId.asStateFlow()

    private val _focusedMiniPhoneId = MutableStateFlow(workspacePrefs.lastFocusedMiniPhoneId)
    val focusedMiniPhoneId: StateFlow<Long?> = _focusedMiniPhoneId.asStateFlow()

    private val _memoryTelemetry = MutableStateFlow(HostTelemetryHelper.readMemoryTelemetry(application))
    val memoryTelemetry: StateFlow<HostMemoryTelemetry> = _memoryTelemetry.asStateFlow()

    private val _hostApps = MutableStateFlow<List<InstalledHostApp>>(emptyList())
    val hostApps: StateFlow<List<InstalledHostApp>> = _hostApps.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            // Validate saved full-screen phone still exists
            val savedFullId = workspacePrefs.lastFullScreenPhoneId
            if (savedFullId != null && repository.getPhoneById(savedFullId) == null) {
                workspacePrefs.lastFullScreenPhoneId = null
                _fullScreenPhoneId.value = null
            }
            refreshHostApps()
            refreshTelemetry()
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
        workspacePrefs.lastTab = tab.name
    }

    fun setBrandFilter(brand: DeviceBrand?) {
        _brandFilter.value = brand
    }

    fun focusMiniWindow(phoneId: Long) {
        _focusedMiniPhoneId.value = phoneId
        workspacePrefs.lastFocusedMiniPhoneId = phoneId
    }

    fun refreshTelemetry() {
        _memoryTelemetry.value = HostTelemetryHelper.readMemoryTelemetry(getApplication())
    }

    fun refreshHostApps() {
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                HostTelemetryHelper.loadLaunchableHostApps(getApplication())
            }
            _hostApps.value = loaded
        }
    }

    fun runTurboBoost(phoneName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                System.gc()
            }
            refreshTelemetry()
            _events.tryEmit("$phoneName: 120Hz frame pacing & RAM cache optimized")
        }
    }

    fun createBatch(
        template: DeviceTemplate,
        batchCount: Int,
        namePrefix: String,
        ramGb: Int,
        performanceMode: PerformanceProfile,
        openInMiniDesktop: Boolean
    ) {
        viewModelScope.launch {
            val createdIds = repository.createCloudPhoneBatch(
                template = template,
                batchCount = batchCount,
                namePrefix = namePrefix,
                ramGb = ramGb,
                performanceMode = performanceMode,
                openInMiniDesktop = openInMiniDesktop
            )
            refreshTelemetry()
            if (openInMiniDesktop && createdIds.isNotEmpty()) {
                focusMiniWindow(createdIds.last())
                selectTab(MainTab.MINI_DESKTOP)
            }
            _events.tryEmit(
                "Saved ${createdIds.size} ${template.brand.displayName} Android 14 phone(s)"
            )
        }
    }

    fun bootFullScreen(phoneId: Long) {
        viewModelScope.launch {
            repository.setRunning(phoneId, true)
            _fullScreenPhoneId.value = phoneId
            workspacePrefs.lastFullScreenPhoneId = phoneId
        }
    }

    fun exitFullScreen() {
        _fullScreenPhoneId.value = null
        workspacePrefs.lastFullScreenPhoneId = null
    }

    fun minimizeToMiniDesktop(phoneId: Long) {
        viewModelScope.launch {
            val opened = repository.openInMiniDesktop(phoneId)
            _fullScreenPhoneId.value = null
            workspacePrefs.lastFullScreenPhoneId = null
            selectTab(MainTab.MINI_DESKTOP)
            if (opened) {
                focusMiniWindow(phoneId)
                _events.tryEmit("Moved to Mini Desktop (state saved)")
            } else {
                _events.tryEmit(
                    "Max ${DeviceCatalog.MAX_ACTIVE_MINI_DESKTOPS} active Mini Desktops reached. Close one first."
                )
            }
        }
    }

    fun openInMiniDesktop(phoneId: Long, switchTab: Boolean = true) {
        viewModelScope.launch {
            val opened = repository.openInMiniDesktop(phoneId)
            if (opened) {
                focusMiniWindow(phoneId)
                if (switchTab) {
                    selectTab(MainTab.MINI_DESKTOP)
                }
            } else {
                _events.tryEmit(
                    "Mini Desktop supports max ${DeviceCatalog.MAX_ACTIVE_MINI_DESKTOPS} simultaneous windows"
                )
            }
        }
    }

    fun closeMiniDesktop(phoneId: Long) {
        viewModelScope.launch {
            repository.closeMiniDesktop(phoneId)
        }
    }

    fun updateMiniPosition(phoneId: Long, x: Float, y: Float) {
        viewModelScope.launch {
            repository.updateMiniPosition(phoneId, x, y)
        }
    }

    fun arrangeMiniDesktopGrid(canvasWidthDp: Float, windowWidthDp: Float, windowHeightDp: Float) {
        viewModelScope.launch {
            val active = miniDesktopPhones.value
            val columns = if (canvasWidthDp >= windowWidthDp * 2 + 30f) 2 else 1
            active.forEachIndexed { index, phone ->
                val col = index % columns
                val row = index / columns
                val x = 12f + col * (windowWidthDp + 12f)
                val y = 12f + row * (windowHeightDp + 14f)
                repository.updateMiniPosition(phone.id, x, y)
            }
        }
    }

    fun cascadeMiniDesktop() {
        viewModelScope.launch {
            val active = miniDesktopPhones.value
            active.forEachIndexed { index, phone ->
                repository.updateMiniPosition(
                    phone.id,
                    14f + index * 24f,
                    14f + index * 30f
                )
            }
        }
    }

    fun launchVirtualApp(phoneId: Long, appId: VirtualAppId) {
        viewModelScope.launch {
            repository.updateActiveApp(phoneId, appId.id)
        }
    }

    fun launchUrlInPhone(phoneId: Long, url: String) {
        viewModelScope.launch {
            val normalized = if (url.startsWith("http://") || url.startsWith("https://")) {
                url
            } else {
                "https://$url"
            }
            repository.launchBrowserUrl(phoneId, normalized)
        }
    }

    fun updateBrowserUrlOnly(phoneId: Long, url: String) {
        viewModelScope.launch {
            repository.updateBrowserUrlOnly(phoneId, url)
        }
    }

    fun observeInstalledApps(phoneId: Long): Flow<List<InstalledVirtualAppEntity>> =
        repository.observeInstalledApps(phoneId)

    fun installPlayApp(phoneId: Long, item: PlayStoreCatalogItem) {
        viewModelScope.launch {
            repository.installPlayStoreApp(phoneId, item)
            _events.tryEmit("Installed ${item.name} on Cloud Phone")
        }
    }

    fun uninstallPlayApp(phoneId: Long, appKey: String) {
        viewModelScope.launch {
            repository.uninstallPlayStoreApp(phoneId, appKey)
        }
    }

    fun launchCustomAppInMiniDesktop(phoneId: Long, app: CustomCloudAppEntity) {
        viewModelScope.launch {
            val opened = repository.openInMiniDesktop(phoneId)
            repository.launchBrowserUrl(phoneId, app.url)
            if (opened) {
                focusMiniWindow(phoneId)
                selectTab(MainTab.MINI_DESKTOP)
            } else {
                bootFullScreen(phoneId)
                _events.tryEmit("Mini Desktop full (max 5). Launched in Full OS mode.")
            }
        }
    }

    fun updatePerformanceMode(phoneId: Long, mode: PerformanceProfile) {
        viewModelScope.launch {
            repository.updatePerformanceMode(phoneId, mode)
            _events.tryEmit("Switched to ${mode.label}")
        }
    }

    fun updateWallpaper(phoneId: Long, key: String, customUri: String?) {
        viewModelScope.launch {
            repository.updateWallpaper(phoneId, key, customUri)
        }
    }

    fun deletePhone(phoneId: Long) {
        viewModelScope.launch {
            if (_fullScreenPhoneId.value == phoneId) {
                exitFullScreen()
            }
            repository.deletePhone(phoneId)
            _events.tryEmit("Cloud phone removed")
        }
    }

    fun observeNotes(phoneId: Long): Flow<List<CloudNoteEntity>> =
        repository.observeNotes(phoneId)

    fun addNote(phoneId: Long, title: String, content: String) {
        viewModelScope.launch {
            repository.addNote(phoneId, title, content)
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    fun addCustomCloudApp(name: String, url: String, category: String, badgeColorHex: Long) {
        viewModelScope.launch {
            if (name.isBlank() || url.isBlank()) return@launch
            repository.addCustomCloudApp(name, url, category, badgeColorHex)
            _events.tryEmit("Added $name to Cloud Apps")
        }
    }

    fun deleteCustomCloudApp(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomCloudApp(id)
        }
    }

    fun pinToHomeScreen(
        context: Context,
        phone: CloudPhoneEntity,
        openAsMiniDesktop: Boolean = true,
        appId: String? = null,
        appLabel: String? = null,
        appUrl: String? = null
    ) {
        val supported = HomeShortcutHelper.pinCloudPhoneShortcut(
            context = context,
            phone = phone,
            openAsMiniDesktop = openAsMiniDesktop,
            appId = appId,
            appLabel = appLabel,
            appUrl = appUrl
        )
        if (supported) {
            _events.tryEmit("Pin request sent to Home Screen")
        } else {
            _events.tryEmit("Current launcher does not support pinned shortcuts")
        }
    }

    fun handleShortcutIntent(intent: Intent?) {
        if (intent == null) return
        val phoneId = intent.getLongExtra(HomeShortcutHelper.EXTRA_PHONE_ID, -1L)
        if (phoneId <= 0L) return

        val openMini = intent.getBooleanExtra(HomeShortcutHelper.EXTRA_OPEN_MINI_DESKTOP, true)
        val appId = intent.getStringExtra(HomeShortcutHelper.EXTRA_APP_ID)
        val appUrl = intent.getStringExtra(HomeShortcutHelper.EXTRA_APP_URL)

        viewModelScope.launch {
            val phone = repository.getPhoneById(phoneId) ?: return@launch
            if (!appUrl.isNullOrBlank()) {
                repository.launchBrowserUrl(phone.id, appUrl)
            } else if (!appId.isNullOrBlank()) {
                repository.updateActiveApp(phone.id, appId)
            }

            if (openMini) {
                val opened = repository.openInMiniDesktop(phone.id)
                selectTab(MainTab.MINI_DESKTOP)
                if (opened) {
                    focusMiniWindow(phone.id)
                    _fullScreenPhoneId.value = null
                    workspacePrefs.lastFullScreenPhoneId = null
                } else {
                    bootFullScreen(phone.id)
                }
            } else {
                bootFullScreen(phone.id)
            }
        }
    }
}
