package com.psm2.inva.presentation.features.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.psm2.inva.data.AuthRepository
import com.psm2.inva.data.BlogRepository
import com.psm2.inva.data.FishRepository
import com.psm2.inva.data.SightingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val blogRepository: BlogRepository,
    private val fishRepository: FishRepository,
    private val sightingRepository: SightingRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Check admin status
            var user = authRepository.getSignedInUser()
            if (user != null) {
                val isAdmin = authRepository.isAdmin(user.userId)
                user = user.copy(isAdmin = isAdmin)
            }
            _state.update { it.copy(user = user) }

            // Fetch Blogs
            launch {
                blogRepository.getAllBlogs().collect { blogs ->
                    _state.update { it.copy(blogs = blogs) }
                }
            }

            // Fetch Fish List for Comparison Dropdowns
            launch {
                fishRepository.getAllFish().collect { list ->
                    _state.update { it.copy(fishList = list) }
                }
            }

            // Fetch Recent Invasive Sightings (Filtered by User Location)
            launch {
                val detectedState = getUserStateLocation()

                sightingRepository.getAllSightings().collect { sightings ->
                    // 1. Filter for Invasive
                    var relevantSightings = sightings.filter {
                        it.category.equals("Invasive", ignoreCase = true)
                    }

                    // 2. Filter by State if detected
                    if (!detectedState.isNullOrBlank()) {
                        val stateFiltered = relevantSightings.filter {
                            it.locationState.contains(detectedState, ignoreCase = true)
                        }
                        // Only apply filter if we actually have results, otherwise show all invasive (fallback)
                        if (stateFiltered.isNotEmpty()) {
                            relevantSightings = stateFiltered
                        }
                    }

                    // 3. Sort by date and take top 5
                    val recent = relevantSightings
                        .sortedByDescending { it.date }
                        .take(5)

                    _state.update {
                        it.copy(
                            recentInvasiveSightings = recent,
                            userLocationState = detectedState
                        )
                    }
                }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun getUserStateLocation(): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    val location = suspendCancellableCoroutine { cont ->
                        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                            cont.resume(loc)
                        }.addOnFailureListener {
                            cont.resume(null)
                        }
                    }

                    if (location != null) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            // Return the state name (e.g., "Selangor", "Pahang")
                            return@withContext addresses[0].adminArea
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to get user location state", e)
            }
            return@withContext null
        }
    }
}