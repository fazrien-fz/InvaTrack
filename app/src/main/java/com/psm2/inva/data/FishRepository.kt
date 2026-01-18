package com.psm2.inva.data

import android.net.Uri
import com.psm2.inva.model.Fish
import kotlinx.coroutines.flow.Flow

interface FishRepository {
    fun getAllFish(): Flow<List<Fish>>
    suspend fun getFishById(fishId: String): Fish?
    suspend fun addFish(fish: Fish, imageUri: Uri): Result<Unit>

    suspend fun updateFish(
        fish: Fish,
        newImageUri: Uri?
    ): Result<Unit>

    suspend fun deleteFish(fishId: String): Result<Unit>
}