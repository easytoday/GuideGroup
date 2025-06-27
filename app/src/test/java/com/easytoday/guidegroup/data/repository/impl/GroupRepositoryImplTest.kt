package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.Result
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class GroupRepositoryImplTest : BehaviorSpec({
    val mockFirestoreHelper = mockk<FirestoreHelper>()
    val mockFirebaseFirestore = mockk<FirebaseFirestore>(relaxed = true)
    val mockCollectionReference = mockk<CollectionReference>(relaxed = true)

    // CORRECTION FINALE : Le constructeur ne prend qu'un seul argument.
    val groupRepository = GroupRepositoryImpl(mockFirestoreHelper)

    beforeEach {
        clearAllMocks()
        // Le repository accède à `firestoreHelper.db`. Nous devons donc moquer cet accès.
        every { mockFirestoreHelper.db } returns mockFirebaseFirestore
        every { mockFirebaseFirestore.collection(any()) } returns mockCollectionReference
    }

    Given("GroupRepositoryImpl") {

        When("getGroup est appelé pour un groupe existant") {
            val groupId = "testGroupId"
            val testGroup = Group(id = groupId, name = "Test Group")
            coEvery { mockFirestoreHelper.getDocumentAsFlow<Group>("groups", groupId) } returns flowOf(testGroup)

            Then("il devrait émettre le groupe") {
                runTest {
                    val result = groupRepository.getGroup(groupId).first()
                    result shouldBe testGroup
                    coVerify(exactly = 1) { mockFirestoreHelper.getDocumentAsFlow<Group>("groups", groupId) }
                }
            }
        }

        When("getAllGroups est appelé avec succès") {
            val testGroups = listOf(Group(id = "g1", name = "Group A"))
            coEvery { mockFirestoreHelper.getCollectionAsFlow<Group>("groups") } returns flowOf(testGroups)

            Then("il devrait émettre Loading puis Success avec la liste des groupes") {
                runTest {
                    val results = groupRepository.getAllGroups().toList()
                    results[0].shouldBeInstanceOf<Result.Loading>()
                    val successResult = results[1] as Result.Success<*>
                    successResult.data shouldBe testGroups
                    coVerify(exactly = 1) { mockFirestoreHelper.getCollectionAsFlow<Group>("groups") }
                }
            }
        }

        When("createGroup est appelé") {
            val newGroup = Group(name = "New Group")
            // Moquer la génération d'ID
            every { mockCollectionReference.document().id } returns "newGroupId"
            coEvery { mockFirestoreHelper.addDocument(any(), any(), any()) } just Runs

            Then("il devrait émettre Loading puis Success et retourner le nouvel ID") {
                runTest {
                    val results = groupRepository.createGroup(newGroup).toList()
                    results[0].shouldBeInstanceOf<Result.Loading>()
                    val successResult = results[1] as Result.Success<String>
                    successResult.data shouldBe "newGroupId"
                    // Vérifier que la méthode addDocument a été appelée avec le bon groupe (avec l'ID mis à jour)
                    coVerify(exactly = 1) { mockFirestoreHelper.addDocument("groups", newGroup.copy(id = "newGroupId"), "newGroupId") }
                }
            }
        }
    }
})