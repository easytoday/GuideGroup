package com.easytoday.guidegroup.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import com.easytoday.guidegroup.domain.model.Group
import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf // AJOUT : Import manquant
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest : BehaviorSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec { Dispatchers.setMain(testDispatcher) }
    afterSpec { Dispatchers.resetMain() }

    val mockLocationClient = mockk<LocationClient>()
    val mockLocationRepository = mockk<LocationRepository>()
    val mockPointOfInterestRepository = mockk<PointOfInterestRepository>()
    val mockAuthRepository = mockk<AuthRepository>()
    val mockGroupRepository = mockk<GroupRepository>()
    lateinit var savedStateHandle: SavedStateHandle

    val testUser = User(id = "user1", username = "TestUser")
    val testGroupId = "group1"
    val testGroup = Group(id = testGroupId, name = "Test Group", memberIds = listOf("user1", "user2"))

    beforeEach {
        savedStateHandle = SavedStateHandle(mapOf("groupId" to testGroupId))

        // Mock dependencies
        every { mockAuthRepository.getCurrentUser() } returns flowOf(Result.Success(testUser))
        every { mockLocationClient.getLocationUpdates(any()) } returns flowOf(Location("mock").apply { latitude = 1.0; longitude = 1.0 })
        every { mockGroupRepository.getGroup(testGroupId) } returns flowOf(testGroup)
        every { mockLocationRepository.getMemberLocations(any(), any()) } returns flowOf(emptyList())
        every { mockPointOfInterestRepository.getGroupPointsOfInterest(any()) } returns flowOf(emptyList())
    }

    Given("MapViewModel") {

        When("le ViewModel est initialisé") {
            val viewModel = MapViewModel(mockLocationClient, mockLocationRepository, mockPointOfInterestRepository, mockAuthRepository, mockGroupRepository, savedStateHandle)
            // CORRECTION : L'appel à advanceUntilIdle est supprimé car runTest s'en charge.

            Then("il devrait observer les données initiales") {
                // On laisse un peu de temps au dispatcher pour exécuter le bloc init
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.currentGroupId.value shouldBe testGroupId
                viewModel.currentUser.value shouldBe testUser
                viewModel.currentGroup.value shouldBe testGroup
                viewModel.userRealtimeLocation.value?.latitude shouldBe 1.0

                coVerify { mockAuthRepository.getCurrentUser() }
                coVerify { mockLocationClient.getLocationUpdates(any()) }
                coVerify { mockGroupRepository.getGroup(testGroupId) }
            }
        }

        When("addPointOfInterest est appelé avec succès") {
            val viewModel = MapViewModel(mockLocationClient, mockLocationRepository, mockPointOfInterestRepository, mockAuthRepository, mockGroupRepository, savedStateHandle)
            coEvery { mockPointOfInterestRepository.addPointOfInterest(any()) } returns "newPoiId"

            Then("l'état addPoiState devrait passer par Loading puis Success") {
                runTest(testDispatcher) {
                    viewModel.addPointOfInterest("New POI", "Desc", 1.0, 2.0)

                    viewModel.addPoiState.value.shouldBeInstanceOf<Result.Success<String>>()
                    (viewModel.addPoiState.value as Result.Success<String>).data shouldBe "newPoiId"

                    coVerify { mockPointOfInterestRepository.addPointOfInterest(any()) }
                }
            }
        }
    }
})