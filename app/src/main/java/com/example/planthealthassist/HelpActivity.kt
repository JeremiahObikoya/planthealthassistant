package com.example.planthealthassist

import android.os.Bundle
import com.example.planthealthassist.databinding.ActivityHelpBinding

class HelpActivity : BaseActivity() {
    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
    }
} 