package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.InfinixGreen
import com.example.ui.theme.RealmeAmber
import com.example.ui.theme.RealmeCoral

enum class DeviceBrand(
    val id: String,
    val displayName: String,
    val osSkin: String,
    val androidVersion: String,
    val buildId: String,
    val accentColor: Color,
    val secondaryColor: Color,
    val defaultWallpaperKey: String
) {
    INFINIX(
        id = "INFINIX",
        displayName = "Infinix",
        osSkin = "XOS 14.5",
        androidVersion = "Android 14",
        buildId = "UP1A.231005.007-XOS14",
        accentColor = InfinixGreen,
        secondaryColor = CyberCyan,
        defaultWallpaperKey = "xos_wave"
    ),
    REALME(
        id = "REALME",
        displayName = "Realme",
        osSkin = "Realme UI 5.0",
        androidVersion = "Android 14",
        buildId = "UP1A.231005.007-RUI5",
        accentColor = RealmeAmber,
        secondaryColor = RealmeCoral,
        defaultWallpaperKey = "realme_prism"
    );

    companion object {
        fun fromId(id: String): DeviceBrand =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: INFINIX
    }
}

data class DeviceTemplate(
    val id: String,
    val brand: DeviceBrand,
    val modelName: String,
    val chipset: String,
    val defaultRamGb: Int,
    val defaultStorageGb: Int,
    val displaySpec: String,
    val userAgent: String
)

enum class PerformanceProfile(
    val id: String,
    val label: String,
    val refreshRateHz: Int,
    val description: String
) {
    SMOOTH_120("SMOOTH_120", "120Hz Turbo", 120, "Ultra-smooth 120fps UI & app rendering"),
    BALANCED_90("BALANCED_90", "90Hz Smooth", 90, "Balanced frame pacing for multi-window"),
    ECO_60("ECO_60", "60Hz Eco", 60, "Low memory overhead for 5 mini desktops");

    companion object {
        fun fromId(id: String): PerformanceProfile =
            entries.firstOrNull { it.id == id } ?: SMOOTH_120
    }
}

enum class VirtualAppId(val id: String, val label: String) {
    HOME("home", "Home"),
    PLAY_STORE("play_store", "Play Store"),
    BROWSER("browser", "Chrome"),
    DIALER("dialer", "Phone"),
    APPS("apps", "Apps"),
    NOTES("notes", "Notes"),
    CALCULATOR("calc", "Calculator"),
    BOOST("boost", "Settings"),
    WALLPAPER("wallpaper", "Themes");

    companion object {
        fun fromId(id: String): VirtualAppId =
            entries.firstOrNull { it.id == id } ?: HOME
    }
}

data class PlayStoreCatalogItem(
    val appKey: String,
    val name: String,
    val developer: String,
    val category: String,
    val rating: String,
    val sizeLabel: String,
    val url: String,
    val badgeColorHex: Long
)

object DeviceCatalog {
    const val MIN_BATCH_COUNT = 1
    const val MAX_BATCH_COUNT = 5
    const val MAX_ACTIVE_MINI_DESKTOPS = 5

    val playStoreCatalog: List<PlayStoreCatalogItem> = listOf(
        PlayStoreCatalogItem(
            appKey = "youtube",
            name = "YouTube",
            developer = "Google LLC",
            category = "Video",
            rating = "4.5★",
            sizeLabel = "38 MB",
            url = "https://m.youtube.com",
            badgeColorHex = 0xFFFF1744
        ),
        PlayStoreCatalogItem(
            appKey = "facebook",
            name = "Facebook",
            developer = "Meta Platforms",
            category = "Social",
            rating = "4.3★",
            sizeLabel = "44 MB",
            url = "https://m.facebook.com",
            badgeColorHex = 0xFF1877F2
        ),
        PlayStoreCatalogItem(
            appKey = "instagram",
            name = "Instagram",
            developer = "Instagram",
            category = "Social",
            rating = "4.6★",
            sizeLabel = "52 MB",
            url = "https://www.instagram.com",
            badgeColorHex = 0xFFE1306C
        ),
        PlayStoreCatalogItem(
            appKey = "tiktok",
            name = "TikTok",
            developer = "TikTok Pte. Ltd.",
            category = "Entertainment",
            rating = "4.4★",
            sizeLabel = "64 MB",
            url = "https://www.tiktok.com",
            badgeColorHex = 0xFF00F2EA
        ),
        PlayStoreCatalogItem(
            appKey = "telegram",
            name = "Telegram Web",
            developer = "Telegram FZ-LLC",
            category = "Social",
            rating = "4.7★",
            sizeLabel = "24 MB",
            url = "https://web.telegram.org",
            badgeColorHex = 0xFF29B6F6
        ),
        PlayStoreCatalogItem(
            appKey = "discord",
            name = "Discord",
            developer = "Discord Inc.",
            category = "Social",
            rating = "4.5★",
            sizeLabel = "49 MB",
            url = "https://discord.com/app",
            badgeColorHex = 0xFF5865F2
        ),
        PlayStoreCatalogItem(
            appKey = "x_twitter",
            name = "X",
            developer = "X Corp.",
            category = "News",
            rating = "4.2★",
            sizeLabel = "31 MB",
            url = "https://x.com",
            badgeColorHex = 0xFF90A4AE
        ),
        PlayStoreCatalogItem(
            appKey = "spotify",
            name = "Spotify",
            developer = "Spotify AB",
            category = "Music",
            rating = "4.6★",
            sizeLabel = "41 MB",
            url = "https://open.spotify.com",
            badgeColorHex = 0xFF1DB954
        ),
        PlayStoreCatalogItem(
            appKey = "poki_games",
            name = "Cloud Games",
            developer = "Poki Arcade",
            category = "Games",
            rating = "4.7★",
            sizeLabel = "18 MB",
            url = "https://poki.com",
            badgeColorHex = 0xFFFFC400
        ),
        PlayStoreCatalogItem(
            appKey = "gmail",
            name = "Gmail",
            developer = "Google LLC",
            category = "Productivity",
            rating = "4.6★",
            sizeLabel = "29 MB",
            url = "https://mail.google.com",
            badgeColorHex = 0xFFEA4335
        ),
        PlayStoreCatalogItem(
            appKey = "maps",
            name = "Google Maps",
            developer = "Google LLC",
            category = "Navigation",
            rating = "4.7★",
            sizeLabel = "35 MB",
            url = "https://maps.google.com",
            badgeColorHex = 0xFF34A853
        ),
        PlayStoreCatalogItem(
            appKey = "wikipedia",
            name = "Wikipedia",
            developer = "Wikimedia",
            category = "Reference",
            rating = "4.8★",
            sizeLabel = "14 MB",
            url = "https://en.m.wikipedia.org",
            badgeColorHex = 0xFF7C4DFF
        )
    )

