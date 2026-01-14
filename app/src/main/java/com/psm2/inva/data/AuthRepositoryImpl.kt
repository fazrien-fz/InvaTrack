package com.psm2.inva.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.psm2.inva.auth.GoogleAuthUiClient
import com.psm2.inva.model.SignInResult
import com.psm2.inva.model.UserData
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val auth: FirebaseAuth
) : AuthRepository {
    private val firestore = Firebase.firestore

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
            // Check if a document with the user's ID exists in the 'admins' collection
            val doc = firestore.collection("admins").document(userId).get().await()
            doc.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
