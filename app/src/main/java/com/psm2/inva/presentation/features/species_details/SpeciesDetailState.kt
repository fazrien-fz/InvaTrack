package com.psm2.inva.presentation.features.species_details

import com.psm2.inva.model.Fish

data class SpeciesDetailState(
    val fish: Fish? = null,
    val isLoading: Boolean = false,
    val isAdmin: Boolean = false,

    // Delete State
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val deleteError: String? = null
)