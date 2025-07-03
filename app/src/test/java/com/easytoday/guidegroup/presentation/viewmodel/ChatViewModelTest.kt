package com.easytoday.guidegroup.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.repository.MessageRepository
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

    // Mocks
    val mockSendMessageUseCase: SendMessageUseCase = mockk(relaxed = true)
    val mockMessageRepository: MessageRepository = mockk(relaxed = true)
    val mockAuthRepository: AuthRepository = mockk(relaxed = true)
    lateinit var savedStateHandle: SavedStateHandle
    lateinit var viewModel: ChatViewModel

    // Données de test
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
    }

    Given("un ChatViewModel") {

        When("le ViewModel est initialisé") {
            viewModel = ChatViewModel(mockSendMessageUseCase, mockMessageRepository, mockAuthRepository, savedStateHandle)

            Then("il devrait charger le groupId, l'utilisateur et les messages") {
                runTest(testDispatcher) {
                    testDispatcher.scheduler.advanceUntilIdle()

                    viewModel.groupId.value shouldBe testGroupId
                    viewModel.currentUser.value shouldBe testUser
                    viewModel.messages.value shouldBe emptyList()

                    coVerify(timeout = 100) { mockAuthRepository.getCurrentUser() }
                    coVerify(timeout = 100) { mockMessageRepository.getMessagesForGroup(testGroupId) }
                }
            }
        }

        When("sendMessage est appelé avec un texte valide") {
            val textMessage = "Hello World"
            coEvery { mockSendMessageUseCase(any(), any()) } returns flowOf(Result.Success(Unit))

            viewModel = ChatViewModel(mockSendMessageUseCase, mockMessageRepository, mockAuthRepository, savedStateHandle)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("il devrait appeler le use case et réinitialiser l'état") {
                runTest(testDispatcher) {
                    viewModel.sendMessageState.test {
                        // CORRECTION de la syntaxe pour shouldBeInstanceOf
                        awaitItem().shouldBeInstanceOf<Result.Initial>()

                        viewModel.sendMessage(textMessage)

                        awaitItem().shouldBeInstanceOf<Result.Loading>()
                        awaitItem().shouldBeInstanceOf<Result.Success<*>>() // Le type générique n'importe pas ici
                        awaitItem().shouldBeInstanceOf<Result.Initial>()
                    }

                    coVerify(exactly = 1) { mockSendMessageUseCase(testGroupId, any()) }
                }
            }
        }

        When("sendMediaMessage est appelé avec une image") {
            val imageUri: Uri = mockk()
            val fakeDownloadUrl = "http://fake.url/image.jpg"

            coEvery { mockMessageRepository.uploadMedia(imageUri, Message.MediaType.IMAGE, testGroupId) } returns flowOf(Result.Loading, Result.Success(fakeDownloadUrl))
            coEvery { mockSendMessageUseCase(any(), any()) } returns flowOf(Result.Loading, Result.Success(Unit))

            viewModel = ChatViewModel(mockSendMessageUseCase, mockMessageRepository, mockAuthRepository, savedStateHandle)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("il devrait uploader le média puis envoyer le message") {
                runTest(testDispatcher) {
                    viewModel.sendMediaMessage(imageUri, Message.MediaType.IMAGE)

                    coVerify(exactly = 1) { mockMessageRepository.uploadMedia(imageUri, Message.MediaType.IMAGE, testGroupId) }
                    coVerify(exactly = 1) { mockSendMessageUseCase(testGroupId, match { it.mediaUrl == fakeDownloadUrl }) }
                }
            }
        }
    }
})