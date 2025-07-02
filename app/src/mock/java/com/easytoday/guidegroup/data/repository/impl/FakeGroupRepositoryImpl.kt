package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.repository.GroupRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FakeGroupRepositoryImpl @Inject constructor() : GroupRepository {

    private val fakeGroupsDb = MutableStateFlow<MutableMap<String, Group>>(mutableMapOf())

    init {
        val group1 = Group(
            id = "group1_id",
            name = "Randonnée Montagne",
            description = "Exploration des sentiers alpins.",
            creatorId = "user1_id",
            memberIds = listOf("user1_id", "user2_id")
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

    override fun getGroup(groupId: String): Flow<Group?> {
        return fakeGroupsDb.map { groupsMap ->
            delay(300)
            groupsMap[groupId]
        }
    }

    override fun getAllGroups(): Flow<Result<List<Group>>> = flow {
        emit(Result.Loading)
        delay(500)
        emit(Result.Success(fakeGroupsDb.value.values.toList()))
    }

    // CORRIGÉ : Implémentation de la fonction suspendue.
    override suspend fun createGroup(group: Group): Result<String> {
        delay(400)
        val newId = group.id.ifEmpty { "fakeGroupId_${System.currentTimeMillis()}" }
        val groupWithId = group.copy(id = newId)
        val currentGroups = fakeGroupsDb.value
        currentGroups[groupWithId.id] = groupWithId
        fakeGroupsDb.value = currentGroups
        return Result.Success(newId)
    }

    // CORRIGÉ : Implémentation de la fonction suspendue.
    override suspend fun updateGroup(group: Group): Result<Unit> {
        delay(400)
        return if (fakeGroupsDb.value.containsKey(group.id)) {
            val currentGroups = fakeGroupsDb.value
            currentGroups[group.id] = group
            fakeGroupsDb.value = currentGroups
            Result.Success(Unit)
        } else {
            Result.Error("Le groupe avec l'ID ${group.id} n'existe pas.", Exception("Group not found."))
        }
    }

    // CORRIGÉ : Implémentation de la fonction suspendue.
    override suspend fun deleteGroup(groupId: String): Result<Unit> {
        delay(400)
        return if (fakeGroupsDb.value.containsKey(groupId)) {
            val currentGroups = fakeGroupsDb.value
            currentGroups.remove(groupId)
            fakeGroupsDb.value = currentGroups
            Result.Success(Unit)
        } else {
            Result.Error("Le groupe avec l'ID $groupId n'existe pas.", Exception("Group not found."))
        }
    }

    override suspend fun addMemberToGroup(groupId: String, memberId: String) {
        delay(300)
        val currentGroups = fakeGroupsDb.value
        val group = currentGroups[groupId]
        if (group != null && !group.memberIds.contains(memberId)) {
            val updatedMemberIds = group.memberIds + memberId
            val updatedGroup = group.copy(memberIds = updatedMemberIds)
            currentGroups[groupId] = updatedGroup
            fakeGroupsDb.value = currentGroups
        }
    }

    override suspend fun removeMemberFromGroup(groupId: String, memberId: String) {
        delay(300)
        val currentGroups = fakeGroupsDb.value
        val group = currentGroups[groupId]
        if (group != null && group.memberIds.contains(memberId)) {
            val updatedMemberIds = group.memberIds.filter { it != memberId }
            val updatedGroup = group.copy(memberIds = updatedMemberIds)
            currentGroups[groupId] = updatedGroup
            fakeGroupsDb.value = currentGroups
        }
    }
}