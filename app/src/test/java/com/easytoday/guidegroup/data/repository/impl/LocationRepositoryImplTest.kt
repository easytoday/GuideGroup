package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.Location
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
class LocationRepositoryImplTest : BehaviorSpec({

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
    val locationRepository = LocationRepositoryImpl(mockFirestoreHelper)

    beforeEach {
        clearAllMocks()
    }

    Given("LocationRepositoryImpl") {

        When("getUserLocation est appelé") {
            val userId = "user1"
            val testLocation = Location(userId = userId, latitude = 1.0, longitude = 2.0)

            And("la localisation existe") {
                coEvery { mockFirestoreHelper.getDocumentAsFlow<Location>("locations", userId) } returns flowOf(testLocation)

                Then("il devrait émettre la localisation") {
                    runTest {
                        val result = locationRepository.getUserLocation(userId).first()
                        result shouldBe testLocation
                        coVerify(exactly = 1) { mockFirestoreHelper.getDocumentAsFlow<Location>("locations", userId) }
                    }
                }
            }

            And("la localisation n'existe pas") {
                coEvery { mockFirestoreHelper.getDocumentAsFlow<Location>("locations", userId) } returns flowOf(null)

                Then("il devrait émettre null") {
                    runTest {
                        val result = locationRepository.getUserLocation(userId).first()
                        result shouldBe null
                        coVerify(exactly = 1) { mockFirestoreHelper.getDocumentAsFlow<Location>("locations", userId) }
                    }
                }
            }

            And("une erreur se produit") {
                val exception = Exception("Location fetch error")
                coEvery { mockFirestoreHelper.getDocumentAsFlow<Location>("locations", userId) } throws exception

                Then("il devrait émettre null") {
                    runTest {
                        val result = locationRepository.getUserLocation(userId).first()
                        result shouldBe null
                    }
                }
            }
        }

        When("updateLocation est appelé") {
            val locationToUpdate = Location(userId = "user1", latitude = 3.0, longitude = 4.0)

            And("la mise à jour réussit") {
                coEvery { mockFirestoreHelper.addDocument("locations", locationToUpdate, locationToUpdate.userId) } just Runs

                Then("il devrait appeler addDocument sur FirestoreHelper") {
                    runTest {
                        locationRepository.updateLocation(locationToUpdate)
                        coVerify(exactly = 1) { mockFirestoreHelper.addDocument("locations", locationToUpdate, locationToUpdate.userId) }
                    }
                }
            }

            And("une erreur se produit") {
                val exception = Exception("Failed to update location")
                coEvery { mockFirestoreHelper.addDocument(any(), any(), any()) } throws exception

                Then("il devrait gérer l'erreur (via Timber)") {
                    runTest {
                        locationRepository.updateLocation(locationToUpdate)
                        coVerify(exactly = 1) { mockFirestoreHelper.addDocument("locations", locationToUpdate, locationToUpdate.userId) }
                    }
                }
            }
        }

        When("getMemberLocations est appelé") {
            val groupId = "group1"
            val memberIds = listOf("user1", "user2", "user3")
            val allLocations = listOf(
                Location(userId = "user1", latitude = 10.0, longitude = 20.0),
                Location(userId = "user2", latitude = 11.0, longitude = 21.0),
                Location(userId = "user4", latitude = 12.0, longitude = 22.0) // Not in memberIds
            )
            val expectedLocations = listOf(
                Location(userId = "user1", latitude = 10.0, longitude = 20.0),
                Location(userId = "user2", latitude = 11.0, longitude = 21.0)
            )

            And("il y a des localisations pour les membres") {
                coEvery { mockFirestoreHelper.getCollectionAsFlow<Location>("locations") } returns flowOf(allLocations)

                Then("il devrait émettre les localisations filtrées pour les membres") {
                    runTest {
                        val result = locationRepository.getMemberLocations(groupId, memberIds).first()
                        result shouldBe expectedLocations
                        coVerify(exactly = 1) { mockFirestoreHelper.getCollectionAsFlow<Location>("locations") }
                    }
                }
            }

            And("aucune localisation n'est trouvée pour les membres") {
                coEvery { mockFirestoreHelper.getCollectionAsFlow<Location>("locations") } returns flowOf(emptyList())

                Then("il devrait émettre une liste vide") {
                    runTest {
                        val result = locationRepository.getMemberLocations(groupId, memberIds).first()
                        result shouldBe emptyList()
                    }
                }
            }

            And("une erreur se produit") {
                val exception = Exception("Collection fetch error")
                coEvery { mockFirestoreHelper.getCollectionAsFlow<Location>("locations") } throws exception

                Then("il devrait émettre une liste vide") {
                    runTest {
                        val result = locationRepository.getMemberLocations(groupId, memberIds).first()
                        result shouldBe emptyList()
                    }
                }
            }
        }
    }
})


