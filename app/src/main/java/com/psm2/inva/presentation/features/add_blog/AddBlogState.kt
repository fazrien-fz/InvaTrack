package com.psm2.inva.presentation.features.add_blog

import android.net.Uri

data class AddBlogState(
    val title: String = "",
    val category: String = "Identification", // Default
    val summary: String = "",
    val content: String = "",
    val coverImageUri: Uri? = null,

    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean = false,
    val submissionError: String? = null
)