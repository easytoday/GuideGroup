package com.easytoday.guidegroup.domain.usecase

import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.repository.MessageRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SendMessageUseCaseTest : BehaviorSpec({

    val mockMessageRepository = mockk<MessageRepository>()
    val sendMessageUseCase = SendMessageUseCase(mockMessageRepository)

    beforeEach { clearAllMocks() }

    Given("SendMessageUseCase") {
        val groupId = "group123"
        val message = Message(senderId = "user1", senderName = "User1", text = "Hello")

        When("invoke est appelé et l'envoi réussit") {
            coEvery { mockMessageRepository.sendMessage(groupId, message) } returns flowOf(Result.Loading, Result.Success(Unit))

            Then("il devrait émettre Result.Loading puis Result.Success") {
                runTest {
                    val results = sendMessageUseCase(groupId, message).toList()
                    results[0].shouldBeInstanceOf<Result.Loading>()
                    results[1].shouldBeInstanceOf<Result.Success<Unit>>()
                    coVerify(exactly = 1) { mockMessageRepository.sendMessage(groupId, message) }
                }
            }
        }

        When("invoke est appelé et l'envoi échoue") {
            val errorMessage = "Erreur d'envoi"
            val exception = Exception("Test exception")
            coEvery { mockMessageRepository.sendMessage(groupId, message) } returns flowOf(Result.Loading, Result.Error(errorMessage, exception))

            Then("il devrait émettre Result.Loading puis Result.Error") {
                runTest {
                    val results = sendMessageUseCase(groupId, message).toList()
                    results[0].shouldBeInstanceOf<Result.Loading>()
                    val errorResult = results[1] as Result.Error
                    errorResult.message shouldBe errorMessage
                    coVerify(exactly = 1) { mockMessageRepository.sendMessage(groupId, message) }
                }
            }
        }
    }
})