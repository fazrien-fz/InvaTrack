package com.psm2.inva.presentation.features.edit_fish

import android.net.Uri
import com.psm2.inva.model.Fish

data class EditFishState(
    val fishId: String = "",
    val isLoading: Boolean = true,
    val fish: Fish? = null,

    // Form fields
    val name: String = "",
    val localName: String = "",
    val scientificName: String = "",
    val type: String = "Invasive",
    val description: String = "",
    val habitat: String = "",
    val diet: String = "",
    val origin: String = "",
    val impact: String = "",
    val conservationStatus: String = "NE",
    val imageUri: Uri? = null,
    val existingImageUrl: String = "",

    // Comparison / Morphological Details
    val headDescription: String = "",
    val headImageUrl: String = "",
    val headUri: Uri? = null,

    val bodyDescription: String = "",
    val bodyImageUrl: String = "",
    val bodyUri: Uri? = null,

    val tailDescription: String = "",
    val tailImageUrl: String = "",
    val tailUri: Uri? = null,

    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val updateError: String? = null,

    // Delete State
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val deleteError: String? = null,
    val showDeleteConfirmation: Boolean = false
)