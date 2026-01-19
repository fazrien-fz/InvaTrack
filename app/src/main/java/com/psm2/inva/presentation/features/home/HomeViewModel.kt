package com.psm2.inva.presentation.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.BlogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val blogRepository: BlogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Fetch Blogs
            blogRepository.getAllBlogs().collect { blogs ->
                _state.update {
                    it.copy(
                        blogs = blogs,
                        isLoading = false
                    )
                }
            }
        }
    }
}