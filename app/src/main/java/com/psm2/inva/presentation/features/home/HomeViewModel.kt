package com.psm2.inva.presentation.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.AuthRepository
import com.psm2.inva.data.BlogRepository
import com.psm2.inva.data.FishRepository
import com.psm2.inva.data.SightingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val blogRepository: BlogRepository,
    private val fishRepository: FishRepository,
    private val sightingRepository: SightingRepository // Injected
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Check admin status
            var user = authRepository.getSignedInUser()
            if (user != null) {
                val isAdmin = authRepository.isAdmin(user.userId)
                user = user.copy(isAdmin = isAdmin)
            }
            _state.update { it.copy(user = user) }

            // Fetch Blogs
            launch {
                blogRepository.getAllBlogs().collect { blogs ->
                    _state.update { it.copy(blogs = blogs) }
                }
            }

            // Fetch Fish List for Comparison Dropdowns
            launch {
                fishRepository.getAllFish().collect { list ->
                    _state.update { it.copy(fishList = list) }
                }
            }

            // Fetch Recent Invasive Sightings
            launch {
                sightingRepository.getAllSightings().collect { sightings ->
                    val recent = sightings
                        .filter { it.category.equals("Invasive", ignoreCase = true) }
                        .sortedByDescending { it.date }
                        .take(5) // Limit to 5 recent sightings
                    _state.update { it.copy(recentInvasiveSightings = recent) }
                }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }
}