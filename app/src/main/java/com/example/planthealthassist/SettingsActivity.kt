package com.example.planthealthassist

import android.os.Bundle
import android.widget.AdapterView
import android.widget.RadioGroup
import com.example.planthealthassist.databinding.ActivitySettingsBinding
import com.example.planthealthassist.notifications.NotificationManager

class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupLanguageSpinner()
        setupNotificationSettings()
    }

    private fun setupLanguageSpinner() {
        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                // TODO: Implement language change
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupNotificationSettings() {
        notificationManager = NotificationManager(this)

        // Set the current frequency
        when (notificationManager.getCurrentFrequency()) {
            NotificationManager.FREQUENCY_DAILY -> binding.radioDaily.isChecked = true
            NotificationManager.FREQUENCY_WEEKLY -> binding.radioWeekly.isChecked = true
            NotificationManager.FREQUENCY_OFF -> binding.radioOff.isChecked = true
        }

        // Handle radio button changes
        binding.notificationFrequencyGroup.setOnCheckedChangeListener { _, checkedId ->
            val frequency = when (checkedId) {
                R.id.radioDaily -> NotificationManager.FREQUENCY_DAILY
                R.id.radioWeekly -> NotificationManager.FREQUENCY_WEEKLY
                else -> NotificationManager.FREQUENCY_OFF
            }
            notificationManager.scheduleNotifications(frequency)
        }
    }
} 