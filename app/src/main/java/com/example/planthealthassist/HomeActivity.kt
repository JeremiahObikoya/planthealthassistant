package com.example.planthealthassist

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planthealthassist.databinding.ActivityHomeBinding

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar(false)
        setupButtons()
        setupRecyclerView()
    }

    private fun setupButtons() {
        binding.apply {
            cameraButton.setOnClickListener {
                startActivity(Intent(this@HomeActivity, CameraActivity::class.java))
            }

            historyButton.setOnClickListener {
                startActivity(Intent(this@HomeActivity, HistoryActivity::class.java))
            }

            helpButton.setOnClickListener {
                startActivity(Intent(this@HomeActivity, HelpActivity::class.java))
            }

            settingsButton.setOnClickListener {
                startActivity(Intent(this@HomeActivity, SettingsActivity::class.java))
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recentScans.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            // TODO: Add adapter implementation for recent scans
        }
    }
} 