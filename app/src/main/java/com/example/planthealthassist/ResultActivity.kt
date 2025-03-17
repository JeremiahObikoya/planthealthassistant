package com.example.planthealthassist

import android.content.Intent
import android.os.Bundle
import com.example.planthealthassist.databinding.ActivityResultBinding

class ResultActivity : BaseActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupShareButton()
        // TODO: Load and display scan results
    }

    private fun setupShareButton() {
        binding.shareButton.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Plant Health Analysis Result: " +
                        "${binding.diseaseName.text}\n\n${binding.solutionText.text}")
            }
            startActivity(Intent.createChooser(shareIntent, null))
        }
    }
} 