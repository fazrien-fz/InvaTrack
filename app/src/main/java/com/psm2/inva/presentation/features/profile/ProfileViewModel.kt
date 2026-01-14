package com.psm2.inva.presentation.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.AuthRepository
import com.psm2.inva.data.SightingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val sightingRepository: SightingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val user = authRepository.getSignedInUser()
            val isAdmin = user?.let { authRepository.isAdmin(it.userId) } ?: false

            _state.update {
                it.copy(
                    signedInUser = user,
                    isAdmin = isAdmin
                )
            }

            // Load stats and sightings if user is logged in
            if (user != null) {
                sightingRepository.getAllSightings().collect { sightings ->
                    // Filter sightings for the current user
                    val mySightings = sightings.filter { it.userId == user.userId }
                        .sortedByDescending { it.date } // Sort by newest first

                    // Calculate Stats
                    val invasiveRemoved = mySightings.count {
                        it.category.equals("Invasive", ignoreCase = true) &&
                                it.actionTaken.equals("Removed", ignoreCase = true)
                    }

                    val nativeReleased = mySightings.count {
                        it.category.equals("Native", ignoreCase = true) &&
                                it.actionTaken.equals("Released", ignoreCase = true)
                    }

                    _state.update {
                        it.copy(
                            sightingCount = mySightings.size,
                            userSightings = mySightings,
                            invasiveRemovedCount = invasiveRemoved,
                            nativeReleasedCount = nativeReleased,
                            isLoading = false
                        )
                    }
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}