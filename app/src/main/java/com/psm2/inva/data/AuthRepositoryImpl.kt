package com.psm2.inva.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.psm2.inva.auth.GoogleAuthUiClient
import com.psm2.inva.model.SignInResult
import com.psm2.inva.model.UserData
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepositoryImpl(
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AuthRepository {

    override suspend fun signIn(): SignInResult {
        return googleAuthUiClient.signIn()
    }

    override suspend fun signOut() {
        googleAuthUiClient.signOut()
    }

    override fun getSignedInUser(): UserData? {
        return googleAuthUiClient.getSignedInUser()
    }

    override suspend fun isAdmin(userId: String): Boolean {
        return try {
            val doc = firestore.collection("admins").document(userId).get().await()
            doc.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getUserProfile(userId: String): UserData? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                doc.toObject(UserData::class.java)
            } else {
                // Fallback to Auth data if Firestore doc doesn't exist yet
                getSignedInUser()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun updateUserProfile(userData: UserData, imageUri: Uri?): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No user signed in")
            var finalUserData = userData

            // 1. Upload Image if provided
            if (imageUri != null) {
                val imageRef = storage.reference.child("profile_images/${user.uid}/${UUID.randomUUID()}.jpg")
                imageRef.putFile(imageUri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()

                finalUserData = finalUserData.copy(profilePictureUrl = downloadUrl)

                // Update Firebase Auth Profile Picture as well for consistency
                val profileUpdates = userProfileChangeRequest {
                    photoUri = Uri.parse(downloadUrl)
                }
                user.updateProfile(profileUpdates).await()
            }

            // 2. Update Firebase Auth Display Name if username changed
            if (userData.username != user.displayName) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = userData.username
                }
                user.updateProfile(profileUpdates).await()
            }

            // 3. Save full details to Firestore
            firestore.collection("users").document(user.uid).set(finalUserData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}