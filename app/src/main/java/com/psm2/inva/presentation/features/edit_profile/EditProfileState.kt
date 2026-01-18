package com.psm2.inva.presentation.features.edit_profile

import android.net.Uri

data class EditProfileState(
    val userId: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profilePictureUrl: String = "",
    val newImageUri: Uri? = null,

    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val updateError: String? = null
)