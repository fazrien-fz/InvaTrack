package com.psm2.inva.presentation.features.edit_fish

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

class EditFishViewModel(
    private val fishId: String,
    private val repository: FishRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditFishState(fishId = fishId))
    val state = _state.asStateFlow()

    init {
        loadFishDetails()
    }

    private fun loadFishDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val fish = repository.getFishById(fishId)
                if (fish != null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            fish = fish,
                            name = fish.name,
                            localName = fish.localName,
                            scientificName = fish.scientificName,
                            type = fish.type,
                            description = fish.description,
                            habitat = fish.habitat,
                            diet = fish.diet,
                            origin = fish.origin,
                            impact = fish.impact,
                            conservationStatus = fish.conservationStatus,
                            existingImageUrl = fish.imageUrl
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, updateError = "Fish not found.") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, updateError = e.message) }
            }
        }
    }

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

    fun submitUpdate() {
        val currentState = _state.value

        if (currentState.name.isBlank()) {
            _state.update { it.copy(updateError = "Species name cannot be empty.") }
            return
        }

        _state.update { it.copy(isUpdating = true, updateError = null) }

        viewModelScope.launch {
            try {
                val updatedFish = Fish(
                    id = fishId,
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
                    imageUrl = currentState.existingImageUrl
                )

                repository.updateFish(
                    fish = updatedFish,
                    newImageUri = currentState.imageUri
                ).getOrThrow()

                _state.update {
                    it.copy(isUpdating = false, updateSuccess = true)
                }
            } catch (e: Exception) {
                Log.e("EditFishViewModel", "Failed to update fish", e)
                _state.update {
                    it.copy(isUpdating = false, updateError = e.message ?: "An unknown error occurred.")
                }
            }
        }
    }

    fun showDeleteConfirmation(show: Boolean) {
        _state.update { it.copy(showDeleteConfirmation = show) }
    }

    fun deleteFish() {
        _state.update { it.copy(isDeleting = true, showDeleteConfirmation = false, deleteError = null) }
        viewModelScope.launch {
            try {
                repository.deleteFish(fishId).getOrThrow()
                _state.update { it.copy(isDeleting = false, deleteSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isDeleting = false, deleteError = e.message ?: "Failed to delete species.") }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(updateSuccess = false, updateError = null, deleteSuccess = false, deleteError = null) }
    }
}