package com.easytoday.guidegroup.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest : BehaviorSpec({

    val testDispatcher = StandardTestDispatcher()

    // Dépendances moquées
    val mockLocationClient: LocationClient = mockk(relaxed = true)
    val mockLocationRepository: LocationRepository = mockk(relaxed = true)
    val mockPointOfInterestRepository: PointOfInterestRepository = mockk(relaxed = true)
    val mockAuthRepository: AuthRepository = mockk(relaxed = true)
    val mockGroupRepository: GroupRepository = mockk(relaxed = true)
    lateinit var savedStateHandle: SavedStateHandle
    lateinit var viewModel: MapViewModel

    // Données de test
    val testUser = User(id = "user1", username = "TestUser")
    val testGroupId = "group1"
    val testGroup = Group(id = testGroupId, name = "Test Group", memberIds = listOf("user1", "user2"))
    val testLocation = Location("mock").apply { latitude = 1.0; longitude = 1.0 }

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }
    afterSpec {
        Dispatchers.resetMain()
    }

    beforeEach {
        savedStateHandle = SavedStateHandle(mapOf("groupId" to testGroupId))
        every { mockAuthRepository.getCurrentUser() } returns flowOf(Result.Success(testUser))
        every { mockLocationClient.getLocationUpdates(any()) } returns flowOf(testLocation)
        every { mockGroupRepository.getGroup(testGroupId) } returns flowOf(testGroup)
    }

    Given("un MapViewModel") {

        When("le ViewModel est initialisé") {
            viewModel = MapViewModel(mockLocationClient, mockLocationRepository, mockPointOfInterestRepository, mockAuthRepository, mockGroupRepository, savedStateHandle)

            Then("il devrait charger les données initiales correctement") {
                runTest(testDispatcher) {
                    testDispatcher.scheduler.advanceUntilIdle()

                    viewModel.currentGroupId.value shouldBe testGroupId
                    viewModel.currentUser.value shouldBe testUser
                    viewModel.currentGroup.value shouldBe testGroup
                    viewModel.userRealtimeLocation.value?.latitude shouldBe 1.0

                    coVerify { mockAuthRepository.getCurrentUser() }
                    coVerify { mockLocationClient.getLocationUpdates(any()) }
                    coVerify { mockGroupRepository.getGroup(testGroupId) }
                    coVerify { mockLocationRepository.updateLocation(any()) }
                }
            }
        }

        When("addPointOfInterest est appelé avec succès") {
            val newPoi = PointOfInterest(name = "New POI", description = "Desc", latitude = 1.0, longitude = 2.0, groupId = testGroupId)
            coEvery { mockPointOfInterestRepository.addPointOfInterest(any()) } returns "newPoiId"

            viewModel = MapViewModel(mockLocationClient, mockLocationRepository, mockPointOfInterestRepository, mockAuthRepository, mockGroupRepository, savedStateHandle)

            Then("l'état addPoiState devrait passer par Loading puis Success") {
                runTest(testDispatcher) {
                    viewModel.addPoiState.test {
                        // CORRECTION FINALE : La syntaxe correcte pour un objet
                        awaitItem().shouldBeInstanceOf<Result.Initial>()

                        viewModel.addPointOfInterest(newPoi.name, newPoi.description, newPoi.latitude, newPoi.longitude)

                        awaitItem().shouldBeInstanceOf<Result.Loading>()

                        val successResult = awaitItem()
                        successResult.shouldBeInstanceOf<Result.Success<String>>()
                        successResult.data shouldBe "newPoiId"

                        coVerify { mockPointOfInterestRepository.addPointOfInterest(any()) }
                    }
                }
            }
        }
    }
})