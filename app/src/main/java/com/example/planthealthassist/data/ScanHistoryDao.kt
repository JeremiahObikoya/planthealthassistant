package com.example.planthealthassist.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY scanDate DESC")
    fun getAllScans(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history ORDER BY scanDate DESC LIMIT :limit")
    fun getRecentScans(limit: Int): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getScanById(id: Long): ScanHistoryEntity?

    @Insert
    suspend fun insert(scan: ScanHistoryEntity): Long

    @Delete
    suspend fun delete(scan: ScanHistoryEntity)

    @Query("DELETE FROM scan_history")
    suspend fun deleteAll()
} 