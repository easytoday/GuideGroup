package com.easytoday.guidegroup.data.repository.impl

import android.net.Uri
import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.model.Result
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import timber.log.Timber
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class MessageRepositoryImplTest : BehaviorSpec({

    // Initialisation de Timber pour les tests
    beforeSpec {
        if (Timber.treeCount == 0) {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    // Ne rien faire ou imprimer sur la console pour les tests
                }
            })
        }
    }

    val mockFirestoreHelper = mockk<FirestoreHelper>()
    val mockFirebaseStorage = mockk<FirebaseStorage>()
    val messageRepository = MessageRepositoryImpl(mockFirestoreHelper, mockFirebaseStorage)

    val mockFirestoreDb = mockk<FirebaseFirestore>()
    val mockCollectionRef = mockk<CollectionReference>()
    val mockDocumentRef = mockk<DocumentReference>()

    beforeEach {
        clearAllMocks()
        // Comportement de base pour FirestoreHelper
        every { mockFirestoreHelper.db } returns mockFirestoreDb
        every { mockFirestoreDb.collection(any()) } returns mockCollectionRef
        every { mockCollectionRef.document(any()) } returns mockDocumentRef
        every { mockCollectionRef.document() } returns mockDocumentRef // Pour la génération d'ID
    }

    Given("MessageRepositoryImpl") {

        When("sendMessage est appelé") {
            val groupId = "group123"
            val message = Message(senderId = "user1", senderName = "User1", text = "Hello")

            // Moquer la dépendance FirestoreHelper
            // Simule que l'appel `set` sur le document réussit
            every { mockDocumentRef.id } returns "newMessageId"
            coEvery { mockDocumentRef.set(any()).await() } returns mockk() // .await() renvoie Void

            Then("il devrait émettre Result.Loading puis Result.Success") {
                runTest {
                    val results = messageRepository.sendMessage(groupId, message).toList()

                    results[0].shouldBeInstanceOf<Result.Loading>()
                    results[1].shouldBeInstanceOf<Result.Success<Unit>>()

                    // Vérifier que la méthode `set` a été appelée sur le document avec le bon message
                    coVerify(exactly = 1) {
                        mockDocumentRef.set(message.copy(id = "newMessageId"))
                    }
                }
            }
        }

        When("uploadMedia est appelé avec succès") {
            val testUri = mockk<Uri>()
            val mediaType = Message.MediaType.IMAGE
            val groupId = "group123"
            val expectedDownloadUrl = "http://example.com/media.jpg"

            // Moquer les dépendances de Firebase Storage
            val mockStorageRef = mockk<StorageReference>(relaxed = true)
            val mockUploadTaskSnapshot = mockk<UploadTask.TaskSnapshot>()
            val mockDownloadUrlTask = mockk<Task<Uri>>()
            val mockFinalUri = mockk<Uri>()

            every { mockFirebaseStorage.reference } returns mockStorageRef
            coEvery { mockStorageRef.putFile(testUri).await() } returns mockUploadTaskSnapshot
            every { mockUploadTaskSnapshot.storage.downloadUrl } returns mockDownloadUrlTask
            coEvery { mockDownloadUrlTask.await() } returns mockFinalUri
            every { mockFinalUri.toString() } returns expectedDownloadUrl

            Then("il devrait émettre Result.Loading puis Result.Success avec l'URL") {
                runTest {
                    val results = messageRepository.uploadMedia(testUri, mediaType, groupId).toList()

                    results[0].shouldBeInstanceOf<Result.Loading>()
                    val successResult = results[1] as Result.Success<String>
                    successResult.data shouldBe expectedDownloadUrl

                    coVerify(exactly = 1) { mockStorageRef.putFile(testUri) }
                }
            }
        }

        // ... autres tests ...
    }
})