package com.assisment.newschatprofileapp.domain.usecase

import com.assisment.newschatprofileapp.data.repository.ProfileRepository
import com.assisment.newschatprofileapp.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<UserProfile> = repository.getUserProfile()
}