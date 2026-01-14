package com.psm2.inva.data

import android.net.Uri
import com.psm2.inva.model.Sighting
import kotlinx.coroutines.flow.Flow

interface SightingRepository {
    suspend fun uploadSightingImage(uri: Uri, userId: String): Result<String>
    suspend fun submitSighting(sighting: Sighting): Result<Unit>
    fun getAllSightings(): Flow<List<Sighting>>
    suspend fun deleteSighting(sightingId: String, imageUrls: List<String>): Result<Unit>
    suspend fun getSightingById(sightingId: String): Sighting?

    // Added for Edit Feature
    suspend fun updateSighting(sighting: Sighting): Result<Unit>
}