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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationClient: LocationClient,
    private val locationRepository: LocationRepository, // Toujours nécessaire pour updateLocation et getMemberLocations
    private val pointOfInterestRepository: PointOfInterestRepository,
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository, // Nouveau : pour obtenir les membres du groupe
    savedStateHandle: SavedStateHandle // Pour récupérer le groupId
) : ViewModel() {

    // L'ID du groupe actuel, récupéré des arguments de navigation
    private val _currentGroupId = MutableStateFlow<String?>(null)
    val currentGroupId: StateFlow<String?> = _currentGroupId.asStateFlow()

    // L'utilisateur actuel
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Le groupe actuel
    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup: StateFlow<Group?> = _currentGroup.asStateFlow()

    // Localisation de l'utilisateur actuel (temps réel via LocationClient)
    private val _userRealtimeLocation = MutableStateFlow<Location?>(null)
    val userRealtimeLocation: StateFlow<Location?> = _userRealtimeLocation.asStateFlow()

    // Localisations des autres membres du groupe
    private val _memberLocations = MutableStateFlow<List<com.easytoday.guidegroup.domain.model.Location>>(emptyList())
    val memberLocations: StateFlow<List<com.easytoday.guidegroup.domain.model.Location>> = _memberLocations.asStateFlow()

    // Points d'intérêt pour le groupe actuel
    private val _pointsOfInterest = MutableStateFlow<List<PointOfInterest>>(emptyList())
    val pointsOfInterest: StateFlow<List<PointOfInterest>> = _pointsOfInterest.asStateFlow()

    // État de l'ajout d'un point d'intérêt
    private val _addPoiState = MutableStateFlow<Result<String>>(Result.Loading)
    val addPoiState: StateFlow<Result<String>> = _addPoiState.asStateFlow()

    init {
        val groupIdFromArgs = savedStateHandle.get<String>("groupId")
        _currentGroupId.value = groupIdFromArgs

        observeCurrentUser()
        observeCurrentGroup()
        observeUserRealtimeLocation()
        observeMemberLocations()
        observePointsOfInterest()

        Timber.d("MapViewModel initialized for group ID: $groupIdFromArgs")
    }

//    private fun observeCurrentUser() {
//        authRepository.getCurrentUser().onEach { user ->
//            _currentUser.value = user
//            Timber.d("MapViewModel: Current user observed: ${user?.email}")
//        }.launchIn(viewModelScope)
//    }


    /**
     * Définit l'ID du groupe actuel.
     * Cette fonction permet à l'UI (ou à d'autres composants) de notifier le ViewModel de l'ID du groupe.
     */
    fun setGroupId(id: String) {
        if (_currentGroupId.value != id) { // Évite les mises à jour inutiles
            _currentGroupId.value = id
            Timber.d("MapViewModel: currentGroupId set to $id")
        }
    }

    private fun observeCurrentUser() {
        authRepository.getCurrentUser().onEach { resultUser -> // Renommez le paramètre pour clarifier
            when (resultUser) {
                is Result.Success -> {
                    _currentUser.value = resultUser.data // Assignez le User? déballé
                    Timber.d("MapViewModel: Current user observed: ${resultUser.data?.email}") // Accédez à email via .data
                }
                is Result.Error -> {
                    _currentUser.value = null // En cas d'erreur, l'utilisateur est null
                    Timber.e("MapViewModel: Error observing current user: ${resultUser.message}")
                }
                Result.Loading -> {
                    // Optionnel: Gérer l'état de chargement si nécessaire
                    Timber.d("MapViewModel: Current user loading...")
                }
                is Result.Initial -> {
                    // Optionnel: Gérer l'état initial si nécessaire
                    Timber.d("MapViewModel: Current user initial state.")
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun observeCurrentGroup() {
        _currentGroupId.flatMapLatest { groupId ->
            if (groupId != null) {
                groupRepository.getGroup(groupId)
            } else {
                MutableStateFlow(null)
            }
        }.onEach { group ->
            _currentGroup.value = group
            Timber.d("MapViewModel: Current group observed: ${group?.name}")
        }.launchIn(viewModelScope)
    }

    private fun observeUserRealtimeLocation() {
        viewModelScope.launch {
            locationClient.getLocationUpdates(5000L) // Mises à jour toutes les 5 secondes
                .distinctUntilChanged()
                .catch { e ->
                    Timber.e("Error getting realtime location updates: ${e.message}", e)
                    // Informer l'utilisateur d'un problème de localisation si nécessaire
                }
                .collect { location ->
                    _userRealtimeLocation.value = location
                    Timber.d("MapViewModel: Realtime location updated: ${location.latitude}, ${location.longitude}")

                    // Mettre à jour la localisation de l'utilisateur dans Firestore
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

    private fun observeMemberLocations() {
        // Combine le groupe actuel et l'utilisateur actuel pour filtrer les localisations
        combine(
            _currentGroup,
            _currentUser
        ) { group, currentUser ->
            if (group != null && currentUser != null) {
                // Obtenir les localisations de tous les membres du groupe sauf l'utilisateur actuel
                val otherMemberIds = group.memberIds.filter { it != currentUser.id }
                locationRepository.getMemberLocations(group.id, otherMemberIds)
            } else {
                MutableStateFlow(emptyList())
            }
        }.flatMapLatest { it } // Aplatir le Flow de Flow
            .onEach { memberLocs ->
                _memberLocations.value = memberLocs
                Timber.d("MapViewModel: Member locations updated: ${memberLocs.size} locations.")
            }
            .launchIn(viewModelScope)
    }


    private fun observePointsOfInterest() {
        _currentGroupId.flatMapLatest { groupId ->
            if (groupId != null) {
                pointOfInterestRepository.getGroupPointsOfInterest(groupId)
            } else {
                MutableStateFlow(emptyList())
            }
        }.onEach { pois ->
            _pointsOfInterest.value = pois
            Timber.d("MapViewModel: Points of interest updated: ${pois.size} POIs for group.")
        }.launchIn(viewModelScope)
    }

    /**
     * Ajoute un nouveau point d'intérêt.
     * @param name Le nom du point d'intérêt.
     * @param description La description du point d'intérêt.
     * @param latitude La latitude du point d'intérêt.
     * @param longitude La longitude du point d'intérêt.
     */
    fun addPointOfInterest(name: String, description: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val groupId = _currentGroupId.value
            if (groupId == null) {
                _addPoiState.value = Result.Error("Impossible d'ajouter un POI: ID de groupe non disponible.")
                Timber.e("MapViewModel: Cannot add POI, group ID is null.")
                return@launch
            }

            _addPoiState.value = Result.Loading
            try {
                val newPoi = PointOfInterest(
                    name = name,
                    description = description,
                    latitude = latitude,
                    longitude = longitude,
                    groupId = groupId
                )
                val poiId = pointOfInterestRepository.addPointOfInterest(newPoi)
                _addPoiState.value = Result.Success(poiId)
                Timber.d("MapViewModel: Point of interest added with ID: $poiId for group $groupId")
            } catch (e: Exception) {
                _addPoiState.value = Result.Error("Échec de l'ajout du point d'intérêt: ${e.message}", e)
                Timber.e("MapViewModel: Error adding point of interest: ${e.message}", e)
            }
        }
    }

    /**
     * Réinitialise l'état d'ajout de POI.
     */
    fun resetAddPoiState() {
        _addPoiState.value = Result.Loading
    }
}

