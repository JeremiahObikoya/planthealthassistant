package com.example.planthealthassist.data

import android.net.Uri
import java.util.Date

data class ScanHistoryItem(
    val id: Long,
    val imageUri: Uri,
    val diseaseName: String,
    val solution: String,
    val scanDate: Date
) 