package com.psm2.inva.model

import com.google.firebase.firestore.DocumentId

data class FishComparison(
    @DocumentId
    val id: String = "",
    val fishName: String = "",
    val status: String = "Invasive", // Invasive, Native, Alien

    // Head details
    val headDescription: String = "",
    val headImageUrl: String = "",

    // Body details
    val bodyDescription: String = "",
    val bodyImageUrl: String = "",

    // Tail details
    val tailDescription: String = "",
    val tailImageUrl: String = ""
)