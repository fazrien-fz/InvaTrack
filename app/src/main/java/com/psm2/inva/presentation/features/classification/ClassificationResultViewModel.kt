package com.psm2.inva.presentation.features.classification

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.ClassificationResult
import com.psm2.inva.data.FishClassifier
import com.psm2.inva.data.FishRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClassificationResultState(
    val imageUri: Uri? = null,
    val isClassifying: Boolean = true,
    val classificationResults: List<ClassificationResult> = emptyList(),
    val classificationError: String? = null,
    val matchedFishId: String? = null // ID of the fish in Firestore if found
)

class ClassificationResultViewModel(
    savedStateHandle: SavedStateHandle,
    private val classifier: FishClassifier,
    private val context: Context,
    private val repository: FishRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClassificationResultState())
    val state = _state.asStateFlow()

    init {
        // Retrieve the URI from navigation arguments
        val imageUriString = savedStateHandle.get<String>("imageUri") ?: ""
        if (imageUriString.isNotEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            _state.update { it.copy(imageUri = imageUri) }
            classifyImage(imageUri)
        } else {
            _state.update {
                it.copy(
                    isClassifying = false,
                    classificationError = "Image could not be loaded."
                )
            }
        }
    }

    private fun classifyImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                val results = classifier.classify(bitmap)

                if (results.isEmpty()) {
                    _state.update {
                        it.copy(
                            classificationError = "Could not identify the fish. Please try another photo.",
                            isClassifying = false
                        )
                    }
                } else {
                    val topResult = results.first()
                    _state.update {
                        it.copy(
                            classificationResults = results,
                            isClassifying = false
                        )
                    }
                    // Attempt to link the prediction to a database entry
                    findMatchingFish(topResult.label)
                }
            } catch (e: Exception) {
                Log.e("ClassificationVM", "Failed to classify image.", e)
                _state.update {
                    it.copy(
                        classificationError = "An error occurred during analysis.",
                        isClassifying = false
                    )
                }
            }
        }
    }

    private fun findMatchingFish(label: String) {
        viewModelScope.launch {
            // Normalize label: e.g., "peacock_bass" -> "peacock bass"
            val normalizedLabel = label.replace("_", " ")

            try {
                // Get the list of fish once to check for a match
                repository.getAllFish().take(1).collect { fishList ->
                    val match = fishList.find { fish ->
                        fish.name.equals(normalizedLabel, ignoreCase = true) ||
                                fish.scientificName.equals(normalizedLabel, ignoreCase = true)
                    }
                    if (match != null) {
                        _state.update { it.copy(matchedFishId = match.id) }
                    }
                }
            } catch (e: Exception) {
                Log.e("ClassificationVM", "Failed to find matching fish in DB", e)
            }
        }
    }

    override fun onCleared() {
        classifier.close()
        super.onCleared()
    }
}