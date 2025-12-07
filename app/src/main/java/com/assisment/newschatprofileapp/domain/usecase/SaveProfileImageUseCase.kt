package com.assisment.newschatprofileapp.domain.usecase



import android.graphics.Bitmap
import com.assisment.newschatprofileapp.data.repository.ProfileRepository

import javax.inject.Inject


class SaveProfileImageUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(bitmap: Bitmap): String {  // ← MUST return String
        return repository.saveProfileImage(bitmap)
    }
}