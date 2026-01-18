package com.psm2.inva.presentation.features.edit_profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.AuthRepository
import com.psm2.inva.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val currentUser = authRepository.getSignedInUser()

            if (currentUser != null) {
                // Try to get extended data from Firestore
                val fullProfile = authRepository.getUserProfile(currentUser.userId) ?: currentUser

                _state.update {
                    it.copy(
                        userId = fullProfile.userId,
                        username = fullProfile.username ?: "",
                        firstName = fullProfile.firstName ?: "",
                        lastName = fullProfile.lastName ?: "",
                        profilePictureUrl = fullProfile.profilePictureUrl ?: "",
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, updateError = "User not logged in") }
            }
        }
    }

    fun updateUsername(value: String) {
        _state.update { it.copy(username = value) }
    }

    fun updateFirstName(value: String) {
        _state.update { it.copy(firstName = value) }
    }

    fun updateLastName(value: String) {
        _state.update { it.copy(lastName = value) }
    }

    fun updateImageUri(uri: Uri?) {
        _state.update { it.copy(newImageUri = uri) }
    }

    fun updateProfile() {
        val currentState = _state.value
        if (currentState.username.isBlank()) {
            _state.update { it.copy(updateError = "Username cannot be empty") }
            return
        }

        _state.update { it.copy(isUpdating = true, updateError = null) }

        viewModelScope.launch {
            val userData = UserData(
                userId = currentState.userId,
                username = currentState.username,
                firstName = currentState.firstName,
                lastName = currentState.lastName,
                profilePictureUrl = currentState.profilePictureUrl // Will be updated in repo if new URI exists
            )

            val result = authRepository.updateUserProfile(userData, currentState.newImageUri)

            if (result.isSuccess) {
                _state.update { it.copy(isUpdating = false, updateSuccess = true) }
            } else {
                _state.update {
                    it.copy(
                        isUpdating = false,
                        updateError = result.exceptionOrNull()?.message ?: "Failed to update profile"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(updateSuccess = false, updateError = null) }
    }
}