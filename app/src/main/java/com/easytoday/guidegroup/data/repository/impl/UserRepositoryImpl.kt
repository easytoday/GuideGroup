// app/src/main/java/com/easytoday/guidegroup/data/repository/impl/UserRepositoryImpl.kt
package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestoreHelper: FirestoreHelper
) : UserRepository {

    private val COLLECTION_NAME = "users"

    override fun getUser(userId: String): Flow<Result<User?>> {
        return firestoreHelper.getDocumentAsFlow<User>(COLLECTION_NAME, userId)
            .map<User?, Result<User?>> { user -> Result.Success(user) } // Enveloppe dans Result.Success
            .onStart { emit(Result.Loading) } // Émet Loading au début
            .catch { e ->
                Timber.e(e, "Erreur lors de la récupération de l'utilisateur $userId")
                emit(Result.Error("Erreur lors de la récupération de l'utilisateur: ${e.localizedMessage}", e))
            }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getCurrentUser(): Flow<Result<User?>> {
        // Crée un Flow qui émet l'ID de l'utilisateur (ou null) chaque fois que l'état d'authentification change.
        return callbackFlow<String?> {
            // 1. CRÉER le listener
            val listener = FirebaseAuth.AuthStateListener { auth ->
                trySend(auth.currentUser?.uid)
            }
            // 2. AJOUTER le listener
            firestoreHelper.auth.addAuthStateListener(listener)

            // 3. DÉFINIR comment le retirer lorsque le Flow est annulé
            awaitClose {
                firestoreHelper.auth.removeAuthStateListener(listener)
            }
        }.flatMapLatest { userId ->
            // `flatMapLatest` transforme le Flow d'ID en un Flow de `Result<User?>`.
            // Il annulera l'ancienne requête si une nouvelle ID arrive (connexion/déconnexion).
            if (userId == null) {
                // Si l'utilisateur s'est déconnecté, émet un succès avec null.
                flowOf(Result.Success(null))
            } else {
                // Si un utilisateur est connecté, réutilise la fonction getUser pour obtenir ses données.
                // Elle gère déjà les états Loading, Success et Error.
                getUser(userId)
            }
        }
    }

    override suspend fun addUser(user: User) {
        try {
            firestoreHelper.addDocument(COLLECTION_NAME, user, user.id)
            Timber.d("Utilisateur ajouté avec succès: ${user.email} (ID: ${user.id})")
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de l'ajout de l'utilisateur ${user.id}")
        }
    }

    override suspend fun updateUser(user: User) {
        try {
            firestoreHelper.updateDocument(COLLECTION_NAME, user.id, user)
            Timber.d("Utilisateur mis à jour avec succès: ${user.email} (ID: ${user.id})")
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la mise à jour de l'utilisateur ${user.id}")
        }
    }

    override suspend fun deleteUser(userId: String) {
        try {
            firestoreHelper.deleteDocument(COLLECTION_NAME, userId)
            Timber.d("Utilisateur supprimé avec succès: $userId")
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la suppression de l'utilisateur $userId")
        }
    }
}
