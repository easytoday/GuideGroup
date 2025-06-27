package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import timber.log.Timber
import java.lang.Exception

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest : BehaviorSpec({

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
    val userRepository = UserRepositoryImpl(mockFirestoreHelper)

    beforeEach {
        clearAllMocks()
    }

    Given("UserRepositoryImpl") {

        When("getUser est appelé") {
            val userId = "testUser1"
            val testUser = User(id = userId, email = "test@example.com", username = "TestUser")

            And("l'utilisateur existe") {
                coEvery { mockFirestoreHelper.getDocumentAsFlow<User>("users", userId) } returns flowOf(testUser)

                Then("il devrait émettre Result.Success avec l'utilisateur") {
                    runTest {
                        val result = userRepository.getUser(userId).first()
                        result.shouldBeInstanceOf<Result.Success<User?>>()
                        result.data shouldBe testUser
                        coVerify(exactly = 1) { mockFirestoreHelper.getDocumentAsFlow<User>("users", userId) }
                    }
                }
            }

            And("l'utilisateur n'existe pas") {
                coEvery { mockFirestoreHelper.getDocumentAsFlow<User>("users", userId) } returns flowOf(null)

                Then("il devrait émettre Result.Success avec null") {
                    runTest {
                        val result = userRepository.getUser(userId).first()
                        result.shouldBeInstanceOf<Result.Success<User?>>()
                        result.data shouldBe null
                    }
                }
            }

            And("une erreur se produit") {
                val exception = Exception("Network error")
                coEvery { mockFirestoreHelper.getDocumentAsFlow<User>("users", userId) } throws exception

                Then("il devrait émettre Result.Error") {
                    runTest {
                        val result = userRepository.getUser(userId).first()
                        result.shouldBeInstanceOf<Result.Error>()
                        result.message shouldBe "Erreur lors de la récupération de l'utilisateur."
                    }
                }
            }
        }

        When("addUser est appelé") {
            val newUser = User(id = "newUser1", email = "new@example.com", username = "NewUser")

            And("l'ajout réussit") {
                coEvery { mockFirestoreHelper.addDocument("users", newUser, newUser.id) } just Runs

                Then("il devrait appeler addDocument sur FirestoreHelper") {
                    runTest {
                        userRepository.addUser(newUser)
                        coVerify(exactly = 1) { mockFirestoreHelper.addDocument("users", newUser, newUser.id) }
                    }
                }
            }

            And("une erreur se produit") {
                val exception = Exception("Failed to add user")
                coEvery { mockFirestoreHelper.addDocument(any(), any(), any()) } throws exception

                Then("il devrait gérer l'erreur (via Timber)") {
                    runTest {
                        userRepository.addUser(newUser)
                        // L'erreur est loggée, pas propagée par addUser
                        // On ne peut pas vérifier le log directement avec MockK sans mockkStatic(Timber::class)
                        // Mais on peut vérifier que addDocument a été appelé
                        coVerify(exactly = 1) { mockFirestoreHelper.addDocument("users", newUser, newUser.id) }
                    }
                }
            }
        }

        When("updateUser est appelé") {
            val updatedUser = User(id = "testUser1", email = "updated@example.com", username = "UpdatedUser")

            And("la mise à jour réussit") {
                coEvery { mockFirestoreHelper.updateDocument("users", updatedUser.id, updatedUser) } just Runs

                Then("il devrait appeler updateDocument sur FirestoreHelper") {
                    runTest {
                        userRepository.updateUser(updatedUser)
                        coVerify(exactly = 1) { mockFirestoreHelper.updateDocument("users", updatedUser.id, updatedUser) }
                    }
                }
            }

            And("une erreur se produit") {
                val exception = Exception("Failed to update user")
                coEvery { mockFirestoreHelper.updateDocument(any(), any(), any()) } throws exception

                Then("il devrait gérer l'erreur (via Timber)") {
                    runTest {
                        userRepository.updateUser(updatedUser)
                        coVerify(exactly = 1) { mockFirestoreHelper.updateDocument("users", updatedUser.id, updatedUser) }
                    }
                }
            }
        }

        When("deleteUser est appelé") {
            val userIdToDelete = "testUser1"

            And("la suppression réussit") {
                coEvery { mockFirestoreHelper.deleteDocument("users", userIdToDelete) } just Runs

                Then("il devrait appeler deleteDocument sur FirestoreHelper") {
                    runTest {
                        userRepository.deleteUser(userIdToDelete)
                        coVerify(exactly = 1) { mockFirestoreHelper.deleteDocument("users", userIdToDelete) }
                    }
                }
            }

            And("une erreur se produit") {
                val exception = Exception("Failed to delete user")
                coEvery { mockFirestoreHelper.deleteDocument(any(), any()) } throws exception

                Then("il devrait gérer l'erreur (via Timber)") {
                    runTest {
                        userRepository.deleteUser(userIdToDelete)
                        coVerify(exactly = 1) { mockFirestoreHelper.deleteDocument("users", userIdToDelete) }
                    }
                }
            }
        }
    }
})


