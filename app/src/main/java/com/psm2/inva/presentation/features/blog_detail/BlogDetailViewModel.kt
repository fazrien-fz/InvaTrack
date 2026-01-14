package com.psm2.inva.presentation.features.blog_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.AuthRepository
import com.psm2.inva.data.BlogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BlogDetailViewModel(
    private val blogId: String,
    private val repository: BlogRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BlogDetailState())
    val state = _state.asStateFlow()

    init {
        loadBlogDetails()
    }

    private fun loadBlogDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Check Admin Status
            val user = authRepository.getSignedInUser()
            val isAdmin = user?.let { authRepository.isAdmin(it.userId) } ?: false

            val blog = repository.getBlogById(blogId)

            if (blog != null) {
                _state.update {
                    it.copy(
                        blog = blog,
                        isLoading = false,
                        isAdmin = isAdmin
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "Blog not found") }
            }
        }
    }

    fun deleteBlog() {
        _state.update { it.copy(isDeleting = true, deleteError = null) }
        viewModelScope.launch {
            val result = repository.deleteBlog(blogId)
            if (result.isSuccess) {
                _state.update { it.copy(isDeleting = false, deleteSuccess = true) }
            } else {
                _state.update {
                    it.copy(
                        isDeleting = false,
                        deleteError = result.exceptionOrNull()?.message ?: "Failed to delete blog"
                    )
                }
            }
        }
    }

    fun resetDeleteState() {
        _state.update { it.copy(deleteSuccess = false, deleteError = null) }
    }
}