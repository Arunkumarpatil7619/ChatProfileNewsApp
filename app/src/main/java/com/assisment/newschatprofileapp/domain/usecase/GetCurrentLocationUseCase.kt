package com.assisment.newschatprofileapp.domain.usecase

import android.content.Context
import com.assisment.newschatprofileapp.data.repository.ProfileRepository
import com.assisment.newschatprofileapp.domain.model.Location
import com.assisment.newschatprofileapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(context: Context): Flow<Resource<Location>> = flow {
        emit(Resource.Loading())
        try {
            val location = repository.getCurrentLocation(context)
            emit(Resource.Success(location))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get current location"))
        }
    }
}