// app/src/main/java/com/easytoday/guidegroup/domain/repository/GroupRepository.kt
package com.easytoday.guidegroup.domain.repository


import com.easytoday.guidegroup.domain.model.Result // <<-- CETTE LIGNE EST CRUCIALE POUR QUE LE TYPE 'Result' SOIT RECONNU CORRECTEMENT

import com.easytoday.guidegroup.domain.model.Group
import kotlinx.coroutines.flow.Flow

/**
 * Interface du référentiel pour les opérations liées aux groupes.
 * Définit les méthodes pour interagir avec les données des groupes.
 */
interface GroupRepository {

    /**
     * Récupère un groupe par son ID.
     * @param groupId L'ID du groupe à récupérer.
     * @return Un flux (Flow) du groupe correspondant, ou null si non trouvé.
     */
    fun getGroup(groupId: String): Flow<Group?>


    /**
     * Récupère tous les groupes.
     * @return Un flux (Flow) d'une liste groupe correspondant, ou null si non trouvé.
     */
    fun getAllGroups(): Flow<Result<List<Group>>> // Obtenir tous les groupes


    /**
     * Récupère tous les groupes auxquels un utilisateur appartient.
     * @param userId L'ID de l'utilisateur.
     * @return Un flux (Flow) d'une liste de groupes.
     */
    //fun getGroupsForUser(userId: String): Flow<List<Group>>

    /**
     * Ajoute un nouveau groupe.
     * @param group Le groupe à ajouter.
     * @return L'ID du groupe ajouté.
     */
    suspend fun createGroup(group: Group): Flow<Result<String>>//Flow<Result<Unit>>

    /**
     * Met à jour un groupe existant.
     * @param group Le groupe à mettre à jour.
     */
    fun updateGroup(group: Group) : Flow<Result<Unit>>

    /**
     * Supprime un groupe par son ID.
     * @param groupId L'ID du groupe à supprimer.
     */
    fun deleteGroup(groupId: String) : Flow<Result<Unit>>

    /**
     * Ajoute un membre à un groupe.
     * @param groupId L'ID du groupe.
     * @param userId L'ID de l'utilisateur à ajouter.
     */
    suspend fun addMemberToGroup(groupId: String, userId: String)

    /**
     * Supprime un membre d'un groupe.
     * @param groupId L'ID du groupe.
     * @param userId L'ID de l'utilisateur à supprimer.
     */
    suspend fun removeMemberFromGroup(groupId: String, userId: String)
}


