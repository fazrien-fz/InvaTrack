package com.psm2.inva.presentation.features.home

import com.psm2.inva.model.Blog

data class HomeState(
    val blogs: List<Blog> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)