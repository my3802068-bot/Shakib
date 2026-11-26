package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.InstalledVirtualAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalledVirtualAppDao {
    @Query("SELECT * FROM installed_virtual_apps WHERE phoneId = :phoneId ORDER BY installedAt ASC")
    fun observeForPhone(phoneId: Long): Flow<List<InstalledVirtualAppEntity>>

    @Query("SELECT COUNT(*) FROM installed_virtual_apps WHERE phoneId = :phoneId AND appKey = :appKey")
    suspend fun isInstalled(phoneId: Long, appKey: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: InstalledVirtualAppEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<InstalledVirtualAppEntity>)

    @Query("DELETE FROM installed_virtual_apps WHERE phoneId = :phoneId AND appKey = :appKey")
    suspend fun uninstall(phoneId: Long, appKey: String)

    @Query("DELETE FROM installed_virtual_apps WHERE phoneId = :phoneId")
    suspend fun deleteAllForPhone(phoneId: Long)
}
