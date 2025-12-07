package com.assisment.newschatprofileapp.presentation.profile

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assisment.newschatprofileapp.data.repository.ProfileRepository
import com.assisment.newschatprofileapp.domain.model.UserProfile
import com.assisment.newschatprofileapp.domain.usecase.GetCurrentLocationUseCase
import com.assisment.newschatprofileapp.domain.usecase.GetUserProfileUseCase
import com.assisment.newschatprofileapp.domain.usecase.SaveProfileImageUseCase
import com.assisment.newschatprofileapp.domain.usecase.UpdateProfileUseCase
import com.assisment.newschatprofileapp.utils.Resource
import com.assisment.newschatprofileapp.utils.ThemeManager
import com.assisment.newschatprofileapp.utils.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val saveProfileImageUseCase: SaveProfileImageUseCase,
    private val profileRepository: ProfileRepository,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _permissionsState = MutableStateFlow(PermissionsState())
    val permissionsState: StateFlow<PermissionsState> = _permissionsState.asStateFlow()

    private val _themePreference = MutableStateFlow(ThemePreference.SYSTEM)
    val themePreference: StateFlow<ThemePreference> = _themePreference.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        loadUserProfile()
        loadThemePreference()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.UpdateName -> updateName(event.name)
            is ProfileEvent.UpdateBio -> updateBio(event.bio)
            is ProfileEvent.UpdatePhone -> updatePhone(event.phone)
            is ProfileEvent.SaveProfileImage -> saveProfileImage(event.bitmap)
            is ProfileEvent.GetCurrentLocation -> getCurrentLocation(event.context)
            ProfileEvent.ClearLocation -> clearLocation()
            is ProfileEvent.CheckPermissions -> checkPermissions(event.context)
            ProfileEvent.ToggleDarkMode -> toggleTheme()
            ProfileEvent.ClearError -> clearError()
            is ProfileEvent.ShowMessage -> {}
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                getUserProfileUseCase()
                    .distinctUntilChanged()
                    .collectLatest { profile ->
                        _profileState.update { current ->
                            current.copy(userProfile = profile)
                        }
                    }
            } catch (t: Throwable) {
                _profileState.update { it.copy(error = "Failed to load profile: ${t.message}") }
            }
        }
    }

    private fun updateName(name: String) {
        viewModelScope.launch {
            try {
                val updatedProfile = _profileState.value.userProfile.copy(name = name)
                _profileState.update { it.copy(userProfile = updatedProfile) }
                updateProfileUseCase(updatedProfile)
            } catch (t: Throwable) {
                _profileState.update { it.copy(error = "Failed to update name: ${t.message}") }
            }
        }
    }

    private fun updateBio(bio: String) {
        viewModelScope.launch {
            try {
                val updatedProfile = _profileState.value.userProfile.copy(bio = bio)
                _profileState.update { it.copy(userProfile = updatedProfile) }
                updateProfileUseCase(updatedProfile)
            } catch (t: Throwable) {
                _profileState.update { it.copy(error = "Failed to update bio: ${t.message}") }
            }
        }
    }

    private fun updatePhone(phone: String) {
        viewModelScope.launch {
            try {
                val updatedProfile = _profileState.value.userProfile.copy(phone = phone)
                _profileState.update { it.copy(userProfile = updatedProfile) }
                updateProfileUseCase(updatedProfile)
            } catch (t: Throwable) {
                _profileState.update { it.copy(error = "Failed to update phone: ${t.message}") }
            }
        }
    }

    private fun saveProfileImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, error = null) }

            try {
                val imagePath = saveProfileImageUseCase(bitmap)
                if (!imagePath.isNullOrEmpty()) {
                    val updatedProfile = _profileState.value.userProfile.copy(profileImageUri = imagePath)
                    _profileState.update {
                        it.copy(isLoading = false, userProfile = updatedProfile, imageSaved = true)
                    }
                    updateProfileUseCase(updatedProfile)
                    _events.emit(ProfileEvent.ShowMessage("Profile image saved successfully"))
                } else {
                    _profileState.update { it.copy(isLoading = false, error = "Failed to save image") }
                }
            } catch (t: Throwable) {
                _profileState.update { it.copy(isLoading = false, error = "Failed to save image: ${t.message}") }
            }
        }
    }

    private fun getCurrentLocation(context: Context) {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, locationError = null, error = null) }

            try {
                getCurrentLocationUseCase(context)
                    .collectLatest { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                _profileState.update { it.copy(isLoading = true) }
                            }
                            is Resource.Success -> {
                                val newLoc = resource.data
                                val updatedProfile = _profileState.value.userProfile.copy(location = newLoc)
                                _profileState.update {
                                    it.copy(isLoading = false, userProfile = updatedProfile, showLocation = true)
                                }
                                updateProfileUseCase(updatedProfile)
                                _events.emit(ProfileEvent.ShowMessage("Location updated successfully"))
                            }
                            is Resource.Error -> {
                                _profileState.update { it.copy(isLoading = false, locationError = resource.message) }
                            }
                        }
                    }
            } catch (t: Throwable) {
                _profileState.update { it.copy(isLoading = false, locationError = t.message ?: "Failed to get location") }
            }
        }
    }

    private fun checkPermissions(context: Context) {
        viewModelScope.launch {
            try {
                val cameraPermission = profileRepository.hasPermission(context, android.Manifest.permission.CAMERA)
                val storagePermission = profileRepository.hasPermission(
                    context,
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                )
                val locationPermission = profileRepository.hasPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)

                _permissionsState.update {
                    PermissionsState(
                        hasCameraPermission = cameraPermission,
                        hasStoragePermission = storagePermission,
                        hasLocationPermission = locationPermission
                    )
                }
            } catch (t: Throwable) {
                _permissionsState.update { it }
                _profileState.update { it.copy(error = "Permission check failed: ${t.message}") }
            }
        }
    }

    private fun loadThemePreference() {
        viewModelScope.launch {
            themeManager.themePreference.collect { preference ->
                _themePreference.value = preference
                // Update profile state as well for compatibility
                _profileState.update { it.copy(
                    isDarkMode = when (preference) {
                        ThemePreference.DARK -> true
                        else -> false
                    }
                )}
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val current = _themePreference.value
            val newPreference = when (current) {
                ThemePreference.LIGHT -> ThemePreference.DARK
                ThemePreference.DARK -> ThemePreference.LIGHT
                ThemePreference.SYSTEM -> ThemePreference.DARK
            }
            themeManager.setThemePreference(newPreference)
            _events.emit(ProfileEvent.ShowMessage(
                when (newPreference) {
                    ThemePreference.DARK -> "Dark mode enabled"
                    ThemePreference.LIGHT -> "Light mode enabled"
                    ThemePreference.SYSTEM -> "System theme enabled"
                }
            ))
        }
    }

    fun setThemePreference(preference: ThemePreference) {
        viewModelScope.launch {
            themeManager.setThemePreference(preference)
        }
    }

    private fun clearLocation() {
        viewModelScope.launch {
            try {
                val updatedProfile = _profileState.value.userProfile.copy(location = null)
                updateProfileUseCase(updatedProfile)
                _profileState.update { it.copy(userProfile = updatedProfile, showLocation = false) }
                _events.emit(ProfileEvent.ShowMessage("Location cleared"))
            } catch (t: Throwable) {
                _profileState.update { it.copy(error = "Failed to clear location: ${t.message}") }
            }
        }
    }

    private fun clearError() {
        _profileState.update { it.copy(error = null, locationError = null) }
    }
}

data class ProfileState(
    val userProfile: UserProfile = UserProfile(),
    val isLoading: Boolean = false,
    val locationError: String? = null,
    val showLocation: Boolean = false,
    val isDarkMode: Boolean = false,
    val imageSaved: Boolean = false,
    val error: String? = null
)

data class PermissionsState(
    val hasCameraPermission: Boolean = false,
    val hasStoragePermission: Boolean = false,
    val hasLocationPermission: Boolean = false
)

