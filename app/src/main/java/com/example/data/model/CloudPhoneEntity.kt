package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cloud_phones")
data class CloudPhoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String,
    val modelId: String,
    val modelName: String,
    val osSkin: String,
    val androidVersion: String = "Android 14",
    val ramGb: Int = 12,
    val storageGb: Int = 256,
    val performanceMode: String = PerformanceProfile.SMOOTH_120.id,
    val isRunning: Boolean = true,
    val isMiniDesktopOpen: Boolean = false,
    val miniX: Float = 24f,
    val miniY: Float = 24f,
    val activeAppId: String = VirtualAppId.HOME.id,
    val browserUrl: String = "https://www.google.com",
    val wallpaperKey: String = "xos_wave",
    val customWallpaperUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
