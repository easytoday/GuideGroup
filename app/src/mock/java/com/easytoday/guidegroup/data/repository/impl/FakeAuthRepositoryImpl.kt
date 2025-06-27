package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.model.Result // Import correct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeAuthRepositoryImpl @Inject constructor() : AuthRepository {

    // _currentUser est une source interne de vérité pour l'utilisateur simulé.
    private val _currentUser = MutableStateFlow<User?>(null)

    // getCurrentUser doit retourner Flow<Result<User?>> comme dans l'interface
    override fun getCurrentUser(): Flow<Result<User?>> {
        // Mappe le Flow<User?> interne vers Flow<Result<User?>>
        return _currentUser.map { user ->
            Result.Success(user) // Émet un Result.Success avec l'utilisateur (ou null)
        }
    }

    // getCurrentUserId doit retourner Flow<Result<String?>> comme dans l'interface
    override fun getCurrentUserId(): Flow<Result<String?>> {
        // Émet l'ID de l'utilisateur comme un Flow de Result
        return _currentUser.map { user ->
            Result.Success(user?.id) // Émet l'ID de l'utilisateur (ou null)
        }
    }

    override suspend fun signIn(email: String, password: String): Flow<Result<User>> {
        return flow {
            emit(Result.Loading) // Émet l'état de chargement
            delay(500)
            if (email == "test@example.com" && password == "password") {
                val fakeUser = User(id = "fakeUserId123", email = email, username = "Fake User", isGuide = false)
                _currentUser.value = fakeUser
                emit(Result.Success(fakeUser))
            } else {
                emit(Result.Error("Authentification factice échouée.", Exception("Fake authentication failed."))) // Message et Exception
            }
        }
    }

    override suspend fun signUp(email: String, password: String, username: String, isGuide: Boolean): Flow<Result<User>> {
        return flow {
            emit(Result.Loading) // Émet l'état de chargement
            delay(500)
            if (email.contains("error")) {
                emit(Result.Error("Échec de l'inscription factice.", Exception("Fake signup failed due to 'error' in email."))) // Message et Exception
            } else {
                val newUser = User(id = "newFakeUser_${System.currentTimeMillis()}", email = email, username = username, isGuide = isGuide)
                _currentUser.value = newUser
                emit(Result.Success(newUser))
            }
        }
    }

    override suspend fun signOut(): Flow<Result<Unit>> {
        return flow {
            emit(Result.Loading) // Émet l'état de chargement
            delay(200)
            _currentUser.value = null
            emit(Result.Success(Unit))
        }
    }

    // La méthode resetPassword n'est PAS présente dans l'interface AuthRepository que vous avez fournie.
    // Donc, elle doit être supprimée de FakeAuthRepositoryImpl pour éviter l'erreur "overrides nothing".
    // Si vous l'ajoutez plus tard à l'interface, vous pourrez la réimplémenter ici.

}

