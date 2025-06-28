// app/src/main/java/com/easytoday/guidegroup/presentation/viewmodel/HomeViewModel.kt
package com.easytoday.guidegroup.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.repository.LocationRepository
import com.easytoday.guidegroup.domain.repository.MeetingPointRepository
import com.easytoday.guidegroup.domain.repository.PointOfInterestRepository
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.GroupRepository
import com.easytoday.guidegroup.domain.repository.UserRepository // Import correct pour UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn // << --- S'assurer que cet import est présent
import kotlinx.coroutines.flow.onEach // << --- S'assurer que cet import est présent
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

import com.easytoday.guidegroup.domain.usecase.SeedDatabaseUseCase // <-- AJOUTER


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val seedDatabaseUseCase: SeedDatabaseUseCase // <-- CORRECTION : Injecter le Use Case
) : ViewModel() {

    private val _groups = MutableStateFlow<Result<List<Group>>>(Result.Loading as Result<List<Group>>)
    val groups: StateFlow<Result<List<Group>>> = _groups.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // AJOUT : État pour le seeding
    private val _seedingState = MutableStateFlow("")
    val seedingState: StateFlow<String> = _seedingState.asStateFlow()

    init {
        fetchGroups()
        fetchCurrentUser()
    }

/*    fun fetchGroups() {
        viewModelScope.launch {
            groupRepository.getAllGroups().onEach { result: Result<List<Group>> ->
                _groups.value = result
            }.launchIn(viewModelScope)
        }
    }*/

    fun fetchGroups() {
        viewModelScope.launch {
            groupRepository.getAllGroups().onEach { result -> // Enlever la spécification de type ici
                // Utiliser un when pour gérer les différents états de Result
                when (result) {
                    is Result.Success -> {
                        _groups.value = Result.Success(result.data) // Mettre à jour avec le succès
                        Timber.d("HomeViewModel: Groups fetched successfully: ${result.data.size} groups.")
                    }
                    is Result.Error -> {
                        _groups.value = Result.Error(result.message, result.exception) // Mettre à jour avec l'erreur
                        Timber.e("HomeViewModel: Error fetching groups: ${result.message}", result.exception)
                    }
                    Result.Loading -> {
                        _groups.value = Result.Loading // Mettre à jour avec le chargement
                        Timber.d("HomeViewModel: Loading groups...")
                    }
                    is Result.Initial -> {
                        _groups.value = Result.Initial // Mettre à jour avec l'état initial
                        Timber.d("HomeViewModel: Initial state for groups.")
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

//    private fun fetchCurrentUser() {
//        viewModelScope.launch {
//            userRepository.getCurrentUser().onEach { userResult: Result<User?> ->
//                _currentUser.value = when (userResult) {
//                    is Result.Success -> userResult.data
//                    is Result.Error -> {
//                        Timber.e("Error fetching current user: ${userResult.message}")
//                        null
//                    }
//                    Result.Loading -> null
//                }
//            }.launchIn(viewModelScope)
//        }
//    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser().onEach { userResult: Result<User?> ->
                _currentUser.value = when (userResult) {
                    is Result.Success -> userResult.data
                    is Result.Error -> {
                        Timber.e("Error fetching current user: ${userResult.message}")
                        null
                    }
                    Result.Loading -> null
                    is Result.Initial -> null // Gérer l'état Initial ici aussi si nécessaire
                }
            }.launchIn(viewModelScope)
        }
    }


    // AJOUT : Nouvelle fonction pour déclencher le seeding
    fun seedDatabase() {
        viewModelScope.launch {
            try {
                _seedingState.value = "Seeding en cours..."
                seedDatabaseUseCase()
                _seedingState.value = "Seeding terminé avec succès !"
                fetchGroups() // Rafraîchir la liste des groupes après le seeding
            } catch (e: Exception) {
                _seedingState.value = "Erreur pendant le seeding: ${e.message}"
            }
        }
    }


}

