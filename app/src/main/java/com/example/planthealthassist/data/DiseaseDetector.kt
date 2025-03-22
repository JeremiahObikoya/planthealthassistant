package com.example.planthealthassist.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import org.tensorflow.lite.support.common.FileUtil

class DiseaseDetector(private val context: Context) {
    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    companion object {
        private const val TAG = "DiseaseDetector"
        private const val MODEL_FILE = "plant_disease_model.tflite"
        private const val LABELS_FILE = "plant_disease_labels.txt"
        private const val IMAGE_SIZE = 224 // Standard input size for many vision models
        private const val PROBABILITY_THRESHOLD = 0.5f
    }

    init {
        try {
            Log.d(TAG, "Initializing DiseaseDetector")
            
            // First check if model file exists
            if (!context.assets.list("")?.contains(MODEL_FILE)!!) {
                throw IOException("Model file not found in assets: $MODEL_FILE")
            }
            
            // Then check if labels file exists
            if (!context.assets.list("")?.contains(LABELS_FILE)!!) {
                throw IOException("Labels file not found in assets: $LABELS_FILE")
            }
            
            loadModel()
            loadLabels()
            Log.d(TAG, "DiseaseDetector initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing DiseaseDetector: ${e.message}", e)
            throw RuntimeException("Failed to initialize DiseaseDetector: ${e.message}", e)
        }
    }

    private fun loadModel() {
        try {
            Log.d(TAG, "Loading model from assets: $MODEL_FILE")
            val modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE)
            val options = Interpreter.Options()
            interpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "Model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model: ${e.message}", e)
            throw RuntimeException("Failed to load TFLite model: ${e.message}", e)
        }
    }

    private fun loadLabels() {
        try {
            Log.d(TAG, "Loading labels from assets: $LABELS_FILE")
            labels = context.assets.open(LABELS_FILE).bufferedReader().useLines { lines ->
                lines.toList()
            }
            Log.d(TAG, "Labels loaded successfully. Count: ${labels.size}")
            
            if (labels.isEmpty()) {
                throw IOException("No labels found in $LABELS_FILE")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error loading labels: ${e.message}", e)
            throw RuntimeException("Failed to load labels: ${e.message}", e)
        }
    }

    fun detectDisease(imagePath: String): DetectionResult {
        try {
            Log.d(TAG, "Starting disease detection for image: $imagePath")
            
            // Load and verify image
            val bitmap = try {
                // First try to load as a file path
                BitmapFactory.decodeFile(imagePath)
            } catch (e: Exception) {
                // If that fails, try to load as a content URI
                context.contentResolver.openInputStream(Uri.parse(imagePath))?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
            
            if (bitmap == null) {
                throw IOException("Failed to decode image: $imagePath")
            }
            Log.d(TAG, "Image loaded successfully. Size: ${bitmap.width}x${bitmap.height}")
            
            // Resize image to model input size
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true)
            Log.d(TAG, "Image resized to ${IMAGE_SIZE}x${IMAGE_SIZE}")
            
            // Convert image to ByteBuffer
            val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)
            Log.d(TAG, "Image converted to ByteBuffer")
            
            // Output array for model predictions
            val outputArray = Array(1) { FloatArray(39) }
            
            // Run inference
            interpreter?.let { interpreter ->
                Log.d(TAG, "Running model inference")
                interpreter.run(inputBuffer, outputArray)
                Log.d(TAG, "Model inference completed")
            } ?: throw IllegalStateException("Interpreter is null")
            
            // Get the prediction with highest probability (ignoring the last class)
            val prediction = outputArray[0].take(38).toFloatArray()
            val maxIndex = prediction.indices.maxByOrNull { prediction[it] } ?: 0
            val confidence = prediction[maxIndex]
            
            // Get the disease name and plant type
            val fullLabel = labels[maxIndex]
            val parts = fullLabel.split("___")
            val plantType = parts[0].replace("_", " ")
            val diseaseName = if (parts.size > 1) {
                parts[1].replace("_", " ")
            } else {
                "Healthy"
            }
            
            Log.d(TAG, "Detection completed: Plant=$plantType, Disease=$diseaseName, Confidence=$confidence")
            
            return DetectionResult(
                plantType = plantType,
                diseaseName = diseaseName,
                confidence = confidence,
                isHealthy = diseaseName.equals("healthy", ignoreCase = true)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error during disease detection: ${e.message}", e)
            throw RuntimeException("Failed to detect disease: ${e.message}", e)
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        try {
            val inputBuffer = ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * 3 * 4)
            inputBuffer.order(ByteOrder.nativeOrder())
            
            val intValues = IntArray(IMAGE_SIZE * IMAGE_SIZE)
            bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            
            var pixel = 0
            for (i in 0 until IMAGE_SIZE) {
                for (j in 0 until IMAGE_SIZE) {
                    val value = intValues[pixel++]
                    // Normalize pixel values to [-1, 1]
                    inputBuffer.putFloat(((value shr 16 and 0xFF) / 255.0f * 2 - 1))
                    inputBuffer.putFloat(((value shr 8 and 0xFF) / 255.0f * 2 - 1))
                    inputBuffer.putFloat(((value and 0xFF) / 255.0f * 2 - 1))
                }
            }
            
            return inputBuffer
        } catch (e: Exception) {
            Log.e(TAG, "Error converting bitmap to ByteBuffer: ${e.message}", e)
            throw RuntimeException("Failed to convert image: ${e.message}", e)
        }
    }

    fun close() {
        try {
            interpreter?.close()
            interpreter = null
            Log.d(TAG, "DiseaseDetector closed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing interpreter: ${e.message}", e)
        }
    }
}

data class DetectionResult(
    val plantType: String,
    val diseaseName: String,
    val confidence: Float,
    val isHealthy: Boolean
) 