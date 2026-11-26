package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CloudNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CloudNoteDao {
    @Query("SELECT * FROM cloud_notes WHERE phoneId = :phoneId ORDER BY updatedAt DESC")
    fun observeNotesForPhone(phoneId: Long): Flow<List<CloudNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CloudNoteEntity): Long

    @Query("DELETE FROM cloud_notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    @Query("DELETE FROM cloud_notes WHERE phoneId = :phoneId")
    suspend fun deleteNotesForPhone(phoneId: Long)
}
