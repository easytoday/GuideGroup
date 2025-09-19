package com.easytoday.guidegroup.presentation.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.model.*
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.repository.*
import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.work.*
import com.easytoday.guidegroup.data.sync.PoiSyncWorker
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationClient: LocationClient,
    private val locationRepository: LocationRepository,
    private val pointOfInterestRepository: PointOfInterestRepository,
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    private val geofenceRepository: GeofenceRepository,
    private val trackingStateRepository: TrackingStateRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val isTracking = trackingStateRepository.isTracking

    private val _focusEvent = MutableStateFlow<LatLng?>(null)
    val focusEvent: StateFlow<LatLng?> = _focusEvent.asStateFlow()

    private val _currentGroupId = MutableStateFlow(savedStateHandle.get<String>("groupId"))
    val currentGroupId: StateFlow<String?> = _currentGroupId.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup: StateFlow<Group?> = _currentGroup.asStateFlow()

    private val _userRealtimeLocation = MutableStateFlow<Location?>(null)
    val userRealtimeLocation: StateFlow<Location?> = _userRealtimeLocation.asStateFlow()

    private val _memberLocations = MutableStateFlow<List<com.easytoday.guidegroup.domain.model.Location>>(emptyList())
    val memberLocations: StateFlow<List<com.easytoday.guidegroup.domain.model.Location>> = _memberLocations.asStateFlow()

    private val _pointsOfInterest = MutableStateFlow<List<PointOfInterest>>(emptyList())
    val pointsOfInterest: StateFlow<List<PointOfInterest>> = _pointsOfInterest.asStateFlow()

    private val _geofenceAreas = MutableStateFlow<List<GeofenceArea>>(emptyList())
    val geofenceAreas: StateFlow<List<GeofenceArea>> = _geofenceAreas.asStateFlow()

    private val _addPoiState = MutableStateFlow<Result<String>>(Result.Initial)
    val addPoiState: StateFlow<Result<String>> = _addPoiState.asStateFlow()

    private val _addGeofenceState = MutableStateFlow<Result<Unit>>(Result.Initial)
    val addGeofenceState: StateFlow<Result<Unit>> = _addGeofenceState.asStateFlow()

    init {
        observeCurrentUser()
        observeUserRealtimeLocation()

        viewModelScope.launch {
            _currentGroupId.filterNotNull().collect { groupId ->
                observeGroupData(groupId)
                startPoiSync(groupId)
            }
        }

        observeFocusArguments()
    }

    private fun observeFocusArguments() {
        val latFlow = savedStateHandle.getStateFlow<String?>("lat", null)
        val lonFlow = savedStateHandle.getStateFlow<String?>("lon", null)

        combine(latFlow, lonFlow) { latString, lonString ->
            if (latString != null && lonString != null) {
                LatLng(latString.toDouble(), lonString.toDouble())
            } else {
                null
            }
        }.onEach { latLng ->
            _focusEvent.value = latLng
        }.launchIn(viewModelScope)
    }

