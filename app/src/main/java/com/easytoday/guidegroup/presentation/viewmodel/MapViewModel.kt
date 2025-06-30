package com.easytoday.guidegroup.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationClient: LocationClient,
    private val locationRepository: LocationRepository,
    private val pointOfInterestRepository: PointOfInterestRepository,
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _currentGroupId = MutableStateFlow<String?>(savedStateHandle.get<String>("groupId"))
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

    private val _addPoiState = MutableStateFlow<Result<String>>(Result.Initial)
    val addPoiState: StateFlow<Result<String>> = _addPoiState.asStateFlow()

    // CORRECTION : La propriété pour gérer le focus sur le POI
    val focusOnPoi: StateFlow<String?> = savedStateHandle.getStateFlow("focusOnPoi", null)

    init {
        observeCurrentUser()
        observeUserRealtimeLocation()

        viewModelScope.launch {
            _currentGroupId.filterNotNull().collect { groupId ->
                observeGroupData(groupId)
            }
        }
    }

    private fun observeGroupData(groupId: String) {
        groupRepository.getGroup(groupId)
            .onEach { _currentGroup.value = it }
            .launchIn(viewModelScope)

        pointOfInterestRepository.getGroupPointsOfInterest(groupId)
            .onEach { _pointsOfInterest.value = it }
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
        viewModelScope.launch {
            val groupId = _currentGroupId.value ?: return@launch
            _addPoiState.value = Result.Loading
            try {
                val newPoi = PointOfInterest(name = name, description = description, latitude = latitude, longitude = longitude, groupId = groupId)
                val poiId = pointOfInterestRepository.addPointOfInterest(newPoi)
                _addPoiState.value = Result.Success(poiId)
            } catch (e: Exception) {
                _addPoiState.value = Result.Error("Échec de l'ajout du POI: ${e.message}", e)
            }
        }
    }

    fun resetAddPoiState() {
        _addPoiState.value = Result.Initial
    }

    // CORRECTION : La fonction pour réinitialiser l'état de focus
    fun poiFocused() {
        savedStateHandle["focusOnPoi"] = null
    }
}