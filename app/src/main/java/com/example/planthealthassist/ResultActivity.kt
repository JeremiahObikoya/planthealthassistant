package com.example.planthealthassist

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.planthealthassist.data.AppDatabase
import com.example.planthealthassist.data.DiseaseDetector
import com.example.planthealthassist.data.DetectionResult
import com.example.planthealthassist.data.ScanHistoryRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ResultActivity : AppCompatActivity() {
    private lateinit var diseaseDetector: DiseaseDetector
    private lateinit var scanHistoryRepository: ScanHistoryRepository
    private lateinit var imagePath: String
    private var isFromHistory: Boolean = false
    
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var resultCard: MaterialCardView
    private lateinit var diseaseNameText: TextView
    private lateinit var confidenceText: TextView
    private lateinit var recommendationsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Initialize views
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        resultCard = findViewById(R.id.resultCard)
        diseaseNameText = findViewById(R.id.diseaseNameText)
        confidenceText = findViewById(R.id.confidenceText)
        recommendationsText = findViewById(R.id.recommendationsText)

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.scan_result)

        // Show progress initially
        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        resultCard.visibility = View.GONE

        try {
            // Get image path and source from intent
            imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH) ?: run {
                showError("No image path provided")
                return
            }
            isFromHistory = intent.getBooleanExtra(EXTRA_FROM_HISTORY, false)

            // Verify image exists and is readable
            if (!verifyImage()) {
                return
            }

            // Load and display image first
            if (!loadImage()) {
                return
            }

            // Initialize components
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        // Initialize database and repository
                        val database = AppDatabase.getDatabase(applicationContext)
                        scanHistoryRepository = ScanHistoryRepository(database.scanHistoryDao())
                        
                        // Initialize disease detector
                        diseaseDetector = DiseaseDetector(this@ResultActivity)
                    }
                    
                    // If we got here, initialization was successful, start detection
                    detectDisease()
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing components", e)
                    showError("Error initializing disease detector: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in ResultActivity onCreate", e)
            showError("Error: ${e.message}")
        }
    }

    private fun showError(message: String) {
        Log.e(TAG, message)
        progressBar.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        resultCard.visibility = View.GONE
        errorText.text = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun loadImage(): Boolean {
        return try {
            val imageView = findViewById<ImageView>(R.id.imageView)
            val bitmap = try {
                // First try to load as a file path
                BitmapFactory.decodeFile(imagePath)
            } catch (e: Exception) {
                // If that fails, try to load as a content URI
                contentResolver.openInputStream(Uri.parse(imagePath))?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
            
            if (bitmap == null) {
                showError("Failed to decode image: $imagePath")
                false
            } else {
                imageView.setImageBitmap(bitmap)
                Log.d(TAG, "Image loaded successfully")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image", e)
            showError("Error loading image: ${e.message}")
            false
        }
    }

    private fun verifyImage(): Boolean {
        try {
            // Try as file path first
            val file = File(imagePath)
            if (file.exists() && file.canRead()) {
                return true
            }

            // If file doesn't exist, try as content URI
            val uri = Uri.parse(imagePath)
            contentResolver.openInputStream(uri)?.use {
                return true
            }

            showError("Image not found or not readable: $imagePath")
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying image", e)
            showError("Error verifying image: ${e.message}")
            return false
        }
    }

    private fun detectDisease() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting disease detection")
                val result = withContext(Dispatchers.IO) {
                    diseaseDetector.detectDisease(imagePath)
                }
                Log.d(TAG, "Disease detection completed: ${result.diseaseName}")

                // Update UI with results
                diseaseNameText.text = getString(
                    R.string.disease_result,
                    result.plantType,
                    result.diseaseName
                )
                confidenceText.text = getString(
                    R.string.confidence_result,
                    (result.confidence * 100).toInt()
                )
                recommendationsText.text = getRecommendations(result)

                // Save to history
                saveToHistory(result)

                // Show results
                progressBar.visibility = View.GONE
                errorText.visibility = View.GONE
                resultCard.visibility = View.VISIBLE

                // Setup share button
                setupShareButton(result)
            } catch (e: Exception) {
                Log.e(TAG, "Error during disease detection", e)
                showError("Error analyzing image: ${e.message}")
            }
        }
    }

    private fun getRecommendations(result: DetectionResult): String {
        val recommendations = StringBuilder()
        
        if (result.isHealthy) {
            recommendations.append(getString(R.string.healthy_recommendations))
        } else {
            when {
                result.diseaseName.contains("blight", ignoreCase = true) -> {
                    recommendations.append(getString(R.string.blight_recommendations))
                }
                result.diseaseName.contains("rust", ignoreCase = true) -> {
                    recommendations.append(getString(R.string.rust_recommendations))
                }
                result.diseaseName.contains("mildew", ignoreCase = true) -> {
                    recommendations.append(getString(R.string.mildew_recommendations))
                }
                result.diseaseName.contains("virus", ignoreCase = true) -> {
                    recommendations.append(getString(R.string.virus_recommendations))
                }
                result.diseaseName.contains("bacterial", ignoreCase = true) -> {
                    recommendations.append(getString(R.string.bacterial_recommendations))
                }
                else -> {
                    recommendations.append(getString(R.string.general_recommendations))
                }
            }
        }

        return recommendations.toString()
    }

    private fun saveToHistory(result: DetectionResult) {
        // Only save to history if this is a new scan, not when viewing history
        if (isFromHistory) {
            Log.d(TAG, "Skipping history save - viewing existing scan")
            return
        }

        lifecycleScope.launch {
            try {
                scanHistoryRepository.insertScanHistory(
                    plantType = result.plantType,
                    diseaseName = result.diseaseName,
                    confidence = result.confidence,
                    imagePath = imagePath,
                    timestamp = System.currentTimeMillis()
                )
                Log.d(TAG, "Scan history saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving to history", e)
                // Don't show error to user since this is not critical
            }
        }
    }

    private fun setupShareButton(result: DetectionResult) {
        val shareButton = findViewById<MaterialButton>(R.id.shareButton)
        shareButton.setOnClickListener {
            val shareText = buildString {
                append("Plant Health Analysis Results:\n\n")
                append("Plant Type: ${result.plantType}\n")
                append("Disease: ${result.diseaseName}\n")
                append("Confidence: ${(result.confidence * 100).toInt()}%\n\n")
                append("Recommendations:\n")
                append(getRecommendations(result))
            }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_results)))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            diseaseDetector.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing disease detector", e)
        }
    }

    companion object {
        private const val TAG = "ResultActivity"
        const val EXTRA_IMAGE_PATH = "com.example.planthealthassist.EXTRA_IMAGE_PATH"
        const val EXTRA_FROM_HISTORY = "com.example.planthealthassist.EXTRA_FROM_HISTORY"
    }
} 