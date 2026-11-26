package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_cloud_apps")
data class CustomCloudAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val category: String = "Web App",
    val badgeColorHex: Long = 0xFF00E5FF,
    val createdAt: Long = System.currentTimeMillis()
)
