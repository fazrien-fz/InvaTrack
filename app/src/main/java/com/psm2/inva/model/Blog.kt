package com.psm2.inva.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Blog(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val category: String = "", // Options: Identification, Recipe, Risk Alert, Native Spotlight
    val coverImageUrl: String = "",
    val summary: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val timestamp: Timestamp = Timestamp.now()
)