package com.psm2.inva.presentation.features.map

import android.annotation.SuppressLint
import android.location.Location
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
import java.util.UUID

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
        _state.update { it.copy(mapCenter = center) }
        // Generate alerts based on where the user is LOOKING (map center)
        generateAlerts(center, _state.value.sightings)
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
                                userLocation = userLatLng,
                                mapCenter = currentState.mapCenter ?: userLatLng
                            )
                        }
                        // If we just initialized the map center, generate alerts for it
                        if (_state.value.mapCenter == userLatLng) {
                            generateAlerts(userLatLng, _state.value.sightings)
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
                    // Extract unique species names
                    val speciesList = sightings.map { it.speciesName }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()

                    _state.update { currentState ->
                        currentState.copy(
                            sightings = sightings,
                            availableSpecies = speciesList, // Update available species
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
                    // Recalculate alerts using ALL public invasive sightings to ensure safety warnings persist
                    _state.value.mapCenter?.let { center ->
                        generateAlerts(center, sightings)
                    }
                }
        }
    }

    private fun generateAlerts(centerLocation: LatLng, sightings: List<Sighting>) {
        val alerts = mutableListOf<MapAlert>()
        val nearbyRadiusKm = 20.0
        val userLocation = _state.value.userLocation

        // Always base alerts on Invasive Species to keep users safe/informed
        val invasiveSightings = sightings.filter {
            it.category.equals("Invasive", ignoreCase = true)
        }

        val speciesMap = mutableMapOf<String, MutableList<Sighting>>()

        invasiveSightings.forEach { sighting ->
            val results = FloatArray(1)
            Location.distanceBetween(
                centerLocation.latitude, centerLocation.longitude,
                sighting.location.latitude, sighting.location.longitude,
                results
            )
            val distanceKm = results[0] / 1000

            if (distanceKm <= nearbyRadiusKm) {
                if (!speciesMap.containsKey(sighting.speciesName)) {
                    speciesMap[sighting.speciesName] = mutableListOf()
                }
                speciesMap[sighting.speciesName]?.add(sighting)
            }
        }

        speciesMap.forEach { (species, sightingsList) ->
            if (sightingsList.size >= 3) {
                val locationName = sightingsList.firstOrNull { it.locationDescription.isNotBlank() }?.locationDescription
                    ?: "this area"

                alerts.add(
                    MapAlert(
                        id = UUID.randomUUID().toString(),
                        title = "Population Surge Alert",
                        message = "$species population increasing in $locationName (${sightingsList.size} sightings)",
                        level = AlertLevel.HIGH
                    )
                )
            } else {
                sightingsList.forEach { sighting ->
                    val distString = if (userLocation != null) {
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            userLocation.latitude, userLocation.longitude,
                            sighting.location.latitude, sighting.location.longitude,
                            results
                        )
                        "%.1fkm from you".format(results[0] / 1000)
                    } else {
                        "in this area"
                    }

                    alerts.add(
                        MapAlert(
                            id = sighting.speciesName + sighting.date.toString(),
                            title = "Invasive Sighting Nearby",
                            message = "$species sighted $distString",
                            level = AlertLevel.MEDIUM
                        )
                    )
                }
            }
        }

        _state.update { it.copy(alerts = alerts.sortedBy { alert -> alert.level }) }
    }
}