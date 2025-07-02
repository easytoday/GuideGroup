// app/src/main/java/com/easytoday/guidegroup/data/repository/impl/GroupRepositoryImpl.kt
package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val firestoreHelper: FirestoreHelper
) : GroupRepository {

    private val GROUPS_COLLECTION = "groups"

    override fun getGroup(groupId: String): Flow<Group?> {
        return firestoreHelper.getDocumentAsFlow<Group>(GROUPS_COLLECTION, groupId)
            .catch { e ->
                Timber.e(e, "Erreur lors de la récupération du groupe $groupId")
                emit(null)
            }
    }

    // CORRIGÉ : Suppression de l'anti-pattern flow { collect }.
    override fun getAllGroups(): Flow<Result<List<Group>>> {
        return firestoreHelper.getCollectionAsFlow<Group>(GROUPS_COLLECTION)
            .map<List<Group>, Result<List<Group>>> { groups -> Result.Success(groups) }
            .catch { e ->
                Timber.e(e, "Error getting all groups")
                emit(Result.Error("Failed to get all groups.", e))
            }
    }

    // CORRIGÉ : Implémentation de la fonction suspendue, sans Flow builder.
    override suspend fun createGroup(group: Group): Result<String> {
        return try {
            val newId = group.id.ifEmpty { firestoreHelper.db.collection(GROUPS_COLLECTION).document().id }
            val groupWithId = group.copy(id = newId)
            firestoreHelper.addDocument(GROUPS_COLLECTION, groupWithId, newId)
            Timber.d("Group '${groupWithId.name}' created with ID: $newId")
            Result.Success(newId)
        } catch (e: Exception) {
            Timber.e(e, "Error creating group")
            Result.Error("Failed to create group.", e)
        }
    }

    // CORRIGÉ : Implémentation de la fonction suspendue.
    override suspend fun updateGroup(group: Group): Result<Unit> {
        return try {
            firestoreHelper.updateDocument(GROUPS_COLLECTION, group.id, group)
            Timber.d("Group '${group.name}' updated with ID: ${group.id}")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating group")
            Result.Error("Failed to update group.", e)
        }
    }

    // CORRIGÉ : Implémentation de la fonction suspendue.
    override suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            firestoreHelper.deleteDocument(GROUPS_COLLECTION, groupId)
            Timber.d("Group with ID '$groupId' deleted.")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting group")
            Result.Error("Failed to delete group.", e)
        }
    }

    override suspend fun addMemberToGroup(groupId: String, memberId: String) {
        try {
            val group = getGroup(groupId).firstOrNull()
            if (group != null && !group.memberIds.contains(memberId)) {
                val updatedMemberIds = group.memberIds + memberId
                val updatedGroup = group.copy(memberIds = updatedMemberIds)
                firestoreHelper.updateDocument(GROUPS_COLLECTION, groupId, updatedGroup)
                Timber.d("Membre $memberId ajouté au groupe $groupId.")
            } else {
                Timber.w("Le groupe $groupId n'existe pas ou le membre $memberId est déjà présent.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de l'ajout du membre $memberId au groupe $groupId: ${e.message}", e)
        }
    }

    override suspend fun removeMemberFromGroup(groupId: String, memberId: String) {
        try {
            val group = getGroup(groupId).firstOrNull()
            if (group != null && group.memberIds.contains(memberId)) {
                val updatedMemberIds = group.memberIds.filter { it != memberId }
                val updatedGroup = group.copy(memberIds = updatedMemberIds)
                firestoreHelper.updateDocument(GROUPS_COLLECTION, groupId, updatedGroup)
                Timber.d("Membre $memberId supprimé du groupe $groupId.")
            } else {
                Timber.w("Le groupe $groupId n'existe pas ou le membre $memberId n'est pas présent.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la suppression du membre $memberId du groupe $groupId: ${e.message}", e)
        }
    }
}