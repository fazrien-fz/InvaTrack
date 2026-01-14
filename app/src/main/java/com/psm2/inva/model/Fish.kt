package com.psm2.inva.model

import com.google.firebase.firestore.DocumentId

data class Fish(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val localName: String = "",
    val scientificName: String = "",
    val type: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val habitat: String = "",
    val diet: String = "",
    val origin: String = "",
    val impact: String = "",
    val conservationStatus: String = "(NE) Not Evaluated",

    // Comparison / Morphological Details
    val headDescription: String = "",
    val headImageUrl: String = "",
    val bodyDescription: String = "",
    val bodyImageUrl: String = "",
    val tailDescription: String = "",
    val tailImageUrl: String = ""
)