    val templates: List<DeviceTemplate> = listOf(
        // Infinix Android 14 Update lineup
        DeviceTemplate(
            id = "infinix_gt20_pro",
            brand = DeviceBrand.INFINIX,
            modelName = "Infinix GT 20 Pro",
            chipset = "Dimensity 8200 Ultimate",
            defaultRamGb = 12,
            defaultStorageGb = 256,
            displaySpec = "6.78\" AMOLED • 144Hz",
            userAgent = "Mozilla/5.0 (Linux; Android 14; Infinix X6871) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        ),
        DeviceTemplate(
            id = "infinix_note40_pro",
            brand = DeviceBrand.INFINIX,
            modelName = "Infinix Note 40 Pro+",
            chipset = "Dimensity 7020 5G",
            defaultRamGb = 12,
            defaultStorageGb = 256,
            displaySpec = "6.78\" Curved AMOLED • 120Hz",
            userAgent = "Mozilla/5.0 (Linux; Android 14; Infinix X6851B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        ),
        DeviceTemplate(
            id = "infinix_zero40_5g",
            brand = DeviceBrand.INFINIX,
            modelName = "Infinix Zero 40 5G",
            chipset = "Dimensity 8200 5G",
            defaultRamGb = 12,
            defaultStorageGb = 512,
            displaySpec = "6.78\" AMOLED • 144Hz",
            userAgent = "Mozilla/5.0 (Linux; Android 14; Infinix X6861) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        ),
        DeviceTemplate(
            id = "infinix_hot50_pro",
            brand = DeviceBrand.INFINIX,
            modelName = "Infinix Hot 50 Pro+",
            chipset = "Helio G100 • XOS 14.5",
            defaultRamGb = 8,
            defaultStorageGb = 256,
            displaySpec = "6.78\" Slim AMOLED • 120Hz",
            userAgent = "Mozilla/5.0 (Linux; Android 14; Infinix X6880) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        ),
        // Realme Android 14 Update lineup
        DeviceTemplate(
            id = "realme_gt6t",
            brand = DeviceBrand.REALME,
            modelName = "Realme GT 6T",
            chipset = "Snapdragon 7+ Gen 3",
            defaultRamGb = 12,
            defaultStorageGb = 256,
            displaySpec = "6.78\" LTPO AMOLED • 120Hz",
            userAgent = "Mozilla/5.0 (Linux; Android 14; RMX3853) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        ),
        DeviceTemplate(
            id = "realme_13_pro_plus",
            brand = DeviceBrand.REALME,
            modelName = "Realme 13 Pro+ 5G",
            chipset = "Snapdragon 7s Gen 2",
            defaultRamGb = 12,
            defaultStorageGb = 256,
            displaySpec = "6.7\" Pro-XDR • 120Hz",
            userAgent = "Mozilla/5.0 (Linux; Android 14; RMX3921) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        ),
        DeviceTemplate(
            id = "realme_narzo70_pro",
            brand = DeviceBrand.REALME,
            modelName = "Realme Narzo 70 Pro",
            chipset = "Dimensity 7050 5G",
            defaultRamGb = 8,
            defaultStorageGb = 128,
            displaySpec = "6.67\" Horizon AMOLED • 120Hz",
            userAgent = "Mozilla/5.0 (Linux; Android 14; RMX3868) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        ),
        DeviceTemplate(
            id = "realme_p1_5g",
            brand = DeviceBrand.REALME,
            modelName = "Realme P1 5G",
            chipset = "Dimensity 7050 • RUI 5.0",
            defaultRamGb = 8,
            defaultStorageGb = 256,
            displaySpec = "6.67\" AMOLED • 120Hz",
            userAgent = "Mozilla/5.0 (Linux; Android 14; RMX3870) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        )
    )

    fun forBrand(brand: DeviceBrand): List<DeviceTemplate> =
        templates.filter { it.brand == brand }

    fun findById(id: String): DeviceTemplate =
        templates.firstOrNull { it.id == id } ?: templates.first()
}
