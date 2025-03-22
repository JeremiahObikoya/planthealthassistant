package com.example.planthealthassist.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "scan_history")
@TypeConverters(Converters::class)
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageUri: String,
    val diseaseName: String,
    val solution: String,
    val scanDate: Date
) 