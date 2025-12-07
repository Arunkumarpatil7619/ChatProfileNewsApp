package com.assisment.newschatprofileapp.presentation.profile

import android.content.Context
import android.graphics.Bitmap

sealed class ProfileEvent {
    data class UpdateName(val name: String) : ProfileEvent()
    data class UpdateBio(val bio: String) : ProfileEvent()
    data class UpdatePhone(val phone: String) : ProfileEvent()
    data class SaveProfileImage(val bitmap: Bitmap) : ProfileEvent()
    data class GetCurrentLocation(val context: android.content.Context) : ProfileEvent()
    object ClearLocation : ProfileEvent()
    data class CheckPermissions(val context: android.content.Context) : ProfileEvent()
    object ToggleDarkMode : ProfileEvent()
    object ClearError : ProfileEvent()
    data class ShowMessage(val message: String) : ProfileEvent()
}