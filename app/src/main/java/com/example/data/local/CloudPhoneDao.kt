package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CloudPhoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CloudPhoneDao {
    @Query("SELECT * FROM cloud_phones ORDER BY createdAt DESC, id DESC")
    fun observeAllPhones(): Flow<List<CloudPhoneEntity>>

    @Query("SELECT * FROM cloud_phones WHERE isMiniDesktopOpen = 1 ORDER BY id ASC")
    fun observeMiniDesktopPhones(): Flow<List<CloudPhoneEntity>>

    @Query("SELECT * FROM cloud_phones WHERE id = :id LIMIT 1")
    suspend fun getPhoneById(id: Long): CloudPhoneEntity?

    @Query("SELECT * FROM cloud_phones WHERE id = :id LIMIT 1")
    fun observePhoneById(id: Long): Flow<CloudPhoneEntity?>

    @Query("SELECT COUNT(*) FROM cloud_phones")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM cloud_phones WHERE isMiniDesktopOpen = 1")
    suspend fun getOpenMiniDesktopCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(phones: List<CloudPhoneEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(phone: CloudPhoneEntity): Long

    @Update
    suspend fun update(phone: CloudPhoneEntity)

    @Query("UPDATE cloud_phones SET activeAppId = :appId WHERE id = :phoneId")
    suspend fun updateActiveApp(phoneId: Long, appId: String)

    @Query("UPDATE cloud_phones SET browserUrl = :url, activeAppId = 'browser' WHERE id = :phoneId")
    suspend fun launchBrowserUrl(phoneId: Long, url: String)

    @Query("UPDATE cloud_phones SET browserUrl = :url WHERE id = :phoneId")
    suspend fun updateBrowserUrlOnly(phoneId: Long, url: String)

    @Query("UPDATE cloud_phones SET miniX = :x, miniY = :y WHERE id = :phoneId")
    suspend fun updateMiniPosition(phoneId: Long, x: Float, y: Float)

    @Query("UPDATE cloud_phones SET isMiniDesktopOpen = :open, isRunning = 1 WHERE id = :phoneId")
    suspend fun setMiniDesktopOpen(phoneId: Long, open: Boolean)

    @Query("UPDATE cloud_phones SET isRunning = :running WHERE id = :phoneId")
    suspend fun setRunning(phoneId: Long, running: Boolean)

    @Query("UPDATE cloud_phones SET performanceMode = :mode WHERE id = :phoneId")
    suspend fun updatePerformanceMode(phoneId: Long, mode: String)

    @Query("UPDATE cloud_phones SET wallpaperKey = :key, customWallpaperUri = :customUri WHERE id = :phoneId")
    suspend fun updateWallpaper(phoneId: Long, key: String, customUri: String?)

    @Query("DELETE FROM cloud_phones WHERE id = :phoneId")
    suspend fun deleteById(phoneId: Long)
}
