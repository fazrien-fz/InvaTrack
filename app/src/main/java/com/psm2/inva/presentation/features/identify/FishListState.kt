package com.psm2.inva.presentation.features.identify

import com.psm2.inva.model.Fish

data class FishListState(
    val fishList: List<Fish> = emptyList(),
    val isLoading: Boolean = false,
    val isAdmin: Boolean = false
)
