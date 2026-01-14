package com.psm2.inva.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.psm2.inva.model.Sighting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SightingRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : SightingRepository {

    override suspend fun uploadSightingImage(uri: Uri, userId: String): Result<String> {
        return try {
            val storageRef = storage.reference
            val imageRef = storageRef.child("sightings/${userId}/${UUID.randomUUID()}.jpg")

            imageRef.putFile(uri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitSighting(sighting: Sighting): Result<Unit> {
        return try {
            firestore.collection("sightings").add(sighting).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllSightings(): Flow<List<Sighting>> {
        return firestore.collection("sightings")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Sighting>()
            }
    }

    override suspend fun getSightingById(sightingId: String): Sighting? {
        return try {
            firestore.collection("sightings")
                .document(sightingId)
                .get()
                .await()
                .toObject(Sighting::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun deleteSighting(sightingId: String, imageUrls: List<String>): Result<Unit> {
        return try {
            firestore.collection("sightings").document(sightingId).delete().await()

            // Attempt to delete associated images
            imageUrls.forEach { url ->
                try {
                    val ref = storage.getReferenceFromUrl(url)
                    ref.delete().await()
                } catch (e: Exception) {
                    // Log error but continue
                    e.printStackTrace()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSighting(sighting: Sighting): Result<Unit> {
        return try {
            firestore.collection("sightings")
                .document(sighting.id)
                .set(sighting)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}