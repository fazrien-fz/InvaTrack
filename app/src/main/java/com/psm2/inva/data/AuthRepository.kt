package com.psm2.inva.data

import android.net.Uri
import com.psm2.inva.model.SignInResult
import com.psm2.inva.model.UserData

interface AuthRepository {
    suspend fun signIn(): SignInResult
    suspend fun signOut()
    fun getSignedInUser(): UserData?
    suspend fun isAdmin(userId: String): Boolean

    // New method for updating profile
    suspend fun updateUserProfile(userData: UserData, imageUri: Uri?): Result<Unit>

    // New method to get full user details from Firestore
    suspend fun getUserProfile(userId: String): UserData?
}