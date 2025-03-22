package com.example.planthealthassist.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.planthealthassist.R
import com.example.planthealthassist.ResultActivity
import com.example.planthealthassist.data.AppDatabase
import kotlinx.coroutines.runBlocking

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val CHANNEL_ID = "plant_health_reminders"
        const val NOTIFICATION_ID = 1
    }

    override fun doWork(): Result {
        return runBlocking {
            try {
                // Get the latest scan from database
                val database = AppDatabase.getDatabase(context)
                val latestScan = database.scanHistoryDao().getLatestScan()

                if (latestScan != null) {
                    // Create notification channel for Android O and above
                    createNotificationChannel()

                    // Create intent for when notification is tapped
                    val intent = Intent(context, ResultActivity::class.java).apply {
                        putExtra(ResultActivity.EXTRA_IMAGE_PATH, latestScan.imageUri)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }

                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // Build the notification
                    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Plant Health Reminder")
                        .setContentText("Don't forget to check on your ${latestScan.diseaseName} treatment")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build()

                    // Show the notification
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, notification)

                    Result.success()
                } else {
                    Result.success() // No scans yet, but not a failure
                }
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
} 