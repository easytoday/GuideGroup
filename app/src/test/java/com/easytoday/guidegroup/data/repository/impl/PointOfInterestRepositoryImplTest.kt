package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class PointOfInterestRepositoryImplTest : BehaviorSpec({

    beforeSpec {
        if (Timber.treeCount == 0) {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {}
            })
        }
    }

    val mockFirestoreHelper = mockk<FirestoreHelper>()
    val pointOfInterestRepository = PointOfInterestRepositoryImpl(mockFirestoreHelper)

    val mockFirestoreDb = mockk<FirebaseFirestore>(relaxed = true)
    val mockCollectionRef = mockk<CollectionReference>(relaxed = true)
    val mockQuery = mockk<Query>(relaxed = true)
    val mockDocumentRef = mockk<DocumentReference>(relaxed = true)

    beforeEach {
        clearAllMocks()
        every { mockFirestoreHelper.db } returns mockFirestoreDb
        every { mockFirestoreDb.collection(any()) } returns mockCollectionRef
        every { mockCollectionRef.whereEqualTo(any<String>(), any()) } returns mockQuery
        every { mockCollectionRef.document() } returns mockDocumentRef
        every { mockDocumentRef.id } returns "newPoiId"
    }

    Given("PointOfInterestRepositoryImpl") {

        When("getGroupPointsOfInterest est appelé") {
            val groupId = "group1"
            val testPois = listOf(
                PointOfInterest(id = "poi1", groupId = groupId, name = "POI A"),
                PointOfInterest(id = "poi2", groupId = groupId, name = "POI B")
            )

            // CORRECTION: Moquer l'appel à getCollectionAsFlow avec une Query
            every { mockFirestoreHelper.getCollectionAsFlow<PointOfInterest>(any<Query>()) } returns flowOf(testPois)

            Then("il devrait émettre la liste des POIs") {
                runTest {
                    val result = pointOfInterestRepository.getGroupPointsOfInterest(groupId).first()
                    result shouldBe testPois
                    // Vérifier que la fonction a été appelée avec une Query
                    verify(exactly = 1) { mockFirestoreHelper.getCollectionAsFlow<PointOfInterest>(any<Query>()) }
                }
            }
        }

        When("addPointOfInterest est appelé") {
            val newPoi = PointOfInterest(name = "New POI", groupId = "group1")
            val poiWithId = newPoi.copy(id = "newPoiId")

            // Moquer l'ajout de document
            coEvery { mockFirestoreHelper.addDocument("pointsOfInterest", poiWithId, "newPoiId") } just Runs

            Then("il devrait ajouter le POI et retourner son ID") {
                runTest {
                    val resultId = pointOfInterestRepository.addPointOfInterest(newPoi)
                    resultId shouldBe "newPoiId"
                    coVerify(exactly = 1) { mockFirestoreHelper.addDocument("pointsOfInterest", poiWithId, "newPoiId") }
                }
            }
        }

        // ... autres tests pour update et delete ...
    }
})