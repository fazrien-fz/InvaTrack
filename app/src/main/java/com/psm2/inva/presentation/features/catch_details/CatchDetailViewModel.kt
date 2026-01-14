package com.psm2.inva.presentation.features.catch_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.AuthRepository
import com.psm2.inva.data.SightingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CatchDetailViewModel(
    private val sightingId: String,
    private val repository: SightingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CatchDetailState())
    val state = _state.asStateFlow()

    init {
        loadSightingDetails()
    }

    private fun loadSightingDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val sighting = repository.getSightingById(sightingId)
                val user = authRepository.getSignedInUser()

                if (sighting != null) {
                    val isOwner = user?.userId == sighting.userId
                    val isAdmin = user?.let { authRepository.isAdmin(it.userId) } ?: false

                    _state.update {
                        it.copy(
                            sighting = sighting,
                            isLoading = false,
                            isOwnerOrAdmin = isOwner || isAdmin
                        )
                    }
                } else {
                    _state.update { it.copy(error = "Sighting not found", isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun deleteSighting() {
        val sighting = _state.value.sighting ?: return
        _state.update { it.copy(isDeleting = true, deleteError = null) }

        viewModelScope.launch {
            val result = repository.deleteSighting(sighting.id, sighting.imageUrls)
            if (result.isSuccess) {
                _state.update { it.copy(isDeleting = false, deleteSuccess = true) }
            } else {
                _state.update {
                    it.copy(
                        isDeleting = false,
                        deleteError = result.exceptionOrNull()?.message ?: "Failed to delete catch"
                    )
                }
            }
        }
    }

    fun resetDeleteState() {
        _state.update { it.copy(deleteSuccess = false, deleteError = null) }
    }
}