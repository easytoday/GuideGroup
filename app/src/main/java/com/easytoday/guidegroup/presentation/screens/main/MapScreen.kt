package com.easytoday.guidegroup.presentation.screens.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.presentation.viewmodel.ChatViewModel
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
    viewModel: MapViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(groupId) {
        if (groupId != null) {
            viewModel.setGroupId(groupId)
            chatViewModel.setGroupId(groupId)
        }
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val userRealtimeLocation by viewModel.userRealtimeLocation.collectAsState()
    val memberLocations by viewModel.memberLocations.collectAsState()
    val pointsOfInterest by viewModel.pointsOfInterest.collectAsState()
    val currentGroup by viewModel.currentGroup.collectAsState()
    val addPoiState by viewModel.addPoiState.collectAsState()
    var isTrackingLocation by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            Toast.makeText(context, "Permission accordée.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission refusée.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(addPoiState) {
        if (addPoiState is Result.Success) {
            val poiId = (addPoiState as Result.Success<String>).data
            val poiName = viewModel.pointsOfInterest.value.find { it.id == poiId }?.name ?: "Nouveau lieu"

            val result = snackbarHostState.showSnackbar(
                message = "$poiName a été ajouté",
                actionLabel = "Partager",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                chatViewModel.sharePoiInChat(poiId, poiName, currentUser, groupId)
                Toast.makeText(context, "Point d'intérêt partagé dans le chat !", Toast.LENGTH_SHORT).show()
            }
            viewModel.resetAddPoiState()
        }
    }

    MapScreenContent(
        currentUser = currentUser,
        currentGroup = currentGroup,
        userRealtimeLocation = userRealtimeLocation,
        memberLocations = memberLocations,
        pointsOfInterest = pointsOfInterest,
        isTracking = isTrackingLocation,
        snackbarHostState = snackbarHostState,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenContent(
    currentUser: User?,
    currentGroup: com.easytoday.guidegroup.domain.model.Group?,
    userRealtimeLocation: android.location.Location?,
    memberLocations: List<DomainLocation>,
    pointsOfInterest: List<PointOfInterest>,
    isTracking: Boolean,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onAddPoi: (name: String, description: String, latitude: Double, longitude: Double) -> Unit,
    onToggleTracking: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(48.8566, 2.3522), 10f)
    }
    val coroutineScope = rememberCoroutineScope()

    var showAddPoiDialog by remember { mutableStateOf(false) }
    var isInAddPoiMode by remember { mutableStateOf(false) }
    var hasUserInteractedWithMap by remember { mutableStateOf(false) }

    LaunchedEffect(userRealtimeLocation) {
        if (!hasUserInteractedWithMap) {
            userRealtimeLocation?.let {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
            }
        }
    }

    // CORRECTION : Détecter l'interaction utilisateur en observant l'état de la caméra
    if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
        hasUserInteractedWithMap = true
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
            AnimatedVisibility(visible = !isInAddPoiMode) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FloatingActionButton(onClick = {
                        hasUserInteractedWithMap = false
                        userRealtimeLocation?.let {
                            coroutineScope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
                            }
                        }
                    }) { Icon(Icons.Default.MyLocation, contentDescription = "Recentrer") }

                    FloatingActionButton(
                        onClick = onToggleTracking,
                        containerColor = if (isTracking) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) { Icon(if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = "Suivi") }

                    FloatingActionButton(onClick = { isInAddPoiMode = true }) {
                        Icon(Icons.Default.AddLocation, contentDescription = "Ajouter un point d'intérêt")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                // CORRECTION : Activer les boutons de zoom natifs
                uiSettings = MapUiSettings(zoomControlsEnabled = false),
                onMapLongClick = { latLng ->
                    if (!isInAddPoiMode) {
                        hasUserInteractedWithMap = true
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng), 500)
                        }
                        isInAddPoiMode = true
                    }
                }
            ) {
                userRealtimeLocation?.let {
                    Marker(state = MarkerState(position = LatLng(it.latitude, it.longitude)), title = currentUser?.username ?: "Ma Position", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                }
                memberLocations.forEach { memberLoc ->
                    Marker(state = MarkerState(position = LatLng(memberLoc.latitude, memberLoc.longitude)), title = "Membre ${memberLoc.userId}")
                }
                pointsOfInterest.forEach { poi ->
                    Marker(state = MarkerState(position = LatLng(poi.latitude, poi.longitude)), title = poi.name, snippet = poi.description, icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                }
            }

            if (isInAddPoiMode) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Marqueur central",
                    modifier = Modifier.align(Alignment.Center).size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(onClick = { isInAddPoiMode = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("Annuler") }
                    Button(onClick = { showAddPoiDialog = true; isInAddPoiMode = false }) { Text("Confirmer l'emplacement") }
                }
            }
        }

        if (showAddPoiDialog) {
            AddPoiDialog(
                onDismiss = { showAddPoiDialog = false },
                onConfirm = { name, description ->
                    val poiLatLng = cameraPositionState.position.target
                    onAddPoi(name, description, poiLatLng.latitude, poiLatLng.longitude)
                    showAddPoiDialog = false
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
        snackbarHostState = remember { SnackbarHostState() },
        onNavigateBack = {},
        onAddPoi = { _, _, _, _ -> },
        onToggleTracking = {}
    )
}