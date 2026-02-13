// app/src/main/java/com/easytoday/guidegroup/presentation/viewmodel/HomeViewModel.kt
package com.easytoday.guidegroup.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.GroupRepository
import com.easytoday.guidegroup.domain.repository.UserRepository
import com.easytoday.guidegroup.domain.usecase.SeedDatabaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val seedDatabaseUseCase: SeedDatabaseUseCase
) : ViewModel() {

    // Initialisation explicite avec Result.Initial pour éviter les casts
    private val _groups = MutableStateFlow<Result<List<Group>>>(Result.Initial)
    val groups: StateFlow<Result<List<Group>>> = _groups.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _seedingState = MutableStateFlow("")
    val seedingState: StateFlow<String> = _seedingState.asStateFlow()

    init {
        fetchGroups()
        fetchCurrentUser()
    }

    fun fetchGroups() {
        // CORRIGÉ : La logique est simplifiée, car le repository gère maintenant la complexité.
        groupRepository.getAllGroups().onEach { result ->
            _groups.value = result
            if (result is Result.Error) {
                Timber.e("HomeViewModel: Error fetching groups: ${result.message}", result.exception)
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchCurrentUser() {
        // CORRIGÉ : La logique gère maintenant tous les cas de Result de manière propre.
        userRepository.getCurrentUser().onEach { userResult ->
            when (userResult) {
                is Result.Success -> _currentUser.value = userResult.data
                is Result.Error -> {
                    _currentUser.value = null
                    Timber.e("Error fetching current user: ${userResult.message}")
                }
                is Result.Loading, is Result.Initial -> {
                    // afficher un état de chargement pour l'utilisateur
                    // Pour l'instant, on met à null
                    _currentUser.value = null
                }
            }
        }.launchIn(viewModelScope)
    }

    fun seedDatabase() {
        viewModelScope.launch {
            try {
                _seedingState.value = "Seeding en cours..."
                seedDatabaseUseCase()
                _seedingState.value = "Seeding terminé avec succès !"
                fetchGroups()
            } catch (e: Exception) {
                _seedingState.value = "Erreur pendant le seeding: ${e.message}"
                Timber.e(e, "Erreur pendant le seeding")
            }
        }
    }
}
