package com.psm2.inva.data

import com.psm2.inva.model.SignInResult
import com.psm2.inva.model.UserData

interface AuthRepository {
    suspend fun signIn(): SignInResult
    suspend fun signOut()
    fun getSignedInUser(): UserData?
    suspend fun isAdmin(userId: String): Boolean
}
