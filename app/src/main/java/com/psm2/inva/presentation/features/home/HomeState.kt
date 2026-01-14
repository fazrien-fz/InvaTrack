package com.psm2.inva.presentation.features.home

import com.psm2.inva.model.Blog
import com.psm2.inva.model.Fish
import com.psm2.inva.model.Sighting
import com.psm2.inva.model.UserData

data class HomeState(
    val user: UserData? = null,
    val blogs: List<Blog> = emptyList(),
    val fishList: List<Fish> = emptyList(),
    val recentInvasiveSightings: List<Sighting> = emptyList(), // Added field
    val isLoading: Boolean = false,
    val error: String? = null
)