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
class SignUpUseCaseTest : BehaviorSpec({

    val mockAuthRepository = mockk<AuthRepository>()
    val signUpUseCase = SignUpUseCase(mockAuthRepository)

    beforeEach { clearAllMocks() }

    Given("SignUpUseCase") {
        val email = "newuser@example.com"
        val password = "strongpassword"
        val username = "NewUser"
        val isGuide = false

        When("invoke est appelé et l'inscription réussit") {
            val newUser = User(id = "newUserId", email = email, username = username, isGuide = isGuide)
            coEvery { mockAuthRepository.signUp(email, password, username, isGuide) } returns flowOf(Result.Loading, Result.Success(newUser))

            Then("il devrait émettre Result.Loading puis Result.Success") {
                runTest {
                    val results = signUpUseCase(email, password, username, isGuide).toList()
                    results[0].shouldBeInstanceOf<Result.Loading>()
                    val successResult = results[1] as Result.Success
                    successResult.data shouldBe newUser
                    coVerify(exactly = 1) { mockAuthRepository.signUp(email, password, username, isGuide) }
                }
            }
        }

        When("invoke est appelé et l'inscription échoue") {
            val errorMessage = "Email déjà utilisé"
            val exception = Exception("Registration failed")
            coEvery { mockAuthRepository.signUp(email, password, username, isGuide) } returns flowOf(Result.Loading, Result.Error(errorMessage, exception))

            Then("il devrait émettre Result.Loading puis Result.Error") {
                runTest {
                    val results = signUpUseCase(email, password, username, isGuide).toList()
                    results[0].shouldBeInstanceOf<Result.Loading>()
                    val errorResult = results[1] as Result.Error
                    errorResult.message shouldBe errorMessage
                    coVerify(exactly = 1) { mockAuthRepository.signUp(email, password, username, isGuide) }
                }
            }
        }
    }
})