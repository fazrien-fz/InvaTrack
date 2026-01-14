package com.psm2.inva.presentation.features.edit_blog

import android.net.Uri
import com.psm2.inva.model.Blog

data class EditBlogState(
    val blogId: String = "",
    val isLoading: Boolean = true,
    val blog: Blog? = null,

    // Form Fields
    val title: String = "",
    val category: String = "",
    val summary: String = "",
    val content: String = "",
    val imageUri: Uri? = null,
    val existingImageUrl: String = "",

    // Update Status
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val updateError: String? = null
)