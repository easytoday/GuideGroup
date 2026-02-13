package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.random.Random

// Implémentation factice de UserRepository pour l'environnement de test (mock).
// Simule les opérations CRUD (Create, Read, Update, Delete) sur les utilisateurs.
class FakeUserRepositoryImpl @Inject constructor() : UserRepository {

    // Simule une base de données d'utilisateurs en mémoire
    private val fakeUsersDb = MutableStateFlow<MutableMap<String, User>>(mutableMapOf())

    // Initialise avec quelques utilisateurs factices pour les tests
    init {
        val user1 = User(id = "user1_id", email = "test1@example.com", username = "Alice Test", isGuide = false)
        val user2 = User(id = "user2_id", email = "test2@example.com", username = "Bob Guide", isGuide = true)
        fakeUsersDb.value[user1.id] = user1
        fakeUsersDb.value[user2.id] = user2
    }

    // --- Méthodes de lecture (GET) ---

    // Récupère un utilisateur par son ID
    override fun getUser(userId: String): Flow<Result<User?>> = flow {
        emit(Result.Loading)
        delay(500) // Simule un délai réseau
        val user = fakeUsersDb.value[userId]
        if (user != null) {
            emit(Result.Success(user))
        } else {
            // Dans un scénario réel, on pourrait émettre Error si l'ID est invalide,
            // mais pour un utilisateur non trouvé  ==> Success(null).
            emit(Result.Success(null))
        }
    }

    // Récupère l'utilisateur actuellement connecté (simulé)
    override fun getCurrentUser(): Flow<Result<User?>> {
        // Simule l'utilisateur connecté comme étant le premier utilisateur dans la DB factice
        // Pour un mock plus réaliste, lier à l'état de FakeAuthRepositoryImpl
        return fakeUsersDb.map { usersMap ->
            val currentUser = usersMap.values.firstOrNull() // Prend le premier utilisateur comme "courant" pour la simulation
            Result.Success(currentUser)
        }
    }

    // --- Méthodes d'écriture (CRUD) ---

    // Ajoute un nouvel utilisateur
    override suspend fun addUser(user: User) {
        delay(300) // Simule un délai
        fakeUsersDb.value[user.id] = user
        // logguer avec Timber pour les mocks
        // Timber.d("FakeUser: User added: ${user.id}")
    }

    // Met à jour un utilisateur existant
    override suspend fun updateUser(user: User) {
        delay(300) // Simule un délai
        if (fakeUsersDb.value.containsKey(user.id)) {
            fakeUsersDb.value[user.id] = user
            // Timber.d("FakeUser: User updated: ${user.id}")
        } else {
            // Optionnel: Gérer le cas où l'utilisateur n'existe pas pour la mise à jour
            // throw Exception("User with ID ${user.id} not found for update.")
        }
    }

    // Supprime un utilisateur par son ID
    override suspend fun deleteUser(userId: String) {
        delay(3300) // Simule un délai
        if (fakeUsersDb.value.containsKey(userId)) {
            fakeUsersDb.value.remove(userId)
            // Timber.d("FakeUser: User deleted: $userId")
        } else {
            // Optionnel: Gérer le cas où l'utilisateur n'existe pas pour la suppression
            // throw Exception("User with ID $userId not found for deletion.")
        }
    }
}

