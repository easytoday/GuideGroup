package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.MeetingPoint
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import timber.log.Timber
import java.lang.Exception

@OptIn(ExperimentalCoroutinesApi::class)
class MeetingPointRepositoryImplTest : BehaviorSpec({

    beforeSpec {
        if (Timber.treeCount == 0) {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    // Ne rien faire
                }
            })
        }
    }

    val mockFirestoreHelper = mockk<FirestoreHelper>()
    val meetingPointRepository = MeetingPointRepositoryImpl(mockFirestoreHelper)

    beforeEach {
        clearAllMocks()
    }

    Given("MeetingPointRepositoryImpl") {

        When("getGroupMeetingPoint est appelé") {
            val groupId = "group1"
            val testMeetingPoint = MeetingPoint(groupId = groupId, latitude = 10.0, longitude = 20.0)

            And("le point de rencontre existe") {
                coEvery { mockFirestoreHelper.getDocumentAsFlow<MeetingPoint>("meetingPoints", groupId) } returns flowOf(testMeetingPoint)

                Then("il devrait émettre le point de rencontre") {
                    runTest {
                        val result = meetingPointRepository.getGroupMeetingPoint(groupId).first()
                        result shouldBe testMeetingPoint
                        coVerify(exactly = 1) { mockFirestoreHelper.getDocumentAsFlow<MeetingPoint>("meetingPoints", groupId) }
                    }
                }
            }

            And("le point de rencontre n'existe pas") {
                coEvery { mockFirestoreHelper.getDocumentAsFlow<MeetingPoint>("meetingPoints", groupId) } returns flowOf(null)

                Then("il devrait émettre null") {
                    runTest {
                        val result = meetingPointRepository.getGroupMeetingPoint(groupId).first()
                        result shouldBe null
                        coVerify(exactly = 1) { mockFirestoreHelper.getDocumentAsFlow<MeetingPoint>("meetingPoints", groupId) }
                    }
                }
            }

            And("une erreur se produit") {
                val exception = Exception("Meeting point fetch error")
                coEvery { mockFirestoreHelper.getDocumentAsFlow<MeetingPoint>("meetingPoints", groupId) } throws exception

                Then("il devrait émettre null") {
                    runTest {
                        val result = meetingPointRepository.getGroupMeetingPoint(groupId).first()
                        result shouldBe null
                    }
                }
            }
        }

        When("setMeetingPoint est appelé") {
            val meetingPointToSet = MeetingPoint(groupId = "group1", latitude = 30.0, longitude = 40.0)

            And("la définition/mise à jour réussit") {
                coEvery { mockFirestoreHelper.addDocument("meetingPoints", meetingPointToSet, meetingPointToSet.groupId) } just Runs

                Then("il devrait appeler addDocument sur FirestoreHelper") {
                    runTest {
                        meetingPointRepository.setMeetingPoint(meetingPointToSet)
                        coVerify(exactly = 1) { mockFirestoreHelper.addDocument("meetingPoints", meetingPointToSet, meetingPointToSet.groupId) }
                    }
                }
            }

            And("une erreur se produit") {
                val exception = Exception("Failed to set meeting point")
                coEvery { mockFirestoreHelper.addDocument(any(), any(), any()) } throws exception

                Then("il devrait gérer l'erreur (via Timber)") {
                    runTest {
                        meetingPointRepository.setMeetingPoint(meetingPointToSet)
                        coVerify(exactly = 1) { mockFirestoreHelper.addDocument("meetingPoints", meetingPointToSet, meetingPointToSet.groupId) }
                    }
                }
            }
        }

        When("deleteMeetingPoint est appelé") {
            val groupIdToDelete = "group1"

            And("la suppression réussit") {
                coEvery { mockFirestoreHelper.deleteDocument("meetingPoints", groupIdToDelete) } just Runs

                Then("il devrait appeler deleteDocument sur FirestoreHelper") {
                    runTest {
                        meetingPointRepository.deleteMeetingPoint(groupIdToDelete)
                        coVerify(exactly = 1) { mockFirestoreHelper.deleteDocument("meetingPoints", groupIdToDelete) }
                    }
                }
            }

            And("une erreur se produit") {
                val exception = Exception("Failed to delete meeting point")
                coEvery { mockFirestoreHelper.deleteDocument(any(), any()) } throws exception

                Then("il devrait gérer l'erreur (via Timber)") {
                    runTest {
                        meetingPointRepository.deleteMeetingPoint(groupIdToDelete)
                        coVerify(exactly = 1) { mockFirestoreHelper.deleteDocument("meetingPoints", groupIdToDelete) }
                    }
                }
            }
        }
    }
})


