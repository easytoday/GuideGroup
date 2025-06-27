// app/src/main/java/com/easytoday/guidegroup/presentation/viewmodel/AuthViewModel.kt
package com.easytoday.guidegroup.presentation.viewmodel

import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.model.Result


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.usecase.SignInUseCase
import com.easytoday.guidegroup.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel pour gérer l'état de l'interface utilisateur et la logique métier
 * liée à l'authentification (connexion, inscription, déconnexion).
 *
 * @param signInUseCase Cas d'utilisation pour la connexion.
 * @param signUpUseCase Cas d'utilisation pour l'inscription.
 * @param authRepository Référentiel d'authentification pour observer l'utilisateur courant.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val authRepository: AuthRepository // Pour observer l'utilisateur courant
) : ViewModel() {

    private val TAG = "AuthViewModel"

    // États de l'UI pour la connexion
    private val _signInState = MutableStateFlow<Result<User>>(Result.Loading)
    val signInState: StateFlow<Result<User>> = _signInState.asStateFlow()

    // États de l'UI pour l'inscription
    private val _signUpState = MutableStateFlow<Result<User>>(Result.Loading)
    val signUpState: StateFlow<Result<User>> = _signUpState.asStateFlow()

    // État de l'utilisateur actuellement connecté
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

//    // État mutable de l'interface utilisateur pour l'authentification
//    private val _uiState = MutableStateFlow(AuthUiState())
//    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Collecte l'utilisateur courant du référentiel et met à jour l'état de l'UI.
        // Observer l'état d'authentification dès l'initialisation du ViewModel
        observeCurrentUser()
//        viewModelScope.launch {
//            authRepository.currentUser.collectLatest { user ->
//                _uiState.value = _uiState.value.copy(
//                    currentUser = user,
//                    isAuthenticated = user != null,
//                    isLoading = false // Assurez-vous que l'état de chargement est réinitialisé
//                )
//                Log.d(TAG, "État d'authentification mis à jour: isAuthenticated=${user != null}, currentUser=${user?.email}")
//            }
//        }
    }

//    private fun observeCurrentUser() {
//        viewModelScope.launch {
//            authRepository.getCurrentUser().onEach { user ->
//                _currentUser.value = user
//            }.launchIn(viewModelScope) // Utilisez launchIn pour que le Flow soit collecté dans le scope du ViewModel
//        }
//    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser().onEach { resultUser -> // Renommez le paramètre pour clarifier
                // Gérer les différents états du Result
                when (resultUser) {
                    is Result.Success -> {
                        _currentUser.value = resultUser.data // Assignez le User? déballé
                        Log.d(TAG, "Current user observed: ${resultUser.data?.email}")
                    }
                    is Result.Error -> {
                        _currentUser.value = null // En cas d'erreur, l'utilisateur est null
                        Log.e(TAG, "Error observing current user: ${resultUser.message}")
                        // Optionnel: Vous pouvez aussi vouloir mettre à jour un état d'erreur spécifique à l'AuthViewModel
                    }
                    Result.Loading -> {
                        // Optionnel: Gérer l'état de chargement si nécessaire
                        Log.d(TAG, "Observing current user: Loading...")
                    }
                    is Result.Initial -> {
                        // Optionnel: Gérer l'état initial si nécessaire
                        Log.d(TAG, "Observing current user: Initial state.")
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Tente de connecter un utilisateur.
     * @param email L'email de l'utilisateur.
     * @param password Le mot de passe de l'utilisateur.
     */
    fun signIn(email: String, password: String) {
        //_uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            signInUseCase(email, password).onEach { result ->
                _signInState.value = result
            }.launchIn(viewModelScope)
//            val result = signInUseCase(email, password)
//            result.onSuccess { user ->
//                Log.d(TAG, "Connexion réussie pour: ${user.email}")
//                // L'état _uiState sera mis à jour par le collecteur de currentUser
//            }.onFailure { e ->
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    error = e.message ?: "Erreur de connexion inconnue."
//                )
//                Log.e(TAG, "Échec de la connexion: ${e.message}")
//            }
        }
    }

    /**
     * Tente d'inscrire un nouvel utilisateur.
     * @param email L'email du nouvel utilisateur.
     * @param password Le mot de passe du nouvel utilisateur.
     * @param username Le nom du nouvel utilisateur.
     * @param isGuide Le rôle de l'utilisateur (guide ou non). <--- NOUVEAU PARAMÈTRE ICI
     */
    fun signUp(email: String, password: String, username: String, isGuide: Boolean) { // <--- MODIFIÉ ICI
        viewModelScope.launch {
            _signUpState.value = Result.Loading // Set loading state before use case call
            signUpUseCase(email, password, username, isGuide).onEach { result -> // <--- PASSEZ 'isGuide' AU USE CASE
                _signUpState.value = result
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Tente de déconnecter l'utilisateur.
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut().onEach { result ->
                // Gérer le résultat de la déconnexion si nécessaire (ex: réinitialiser _currentUser)
                if (result is Result.Success) {
                    _currentUser.value = null // Effacer l'utilisateur après déconnexion réussie
                }
                // Vous pouvez aussi avoir un _signOutState si vous voulez suivre cet événement spécifiquement
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Réinitialise l'état de connexion/inscription, utile après une tentative.
     */
    fun resetAuthStates() {
        _signInState.value = Result.Loading // Ou un état initial approprié
        _signUpState.value = Result.Loading // Ou un état initial approprié
    }





//    /**
//     * Efface le message d'erreur actuel.
//     */
//    fun clearError() {
//        _uiState.value = _uiState.value.copy(error = null)
//    }

//    /**
//     * Représente l'état de l'interface utilisateur pour l'authentification.
//     * @param isLoading Indique si une opération est en cours.
//     * @param isAuthenticated Indique si un utilisateur est connecté.
//     * @param currentUser L'objet utilisateur actuellement connecté, ou null.
//     * @param error Message d'erreur à afficher, ou null.
//     */
//    data class AuthUiState(
//        val isLoading: Boolean = false,
//        val isAuthenticated: Boolean = false,
//        val currentUser: User? = null,
//        val error: String? = null
//    )
}


