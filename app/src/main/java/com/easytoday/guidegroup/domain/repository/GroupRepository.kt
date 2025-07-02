// app/src/main/java/com/easytoday/guidegroup/domain/repository/GroupRepository.kt
package com.easytoday.guidegroup.domain.repository

import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.Group
import kotlinx.coroutines.flow.Flow

/**
 * Interface du référentiel pour les opérations liées aux groupes.
 * Définit les méthodes pour interagir avec les données des groupes.
 */
interface GroupRepository {

    fun getGroup(groupId: String): Flow<Group?>

    fun getAllGroups(): Flow<Result<List<Group>>>

    // CORRIGÉ : Les opérations d'écriture sont maintenant des fonctions suspendues.
    suspend fun createGroup(group: Group): Result<String>

    suspend fun updateGroup(group: Group): Result<Unit>

    suspend fun deleteGroup(groupId: String): Result<Unit>

    suspend fun addMemberToGroup(groupId: String, userId: String)

    suspend fun removeMemberFromGroup(groupId: String, userId: String)
}