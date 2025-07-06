package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.data.local.PointOfInterestDao
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

    // CORRECTION : Ajouter les mocks pour les nouvelles dépendances
    val mockPoiDao = mockk<PointOfInterestDao>(relaxed = true)
    val mockFirestoreHelper = mockk<FirestoreHelper>(relaxed = true)
    // CORRECTION : Mettre à jour l'instanciation
    val pointOfInterestRepository = PointOfInterestRepositoryImpl(mockPoiDao, mockFirestoreHelper)

    val mockFirestoreDb = mockk<FirebaseFirestore>(relaxed = true)
    val mockCollectionRef = mockk<CollectionReference>(relaxed = true)
    val mockDocumentRef = mockk<DocumentReference>(relaxed = true)

    beforeEach {
        clearAllMocks()
        every { mockFirestoreHelper.db } returns mockFirestoreDb
        every { mockFirestoreDb.collection(any()) } returns mockCollectionRef
        every { mockCollectionRef.document() } returns mockDocumentRef
        every { mockDocumentRef.id } returns "newPoiId"
    }

    Given("PointOfInterestRepositoryImpl") {

        When("addPointOfInterest est appelé") {
            val newPoi = PointOfInterest(name = "New POI", groupId = "group1")
            val poiWithId = newPoi.copy(id = "newPoiId")

            coEvery { mockFirestoreHelper.addDocument("pointsOfInterest", poiWithId, "newPoiId") } just Runs

            Then("il devrait ajouter le POI sur Firestore et retourner son ID") {
                runTest {
                    val resultId = pointOfInterestRepository.addPointOfInterest(newPoi)
                    resultId shouldBe "newPoiId"
                    coVerify(exactly = 1) { mockFirestoreHelper.addDocument("pointsOfInterest", poiWithId, "newPoiId") }
                }
            }
        }
    }
})