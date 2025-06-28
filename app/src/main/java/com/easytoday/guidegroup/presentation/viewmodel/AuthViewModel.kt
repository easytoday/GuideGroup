package com.easytoday.guidegroup.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.usecase.SignInUseCase
import com.easytoday.guidegroup.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _signInState = MutableStateFlow<Result<User>>(Result.Initial)
    val signInState: StateFlow<Result<User>> = _signInState.asStateFlow()

    private val _signUpState = MutableStateFlow<Result<User>>(Result.Initial)
    val signUpState: StateFlow<Result<User>> = _signUpState.asStateFlow()

    // L'état de l'utilisateur est maintenant initialisé à null
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // CORRECTION : Au lieu d'une observation continue, on fait une vérification unique.
        // Cela évite de lancer un flow infini et complexe au démarrage de l'app.
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            // On prend seulement le PREMIER résultat du flow. C'est rapide et ça ne gèle pas.
            when(val result = authRepository.getCurrentUser().first()) {
                is Result.Success -> _currentUser.value = result.data
                else -> _currentUser.value = null
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _signInState.value = Result.Loading
            signInUseCase(email, password).collect { result ->
                _signInState.value = result
                // Si la connexion réussit, on met à jour l'utilisateur actuel
                if (result is Result.Success) {
                    _currentUser.value = result.data
                }
            }
        }
    }

    fun signUp(email: String, password: String, username: String, isGuide: Boolean) {
        viewModelScope.launch {
            _signUpState.value = Result.Loading
            signUpUseCase(email, password, username, isGuide).collect { result ->
                _signUpState.value = result
                // Si l'inscription réussit, on met à jour l'utilisateur actuel
                if (result is Result.Success) {
                    _currentUser.value = result.data
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _currentUser.value = null // On met manuellement l'utilisateur à null
            resetAuthStates()
        }
    }

    fun resetAuthStates() {
        _signInState.value = Result.Initial
        _signUpState.value = Result.Initial
    }
}