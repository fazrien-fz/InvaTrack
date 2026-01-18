package com.psm2.inva.presentation.features.report

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.psm2.inva.data.AuthRepository
import com.psm2.inva.data.FishRepository
import com.psm2.inva.data.SightingRepository
import com.psm2.inva.model.Sighting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReportViewModel(
    private val repository: SightingRepository,
    private val fishRepository: FishRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ReportState())
    val state = _state.asStateFlow()

    init {
        loadSpeciesSuggestions()
    }

    private fun loadSpeciesSuggestions() {
        viewModelScope.launch {
            fishRepository.getAllFish().collect { fishList ->
                _state.update { it.copy(speciesSuggestions = fishList) }
            }
        }
    }

    // --- Field Updates ---
    fun updateSpeciesName(name: String) {
        val matchingFish = _state.value.speciesSuggestions.find {
            it.name.equals(name, ignoreCase = true)
        }
        val category = matchingFish?.type ?: "Unknown"
        _state.update { it.copy(speciesName = name, category = category) }
    }

    fun addImageUri(uri: Uri?) {
        if (uri == null) return
        _state.update {
            if (it.imageUris.size < 3) {
                it.copy(imageUris = it.imageUris + uri)
            } else {
                it.copy(submissionError = "You can only select up to 3 images.")
            }
        }
    }

    fun removeImageUri(uri: Uri) {
        _state.update { it.copy(imageUris = it.imageUris - uri) }
    }

    fun updateQuantity(quantity: String) {
        if (quantity.all { it.isDigit() }) {
            _state.update { it.copy(quantity = quantity) }
        }
    }

    fun updateLength(length: String) {
        if (length.count { it == '.' } <= 1 && length.replace(".", "").all { it.isDigit() }) {
            _state.update { it.copy(length = length) }
        }
    }

    fun updateWeight(weight: String) {
        if (weight.count { it == '.' } <= 1 && weight.replace(".", "").all { it.isDigit() }) {
            _state.update { it.copy(weight = weight) }
        }
    }

    fun updateBait(bait: String) {
        _state.update { it.copy(baitUsed = bait) }
    }

    fun updateActionTaken(action: String) {
        _state.update { it.copy(actionTaken = action) }
    }

    fun updateLocationPrivacy(privacy: String) {
        _state.update { it.copy(locationPrivacy = privacy) }
    }

    fun updateLocationDescription(description: String) {
        _state.update { it.copy(locationDescription = description) }
    }

    fun updateLocation(latLng: LatLng, context: Context, explicitName: String? = null) {
        _state.update { it.copy(location = latLng) }
        // Always fetch address details to separate place name from state info
        getAddressFromLocation(latLng, context, explicitName)
    }

    fun updateDate(dateMillis: Long) {
        val currentCalendar = Calendar.getInstance().apply {
            timeInMillis = _state.value.selectedDateMillis
        }
        val newDateCalendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
        }
        currentCalendar.set(Calendar.YEAR, newDateCalendar.get(Calendar.YEAR))
        currentCalendar.set(Calendar.MONTH, newDateCalendar.get(Calendar.MONTH))
        currentCalendar.set(Calendar.DAY_OF_MONTH, newDateCalendar.get(Calendar.DAY_OF_MONTH))
        _state.update { it.copy(selectedDateMillis = currentCalendar.timeInMillis) }
    }

    fun updateTime(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = _state.value.selectedDateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        _state.update { it.copy(selectedDateMillis = calendar.timeInMillis) }
    }

    @SuppressLint("MissingPermission")
    fun getLocation(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    updateLocation(latLng, context)
                }
            }
        } else {
            _state.update { it.copy(submissionError = "Location permission not granted.") }
        }
    }

    private fun getAddressFromLocation(latLng: LatLng, context: Context, explicitName: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]

                    // 1. Determine Location Description (User facing name)
                    // Priority: Explicit Name (POI) -> Feature Name -> Locality -> "Unknown"
                    val featureName = explicitName ?: address.featureName
                    val locality = address.locality // City
                    val subAdminArea = address.subAdminArea // District

                    val descriptionParts = mutableListOf<String>()

                    if (!featureName.isNullOrBlank() && featureName != locality && featureName != subAdminArea) {
                        descriptionParts.add(featureName)
                    } else if (!locality.isNullOrBlank()) {
                        descriptionParts.add(locality)
                    } else if (!subAdminArea.isNullOrBlank()) {
                        descriptionParts.add(subAdminArea)
                    } else {
                        descriptionParts.add("Dropped Pin")
                    }

                    val finalDescription = descriptionParts.joinToString(", ")

                    // 2. Determine Location State (Administrative Info)
                    // Format: "State, CountryCode" (e.g., "Pahang, MY")
                    val adminArea = address.adminArea // State
                    val countryCode = address.countryCode ?: "MY"

                    val stateInfo = if (!adminArea.isNullOrBlank()) {
                        "$adminArea, $countryCode"
                    } else {
                        countryCode
                    }

                    withContext(Dispatchers.Main) {
                        _state.update {
                            it.copy(
                                locationDescription = finalDescription,
                                locationState = stateInfo
                            )
                        }
                    }
                } else if (explicitName != null) {
                    // Fallback: Use explicit name, unknown state
                    withContext(Dispatchers.Main) {
                        _state.update {
                            it.copy(
                                locationDescription = explicitName,
                                locationState = ""
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Geocoding failed", e)
                if (explicitName != null) {
                    withContext(Dispatchers.Main) {
                        _state.update { it.copy(locationDescription = explicitName) }
                    }
                }
            }
        }
    }

    fun submitReport() {
        val currentState = _state.value
        val user = authRepository.getSignedInUser()

        if (user == null) {
            _state.update { it.copy(submissionError = "User not signed in.") }
            return
        }
        if (currentState.imageUris.isEmpty()) {
            _state.update { it.copy(submissionError = "Photo evidence is required.") }
            return
        }
        if (currentState.speciesName.isBlank()) {
            _state.update { it.copy(submissionError = "Species name is required.") }
            return
        }
        if (currentState.location == null) {
            _state.update { it.copy(submissionError = "Location is required.") }
            return
        }

        val qty = currentState.quantity.toIntOrNull()
        if (qty == null || qty < 1) {
            _state.update { it.copy(submissionError = "Quantity must be at least 1.") }
            return
        }

        _state.update { it.copy(isSubmitting = true, submissionError = null) }

        viewModelScope.launch {
            try {
                val uploadedUrls = currentState.imageUris.map { uri ->
                    async { repository.uploadSightingImage(uri, user.userId).getOrThrow() }
                }.awaitAll()

                val sighting = Sighting(
                    userId = user.userId,
                    username = user.username ?: "Anonymous",
                    speciesName = currentState.speciesName,
                    category = currentState.category,
                    imageUrls = uploadedUrls,
                    quantity = qty,
                    length = currentState.length.toDoubleOrNull() ?: 0.0,
                    weight = currentState.weight.toDoubleOrNull() ?: 0.0,
                    actionTaken = currentState.actionTaken,
                    baitUsed = currentState.baitUsed,
                    locationPrivacy = currentState.locationPrivacy,
                    location = GeoPoint(currentState.location.latitude, currentState.location.longitude),

                    // Save separated fields
                    locationDescription = currentState.locationDescription,
                    locationState = currentState.locationState,

                    date = Timestamp(Date(currentState.selectedDateMillis))
                )

                repository.submitSighting(sighting).getOrThrow()

                _state.update {
                    it.copy(
                        isSubmitting = false,
                        submissionSuccess = true,
                        speciesName = "",
                        category = "",
                        imageUris = emptyList(),
                        quantity = "1",
                        length = "",
                        weight = "",
                        baitUsed = "",
                        actionTaken = "Released",
                        locationPrivacy = "Share Spot",
                        locationDescription = "",
                        locationState = "",
                        selectedDateMillis = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Failed to submit report", e)
                _state.update {
                    it.copy(isSubmitting = false, submissionError = e.message ?: "An unknown error occurred.")
                }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(submissionSuccess = false, submissionError = null) }
    }
}