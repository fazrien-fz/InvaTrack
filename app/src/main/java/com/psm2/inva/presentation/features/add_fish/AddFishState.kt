package com.psm2.inva.presentation.features.add_fish

import android.net.Uri

data class AddFishState(
    val name: String = "",
    val localName: String = "",
    val scientificName: String = "",
    val type: String = "Invasive",
    val description: String = "",
    val habitat: String = "",
    val diet: String = "",
    val origin: String = "",
    val impact: String = "",
    val conservationStatus: String = "(NE) Not Evaluated", // Updated Default
    val imageUri: Uri? = null,

    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean = false,
    val submissionError: String? = null
)