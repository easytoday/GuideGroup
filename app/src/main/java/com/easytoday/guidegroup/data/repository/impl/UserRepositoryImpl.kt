// app/src/main/java/com/easytoday/guidegroup/data/repository/impl/UserRepositoryImpl.kt
package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject


/**
 * Implémentation concrète de [UserRepository] utilisant Firestore.
 */
class UserRepositoryImpl @Inject constructor(
    private val firestoreHelper: FirestoreHelper
    //private val firebaseAuth: FirebaseAuth
) : UserRepository {

    private val COLLECTION_NAME = "users"

    /**
     * Récupère un utilisateur par son ID.
     * Les utilisateurs sont stockés avec leur ID comme ID de document.
     * @param userId L'ID de l'utilisateur à récupérer.
     * @return Un flux (Flow) de [Result] contenant l'utilisateur correspondant, ou null si non trouvé.
     */
    // Correction Finale pour getUser()
    // Le Flow émettra Result.Loading, puis Result.Success(User?) en cas de succès,
    // ou Result.Error en cas d'échec de la collecte du Flow interne.
    override fun getUser(userId: String): Flow<Result<User?>> = flow {
        emit(Result.Loading)
        try {
            // Collectez le Flow de FirestoreHelper (Flow<User?>)
            firestoreHelper.getDocumentAsFlow<User>(COLLECTION_NAME, userId).collect { user ->
                // Chaque fois qu'il y a une mise à jour, émettez un Result.Success
                emit(Result.Success(user))
            }
        } catch (e: Exception) {
            Timber.e("Erreur lors de la récupération de l'utilisateur $userId: ${e.message}", e)
            // Émettez Result.Error si une exception se produit pendant la collecte ou l'accès à Firestore
            // Assurez-vous que Result.Error est bien défini pour être compatible avec Result<User?>
            emit(Result.Error("Erreur lors de la récupération de l'utilisateur: ${e.localizedMessage ?: e.message}", e))
        }
    }


    /**
     * Implémentation de la méthode getCurrentUser de l'interface UserRepository.
     * Récupère les détails de l'utilisateur actuellement connecté depuis Firestore.
     */
    // Correction Finale pour getCurrentUser()
    override fun getCurrentUser(): Flow<Result<User?>> = flow {
        emit(Result.Loading)

        //val currentUserId = firebaseAuth.currentUser?.uid
        val currentUserId = firestoreHelper.auth.currentUser?.uid

        if (currentUserId != null) {
            try {
                // Collectez le Flow de FirestoreHelper pour l'utilisateur actuel
                firestoreHelper.getDocumentAsFlow<User>(COLLECTION_NAME, currentUserId).collect { user ->
                    emit(Result.Success(user))
                }
            } catch (e: Exception) {
                Timber.e("Erreur lors de la récupération de l'utilisateur actuel: ${e.message}", e)
                emit(Result.Error("Erreur lors de la récupération de l'utilisateur actuel: ${e.localizedMessage ?: e.message}", e))
            }
        } else {
            // Si aucun utilisateur n'est connecté, émettez un succès avec null
            emit(Result.Success(null))
        }
    }

    /**
     * Ajoute un nouvel utilisateur.
     * L'ID de l'utilisateur est utilisé comme ID de document Firestore.
     * @param user L'utilisateur à ajouter.
     */
    override suspend fun addUser(user: User) {
        try {
            firestoreHelper.addDocument(COLLECTION_NAME, user, user.id)
            Timber.d("Utilisateur ajouté avec succès: ${user.email} (ID: ${user.id})")
        } catch (e: Exception) {
            Timber.e("Erreur lors de l'ajout de l'utilisateur ${user.id}: ${e.message}", e)
        }
    }

    /**
     * Met à jour un utilisateur existant.
     * L'ID de l'utilisateur est utilisé comme ID de document Firestore.
     * @param user L'utilisateur à mettre à jour.
     */
    override suspend fun updateUser(user: User) {
        try {
            firestoreHelper.updateDocument(COLLECTION_NAME, user.id, user)
            Timber.d("Utilisateur mis à jour avec succès: ${user.email} (ID: ${user.id})")
        } catch (e: Exception) {
            Timber.e("Erreur lors de la mise à jour de l'utilisateur ${user.id}: ${e.message}", e)
        }
    }

    /**
     * Supprime un utilisateur par son ID.
     * @param userId L'ID de l'utilisateur à supprimer.
     */
    override suspend fun deleteUser(userId: String) {
        try {
            firestoreHelper.deleteDocument(COLLECTION_NAME, userId)
            Timber.d("Utilisateur supprimé avec succès: $userId")
        } catch (e: Exception) {
            Timber.e("Erreur lors de la suppression de l'utilisateur $userId: ${e.message}", e)
        }
    }
}

