package com.example.planthealthassist

import android.app.Application
import android.util.Log
import androidx.work.Configuration

class PlantHealthApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
} 