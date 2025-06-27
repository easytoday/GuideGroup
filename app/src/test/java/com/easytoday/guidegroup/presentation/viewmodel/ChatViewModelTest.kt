package com.easytoday.guidegroup.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest : BehaviorSpec({

    val testDispatcher = StandardTestDispatcher()

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

    val mockSendMessageUseCase = mockk<SendMessageUseCase>()
    val mockMessageRepository = mockk<MessageRepository>()
    val mockAuthRepository = mockk<AuthRepository>()
    val mockSavedStateHandle = mockk<SavedStateHandle>()

    val testUser = User(id = "user1", email = "test@example.com", username = "TestUser")
    val testGroupId = "group123"

    val currentUserFlow = MutableStateFlow<Result<User?>>(Result.Success(testUser))
    val messagesForGroupFlow = MutableStateFlow<List<Message>>(emptyList())

    beforeEach {
        clearAllMocks()
        every { mockSavedStateHandle.get<String>("groupId") } returns testGroupId
        every { mockAuthRepository.getCurrentUser() } returns currentUserFlow
        every { mockMessageRepository.getMessagesForGroup(testGroupId) } returns messagesForGroupFlow
    }

    Given("ChatViewModel") {

        When("le ViewModel est initialisé") {
            // CORRECTION : L'initialisation du ViewModel est maintenant dans le bloc de test
            // pour s'assurer que les mocks sont prêts.
            Then("il devrait récupérer le groupId et observer l'utilisateur et les messages") {
                runTest(testDispatcher) {
                    val viewModel = ChatViewModel(mockSendMessageUseCase, mockMessageRepository, mockAuthRepository, mockSavedStateHandle)

                    viewModel.groupId.value shouldBe testGroupId
                    coVerify(timeout = 100) { mockAuthRepository.getCurrentUser() }
                    coVerify(timeout = 100) { mockMessageRepository.getMessagesForGroup(testGroupId) }
                }
            }
        }
    }
})