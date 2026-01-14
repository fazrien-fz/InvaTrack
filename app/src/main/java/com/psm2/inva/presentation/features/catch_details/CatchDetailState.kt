package com.psm2.inva.presentation.features.catch_detail

import com.psm2.inva.model.Sighting

data class CatchDetailState(
    val sighting: Sighting? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isOwnerOrAdmin: Boolean = false,

    // Delete State
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val deleteError: String? = null
)