/*    private fun startPoiSync(groupId: String) {
        val workData = Data.Builder().putString("groupId", groupId).build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncRequest = OneTimeWorkRequestBuilder<PoiSyncWorker>()
            .setInputData(workData)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "poi-sync-$groupId",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }*/


    private fun startPoiSync(groupId: String) {
        val workData = Data.Builder().putString("groupId", groupId).build()

        // CORRECTION : On retire la contrainte réseau qui bloque l'exécution sur l'émulateur.
        // val constraints = Constraints.Builder()
        //     .setRequiredNetworkType(NetworkType.CONNECTED)
        //     .build()

        val syncRequest = OneTimeWorkRequestBuilder<PoiSyncWorker>()
            .setInputData(workData)
            // .setConstraints(constraints) // On ne met plus la contrainte
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "poi-sync-$groupId",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }




    private fun observeGroupData(groupId: String) {
        groupRepository.getGroup(groupId)
            .onEach { _currentGroup.value = it }
            .launchIn(viewModelScope)

        pointOfInterestRepository.getGroupPointsOfInterest(groupId)
            .onEach { _pointsOfInterest.value = it }
            .launchIn(viewModelScope)

        geofenceRepository.getGeofenceAreasForGroup(groupId)
            .onEach { _geofenceAreas.value = it }
            .launchIn(viewModelScope)

        combine(
            groupRepository.getGroup(groupId).filterNotNull(),
            _currentUser.filterNotNull()
        ) { group, user ->
            group.memberIds.filter { it != user.id }
        }.flatMapLatest { memberIds ->
            if (memberIds.isNotEmpty()) {
                locationRepository.getMemberLocations(groupId, memberIds)
            } else {
                flowOf(emptyList())
            }
        }.onEach { _memberLocations.value = it }
            .launchIn(viewModelScope)
    }

    fun setGroupId(id: String?) {
        if (id != null && _currentGroupId.value != id) {
            _currentGroupId.value = id
        }
    }

    private fun observeCurrentUser() {
        authRepository.getCurrentUser().onEach { resultUser ->
            if (resultUser is Result.Success) {
                _currentUser.value = resultUser.data
            }
        }.launchIn(viewModelScope)
    }

    private fun observeUserRealtimeLocation() {
        locationClient.getLocationUpdates(5000L)
            .distinctUntilChanged()
            .catch { e -> Timber.e(e, "Error getting location updates") }
            .onEach { location ->
                _userRealtimeLocation.value = location
                _currentUser.value?.id?.let { userId ->
                    locationRepository.updateLocation(
                        com.easytoday.guidegroup.domain.model.Location(
                            userId = userId,
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    )
                }
            }.launchIn(viewModelScope)
    }

    fun addPointOfInterest(name: String, description: String, latitude: Double, longitude: Double) {
        val groupId = _currentGroupId.value ?: return
        val creatorId = _currentUser.value?.id ?: return

        viewModelScope.launch {
            _addPoiState.value = Result.Loading
            try {
                val newPoi = PointOfInterest(name = name, description = description, latitude = latitude, longitude = longitude, groupId = groupId, creatorId = creatorId)
                val poiId = pointOfInterestRepository.addPointOfInterest(newPoi)
                _addPoiState.value = Result.Success(poiId)
            } catch (e: Exception) {
                _addPoiState.value = Result.Error("Échec de l'ajout du POI: ${e.message}", e)
            }
        }
    }

    fun addGeofence(id: String, name: String, latitude: Double, longitude: Double, radius: Float) {
        val groupId = _currentGroupId.value ?: return
        val userId = _currentUser.value?.id ?: return

        val geofenceArea = GeofenceArea(
            id = id,
            groupId = groupId,
            name = name,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            transitionTypes = Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT,
            expirationDurationMillis = 12 * 60 * 60 * 1000,
            setByUserId = userId
        )

        viewModelScope.launch {
            _addGeofenceState.value = Result.Loading
            val saveResult = geofenceRepository.addGeofenceArea(geofenceArea)
            if (saveResult is Result.Success) {
                val monitorResult = geofenceRepository.startMonitoringGeofence(geofenceArea)
                _addGeofenceState.value = monitorResult
            } else {
                _addGeofenceState.value = saveResult
            }
        }
    }

    fun removeGeofence(geofenceId: String) {
        viewModelScope.launch {
            geofenceRepository.stopMonitoringGeofence(listOf(geofenceId))
            geofenceRepository.removeGeofenceArea(geofenceId)
        }
    }

    fun removePointOfInterest(poiId: String) {
        viewModelScope.launch {
            pointOfInterestRepository.deletePointOfInterest(poiId)
        }
    }

    fun resetAddGeofenceState() {
        _addGeofenceState.value = Result.Initial
    }

    fun onFocusEventConsumed() {
        _focusEvent.value = null
        savedStateHandle["lat"] = null
        savedStateHandle["lon"] = null
    }

    fun resetAddPoiState() {
        _addPoiState.value = Result.Initial
    }
}