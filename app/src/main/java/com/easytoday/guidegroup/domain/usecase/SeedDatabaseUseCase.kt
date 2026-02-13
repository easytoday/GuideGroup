// app/src/main/java/com/easytoday/guidegroup/domain/usecase/SeedDatabaseUseCase.kt
package com.easytoday.guidegroup.domain.usecase

import android.content.Context
import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.repository.GroupRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// Classe temporaire pour le parsing JSON
private data class MockUser(val username: String, val email: String, val isGuide: Boolean)
private data class MockGroup(val name: String, val description: String)

class SeedDatabaseUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke() {
        // Étape 1 : Lire les fichiers JSON depuis le dossier assets
        val gson = Gson()
        val mockUsersJson = context.assets.open("mock-users.json").bufferedReader().use { it.readText() }
        val mockGroupsJson = context.assets.open("mock-groups.json").bufferedReader().use { it.readText() }

        val userListType = object : TypeToken<List<MockUser>>() {}.type
        val groupListType = object : TypeToken<List<MockGroup>>() {}.type

        val mockUsers: List<MockUser> = gson.fromJson(mockUsersJson, userListType)
        val mockGroups: List<MockGroup> = gson.fromJson(mockGroupsJson, groupListType)

        val createdUsers = mutableListOf<User>()

        // Étape 2 : Créer les utilisateurs dans Firebase Auth ET Firestore
        mockUsers.forEach { mockUser ->
            // signUp retourne un Flow, donc .first() est ok.
            val result = authRepository.signUp(
                email = mockUser.email,
                password = "password123", // Mot de passe par défaut pour tous les utilisateurs de test
                username = mockUser.username,
                isGuide = mockUser.isGuide
            ).first { it !is com.easytoday.guidegroup.domain.model.Result.Loading } // Attendre la fin

            if (result is com.easytoday.guidegroup.domain.model.Result.Success) {
                createdUsers.add(result.data)
            }
        }

        // Étape 3 : Créer les groupes dans Firestore avec les utilisateurs créés
        val guide = createdUsers.firstOrNull { it.isGuide }
        if (guide == null || createdUsers.size < 2) return // S'assurer qu'on a assez d'utilisateurs

        mockGroups.forEach { mockGroup ->
            val group = Group(
                name = mockGroup.name,
                description = mockGroup.description,
                creatorId = guide.id,
                memberIds = createdUsers.map { it.id }.shuffled().take(2) // Prend 2 membres au hasard
            )
            // CORRECTION : createGroup est maintenant une fonction suspendue directe. On retire .first().
            groupRepository.createGroup(group)
        }
    }
}
