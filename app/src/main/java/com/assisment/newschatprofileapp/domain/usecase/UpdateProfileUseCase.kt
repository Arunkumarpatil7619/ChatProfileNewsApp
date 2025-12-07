package com.assisment.newschatprofileapp.domain.usecase




import com.assisment.newschatprofileapp.data.repository.ProfileRepository
import com.assisment.newschatprofileapp.domain.model.UserProfile
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile) {
        repository.updateProfile(profile)
    }
}