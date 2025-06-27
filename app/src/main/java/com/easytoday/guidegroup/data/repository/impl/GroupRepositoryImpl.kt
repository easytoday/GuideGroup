// app/src/main/java/com/easytoday/guidegroup/data/repository/impl/GroupRepositoryImpl.kt
package com.easytoday.guidegroup.data.repository.impl

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.Result // <<-- CETTE LIGNE EST CRUCIALE
import com.easytoday.guidegroup.domain.repository.GroupRepository
//import com.google.firebase.firestore.FirebaseFirestore //plus necessaire on passe par FirestoreHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val firestoreHelper: FirestoreHelper  // Tous les accès passe par FirestoreHelper
    //private val firestore: FirebaseFirestore // Injectez FirebaseFirestore si vous l'utilisez directement pour des requêtes complexes
) : GroupRepository {

    private val GROUPS_COLLECTION = "groups"

    override fun getGroup(groupId: String): Flow<Group?> {
        // Spécifiez explicitement le type générique <Group> pour l'inférence.
        // Si getDocumentAsFlow retourne un Flow<T?>, alors <Group> est le bon type.
        return firestoreHelper.getDocumentAsFlow<Group>(GROUPS_COLLECTION, groupId)
            .catch { e ->
                Timber.e("Erreur lors de la récupération du groupe $groupId: ${e.message}", e)
                emit(null) // Émet un null en cas d'erreur, respectant le Flow<Group?>
            }
    }


    // <<-- CORRECTION ICI POUR getAllGroups -->>
    override fun getAllGroups(): Flow<Result<List<Group>>> = flow {
        emit(Result.Loading) // Émet l'état de chargement
        try {
            // Appelle getCollectionAsFlow avec le type <Group> pour obtenir un Flow<List<Group>>
            firestoreHelper.getCollectionAsFlow<Group>(GROUPS_COLLECTION)
                .collect { groups -> // Collecte les résultats du Flow interne
                    emit(Result.Success(groups)) // Émet l'état de succès avec la liste
                }
        } catch (e: Exception) {
            Timber.e("Error getting all groups: ${e.message}", e)
            emit(Result.Error("Failed to get all groups.", e)) // Émet l'état d'erreur
        }
    }
    // <<-- FIN DE CORRECTION -->>

    // Supprimez le mot-clé 'suspend' ici
    suspend override fun createGroup(group: Group): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            // Utiliser firestore (l'instance injectée) au lieu de db
            val newId = group.id.ifEmpty { firestoreHelper.db.collection(GROUPS_COLLECTION).document().id } // <<-- MODIFIÉ
            val groupWithId = group.copy(id = newId)
            firestoreHelper.addDocument(GROUPS_COLLECTION, groupWithId, newId)
            Timber.d("Group '${groupWithId.name}' created with ID: $newId")
            //emit(Result.Success(Unit))
            emit(Result.Success(newId))
        } catch (e: Exception) {
            Timber.e("Error creating group: ${e.message}", e)
            emit(Result.Error("Failed to create group.", e))
        }
    }

    // Supprimez le mot-clé 'suspend' ici
    override fun updateGroup(group: Group): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            firestoreHelper.updateDocument(GROUPS_COLLECTION, group.id, group)
            Timber.d("Group '${group.name}' updated with ID: ${group.id}")
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            Timber.e("Error updating group: ${e.message}", e)
            emit(Result.Error("Failed to update group.", e))
        }
    }

    // Supprimez le mot-clé 'suspend' ici
    override fun deleteGroup(groupId: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            firestoreHelper.deleteDocument(GROUPS_COLLECTION, groupId)
            Timber.d("Group with ID '$groupId' deleted.")
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            Timber.e("Error deleting group: ${e.message}", e)
            emit(Result.Error("Failed to delete group.", e))
        }
    }

    override suspend fun addMemberToGroup(groupId: String, memberId: String) {
        try {
            val group = firestoreHelper.getDocumentAsFlow<Group>(GROUPS_COLLECTION, groupId)
                .firstOrNull()

            if (group != null && !group.memberIds.contains(memberId)) {
                val updatedMemberIds = group.memberIds + memberId
                val updatedGroup = group.copy(memberIds = updatedMemberIds)
                firestoreHelper.updateDocument(GROUPS_COLLECTION, groupId, updatedGroup)
                Timber.d("Membre $memberId ajouté au groupe $groupId.")
            } else {
                Timber.w("Le groupe $groupId n'existe pas ou le membre $memberId est déjà présent.")
            }
        } catch (e: Exception) {
            Timber.e("Erreur lors de l'ajout du membre $memberId au groupe $groupId: ${e.message}", e)
        }
    }

    override suspend fun removeMemberFromGroup(groupId: String, memberId: String) {
        try {
            val group = firestoreHelper.getDocumentAsFlow<Group>(GROUPS_COLLECTION, groupId)
                .firstOrNull()

            if (group != null && group.memberIds.contains(memberId)) {
                val updatedMemberIds = group.memberIds.filter { it != memberId }
                val updatedGroup = group.copy(memberIds = updatedMemberIds)
                firestoreHelper.updateDocument(GROUPS_COLLECTION, groupId, updatedGroup)
                Timber.d("Membre $memberId supprimé du groupe $groupId.")
            } else {
                Timber.w("Le groupe $groupId n'existe pas ou le membre $memberId n'est pas présent.")
            }
        } catch (e: Exception) {
            Timber.e("Erreur lors de la suppression du membre $memberId du groupe $groupId: ${e.message}", e)
        }
    }
}

