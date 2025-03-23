package com.example.planthealthassist

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import com.example.planthealthassist.databinding.ActivitySettingsBinding
import com.example.planthealthassist.notifications.NotificationManager
import java.util.*

class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var notificationManager: NotificationManager
    private lateinit var sharedPreferences: SharedPreferences
    private var currentLanguage: String = "en"

    companion object {
        private const val PREF_NAME = "settings"
        private const val PREF_LANGUAGE = "selected_language"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        currentLanguage = sharedPreferences.getString(PREF_LANGUAGE, "en") ?: "en"
        
        setupActionBar()
        setupLanguageSelection()
        setupNotificationSettings()
    }

    private fun setupLanguageSelection() {
        // Get arrays first
        val languages = resources.getStringArray(R.array.languages)
        val languageCodes = resources.getStringArray(R.array.language_values)
        
        // Create adapter with the correct layout
        val adapter = ArrayAdapter(
            this,
            R.layout.item_dropdown_menu,  // We'll create this layout
            languages
        )
        
        // Find current language name
        val currentIndex = languageCodes.indexOf(currentLanguage)
        val currentLanguageName = if (currentIndex != -1) languages[currentIndex] else languages[0]
        
        with(binding.languageSpinner) {
            // Set initial text
            setText(currentLanguageName)
            // Set adapter
            setAdapter(adapter)
            // Handle selection
            setOnItemClickListener { _, _, position, _ ->
                val selectedLanguageCode = languageCodes[position]
                if (selectedLanguageCode != currentLanguage) {
                    currentLanguage = selectedLanguageCode
                    sharedPreferences.edit()
                        .putString(PREF_LANGUAGE, selectedLanguageCode)
                        .apply()
                    updateLocale(selectedLanguageCode)
                }
            }
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

    private fun updateLocale(languageCode: String) {
        val locale = when (languageCode) {
            "yo" -> Locale("yo")
            "ig" -> Locale("ig")
            "ha" -> Locale("ha")
            else -> Locale("en")
        }
        
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        // Create new context with updated configuration
        val context = createConfigurationContext(config)
        
        // Update resources with new configuration
        resources.displayMetrics.setTo(context.resources.displayMetrics)
        
        // Recreate activity to apply changes
        recreate()
    }
} 