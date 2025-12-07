package com.assisment.newschatprofileapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.assisment.newschatprofileapp.domain.model.Location
import com.assisment.newschatprofileapp.domain.model.UserProfile
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "profile_preferences")

class ProfileRepositoryImpl @Inject constructor(
    private val context: Context
) : ProfileRepository {

    companion object {
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_BIO_KEY = stringPreferencesKey("user_bio")
        private val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        private val PROFILE_IMAGE_URI_KEY = stringPreferencesKey("profile_image_uri")

        private val LOCATION_LAT_KEY = doublePreferencesKey("location_latitude")
        private val LOCATION_LONG_KEY = doublePreferencesKey("location_longitude")
        private val LOCATION_ADDRESS_KEY = stringPreferencesKey("location_address")

        private val THEME_PREFERENCE_KEY = booleanPreferencesKey("theme_dark_mode")
    }

    override fun getUserProfile(): Flow<UserProfile> {
        return context.dataStore.data.map { preferences ->
            UserProfile(
                name = preferences[USER_NAME_KEY] ?: "John Doe",
                email = preferences[USER_EMAIL_KEY] ?: "john.doe@example.com",
                bio = preferences[USER_BIO_KEY] ?: "Android Developer passionate about clean code and beautiful UI.",
                phone = preferences[USER_PHONE_KEY] ?: "+1 (555) 123-4567",
                profileImageUri = preferences[PROFILE_IMAGE_URI_KEY],
                location = getSavedLocationFromPreferences(preferences)
            )
        }
    }

    override suspend fun updateProfile(profile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = profile.name
            preferences[USER_EMAIL_KEY] = profile.email
            preferences[USER_BIO_KEY] = profile.bio
            preferences[USER_PHONE_KEY] = profile.phone

            profile.location?.let { location ->
                preferences[LOCATION_LAT_KEY] = location.latitude
                preferences[LOCATION_LONG_KEY] = location.longitude
                preferences[LOCATION_ADDRESS_KEY] = location.address ?: ""
            }

            profile.profileImageUri?.let { uri ->
                preferences[PROFILE_IMAGE_URI_KEY] = uri
            }
        }
    }

    override suspend fun getCurrentLocation(context: Context): Location {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val location = fusedLocationClient.lastLocation.await()

        return if (location != null) {
            // Get address from coordinates
            val address = getAddressFromCoordinates(
                context = context,
                latitude = location.latitude,
                longitude = location.longitude
            )

            Location(
                latitude = location.latitude,
                longitude = location.longitude,
                address = address
            )
        } else {
            throw Exception("Location not available")
        }
    }

    override suspend fun saveLocation(location: Location) {
        context.dataStore.edit { preferences ->
            preferences[LOCATION_LAT_KEY] = location.latitude
            preferences[LOCATION_LONG_KEY] = location.longitude
            preferences[LOCATION_ADDRESS_KEY] = location.address ?: ""
        }
    }

    override suspend fun getSavedLocation(): Location? {
        val preferences = context.dataStore.data.first()
        return getSavedLocationFromPreferences(preferences)
    }

    override suspend fun saveProfileImage(bitmap: Bitmap): String {
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }

        val filePath = file.absolutePath
        context.dataStore.edit { preferences ->
            preferences[PROFILE_IMAGE_URI_KEY] = filePath
        }

        return filePath
    }

    override suspend fun loadProfileImage(): Bitmap? {
        return try {
            val preferences = context.dataStore.data.first()
            val imagePath = preferences[PROFILE_IMAGE_URI_KEY]

            imagePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    BitmapFactory.decodeFile(path)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteProfileImage() {
        val preferences = context.dataStore.data.first()
        val imagePath = preferences[PROFILE_IMAGE_URI_KEY]

        imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }

        context.dataStore.edit { preferences ->
            preferences.remove(PROFILE_IMAGE_URI_KEY)
        }
    }

    override suspend fun hasPermission(context: Context, permission: String): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestPermission(context: Context, permission: String): Boolean {
        return hasPermission(context, permission)
    }

    override suspend fun saveThemePreference(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_PREFERENCE_KEY] = isDarkMode
        }
    }

    override suspend fun getThemePreference(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[THEME_PREFERENCE_KEY] ?: false
    }

    override suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        deleteProfileImage()
    }

    private fun getSavedLocationFromPreferences(preferences: Preferences): Location? {
        val latitude = preferences[LOCATION_LAT_KEY]
        val longitude = preferences[LOCATION_LONG_KEY]

        return if (latitude != null && longitude != null) {
            Location(
                latitude = latitude,
                longitude = longitude,
                address = preferences[LOCATION_ADDRESS_KEY] ?: ""
            )
        } else {
            null
        }
    }

    private suspend fun getAddressFromCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            addresses?.firstOrNull()?.let { address ->
                val addressParts = mutableListOf<String>()

                // Get the best available address lines
                for (i in 0..address.maxAddressLineIndex) {
                    address.getAddressLine(i)?.let { line ->
                        if (line.isNotBlank()) {
                            addressParts.add(line)
                        }
                    }
                }

                // If no address lines, build manually
                if (addressParts.isEmpty()) {
                    // Try to build a readable address
                    val thoroughfare = address.thoroughfare
                    val subThoroughfare = address.subThoroughfare
                    val locality = address.locality
                    val adminArea = address.adminArea
                    val countryName = address.countryName

                    if (thoroughfare != null || subThoroughfare != null) {
                        val streetAddress = if (subThoroughfare != null && thoroughfare != null) {
                            "$subThoroughfare $thoroughfare"
                        } else {
                            thoroughfare ?: subThoroughfare ?: ""
                        }
                        if (streetAddress.isNotBlank()) {
                            addressParts.add(streetAddress)
                        }
                    }

                    if (locality != null) {
                        addressParts.add(locality)
                    }

                    if (adminArea != null && adminArea != locality) {
                        addressParts.add(adminArea)
                    }

                    if (countryName != null) {
                        addressParts.add(countryName)
                    }
                }

                // If we still don't have an address, return coordinates
                if (addressParts.isNotEmpty()) {
                    addressParts.joinToString(", ")
                } else {
                    "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
                }
            } ?: "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
        } catch (e: Exception) {
            e.printStackTrace()
            "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
        }
    }
}