// app/src/main/java/com/easytoday/guidegroup/domain/repository/UserRepository.kt
package com.easytoday.guidegroup.domain.repository

import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface du référentiel pour les opérations liées aux utilisateurs.
 * Définit les méthodes pour interagir avec les données des utilisateurs.
 */
interface UserRepository {

    /**
     * Récupère un utilisateur par son ID.
     * @param userId L'ID de l'utilisateur à récupérer.
     * @return Un flux (Flow) de l'utilisateur correspondant, ou null si non trouvé.
     */
    fun getUser(userId: String): Flow<Result<User?>> // was Flow<User?>

    /**
     * Récupère l'utilisateur actuellement connecté.
     * @return Un flux (Flow) de [Result] contenant l'utilisateur connecté, ou null si non connecté.
     */
    fun getCurrentUser(): Flow<Result<User?>> // <<< AJOUTEZ CETTE LIGNE

    /**
     * Ajoute un nouvel utilisateur.
     * @param user L'utilisateur à ajouter.
     */
    suspend fun addUser(user: User)

    /**
     * Met à jour un utilisateur existant.
     * @param user L'utilisateur à mettre à jour.
     */
    suspend fun updateUser(user: User)

    /**
     * Supprime un utilisateur par son ID.
     * @param userId L'ID de l'utilisateur à supprimer.
     */
    suspend fun deleteUser(userId: String)
}


