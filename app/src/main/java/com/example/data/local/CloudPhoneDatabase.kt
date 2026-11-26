package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CloudNoteEntity
import com.example.data.model.CloudPhoneEntity
import com.example.data.model.CustomCloudAppEntity
import com.example.data.model.InstalledVirtualAppEntity

@Database(
    entities = [
        CloudPhoneEntity::class,
        CloudNoteEntity::class,
        CustomCloudAppEntity::class,
        InstalledVirtualAppEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CloudPhoneDatabase : RoomDatabase() {
    abstract fun cloudPhoneDao(): CloudPhoneDao
    abstract fun cloudNoteDao(): CloudNoteDao
    abstract fun customCloudAppDao(): CustomCloudAppDao
    abstract fun installedVirtualAppDao(): InstalledVirtualAppDao

    companion object {
        @Volatile
        private var INSTANCE: CloudPhoneDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `installed_virtual_apps` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `phoneId` INTEGER NOT NULL,
                        `appKey` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `url` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `badgeColorHex` INTEGER NOT NULL,
                        `installedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): CloudPhoneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CloudPhoneDatabase::class.java,
                    "cloud_phone_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
