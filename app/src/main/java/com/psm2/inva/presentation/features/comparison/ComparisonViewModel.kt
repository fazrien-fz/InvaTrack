package com.psm2.inva.presentation.features.comparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.FishRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ComparisonViewModel(
    private val fishId1: String,
    private val fishId2: String,
    private val repository: FishRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ComparisonState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Fetch both in parallel
                val fish1Deferred = async { repository.getFishById(fishId1) }
                val fish2Deferred = async { repository.getFishById(fishId2) }

                val fish1 = fish1Deferred.await()
                val fish2 = fish2Deferred.await()

                if (fish1 != null && fish2 != null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            fish1 = fish1,
                            fish2 = fish2
                        )
                    }
                } else {
                    _state.update {
                        it.copy(isLoading = false, error = "Failed to load species data.")
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}