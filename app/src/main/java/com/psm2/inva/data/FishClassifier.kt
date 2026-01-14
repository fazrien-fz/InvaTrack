package com.psm2.inva.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class FishClassifier(
    private val context: Context,
    private val modelPath: String = "best_float32.tflite",
    private val labelPath: String = "labels.txt"
) {
    private var interpreter: Interpreter? = null
    private lateinit var labels: List<String>
    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0
    private val TAG = "FishClassifier"

    init {
        setupClassifier()
    }

    private fun setupClassifier() {
        try {
            val modelBuffer = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options()
            options.setNumThreads(4)
            interpreter = Interpreter(modelBuffer, options)

            // Get model input and output details
            val inputTensor = interpreter!!.getInputTensor(0)
            val outputTensor = interpreter!!.getOutputTensor(0)

            inputImageWidth = inputTensor.shape()[1]
            inputImageHeight = inputTensor.shape()[2]

            labels = FileUtil.loadLabels(context, labelPath)

            Log.d(TAG, "Classifier initialized successfully.")
            Log.d(TAG, "Input shape: ${inputTensor.shape().joinToString(", ")}")
            Log.d(TAG, "Output shape: ${outputTensor.shape().joinToString(", ")}")
            Log.d(TAG, "Labels loaded: ${labels.size}")

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TensorFlow Lite interpreter: ${e.message}", e)
            interpreter = null
        }
    }

    fun classify(bitmap: Bitmap): List<ClassificationResult> {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter is not initialized.")
            return emptyList()
        }

        try {
            // Preprocess the image to match the model's input requirements
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f)) // Normalize to [0, 1] for float models
                .build()

            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            // The output is a 2D array, e.g., [[0.1, 0.2, 0.7]]
            val outputBuffer = TensorBuffer.createFixedSize(interpreter!!.getOutputTensor(0).shape(), DataType.FLOAT32)

            // Run inference
            interpreter?.run(tensorImage.buffer, outputBuffer.buffer.rewind())

            // Get the raw float array of probabilities
            val probabilities = outputBuffer.floatArray

            // Log the raw output for debugging
            Log.d(TAG, "Raw model output: [${probabilities.joinToString(", ")}]")

            // Find the index with the highest probability
            var maxProb = 0f
            var maxIndex = -1
            probabilities.forEachIndexed { index, prob ->
                if (prob > maxProb) {
                    maxProb = prob
                    maxIndex = index
                }
            }

            // Return the best result
            if (maxIndex != -1) {
                return listOf(
                    ClassificationResult(
                        label = labels[maxIndex],
                        score = probabilities[maxIndex]
                    )
                )
            }
            return emptyList()

        } catch (e: Exception) {
            Log.e(TAG, "Error during classification.", e)
            return emptyList()
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

data class ClassificationResult(
    val label: String,
    val score: Float
)
