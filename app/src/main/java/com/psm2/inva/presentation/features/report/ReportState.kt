package com.psm2.inva.presentation.features.report

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.psm2.inva.model.Fish

data class ReportState(
    // The Essentials
    val speciesName: String = "",
    val category: String = "",
    val imageUris: List<Uri> = emptyList(),
    val location: LatLng? = null,
    val locationDescription: String = "",
    val locationState: String = "", // New field to hold state info
    val actionTaken: String = "Released",

    // Angler Stats
    val quantity: String = "1",
    val length: String = "",
    val weight: String = "",

    // The Context
    val baitUsed: String = "",

    // Meta
    val locationPrivacy: String = "Share Spot",
    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean = false,
    val submissionError: String? = null,
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val speciesSuggestions: List<Fish> = emptyList()
)