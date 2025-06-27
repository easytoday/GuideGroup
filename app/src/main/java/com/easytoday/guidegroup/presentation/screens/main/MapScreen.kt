package com.easytoday.guidegroup.presentation.screens.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.easytoday.guidegroup.domain.model.Location as DomainLocation
import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.presentation.viewmodel.MapViewModel
import com.easytoday.guidegroup.service.LocationTrackingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Date

/**
 * Écran "intelligent" pour la carte.
 */
@Composable
fun MapScreen(
    navController: NavController,
    groupId: String?,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(groupId) {
        if (groupId != null) {
            viewModel.setGroupId(groupId)
        }
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val userRealtimeLocation by viewModel.userRealtimeLocation.collectAsState()
    val memberLocations by viewModel.memberLocations.collectAsState()
    val pointsOfInterest by viewModel.pointsOfInterest.collectAsState()
    val currentGroup by viewModel.currentGroup.collectAsState()
    var isTrackingLocation by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            Toast.makeText(context, "Permission accordée. Vous pouvez démarrer le suivi.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission refusée. Le suivi ne fonctionnera pas.", Toast.LENGTH_LONG).show()
        }
    }

    MapScreenContent(
        currentUser = currentUser,
        currentGroup = currentGroup,
        userRealtimeLocation = userRealtimeLocation,
        memberLocations = memberLocations,
        pointsOfInterest = pointsOfInterest,
        isTracking = isTrackingLocation,
        onNavigateBack = { navController.popBackStack() },
        onAddPoi = { name, desc, lat, lon ->
            viewModel.addPointOfInterest(name, desc, lat, lon)
        },
        onToggleTracking = {
            if (isTrackingLocation) {
                stopLocationTrackingService(context)
                isTrackingLocation = false
            } else {
                if (context.hasLocationPermission()) {
                    startLocationTrackingService(context, groupId)
                    isTrackingLocation = true
                } else {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                }
            }
        }
    )
}

/**
 * Écran d'affichage "stupide" pour la carte.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenContent(
    currentUser: User?,
    currentGroup: com.easytoday.guidegroup.domain.model.Group?,
    userRealtimeLocation: android.location.Location?,
    memberLocations: List<DomainLocation>,
    pointsOfInterest: List<PointOfInterest>,
    isTracking: Boolean,
    onNavigateBack: () -> Unit,
    onAddPoi: (name: String, description: String, latitude: Double, longitude: Double) -> Unit,
    onToggleTracking: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(48.8566, 2.3522), 10f)
    }

    // CORRECTION : Appel à `animate` dans un LaunchedEffect
    LaunchedEffect(userRealtimeLocation) {
        userRealtimeLocation?.let {
            val userLatLng = LatLng(it.latitude, it.longitude)
            // L'appel à animate() est déjà dans un scope de coroutine ici.
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentGroup?.name ?: "Carte du groupe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(onClick = {
                    userRealtimeLocation?.let {
                        // Pas besoin de `launch` ici car animate est une suspend function
                        // mais on est dans un scope de recomposition, pas une coroutine.
                        // On utilisera la `rememberCoroutineScope` pour les clics.
                        // Pour la simplicité, on peut juste déplacer la caméra sans animation.
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                    }
                }) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recentrer")
                }
                FloatingActionButton(
                    onClick = onToggleTracking,
                    containerColor = if (isTracking) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = "Suivi")
                }
                FloatingActionButton(onClick = { /* TODO: Implement Dialog for POI */ }) {
                    Icon(Icons.Default.AddLocation, contentDescription = "Ajouter un point d'intérêt")
                }
            }
        }
    ) { paddingValues ->
        GoogleMap(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            userRealtimeLocation?.let {
                Marker(
                    state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                    title = currentUser?.username ?: "Ma Position",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            memberLocations.forEach { memberLoc ->
                Marker(
                    state = MarkerState(position = LatLng(memberLoc.latitude, memberLoc.longitude)),
                    title = "Membre ${memberLoc.userId}"
                )
            }

            pointsOfInterest.forEach { poi ->
                Marker(
                    state = MarkerState(position = LatLng(poi.latitude, poi.longitude)),
                    title = poi.name,
                    snippet = poi.description,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }
        }
    }
}

private fun startLocationTrackingService(context: Context, groupId: String?) {
    if (groupId == null) return
    Intent(context, LocationTrackingService::class.java).also {
        it.action = LocationTrackingService.ACTION_START
        it.putExtra("groupId", groupId)
        context.startService(it)
    }
}

private fun stopLocationTrackingService(context: Context) {
    Intent(context, LocationTrackingService::class.java).also {
        it.action = LocationTrackingService.ACTION_STOP
        context.startService(it)
    }
}

private fun Context.hasLocationPermission(): Boolean {
    return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

@Preview(showBackground = true)
@Composable
fun PreviewMapScreenContent() {
    val fakeUser = User(id = "user1", username = "Moi")
    val fakeUserLocation = android.location.Location("preview").apply {
        latitude = 48.8584
        longitude = 2.2945
    }
    val fakeMembers = listOf(
        DomainLocation(userId = "user2", latitude = 48.8606, longitude = 2.3376, timestamp = Date()),
        DomainLocation(userId = "user3", latitude = 48.8530, longitude = 2.3499, timestamp = Date())
    )
    val fakePois = listOf(
        PointOfInterest(id = "poi1", name = "Tour Eiffel", latitude = 48.8584, longitude = 2.2945)
    )

    MapScreenContent(
        currentUser = fakeUser,
        currentGroup = com.easytoday.guidegroup.domain.model.Group(name = "Groupe de Preview"),
        userRealtimeLocation = fakeUserLocation,
        memberLocations = fakeMembers,
        pointsOfInterest = fakePois,
        isTracking = true,
        onNavigateBack = {},
        onAddPoi = { _, _, _, _ -> },
        onToggleTracking = {}
    )
}