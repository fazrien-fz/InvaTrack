package com.psm2.inva.presentation.features.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.psm2.inva.data.AuthRepository
import com.psm2.inva.data.SightingRepository
import com.psm2.inva.model.Sighting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val sightingRepository: SightingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    val state = _state.asStateFlow()

    init {
        loadCurrentUser()
        loadSightings()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getSignedInUser()
            _state.update { it.copy(currentUserId = user?.userId ?: "") }
        }
    }

    // Toggle Functions for each layer
    fun togglePublicSpots(show: Boolean) {
        _state.update {
            it.copy(
                showPublicSpots = show,
                filteredSightings = filterSightings(it.sightings, it.searchQuery, show, it.showMySpots, it.showInvasiveHeatmap, it.currentUserId)
            )
        }
    }

    fun toggleMySpots(show: Boolean) {
        _state.update {
            it.copy(
                showMySpots = show,
                filteredSightings = filterSightings(it.sightings, it.searchQuery, it.showPublicSpots, show, it.showInvasiveHeatmap, it.currentUserId)
            )
        }
    }

    fun toggleInvasiveHeatmap(show: Boolean) {
        _state.update {
            it.copy(
                showInvasiveHeatmap = show,
                filteredSightings = filterSightings(it.sightings, it.searchQuery, it.showPublicSpots, it.showMySpots, show, it.currentUserId)
            )
        }
    }

    fun updateMapCenter(center: LatLng) {
        // Kept empty to satisfy MapScreen callback if needed, but logic removed as alerts are gone
    }

    fun updateSearchQuery(query: String) {
        _state.update {
            it.copy(
                searchQuery = query,
                filteredSightings = filterSightings(
                    it.sightings,
                    query,
                    it.showPublicSpots,
                    it.showMySpots,
                    it.showInvasiveHeatmap,
                    it.currentUserId
                )
            )
        }
    }

    private fun filterSightings(
        sightings: List<Sighting>,
        query: String,
        showPublic: Boolean,
        showPrivate: Boolean,
        showHeatmap: Boolean,
        userId: String
    ): List<Sighting> {
        // We filter the list to include a sighting if it matches ANY of the active layers
        val baseList = sightings.filter { sighting ->
            var matchesLayer = false

            // Layer 1: Public Spots (Share Spot = "Share Spot")
            // Logic: Must be "Share Spot" privacy AND (Native OR Invasive)
            if (showPublic) {
                if (sighting.locationPrivacy == "Share Spot") {
                    matchesLayer = true
                }
            }

            // Layer 2: My Spots (Private Journey)
            // Logic: Must belong to current user
            if (showPrivate && !matchesLayer) {
                if (sighting.userId == userId) {
                    matchesLayer = true
                }
            }

            // Layer 3: Invasive Heatmap
            // Logic: Must be Invasive Category (regardless of privacy, usually aggregated, but for now we filter raw data)
            if (showHeatmap && !matchesLayer) {
                if (sighting.category.equals("Invasive", ignoreCase = true)) {
                    matchesLayer = true
                }
            }

            matchesLayer
        }

        // Apply Search Filter on top of the base list
        if (query.isBlank()) return baseList

        return baseList.filter { sighting ->
            // UPDATED: Using contains for partial match instead of equals
            sighting.speciesName.contains(query, ignoreCase = true)
        }
    }

    @SuppressLint("MissingPermission")
    fun getUserLocation() {
        viewModelScope.launch {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        _state.update { currentState ->
                            currentState.copy(
                                userLocation = userLatLng
                            )
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MapViewModel", "Failed to get location", e)
                }
        }
    }

    private fun loadSightings() {
        viewModelScope.launch {
            sightingRepository.getAllSightings()
                .catch { e ->
                    Log.e("MapViewModel", "Failed to load sightings", e)
                }
                .collect { sightings ->
                    _state.update { currentState ->
                        currentState.copy(
                            sightings = sightings,
                            filteredSightings = filterSightings(
                                sightings,
                                currentState.searchQuery,
                                currentState.showPublicSpots,
                                currentState.showMySpots,
                                currentState.showInvasiveHeatmap,
                                currentState.currentUserId
                            )
                        )
                    }
                }
        }
    }
}