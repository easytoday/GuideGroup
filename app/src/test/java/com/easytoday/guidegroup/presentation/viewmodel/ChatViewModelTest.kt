package com.easytoday.guidegroup.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.repository.MessageRepository
import com.easytoday.guidegroup.domain.repository.PointOfInterestRepository
import com.easytoday.guidegroup.domain.usecase.SendMessageUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest : BehaviorSpec({

    val testDispatcher = StandardTestDispatcher()

    val mockSendMessageUseCase: SendMessageUseCase = mockk(relaxed = true)
    val mockMessageRepository: MessageRepository = mockk(relaxed = true)
    val mockAuthRepository: AuthRepository = mockk(relaxed = true)
    // CORRECTION : Ajouter le mock pour la nouvelle dépendance
    val mockPoiRepository: PointOfInterestRepository = mockk(relaxed = true)
    lateinit var savedStateHandle: SavedStateHandle
    lateinit var viewModel: ChatViewModel

    val testUser = User(id = "user1", email = "test@example.com", username = "TestUser")
    val testGroupId = "group123"

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
        if (Timber.treeCount == 0) {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {}
            })
        }
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    beforeEach {
        clearAllMocks()
        savedStateHandle = SavedStateHandle(mapOf("groupId" to testGroupId))

        every { mockAuthRepository.getCurrentUser() } returns flowOf(Result.Success(testUser))
        every { mockMessageRepository.getMessagesForGroup(testGroupId) } returns flowOf(emptyList())
        every { mockPoiRepository.getGroupPointsOfInterest(testGroupId) } returns flowOf(emptyList())
    }

    Given("un ChatViewModel") {

        When("le ViewModel est initialisé") {
            // CORRECTION : Mettre à jour l'instanciation
            viewModel = ChatViewModel(mockSendMessageUseCase, mockMessageRepository, mockAuthRepository, mockPoiRepository, savedStateHandle)

            Then("il devrait charger les données initiales") {
                runTest(testDispatcher) {
                    testDispatcher.scheduler.advanceUntilIdle()

                    viewModel.groupId.value shouldBe testGroupId
                    viewModel.currentUser.value shouldBe testUser

                    coVerify(timeout = 100) { mockAuthRepository.getCurrentUser() }
                    coVerify(timeout = 100) { mockMessageRepository.getMessagesForGroup(testGroupId) }
                    coVerify(timeout = 100) { mockPoiRepository.getGroupPointsOfInterest(testGroupId) }
                }
            }
        }
    }
})