package com.easytoday.guidegroup.domain.usecase

import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SignInUseCaseTest : BehaviorSpec({

    val mockAuthRepository = mockk<AuthRepository>()
    val signInUseCase = SignInUseCase(mockAuthRepository)

    beforeEach { clearAllMocks() }

    Given("SignInUseCase") {
        val email = "test@example.com"
        val password = "password123"

        When("invoke est appelé et la connexion réussit") {
            val testUser = User(id = "user1", email = email, username = "TestUser")
            coEvery { mockAuthRepository.signIn(email, password) } returns flowOf(Result.Loading, Result.Success(testUser))

            Then("il devrait émettre Result.Loading puis Result.Success") {
                runTest {
                    val results = signInUseCase(email, password).toList()
                    results[0].shouldBeInstanceOf<Result.Loading>()
                    val successResult = results[1] as Result.Success
                    successResult.data shouldBe testUser
                    coVerify(exactly = 1) { mockAuthRepository.signIn(email, password) }
                }
            }
        }

        When("invoke est appelé et la connexion échoue") {
            val errorMessage = "Identifiants incorrects"
            val exception = Exception("Auth failed")
            coEvery { mockAuthRepository.signIn(email, password) } returns flowOf(Result.Loading, Result.Error(errorMessage, exception))

            Then("il devrait émettre Result.Loading puis Result.Error") {
                runTest {
                    val results = signInUseCase(email, password).toList()
                    results[0].shouldBeInstanceOf<Result.Loading>()
                    val errorResult = results[1] as Result.Error
                    errorResult.message shouldBe errorMessage
                    coVerify(exactly = 1) { mockAuthRepository.signIn(email, password) }
                }
            }
        }
    }
})