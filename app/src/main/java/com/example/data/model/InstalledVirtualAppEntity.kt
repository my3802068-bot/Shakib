package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installed_virtual_apps")
data class InstalledVirtualAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneId: Long,
    val appKey: String,
    val name: String,
    val url: String,
    val category: String,
    val badgeColorHex: Long,
    val installedAt: Long = System.currentTimeMillis()
)
