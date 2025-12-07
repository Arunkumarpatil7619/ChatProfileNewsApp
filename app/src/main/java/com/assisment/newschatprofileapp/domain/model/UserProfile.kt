package com.assisment.newschatprofileapp.domain.model





data class UserProfile(
    val id: Int = 0,
    val name: String = "John Doe",
    val email: String = "john.doe@example.com",
    val profileImageUri: String? = null,
    val location: Location? = null,
    val bio: String = "Android Developer passionate about clean code and beautiful UI.",
    val phone: String = "+1 (555) 123-4567"
)

