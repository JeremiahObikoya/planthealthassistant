package com.example.planthealthassist

import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem

abstract class BaseActivity : AppCompatActivity() {
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    protected fun setupActionBar(showBackButton: Boolean = true) {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(showBackButton)
            setDisplayShowHomeEnabled(showBackButton)
        }
    }
} 