package com.assisment.newschatprofileapp.presentation.profile

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.assisment.newschatprofileapp.domain.model.Location
import com.assisment.newschatprofileapp.domain.model.UserProfile
import com.assisment.newschatprofileapp.ui.theme.NewsChatAppTheme
import com.assisment.newschatprofileapp.utils.ThemePreference
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = hiltViewModel()

    ProfileContent()
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileContent() {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = hiltViewModel()
    val profileState by viewModel.profileState.collectAsState()
    val permissionsState by viewModel.permissionsState.collectAsState()
    val themePreference by viewModel.themePreference.collectAsState()

    // Permission states
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val storagePermissionState = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Activity result launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            viewModel.onEvent(ProfileEvent.SaveProfileImage(it))
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch {
                val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val src = ImageDecoder.createSource(context.contentResolver, selectedUri)
                            ImageDecoder.decodeBitmap(src)
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(context.contentResolver, selectedUri)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                bitmap?.let {
                    viewModel.onEvent(ProfileEvent.SaveProfileImage(it))
                }
            }
        }
    }

    // Check permissions
    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileEvent.CheckPermissions(context))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (profileState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Profile Image Section
                ProfileImageSection(
                    profileImageUri = profileState.userProfile.profileImageUri,
                    onCameraClick = {
                        if (cameraPermissionState.status.isGranted) {
                            cameraLauncher.launch(null)
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                    onGalleryClick = {
                        if (storagePermissionState.status.isGranted) {
                            galleryLauncher.launch("image/*")
                        } else {
                            storagePermissionState.launchPermissionRequest()
                        }
                    }
                )

                // Personal Info Section
                PersonalInfoSection(
                    profile = profileState.userProfile,
                    onNameChange = { viewModel.onEvent(ProfileEvent.UpdateName(it)) },
                    onBioChange = { viewModel.onEvent(ProfileEvent.UpdateBio(it)) },
                    onPhoneChange = { viewModel.onEvent(ProfileEvent.UpdatePhone(it)) }
                )

                // Location Section
                LocationSection(
                    location = profileState.userProfile.location,
                    isLoading = profileState.isLoading,
                    error = profileState.locationError,
                    showLocation = profileState.showLocation,
                    onGetLocation = {
                        if (locationPermissionState.status.isGranted) {
                            viewModel.onEvent(ProfileEvent.GetCurrentLocation(context))
                        } else {
                            locationPermissionState.launchPermissionRequest()
                        }
                    },
                    onClearLocation = { viewModel.onEvent(ProfileEvent.ClearLocation) }
                )

                // Permissions Status Section
                PermissionsStatusSection(
                    permissionsState = permissionsState,
                    onRequestPermissions = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:${context.packageName}")
                        context.startActivity(intent)
                    }
                )

                // Theme Toggle Section
                ThemeToggleSection(
                    themePreference = themePreference,
                    onToggle = { viewModel.toggleTheme() }
                )
            }
        }
    }
}

@Composable
fun ProfileImageSection(
    profileImageUri: String?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (!profileImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile Placeholder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(70.dp)
                        )
                    }
                }

                FloatingActionButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCameraClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }

                Button(
                    onClick = onGalleryClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
            }
        }
    }
}

@Composable
fun PersonalInfoSection(
    profile: UserProfile,
    onNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var bio by remember { mutableStateOf(profile.bio ?: "") }
    var phone by remember { mutableStateOf(profile.phone ?: "") }

    LaunchedEffect(profile) {
        name = profile.name
        bio = profile.bio ?: ""
        phone = profile.phone ?: ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Personal Information",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    onNameChange(it)
                },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = profile.email ?: "",
                onValueChange = {},
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                enabled = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    onPhoneChange(it)
                },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = {
                    bio = it
                    onBioChange(it)
                },
                label = { Text("Bio") },
                leadingIcon = { Icon(Icons.Default.Info, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }
    }
}

@Composable
fun LocationSection(
    location: Location?,
    isLoading: Boolean,
    error: String?,
    showLocation: Boolean,
    onGetLocation: () -> Unit,
    onClearLocation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (showLocation && location != null) {
                    IconButton(onClick = onClearLocation) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
                location != null && showLocation -> {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Current Location", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Latitude: ${"%.6f".format(location.latitude)}")
                        Text("Longitude: ${"%.6f".format(location.longitude)}")

                        location.address?.let { addr ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Address: $addr")
                        }
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.LocationOff,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No location set", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onGetLocation,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.MyLocation, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Current Location")
            }
        }
    }
}

@Composable
fun PermissionsStatusSection(
    permissionsState: PermissionsState,
    onRequestPermissions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Permissions Status",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionStatusItem(
                icon = Icons.Default.CameraAlt,
                title = "Camera",
                isGranted = permissionsState.hasCameraPermission,
                description = "For profile photos"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionStatusItem(
                icon = Icons.Default.Storage,
                title = "Storage / Media",
                isGranted = permissionsState.hasStoragePermission,
                description = "For gallery photos"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionStatusItem(
                icon = Icons.Default.LocationOn,
                title = "Location",
                isGranted = permissionsState.hasLocationPermission,
                description = "For current location"
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!permissionsState.hasCameraPermission ||
                !permissionsState.hasStoragePermission ||
                !permissionsState.hasLocationPermission
            ) {
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Permissions")
                }
            }
        }
    }
}

@Composable
fun PermissionStatusItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isGranted: Boolean,
    description: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isGranted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = "Status",
            tint = if (isGranted) Color.Green else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun ThemeToggleSection(
    themePreference: ThemePreference,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (themePreference) {
                        ThemePreference.DARK -> Icons.Default.DarkMode
                        ThemePreference.LIGHT -> Icons.Default.LightMode
                        ThemePreference.SYSTEM -> Icons.Default.Settings
                    },
                    "Theme",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Theme", style = MaterialTheme.typography.titleMedium)
                    Text(
                        when (themePreference) {
                            ThemePreference.DARK -> "Dark Mode"
                            ThemePreference.LIGHT -> "Light Mode"
                            ThemePreference.SYSTEM -> "System Theme"
                        }
                    )
                }
            }

            Switch(
                checked = themePreference == ThemePreference.DARK,
                onCheckedChange = { onToggle() }
            )
        }
    }
}