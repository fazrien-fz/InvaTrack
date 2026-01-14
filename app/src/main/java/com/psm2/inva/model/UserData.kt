package com.psm2.inva.model

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?,
    val isAdmin: Boolean = false //Admin user
)
