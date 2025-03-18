package com.example.planthealthassist

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planthealthassist.adapters.ScanHistoryAdapter
import com.example.planthealthassist.data.ScanHistoryItem
import com.example.planthealthassist.databinding.ActivityHistoryBinding
import com.example.planthealthassist.viewmodels.HistoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class HistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: ScanHistoryAdapter
    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupRecyclerView()
        observeViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all -> {
                showDeleteAllConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = ScanHistoryAdapter { historyItem ->
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("scan_id", historyItem.id)
            }
            startActivity(intent)
        }

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.scans.collect { scans ->
                adapter.submitList(scans)
                showEmptyState(scans.isEmpty())
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@HistoryActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.apply {
            emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            historyRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun showDeleteAllConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_all_title)
            .setMessage(R.string.delete_all_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteAllScans()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
} 