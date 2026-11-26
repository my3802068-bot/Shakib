package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CustomCloudAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCloudAppDao {
    @Query("SELECT * FROM custom_cloud_apps ORDER BY createdAt DESC")
    fun observeAllApps(): Flow<List<CustomCloudAppEntity>>

    @Query("SELECT COUNT(*) FROM custom_cloud_apps")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: CustomCloudAppEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<CustomCloudAppEntity>)

    @Query("DELETE FROM custom_cloud_apps WHERE id = :id")
    suspend fun deleteById(id: Long)
}
