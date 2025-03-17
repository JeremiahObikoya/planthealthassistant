package com.example.planthealthassist

import android.os.Bundle
import android.widget.AdapterView
import com.example.planthealthassist.databinding.ActivitySettingsBinding

class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupLanguageSpinner()
        setupNotificationSwitch()
    }

    private fun setupLanguageSpinner() {
        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                // TODO: Implement language change
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupNotificationSwitch() {
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Implement notification settings
        }
    }
} 