package com.assisment.newschatprofileapp.data.repository



import android.content.Context
import android.graphics.Bitmap
import com.assisment.newschatprofileapp.domain.model.Location
import com.assisment.newschatprofileapp.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    // User Profile
    fun getUserProfile(): Flow<UserProfile>
    suspend fun updateProfile(profile: UserProfile)

    // Location
    suspend fun getCurrentLocation(context: Context): Location
    suspend fun saveLocation(location: Location)
    suspend fun getSavedLocation(): Location?

    // Profile Image
    suspend fun saveProfileImage(bitmap: Bitmap): String
    suspend fun loadProfileImage(): Bitmap?
    suspend fun deleteProfileImage()

    // Permissions
    suspend fun hasPermission(context: Context, permission: String): Boolean
    suspend fun requestPermission(context: Context, permission: String): Boolean

    // Theme
    suspend fun saveThemePreference(isDarkMode: Boolean)
    suspend fun getThemePreference(): Boolean

    // Settings
    suspend fun clearAllData()
}