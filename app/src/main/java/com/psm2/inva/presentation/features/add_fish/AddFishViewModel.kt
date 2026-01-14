package com.psm2.inva.presentation.features.add_fish

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.FishRepository
import com.psm2.inva.model.Fish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddFishViewModel(
    private val repository: FishRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddFishState())
    val state = _state.asStateFlow()

    fun updateName(name: String) { _state.update { it.copy(name = name) } }
    fun updateLocalName(name: String) { _state.update { it.copy(localName = name) } }
    fun updateScientificName(name: String) { _state.update { it.copy(scientificName = name) } }
    fun updateType(type: String) { _state.update { it.copy(type = type) } }
    fun updateDescription(desc: String) { _state.update { it.copy(description = desc) } }
    fun updateHabitat(habitat: String) { _state.update { it.copy(habitat = habitat) } }
    fun updateDiet(diet: String) { _state.update { it.copy(diet = diet) } }
    fun updateOrigin(origin: String) { _state.update { it.copy(origin = origin) } }
    fun updateImpact(impact: String) { _state.update { it.copy(impact = impact) } }
    fun updateConservationStatus(status: String) { _state.update { it.copy(conservationStatus = status) } }
    fun updateImageUri(uri: Uri?) { _state.update { it.copy(imageUri = uri) } }

    fun submitFish() {
        val currentState = _state.value

        if (currentState.name.isBlank()) {
            _state.update { it.copy(submissionError = "Species name cannot be empty.") }
            return
        }
        if (currentState.imageUri == null) {
            _state.update { it.copy(submissionError = "Please select an image.") }
            return
        }

        _state.update { it.copy(isSubmitting = true, submissionError = null) }

        viewModelScope.launch {
            try {
                val fish = Fish(
                    name = currentState.name,
                    localName = currentState.localName,
                    scientificName = currentState.scientificName,
                    type = currentState.type,
                    description = currentState.description,
                    habitat = currentState.habitat,
                    diet = currentState.diet,
                    origin = currentState.origin,
                    impact = currentState.impact,
                    conservationStatus = currentState.conservationStatus,
                    imageUrl = ""
                )

                repository.addFish(fish, currentState.imageUri).getOrThrow()

                _state.update {
                    it.copy(
                        isSubmitting = false,
                        submissionSuccess = true,
                        name = "",
                        localName = "",
                        scientificName = "",
                        type = "Invasive",
                        description = "",
                        habitat = "",
                        diet = "",
                        origin = "",
                        impact = "",
                        conservationStatus = "NE",
                        imageUri = null
                    )
                }
            } catch (e: Exception) {
                Log.e("AddFishViewModel", "Failed to submit fish", e)
                _state.update {
                    it.copy(isSubmitting = false, submissionError = e.message ?: "An unknown error occurred.")
                }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(submissionSuccess = false, submissionError = null) }
    }
}