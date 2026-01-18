package com.psm2.inva.model

data class UserData(
    val userId: String = "",
    val username: String? = null,
    val profilePictureUrl: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val isAdmin: Boolean = false //Admin user
)