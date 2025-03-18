package com.example.planthealthassist.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.planthealthassist.data.AppDatabase
import com.example.planthealthassist.data.ScanHistoryItem
import com.example.planthealthassist.data.ScanHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ScanHistoryRepository
    private val _scans = MutableStateFlow<List<ScanHistoryItem>>(emptyList())
    val scans: StateFlow<List<ScanHistoryItem>> = _scans

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ScanHistoryRepository(database.scanHistoryDao())
        loadScans()
    }

    private fun loadScans() {
        viewModelScope.launch {
            repository.getAllScans()
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { scanList ->
                    _scans.value = scanList
                    _isLoading.value = false
                }
        }
    }

    fun deleteScan(scan: ScanHistoryItem) {
        viewModelScope.launch {
            try {
                repository.deleteScan(scan)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteAllScans() {
        viewModelScope.launch {
            try {
                repository.deleteAllScans()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
} 