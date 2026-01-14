package com.psm2.inva.data

import android.net.Uri
import com.psm2.inva.model.FishComparison
import kotlinx.coroutines.flow.Flow

interface ComparisonRepository {
    fun getAllComparisonData(): Flow<List<FishComparison>>
    suspend fun getComparisonDataById(id: String): FishComparison?
    suspend fun addComparisonData(
        data: FishComparison,
        headUri: Uri,
        bodyUri: Uri,
        tailUri: Uri
    ): Result<Unit>
    suspend fun deleteComparisonData(id: String): Result<Unit>
}