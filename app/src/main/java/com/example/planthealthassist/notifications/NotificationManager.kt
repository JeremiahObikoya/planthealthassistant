package com.example.planthealthassist.notifications

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class NotificationManager(private val context: Context) {

    companion object {
        private const val WORK_NAME = "plant_health_reminders"
        private const val PREF_NAME = "notification_preferences"
        private const val KEY_NOTIFICATION_FREQUENCY = "notification_frequency"
        
        const val FREQUENCY_DAILY = "daily"
        const val FREQUENCY_WEEKLY = "weekly"
        const val FREQUENCY_OFF = "off"
    }

    private val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val workManager = WorkManager.getInstance(context)

    fun scheduleNotifications(frequency: String) {
        // Cancel any existing work
        workManager.cancelUniqueWork(WORK_NAME)

        // Save the preference
        preferences.edit().putString(KEY_NOTIFICATION_FREQUENCY, frequency).apply()

        when (frequency) {
            FREQUENCY_DAILY -> schedulePeriodic(1, TimeUnit.DAYS)
            FREQUENCY_WEEKLY -> schedulePeriodic(7, TimeUnit.DAYS)
            FREQUENCY_OFF -> {} // Do nothing, we already cancelled existing work
        }
    }

    private fun schedulePeriodic(interval: Long, unit: TimeUnit) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            interval, unit
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )
    }

    fun getCurrentFrequency(): String {
        return preferences.getString(KEY_NOTIFICATION_FREQUENCY, FREQUENCY_OFF) ?: FREQUENCY_OFF
    }
} 