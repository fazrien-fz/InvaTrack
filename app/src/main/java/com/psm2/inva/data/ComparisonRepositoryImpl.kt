package com.psm2.inva.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.psm2.inva.model.FishComparison
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ComparisonRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ComparisonRepository {

    private val collection = firestore.collection("fish_comparisons")

    override fun getAllComparisonData(): Flow<List<FishComparison>> {
        return collection.snapshots().map { it.toObjects<FishComparison>() }
    }

    override suspend fun getComparisonDataById(id: String): FishComparison? {
        return try {
            collection.document(id).get().await().toObject(FishComparison::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addComparisonData(
        data: FishComparison,
        headUri: Uri,
        bodyUri: Uri,
        tailUri: Uri
    ): Result<Unit> = try {
        coroutineScope {
            val docId = collection.document().id

            // Upload all 3 images in parallel
            val headUrlTask = async { uploadImage(headUri, "comparisons/$docId/head.jpg") }
            val bodyUrlTask = async { uploadImage(bodyUri, "comparisons/$docId/body.jpg") }
            val tailUrlTask = async { uploadImage(tailUri, "comparisons/$docId/tail.jpg") }

            val finalData = data.copy(
                id = docId,
                headImageUrl = headUrlTask.await(),
                bodyImageUrl = bodyUrlTask.await(),
                tailImageUrl = tailUrlTask.await()
            )

            collection.document(docId).set(finalData).await()
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun uploadImage(uri: Uri, path: String): String {
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun deleteComparisonData(id: String): Result<Unit> = try {
        collection.document(id).delete().await()
        // Storage cleanup could be added here
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}