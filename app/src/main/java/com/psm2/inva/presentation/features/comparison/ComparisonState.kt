package com.psm2.inva.presentation.features.comparison

import com.psm2.inva.model.Fish

data class ComparisonState(
    val isLoading: Boolean = true,
    val fish1: Fish? = null,
    val fish2: Fish? = null,
    val error: String? = null
)