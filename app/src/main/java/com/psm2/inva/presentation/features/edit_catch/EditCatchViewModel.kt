package com.psm2.inva.presentation.features.edit_catch

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.psm2.inva.data.FishRepository
import com.psm2.inva.data.SightingRepository
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

class EditCatchViewModel(
    private val sightingId: String,
    private val repository: SightingRepository,
    private val fishRepository: FishRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditCatchState(sightingId = sightingId))
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Load Sighting
            _state.update { it.copy(isLoading = true) }
            val sighting = repository.getSightingById(sightingId)

            // Load Species Suggestions
            launch {
                fishRepository.getAllFish().collect { fishList ->
                    _state.update { it.copy(speciesSuggestions = fishList) }
                }
            }

            if (sighting != null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        sighting = sighting,
                        speciesName = sighting.speciesName,
                        category = sighting.category,
                        location = LatLng(sighting.location.latitude, sighting.location.longitude),
                        locationDescription = sighting.locationDescription,
                        dateMillis = sighting.date.toDate().time,
                        locationPrivacy = sighting.locationPrivacy,
                        actionTaken = sighting.actionTaken,
                        baitUsed = sighting.baitUsed,
                        quantity = sighting.quantity.toString(),
                        length = if (sighting.length > 0) sighting.length.toString() else "",
                        weight = if (sighting.weight > 0) sighting.weight.toString() else "",
                        existingImageUrls = sighting.imageUrls
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, updateError = "Catch record not found.") }
            }
        }
    }

    // --- Updates ---

    fun updateSpeciesName(name: String) {
        val matchingFish = _state.value.speciesSuggestions.find {
            it.name.equals(name, ignoreCase = true)
        }
        val category = matchingFish?.type ?: _state.value.category
        _state.update { it.copy(speciesName = name, category = category) }
    }

    fun updateLocation(latLng: LatLng, context: Context, explicitName: String? = null) {
        _state.update { it.copy(location = latLng) }
        if (explicitName != null) {
            _state.update { it.copy(locationDescription = explicitName) }
        } else {
            getAddressFromLocation(latLng, context)
        }
    }

    private fun getAddressFromLocation(latLng: LatLng, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val name = address.featureName ?: address.locality ?: "Unknown Location"
                    withContext(Dispatchers.Main) {
                        _state.update { it.copy(locationDescription = name) }
                    }
                }
            } catch (e: Exception) {
                Log.e("EditCatchVM", "Geocoding error", e)
            }
        }
    }

    fun updateLocationDescription(desc: String) { _state.update { it.copy(locationDescription = desc) } }
    fun updateActionTaken(action: String) { _state.update { it.copy(actionTaken = action) } }
    fun updatePrivacy(privacy: String) { _state.update { it.copy(locationPrivacy = privacy) } }
    fun updateBait(bait: String) { _state.update { it.copy(baitUsed = bait) } }
    fun updateQuantity(qty: String) { if (qty.all { it.isDigit() }) _state.update { it.copy(quantity = qty) } }

    fun updateLength(len: String) {
        if (len.count { it == '.' } <= 1 && len.replace(".", "").all { it.isDigit() }) {
            _state.update { it.copy(length = len) }
        }
    }

    fun updateWeight(wgt: String) {
        if (wgt.count { it == '.' } <= 1 && wgt.replace(".", "").all { it.isDigit() }) {
            _state.update { it.copy(weight = wgt) }
        }
    }

    fun updateDate(millis: Long) {
        val current = Calendar.getInstance().apply { timeInMillis = _state.value.dateMillis }
        val newDate = Calendar.getInstance().apply { timeInMillis = millis }
        current.set(Calendar.YEAR, newDate.get(Calendar.YEAR))
        current.set(Calendar.MONTH, newDate.get(Calendar.MONTH))
        current.set(Calendar.DAY_OF_MONTH, newDate.get(Calendar.DAY_OF_MONTH))
        _state.update { it.copy(dateMillis = current.timeInMillis) }
    }

    fun updateTime(hour: Int, minute: Int) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _state.value.dateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        _state.update { it.copy(dateMillis = cal.timeInMillis) }
    }

    // --- Image Management ---

    fun addNewImage(uri: Uri?) {
        if (uri != null && (_state.value.newImageUris.size + _state.value.existingImageUrls.size - _state.value.deletedImageUrls.size) < 5) {
            _state.update { it.copy(newImageUris = it.newImageUris + uri) }
        }
    }

    fun removeNewImage(uri: Uri) {
        _state.update { it.copy(newImageUris = it.newImageUris - uri) }
    }

    fun deleteExistingImage(url: String) {
        _state.update { it.copy(deletedImageUrls = it.deletedImageUrls + url) }
    }

    fun undoDeleteImage(url: String) {
        _state.update { it.copy(deletedImageUrls = it.deletedImageUrls - url) }
    }

    // --- Submit ---

    fun submitUpdate() {
        val s = _state.value
        if (s.sighting == null) return

        if (s.speciesName.isBlank()) {
            _state.update { it.copy(updateError = "Species name required.") }
            return
        }
        if (s.location == null) {
            _state.update { it.copy(updateError = "Location required.") }
            return
        }

        _state.update { it.copy(isUpdating = true, updateError = null) }

        viewModelScope.launch {
            try {
                // 1. Upload new images
                val newUrls = s.newImageUris.map { uri ->
                    async { repository.uploadSightingImage(uri, s.sighting.userId).getOrThrow() }
                }.awaitAll()

                // 2. Compute final image list
                val finalImages = (s.existingImageUrls - s.deletedImageUrls.toSet()) + newUrls

                if (finalImages.isEmpty()) {
                    _state.update { it.copy(isUpdating = false, updateError = "At least one photo is required.") }
                    return@launch
                }

                // 3. Create updated object
                val updatedSighting = s.sighting.copy(
                    speciesName = s.speciesName,
                    category = s.category,
                    location = GeoPoint(s.location.latitude, s.location.longitude),
                    locationDescription = s.locationDescription,
                    date = Timestamp(Date(s.dateMillis)),
                    locationPrivacy = s.locationPrivacy,
                    actionTaken = s.actionTaken,
                    baitUsed = s.baitUsed,
                    quantity = s.quantity.toIntOrNull() ?: 1,
                    length = s.length.toDoubleOrNull() ?: 0.0,
                    weight = s.weight.toDoubleOrNull() ?: 0.0,
                    imageUrls = finalImages
                )

                // 4. Send to Repo
                repository.updateSighting(updatedSighting).getOrThrow()

                _state.update { it.copy(isUpdating = false, updateSuccess = true) }

            } catch (e: Exception) {
                Log.e("EditCatchVM", "Update failed", e)
                _state.update { it.copy(isUpdating = false, updateError = e.message ?: "Update failed") }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(updateSuccess = false, updateError = null) }
    }
}