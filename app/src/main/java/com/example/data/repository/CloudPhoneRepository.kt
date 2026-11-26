package com.example.data.repository

import com.example.data.local.CloudNoteDao
import com.example.data.local.CloudPhoneDao
import com.example.data.local.CustomCloudAppDao
import com.example.data.local.InstalledVirtualAppDao
import com.example.data.local.WorkspacePrefs
import com.example.data.model.CloudNoteEntity
import com.example.data.model.CloudPhoneEntity
import com.example.data.model.CustomCloudAppEntity
import com.example.data.model.DeviceCatalog
import com.example.data.model.DeviceTemplate
import com.example.data.model.InstalledVirtualAppEntity
import com.example.data.model.PerformanceProfile
import com.example.data.model.PlayStoreCatalogItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CloudPhoneRepository(
    private val phoneDao: CloudPhoneDao,
    private val noteDao: CloudNoteDao,
    private val appDao: CustomCloudAppDao,
    private val installedAppDao: InstalledVirtualAppDao? = null,
    private val workspacePrefs: WorkspacePrefs? = null
) {
    val allPhones: Flow<List<CloudPhoneEntity>> = phoneDao.observeAllPhones()
    val miniDesktopPhones: Flow<List<CloudPhoneEntity>> = phoneDao.observeMiniDesktopPhones()
    val customCloudApps: Flow<List<CustomCloudAppEntity>> = appDao.observeAllApps()

    fun observePhone(phoneId: Long): Flow<CloudPhoneEntity?> =
        phoneDao.observePhoneById(phoneId)

    fun observeNotes(phoneId: Long): Flow<List<CloudNoteEntity>> =
        noteDao.observeNotesForPhone(phoneId)

    fun observeInstalledApps(phoneId: Long): Flow<List<InstalledVirtualAppEntity>> =
        installedAppDao?.observeForPhone(phoneId) ?: flowOf(emptyList())

    suspend fun ensureSeeded() {
        val alreadySeeded = workspacePrefs?.hasSeededInitial == true
        if (!alreadySeeded && phoneDao.getCount() == 0) {
            val infinix = DeviceCatalog.findById("infinix_gt20_pro")
            val realme = DeviceCatalog.findById("realme_gt6t")
            val now = System.currentTimeMillis()
            val insertedIds = phoneDao.insertAll(
                listOf(
                    CloudPhoneEntity(
                        name = "Infinix Node 01",
                        brand = infinix.brand.id,
                        modelId = infinix.id,
                        modelName = infinix.modelName,
                        osSkin = infinix.brand.osSkin,
                        androidVersion = infinix.brand.androidVersion,
                        ramGb = 12,
                        storageGb = 256,
                        performanceMode = PerformanceProfile.SMOOTH_120.id,
                        isRunning = true,
                        isMiniDesktopOpen = true,
                        miniX = 16f,
                        miniY = 16f,
                        wallpaperKey = infinix.brand.defaultWallpaperKey,
                        createdAt = now
                    ),
                    CloudPhoneEntity(
                        name = "Realme Node 02",
                        brand = realme.brand.id,
                        modelId = realme.id,
                        modelName = realme.modelName,
                        osSkin = realme.brand.osSkin,
                        androidVersion = realme.brand.androidVersion,
                        ramGb = 12,
                        storageGb = 256,
                        performanceMode = PerformanceProfile.SMOOTH_120.id,
                        isRunning = true,
                        isMiniDesktopOpen = true,
                        miniX = 212f,
                        miniY = 16f,
                        wallpaperKey = realme.brand.defaultWallpaperKey,
                        createdAt = now - 1000L
                    )
                )
            )
            insertedIds.forEach { phoneId ->
                seedStarterPlayAppsForPhone(phoneId)
            }
            workspacePrefs?.hasSeededInitial = true
        } else if (phoneDao.getCount() > 0 && workspacePrefs?.hasSeededInitial == false) {
            workspacePrefs.hasSeededInitial = true
        }

        if (appDao.getCount() == 0) {
            appDao.insertAll(
                listOf(
                    CustomCloudAppEntity(
                        name = "YouTube",
                        url = "https://m.youtube.com",
                        category = "Media",
                        badgeColorHex = 0xFFFF3D57
                    ),
                    CustomCloudAppEntity(
                        name = "Google",
                        url = "https://www.google.com",
                        category = "Search",
                        badgeColorHex = 0xFF00E5FF
                    ),
                    CustomCloudAppEntity(
                        name = "Wikipedia",
                        url = "https://en.m.wikipedia.org",
                        category = "Reference",
                        badgeColorHex = 0xFF7C4DFF
                    ),
                    CustomCloudAppEntity(
                        name = "DuckDuckGo",
                        url = "https://duckduckgo.com",
                        category = "Privacy",
                        badgeColorHex = 0xFFFFC400
                    )
                )
            )
        }
    }

    private suspend fun seedStarterPlayAppsForPhone(phoneId: Long) {
        val dao = installedAppDao ?: return
        val defaults = DeviceCatalog.playStoreCatalog.take(3).map { item ->
            InstalledVirtualAppEntity(
                phoneId = phoneId,
                appKey = item.appKey,
                name = item.name,
                url = item.url,
                category = item.category,
                badgeColorHex = item.badgeColorHex
            )
        }
        dao.insertAll(defaults)
    }

    suspend fun installPlayStoreApp(phoneId: Long, item: PlayStoreCatalogItem) {
        val dao = installedAppDao ?: return
        if (dao.isInstalled(phoneId, item.appKey) == 0) {
            dao.insert(
                InstalledVirtualAppEntity(
                    phoneId = phoneId,
                    appKey = item.appKey,
                    name = item.name,
                    url = item.url,
                    category = item.category,
                    badgeColorHex = item.badgeColorHex
                )
            )
        }
    }

    suspend fun uninstallPlayStoreApp(phoneId: Long, appKey: String) {
        installedAppDao?.uninstall(phoneId, appKey)
    }

    suspend fun createCloudPhoneBatch(
        template: DeviceTemplate,
        batchCount: Int,
        namePrefix: String,
        ramGb: Int,
        performanceMode: PerformanceProfile,
        openInMiniDesktop: Boolean
    ): List<Long> {
        val clampedCount = batchCount.coerceIn(
            DeviceCatalog.MIN_BATCH_COUNT,
            DeviceCatalog.MAX_BATCH_COUNT
        )
        val existingTotal = phoneDao.getCount()
        var currentMiniCount = phoneDao.getOpenMiniDesktopCount()
        val now = System.currentTimeMillis()
        val prefix = namePrefix.trim().ifBlank { template.brand.displayName }

        val toInsert = (1..clampedCount).map { idx ->
            val canOpenMini = openInMiniDesktop &&
                currentMiniCount < DeviceCatalog.MAX_ACTIVE_MINI_DESKTOPS
            if (canOpenMini) {
                currentMiniCount++
            }
            val number = existingTotal + idx
            val offsetIndex = (idx - 1)
            CloudPhoneEntity(
                name = "$prefix #${number.toString().padStart(2, '0')}",
                brand = template.brand.id,
                modelId = template.id,
                modelName = template.modelName,
                osSkin = template.brand.osSkin,
                androidVersion = template.brand.androidVersion,
                ramGb = ramGb,
                storageGb = template.defaultStorageGb,
                performanceMode = performanceMode.id,
                isRunning = true,
                isMiniDesktopOpen = canOpenMini,
                miniX = 18f + (offsetIndex * 28f),
                miniY = 18f + (offsetIndex * 36f),
                wallpaperKey = template.brand.defaultWallpaperKey,
                createdAt = now + idx
            )
        }
        val ids = phoneDao.insertAll(toInsert)
        ids.forEach { phoneId -> seedStarterPlayAppsForPhone(phoneId) }
        workspacePrefs?.hasSeededInitial = true
        return ids
    }

    suspend fun getPhoneById(id: Long): CloudPhoneEntity? = phoneDao.getPhoneById(id)

    suspend fun openInMiniDesktop(phoneId: Long): Boolean {
        val phone = phoneDao.getPhoneById(phoneId) ?: return false
        if (phone.isMiniDesktopOpen) return true
        val openCount = phoneDao.getOpenMiniDesktopCount()
        if (openCount >= DeviceCatalog.MAX_ACTIVE_MINI_DESKTOPS) {
            return false
        }
        phoneDao.setMiniDesktopOpen(phoneId, true)
        return true
    }

    suspend fun closeMiniDesktop(phoneId: Long) {
        phoneDao.setMiniDesktopOpen(phoneId, false)
    }

    suspend fun updateMiniPosition(phoneId: Long, x: Float, y: Float) {
        phoneDao.updateMiniPosition(phoneId, x, y)
    }

    suspend fun setRunning(phoneId: Long, running: Boolean) {
        phoneDao.setRunning(phoneId, running)
        if (!running) {
            phoneDao.setMiniDesktopOpen(phoneId, false)
        }
    }

    suspend fun updateActiveApp(phoneId: Long, appId: String) {
        phoneDao.updateActiveApp(phoneId, appId)
    }

    suspend fun launchBrowserUrl(phoneId: Long, url: String) {
        phoneDao.launchBrowserUrl(phoneId, url)
    }

    suspend fun updateBrowserUrlOnly(phoneId: Long, url: String) {
        phoneDao.updateBrowserUrlOnly(phoneId, url)
    }

    suspend fun updatePerformanceMode(phoneId: Long, mode: PerformanceProfile) {
        phoneDao.updatePerformanceMode(phoneId, mode.id)
    }

    suspend fun updateWallpaper(phoneId: Long, key: String, customUri: String?) {
        phoneDao.updateWallpaper(phoneId, key, customUri)
    }

    suspend fun deletePhone(phoneId: Long) {
        noteDao.deleteNotesForPhone(phoneId)
        installedAppDao?.deleteAllForPhone(phoneId)
        phoneDao.deleteById(phoneId)
    }

    suspend fun addNote(phoneId: Long, title: String, content: String) {
        if (title.isBlank() && content.isBlank()) return
        noteDao.insertNote(
            CloudNoteEntity(
                phoneId = phoneId,
                title = title.trim().ifBlank { "Quick Note" },
                content = content.trim()
            )
        )
    }

    suspend fun deleteNote(noteId: Long) {
        noteDao.deleteNoteById(noteId)
    }

    suspend fun addCustomCloudApp(name: String, url: String, category: String, badgeColorHex: Long) {
        val normalizedUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
            url.trim()
        } else {
            "https://${url.trim()}"
        }
        appDao.insert(
            CustomCloudAppEntity(
                name = name.trim(),
                url = normalizedUrl,
                category = category.trim().ifBlank { "Web App" },
                badgeColorHex = badgeColorHex
            )
        )
    }

    suspend fun deleteCustomCloudApp(id: Long) {
        appDao.deleteById(id)
    }
}
