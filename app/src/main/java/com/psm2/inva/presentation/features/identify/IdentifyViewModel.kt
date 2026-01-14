package com.psm2.inva.presentation.features.identify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.AuthRepository
import com.psm2.inva.data.FishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IdentifyViewModel(
    private val repository: FishRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FishListState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // 1. Check Admin Status (One-time check)
            val user = authRepository.getSignedInUser()
            val isAdmin = user?.let { authRepository.isAdmin(it.userId) } ?: false
            _state.update { it.copy(isAdmin = isAdmin) }

            // 2. Collect Fish List (Real-time stream)
            // This will automatically trigger whenever data in Firestore changes
            repository.getAllFish()
                .collect { fish ->
                    _state.update {
                        it.copy(
                            fishList = fish,
                            isLoading = false
                        )
                    }
                }
        }
    }
}