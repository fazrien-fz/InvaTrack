package com.psm2.inva.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.psm2.inva.model.Fish
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FishRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : FishRepository {

    override fun getAllFish(): Flow<List<Fish>> {
        return firestore.collection("fish")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Fish>()
            }
    }

    override suspend fun getFishById(fishId: String): Fish? {
        return try {
            firestore.collection("fish")
                .document(fishId)
                .get()
                .await()
                .toObject(Fish::class.java)
        } catch (e: Exception) {
            null
        }
    }


    override suspend fun addFish(fish: Fish, imageUri: Uri): Result<Unit> {
        return try {
            val fishDocRef = firestore.collection("fish").document()
            val imageRef = storage.reference.child("fish_images/${fishDocRef.id}.jpg")

            imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()

            val fishWithUrl = fish.copy(id = fishDocRef.id, imageUrl = downloadUrl)
            fishDocRef.set(fishWithUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFish(
        fish: Fish,
        newImageUri: Uri?
    ): Result<Unit> {
        return try {
            var fishToUpdate = fish
            val storageRef = storage.reference

            // Helper to upload and return URL
            suspend fun upload(uri: Uri, path: String): String {
                val ref = storageRef.child(path)
                ref.putFile(uri).await()
                return ref.downloadUrl.await().toString()
            }

            // Update Main Image
            if (newImageUri != null) {
                val url = upload(newImageUri, "fish_images/${fish.id}.jpg")
                fishToUpdate = fishToUpdate.copy(imageUrl = url)
            }

            firestore.collection("fish").document(fish.id).set(fishToUpdate).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFish(fishId: String): Result<Unit> {
        return try {
            firestore.collection("fish").document(fishId).delete().await()
            // Try to delete main image
            try {
                storage.reference.child("fish_images/${fishId}.jpg").delete().await()
            } catch (e: Exception) {}

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}