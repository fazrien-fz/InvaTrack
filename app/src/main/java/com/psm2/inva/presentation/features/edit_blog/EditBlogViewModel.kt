package com.psm2.inva.presentation.features.edit_blog

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.BlogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditBlogViewModel(
    private val blogId: String,
    private val repository: BlogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditBlogState(blogId = blogId))
    val state = _state.asStateFlow()

    init {
        loadBlogDetails()
    }

    private fun loadBlogDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val blog = repository.getBlogById(blogId)
            if (blog != null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        blog = blog,
                        title = blog.title,
                        category = blog.category,
                        summary = blog.summary,
                        content = blog.content,
                        existingImageUrl = blog.coverImageUrl
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, updateError = "Blog not found") }
            }
        }
    }

    fun updateTitle(title: String) { _state.update { it.copy(title = title) } }
    fun updateCategory(category: String) { _state.update { it.copy(category = category) } }
    fun updateSummary(summary: String) {
        if (summary.length <= 150) _state.update { it.copy(summary = summary) }
    }
    fun updateContent(content: String) { _state.update { it.copy(content = content) } }
    fun updateImageUri(uri: Uri?) { _state.update { it.copy(imageUri = uri) } }

    fun submitUpdate() {
        val currentState = _state.value
        val originalBlog = currentState.blog ?: return

        if (currentState.title.isBlank() || currentState.content.isBlank()) {
            _state.update { it.copy(updateError = "Title and Content are required.") }
            return
        }

        _state.update { it.copy(isUpdating = true, updateError = null) }

        viewModelScope.launch {
            val updatedBlog = originalBlog.copy(
                title = currentState.title,
                category = currentState.category,
                summary = currentState.summary,
                content = currentState.content
            )

            val result = repository.updateBlog(updatedBlog, currentState.imageUri)

            if (result.isSuccess) {
                _state.update { it.copy(isUpdating = false, updateSuccess = true) }
            } else {
                _state.update {
                    it.copy(
                        isUpdating = false,
                        updateError = result.exceptionOrNull()?.message ?: "Update failed"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(updateSuccess = false, updateError = null) }
    }
}