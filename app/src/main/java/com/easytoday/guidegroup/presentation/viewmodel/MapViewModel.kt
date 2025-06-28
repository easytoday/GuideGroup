package com.easytoday.guidegroup.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.repository.GroupRepository
import com.easytoday.guidegroup.domain.repository.LocationClient
import com.easytoday.guidegroup.domain.repository.LocationRepository
import com.easytoday.guidegroup.domain.repository.PointOfInterestRepository
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

    private val _currentGroupId = MutableStateFlow<String?>(null)
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

    init {
        setGroupId(savedStateHandle.get<String>("groupId"))
        observeCurrentUser()
        observeCurrentGroup()
        observeUserRealtimeLocation()
        observeMemberLocations()
        observePointsOfInterest()
    }

    // CORRECTION : Ajout de la fonction manquante
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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeCurrentGroup() {
        _currentGroupId.flatMapLatest { groupId ->
            if (groupId != null) {
                groupRepository.getGroup(groupId)
            } else {
                emptyFlow()
            }
        }.onEach { group ->
            _currentGroup.value = group
        }.launchIn(viewModelScope)
    }

    private fun observeUserRealtimeLocation() {
        viewModelScope.launch {
            locationClient.getLocationUpdates(5000L)
                .distinctUntilChanged()
                .catch { e -> Timber.e(e, "Error getting location updates") }
                .collect { location ->
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
                }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeMemberLocations() {
        combine(_currentGroup, _currentUser) { group, currentUser ->
            if (group != null && currentUser != null) {
                val otherMemberIds = group.memberIds.filter { it != currentUser.id }
                if (otherMemberIds.isNotEmpty()) {
                    locationRepository.getMemberLocations(group.id, otherMemberIds)
                } else {
                    flowOf(emptyList())
                }
            } else {
                flowOf(emptyList())
            }
        }.flatMapLatest { it }
            .onEach { memberLocs ->
                _memberLocations.value = memberLocs
            }.launchIn(viewModelScope)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observePointsOfInterest() {
        _currentGroupId.flatMapLatest { groupId ->
            if (groupId != null) {
                pointOfInterestRepository.getGroupPointsOfInterest(groupId)
            } else {
                emptyFlow()
            }
        }.onEach { pois ->
            _pointsOfInterest.value = pois
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
}