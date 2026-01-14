package com.psm2.inva.presentation.features.map

import com.google.android.gms.maps.model.LatLng
import com.psm2.inva.model.Sighting

data class MapState(
    val defaultLocation: LatLng = LatLng(4.2105, 101.9758), // Malaysia Location
    val defaultZoom: Float = 6f,
    val userLocationZoom: Float = 15f,
    val userLocation: LatLng? = null,
    val mapCenter: LatLng? = null, // Tracks where the map is currently looking
    val sightings: List<Sighting> = emptyList(),
    val filteredSightings: List<Sighting> = emptyList(), // Added for search results
    val searchQuery: String = "", // Added for search text
    val alerts: List<MapAlert> = emptyList(),

    // Updated Fields for 3-Layer Toggle
    val showPublicSpots: Boolean = true,      // Layer 1: All Public Shared Spots (Native & Invasive)
    val showMySpots: Boolean = false,         // Layer 2: My Personal Spots (Private)
    val showInvasiveHeatmap: Boolean = false, // Layer 3: All Invasive Spots (Heatmap style)

    val currentUserId: String = "",

    // New Field for Dropdown
    val availableSpecies: List<String> = emptyList()
)

data class MapAlert(
    val id: String,
    val title: String,
    val message: String,
    val level: AlertLevel
)

enum class AlertLevel {
    HIGH,   // Red
    MEDIUM  // Orange/Yellow
}