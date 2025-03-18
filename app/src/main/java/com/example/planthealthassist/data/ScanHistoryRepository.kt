package com.example.planthealthassist.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class ScanHistoryRepository(private val scanHistoryDao: ScanHistoryDao) {

    fun getAllScans(): Flow<List<ScanHistoryItem>> {
        return scanHistoryDao.getAllScans().map { entities ->
            entities.map { it.toScanHistoryItem() }
        }
    }

    fun getRecentScans(limit: Int): Flow<List<ScanHistoryItem>> {
        return scanHistoryDao.getRecentScans(limit).map { entities ->
            entities.map { it.toScanHistoryItem() }
        }
    }

    suspend fun getScanById(id: Long): ScanHistoryItem? {
        return scanHistoryDao.getScanById(id)?.toScanHistoryItem()
    }

    suspend fun addScan(imageUri: Uri, diseaseName: String, solution: String) {
        val scan = ScanHistoryEntity(
            imageUri = imageUri.toString(),
            diseaseName = diseaseName,
            solution = solution,
            scanDate = Date()
        )
        scanHistoryDao.insert(scan)
    }

    suspend fun deleteScan(scan: ScanHistoryItem) {
        scanHistoryDao.delete(scan.toScanHistoryEntity())
    }

    suspend fun deleteAllScans() {
        scanHistoryDao.deleteAll()
    }

    private fun ScanHistoryEntity.toScanHistoryItem(): ScanHistoryItem {
        return ScanHistoryItem(
            id = id,
            imageUri = Uri.parse(imageUri),
            diseaseName = diseaseName,
            solution = solution,
            scanDate = scanDate
        )
    }

    private fun ScanHistoryItem.toScanHistoryEntity(): ScanHistoryEntity {
        return ScanHistoryEntity(
            id = id,
            imageUri = imageUri.toString(),
            diseaseName = diseaseName,
            solution = solution,
            scanDate = scanDate
        )
    }
} 