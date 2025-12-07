package com.assisment.newschatprofileapp.domain.model





data class Message(
    val id: Int = 0,
    val text: String? = null,
    val imageUri: String? = null,
    val isSentByMe: Boolean,
    val timestamp: Long,
    val dateGroup: String
)