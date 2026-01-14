package com.psm2.inva.presentation.features.profile

import com.psm2.inva.model.Sighting
import com.psm2.inva.model.UserData

data class ProfileState(
    val signedInUser: UserData? = null,
    val isAdmin: Boolean = false,
    val sightingCount: Int = 0,
    val userSightings: List<Sighting> = emptyList(),
    // Added Stats
    val invasiveRemovedCount: Int = 0,
    val nativeReleasedCount: Int = 0,
    val isLoading: Boolean = false
)