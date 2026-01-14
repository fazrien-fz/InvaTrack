package com.psm2.inva.presentation.features.edit_catch

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.psm2.inva.model.Fish
import com.psm2.inva.model.Sighting

data class EditCatchState(
    val sightingId: String = "",
    val isLoading: Boolean = true,
    val sighting: Sighting? = null,

    // Form Data
    val speciesName: String = "",
    val category: String = "",
    val location: LatLng? = null,
    val locationDescription: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val locationPrivacy: String = "Share Spot",
    val actionTaken: String = "Released",
    val baitUsed: String = "",

    // Stats
    val quantity: String = "1",
    val length: String = "",
    val weight: String = "",

    // Images
    val existingImageUrls: List<String> = emptyList(),
    val newImageUris: List<Uri> = emptyList(),
    val deletedImageUrls: List<String> = emptyList(), // Track images marked for deletion

    // Suggestions
    val speciesSuggestions: List<Fish> = emptyList(),

    // Status
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val updateError: String? = null
)