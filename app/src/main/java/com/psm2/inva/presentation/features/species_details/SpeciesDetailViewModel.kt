package com.psm2.inva.presentation.features.species_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.AuthRepository
import com.psm2.inva.data.FishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SpeciesDetailViewModel(
    private val fishId: String,
    private val repository: FishRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SpeciesDetailState())
    val state = _state.asStateFlow()

    init {
        loadFishDetails()
    }

    private fun loadFishDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val fish = repository.getFishById(fishId)
            val user = authRepository.getSignedInUser()
            val isAdmin = user?.let { authRepository.isAdmin(it.userId) } ?: false

            _state.update {
                it.copy(
                    fish = fish,
                    isLoading = false,
                    isAdmin = isAdmin
                )
            }
        }
    }

    fun deleteSpecies() {
        _state.update { it.copy(isDeleting = true, deleteError = null) }
        viewModelScope.launch {
            val result = repository.deleteFish(fishId)
            if (result.isSuccess) {
                _state.update { it.copy(isDeleting = false, deleteSuccess = true) }
            } else {
                _state.update {
                    it.copy(
                        isDeleting = false,
                        deleteError = result.exceptionOrNull()?.message ?: "Failed to delete species."
                    )
                }
            }
        }
    }

    fun resetDeleteState() {
        _state.update { it.copy(deleteSuccess = false, deleteError = null) }
    }
}