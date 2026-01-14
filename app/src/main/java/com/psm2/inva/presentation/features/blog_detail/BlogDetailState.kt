package com.psm2.inva.presentation.features.blog_detail

import com.psm2.inva.model.Blog

data class BlogDetailState(
    val blog: Blog? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAdmin: Boolean = false,

    // Deletion states
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val deleteError: String? = null
)