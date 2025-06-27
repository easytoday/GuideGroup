package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User // Pour simuler l'ajout de membres
import com.easytoday.guidegroup.domain.repository.GroupRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Implémentation factice de GroupRepository pour l'environnement de test (mock).
// Simule les opérations sur les groupes et leurs membres.
class FakeGroupRepositoryImpl @Inject constructor() : GroupRepository {

    // Simule une base de données de groupes en mémoire
    private val fakeGroupsDb = MutableStateFlow<MutableMap<String, Group>>(mutableMapOf())

    // Initialise avec quelques groupes factices pour les tests
    init {
        val group1 = Group(
            id = "group1_id",
            name = "Randonnée Montagne",
            description = "Exploration des sentiers alpins.",
            creatorId = "user1_id", // Simule l'ID d'un créateur
            memberIds = listOf("user1_id", "user2_id") // Simule des membres
        )
        val group2 = Group(
            id = "group2_id",
            name = "Visite Paris",
            description = "Découverte des monuments de Paris.",
            creatorId = "user3_id",
            memberIds = listOf("user3_id")
        )
        fakeGroupsDb.value[group1.id] = group1
        fakeGroupsDb.value[group2.id] = group2
    }

    // --- Méthodes de lecture (GET) ---

    // Récupère un groupe par son ID
    override fun getGroup(groupId: String): Flow<Group?> {
        return fakeGroupsDb.map { groupsMap ->
            delay(300) // Simule un délai
            groupsMap[groupId]
        }
    }

    // Récupère tous les groupes
    override fun getAllGroups(): Flow<Result<List<Group>>> = flow {
        emit(Result.Loading)
        delay(500) // Simule un délai réseau
        emit(Result.Success(fakeGroupsDb.value.values.toList()))
    }

    // --- Méthodes d'écriture (CRUD) ---

    // Crée un nouveau groupe
    suspend override fun createGroup(group: Group): Flow<Result<String>> = flow {
        emit(Result.Loading)
        delay(400) // Simule un délai
        val newId = group.id.ifEmpty { "fakeGroupId_${System.currentTimeMillis()}" }
        val groupWithId = group.copy(id = newId)
        fakeGroupsDb.value[groupWithId.id] = groupWithId
        emit(Result.Success(newId))
    }

    // Met à jour un groupe existant
    override fun updateGroup(group: Group): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        delay(400) // Simule un délai
        if (fakeGroupsDb.value.containsKey(group.id)) {
            fakeGroupsDb.value[group.id] = group
            emit(Result.Success(Unit))
        } else {
            emit(Result.Error("Le groupe avec l'ID ${group.id} n'existe pas pour la mise à jour.", Exception("Group not found.")))
        }
    }

    // Supprime un groupe par son ID
    override fun deleteGroup(groupId: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        delay(400) // Simule un délai
        if (fakeGroupsDb.value.containsKey(groupId)) {
            fakeGroupsDb.value.remove(groupId)
            emit(Result.Success(Unit))
        } else {
            emit(Result.Error("Le groupe avec l'ID $groupId n'existe pas pour la suppression.", Exception("Group not found.")))
        }
    }

    // Ajoute un membre à un groupe
    override suspend fun addMemberToGroup(groupId: String, memberId: String) {
        delay(300) // Simule un délai
        val currentGroups = fakeGroupsDb.value
        val group = currentGroups[groupId]
        if (group != null && !group.memberIds.contains(memberId)) {
            val updatedMemberIds = group.memberIds + memberId
            val updatedGroup = group.copy(memberIds = updatedMemberIds)
            currentGroups[groupId] = updatedGroup
            fakeGroupsDb.value = currentGroups // Déclenche la mise à jour du Flow
        } else if (group == null) {
            // Optionnel: lancer une exception ou logguer si le groupe n'existe pas
            // throw Exception("Group with ID $groupId not found.")
        } else if (group.memberIds.contains(memberId)) {
            // Optionnel: logguer si le membre est déjà présent
            // Timber.w("Member $memberId already in group $groupId.")
        }
    }

    // Supprime un membre d'un groupe
    override suspend fun removeMemberFromGroup(groupId: String, memberId: String) {
        delay(300) // Simule un délai
        val currentGroups = fakeGroupsDb.value
        val group = currentGroups[groupId]
        if (group != null && group.memberIds.contains(memberId)) {
            val updatedMemberIds = group.memberIds.filter { it != memberId }
            val updatedGroup = group.copy(memberIds = updatedMemberIds)
            currentGroups[groupId] = updatedGroup
            fakeGroupsDb.value = currentGroups // Déclenche la mise à jour du Flow
        } else if (group == null) {
            // Optionnel: lancer une exception ou logguer si le groupe n'existe pas
            // throw Exception("Group with ID $groupId not found.")
        } else if (!group.memberIds.contains(memberId)) {
            // Optionnel: logguer si le membre n'est pas présent
            // Timber.w("Member $memberId not in group $groupId.")
        }
    }
}

