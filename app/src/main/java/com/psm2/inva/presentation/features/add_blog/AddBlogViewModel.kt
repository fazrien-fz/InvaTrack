package com.psm2.inva.presentation.features.add_blog

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.AuthRepository
import com.psm2.inva.data.BlogRepository
import com.psm2.inva.model.Blog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddBlogViewModel(
    private val categoryName: String?, // Optional pre-fill
    private val blogRepository: BlogRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddBlogState())
    val state = _state.asStateFlow()

    init {
        // If a category was passed via navigation, set it
        if (!categoryName.isNullOrBlank()) {
            _state.update { it.copy(category = categoryName) }
        }
    }

    fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun updateCategory(category: String) {
        _state.update { it.copy(category = category) }
    }

    fun updateSummary(summary: String) {
        if (summary.length <= 150) {
            _state.update { it.copy(summary = summary) }
        }
    }

    fun updateContent(content: String) {
        _state.update { it.copy(content = content) }
    }

    fun updateCoverImage(uri: Uri?) {
        _state.update { it.copy(coverImageUri = uri) }
    }

    fun submitBlog() {
        val currentState = _state.value
        val user = authRepository.getSignedInUser()

        if (user == null) {
            _state.update { it.copy(submissionError = "You must be signed in to post.") }
            return
        }

        if (currentState.title.isBlank() || currentState.content.isBlank()) {
            _state.update { it.copy(submissionError = "Title and Content are required.") }
            return
        }

        if (currentState.coverImageUri == null) {
            _state.update { it.copy(submissionError = "Cover image is required.") }
            return
        }

        _state.update { it.copy(isSubmitting = true, submissionError = null) }

        viewModelScope.launch {
            val blog = Blog(
                title = currentState.title,
                category = currentState.category,
                summary = currentState.summary,
                content = currentState.content,
                authorId = user.userId,
                authorName = user.username ?: "Anonymous"
            )

            val result = blogRepository.addBlog(blog, currentState.coverImageUri)

            if (result.isSuccess) {
                _state.update { it.copy(isSubmitting = false, submissionSuccess = true) }
            } else {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        submissionError = result.exceptionOrNull()?.message ?: "Failed to post blog"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(submissionSuccess = false, submissionError = null) }
    }
}