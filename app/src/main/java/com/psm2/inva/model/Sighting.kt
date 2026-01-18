package com.psm2.inva.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class Sighting(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val speciesName: String = "",
    val category: String = "",
    val imageUrls: List<String> = emptyList(),

    // Angler Stats
    val quantity: Int = 1,
    val length: Double = 0.0,
    val weight: Double = 0.0,

    // The Essentials & Context
    val actionTaken: String = "Released",
    val baitUsed: String = "",
    val locationPrivacy: String = "Share Spot",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val locationDescription: String = "", // e.g., "Tasek Lagenda"
    val locationState: String = "",       // e.g., "Pahang, MY" - New Field
    val date: Timestamp = Timestamp.now()
) {
    val imageUrl: String
        get() = imageUrls.firstOrNull() ?: ""
}