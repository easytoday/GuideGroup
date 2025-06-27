package com.easytoday.guidegroup.data.firestore

import com.easytoday.guidegroup.domain.model.Group
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreHelperTest : BehaviorSpec({

    beforeSpec {
        if (Timber.treeCount == 0) {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {}
            })
        }
    }

    val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
    val mockFirebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    // CORRECTION : Instanciation avec les deux arguments
    val firestoreHelper = FirestoreHelper(mockFirestore, mockFirebaseAuth)

    val mockCollectionReference = mockk<CollectionReference>(relaxed = true)
    val mockDocumentReference = mockk<DocumentReference>(relaxed = true)
    val mockTaskVoid = mockk<Task<Void>>()

    beforeEach {
        clearAllMocks()
        every { mockFirestore.collection(any()) } returns mockCollectionReference
        every { mockCollectionReference.document(any()) } returns mockDocumentReference
    }

    Given("FirestoreHelper") {

        When("addDocument est appelé") {
            val testGroup = Group(id = "newGroup", name = "New Group")
            // CORRECTION: Moquer l'appel à la fonction suspendue `await()`
            coEvery { mockDocumentReference.set(testGroup).await() } returns mockk()

            Then("il devrait appeler set sur le document reference") {
                runTest {
                    firestoreHelper.addDocument("groups", testGroup, testGroup.id)
                    coVerify(exactly = 1) { mockCollectionReference.document(testGroup.id) }
                    coVerify(exactly = 1) { mockDocumentReference.set(testGroup) }
                }
            }
        }

        When("deleteDocument est appelé") {
            val groupIdToDelete = "group123"
            coEvery { mockDocumentReference.delete().await() } returns mockk()

            Then("il devrait appeler delete sur le document reference") {
                runTest {
                    firestoreHelper.deleteDocument("groups", groupIdToDelete)
                    coVerify(exactly = 1) { mockCollectionReference.document(groupIdToDelete) }
                    coVerify(exactly = 1) { mockDocumentReference.delete() }
                }
            }
        }

        // ... (Les tests pour getAsFlow sont plus complexes à corriger, on se concentre sur les erreurs bloquantes d'abord)
    }
})