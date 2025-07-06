package com.easytoday.guidegroup.presentation.screens.main

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.easytoday.guidegroup.domain.model.GeofenceArea
import com.easytoday.guidegroup.domain.model.Location as DomainLocation
import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.presentation.navigation.Screen
import com.easytoday.guidegroup.presentation.screens.main.components.SetGeofenceDialog
import com.easytoday.guidegroup.presentation.viewmodel.MapViewModel
import com.easytoday.guidegroup.service.LocationTrackingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun MapScreen(
    navController: NavController,
    groupId: String?,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(groupId) {
        if (groupId != null) {
            viewModel.setGroupId(groupId)
        }
    }

    val addPoiState by viewModel.addPoiState.collectAsState()
    val addGeofenceState by viewModel.addGeofenceState.collectAsState()
    val focusEvent by viewModel.focusEvent.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()

    LaunchedEffect(addPoiState) {
        if (addPoiState is Result.Success) {
            val poiId = (addPoiState as Result.Success<String>).data
            val poi = viewModel.pointsOfInterest.value.find { it.id == poiId }
            if (poi != null && groupId != null) {
                val result = snackbarHostState.showSnackbar(
                    message = "${poi.name} a été ajouté",
                    actionLabel = "Partager",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    navController.navigate(Screen.ChatScreen.createSharePoiRoute(groupId, poi.id, poi.name))
                }
            }
            viewModel.resetAddPoiState()
        }
    }

    LaunchedEffect(addGeofenceState) {
        when (val result = addGeofenceState) {
            is Result.Success -> {
                Toast.makeText(context, "Zone de géorepérage ajoutée", Toast.LENGTH_SHORT).show()
                viewModel.resetAddGeofenceState()
            }
            is Result.Error -> {
                Toast.makeText(context, "Erreur: ${result.message}", Toast.LENGTH_LONG).show()
                viewModel.resetAddGeofenceState()
            }
            else -> {}
        }
    }

    MapScreenContent(
        currentUser = viewModel.currentUser.collectAsState().value,
        userRealtimeLocation = viewModel.userRealtimeLocation.collectAsState().value,
        memberLocations = viewModel.memberLocations.collectAsState().value,
        pointsOfInterest = viewModel.pointsOfInterest.collectAsState().value,
        geofenceAreas = viewModel.geofenceAreas.collectAsState().value,
        isTracking = isTracking,
        focusEvent = focusEvent,
        onFocusEventConsumed = { viewModel.onFocusEventConsumed() },
        onNavigateBack = { navController.popBackStack() },
        onAddPoi = { name, desc, lat, lon ->
            viewModel.addPointOfInterest(name, desc, lat, lon)
        },
        onAddGeofence = { id, name, lat, lon, radius ->
            viewModel.addGeofence(id, name, lat, lon, radius)
        },
        onRemoveGeofence = { geofenceId ->
            viewModel.removeGeofence(geofenceId)
        },
        onToggleTracking = { trackingStatus ->
            if (trackingStatus) {
                startLocationTrackingService(context, groupId)
            } else {
                stopLocationTrackingService(context)
            }
        },
        onSharePoiClick = { poi ->
            if (groupId != null) {
                navController.navigate(Screen.ChatScreen.createSharePoiRoute(groupId, poi.id, poi.name))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenContent(
    currentUser: User?,
    userRealtimeLocation: android.location.Location?,
    memberLocations: List<DomainLocation>,
    pointsOfInterest: List<PointOfInterest>,
    geofenceAreas: List<GeofenceArea>,
    isTracking: Boolean,
    focusEvent: LatLng?,
    onFocusEventConsumed: () -> Unit,
    onNavigateBack: () -> Unit,
    onAddPoi: (name: String, description: String, latitude: Double, longitude: Double) -> Unit,
    onAddGeofence: (id: String, name: String, lat: Double, lon: Double, radius: Float) -> Unit,
    onRemoveGeofence: (geofenceId: String) -> Unit,
    onToggleTracking: (Boolean) -> Unit,
    onSharePoiClick: (PointOfInterest) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(48.8566, 2.3522), 10f)
    }
    var showAddPoiDialog by remember { mutableStateOf(false) }
    var showAddGeofenceDialog by remember { mutableStateOf(false) }
    var showRemoveGeofenceDialog by remember { mutableStateOf<GeofenceArea?>(null) }
    var newPoiLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasUserInteractedWithMap by remember { mutableStateOf(false) }

    LaunchedEffect(userRealtimeLocation) {
        if (!hasUserInteractedWithMap && userRealtimeLocation != null) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(userRealtimeLocation.latitude, userRealtimeLocation.longitude), 15f))
        }
    }

    LaunchedEffect(focusEvent) {
        if (focusEvent != null) {
            hasUserInteractedWithMap = true
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(focusEvent, 17f),
                durationMs = 1500
            )
            onFocusEventConsumed()
        }
    }

    if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
        hasUserInteractedWithMap = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carte du groupe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(onClick = { hasUserInteractedWithMap = false }) { Icon(Icons.Default.MyLocation, "Recentrer") }

                FloatingActionButton(
                    onClick = { onToggleTracking(!isTracking) },
                    containerColor = if (isTracking) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                ) { Icon(if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow, "Suivi") }

                if (currentUser?.isGuide == true) {
                    FloatingActionButton(onClick = { showAddGeofenceDialog = true }) {
                        Icon(Icons.Default.Security, "Définir une zone")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true),
                contentPadding = PaddingValues(bottom = 80.dp, end = 80.dp),
                onMapLongClick = { latLng ->
                    newPoiLocation = latLng
                    showAddPoiDialog = true
                }
            ) {
                userRealtimeLocation?.let { Marker(state = MarkerState(LatLng(it.latitude, it.longitude)), title = currentUser?.username ?: "Moi", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) }
                memberLocations.forEach { Marker(state = MarkerState(LatLng(it.latitude, it.longitude)), title = "Membre ${it.userId}") }
                pointsOfInterest.forEach { poi -> Marker(state = MarkerState(LatLng(poi.latitude, poi.longitude)), title = poi.name, snippet = poi.description, icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE), onInfoWindowClick = { onSharePoiClick(poi) }) }

                geofenceAreas.forEach { area ->
                    Circle(
                        center = LatLng(area.latitude, area.longitude),
                        radius = area.radius.toDouble(),
                        strokeColor = Color(0x809C27B0),
                        strokeWidth = 5f,
                        fillColor = Color(0x309C27B0),
                        clickable = true,
                        onClick = {
                            if (currentUser?.isGuide == true) {
                                showRemoveGeofenceDialog = area
                            }
                        }
                    )
                }
            }
        }

        if (showAddPoiDialog && newPoiLocation != null) {
            AddPoiDialog(
                onDismiss = { showAddPoiDialog = false },
                onConfirm = { name, description ->
                    onAddPoi(name, description, newPoiLocation!!.latitude, newPoiLocation!!.longitude)
                    showAddPoiDialog = false
                }
            )
        }

        if (showAddGeofenceDialog) {
            SetGeofenceDialog(
                initialLocation = cameraPositionState.position.target,
                onDismiss = { showAddGeofenceDialog = false },
                onConfirm = { id, name, lat, lon, radius ->
                    onAddGeofence(id, name, lat, lon, radius)
                    showAddGeofenceDialog = false
                }
            )
        }

        if (showRemoveGeofenceDialog != null) {
            AlertDialog(
                onDismissRequest = { showRemoveGeofenceDialog = null },
                title = { Text("Supprimer la Zone") },
                text = { Text("Voulez-vous vraiment supprimer la zone \"${showRemoveGeofenceDialog?.name}\" ?") },
                confirmButton = {
                    Button(
                        onClick = {
                            onRemoveGeofence(showRemoveGeofenceDialog!!.id)
                            showRemoveGeofenceDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Supprimer") }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveGeofenceDialog = null }) { Text("Annuler") }
                }
            )
        }
    }
}

@Composable
private fun AddPoiDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un Point d'Intérêt") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = it.isBlank() },
                    label = { Text("Nom du lieu") },
                    isError = nameError,
                    singleLine = true
                )
                if (nameError) {
                    Text("Le nom ne peut pas être vide", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optionnel)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, description)
                    } else {
                        nameError = true
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
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