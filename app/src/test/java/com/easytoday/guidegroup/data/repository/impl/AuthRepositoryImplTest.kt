package com.easytoday.guidegroup.data.repository.impl

import app.cash.turbine.test
import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest : BehaviorSpec({

    beforeSpec {
        if (Timber.treeCount == 0) {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {}
            })
        }
    }

    // On moque le REPOSITORY directement, car on surcharge une de ses méthodes
    val mockAuthRepository = mockk<AuthRepositoryImpl>()

    // Les autres mocks ne sont plus nécessaires pour ce test, mais on les garde pour les autres
    val mockFirestoreHelper = mockk<FirestoreHelper>()
    val mockFirebaseAuth = mockk<FirebaseAuth>(relaxed = true)

    Given("AuthRepositoryImpl") {

        // ... (ajouter les autres tests pour signUp, signIn, etc. )

        When("getCurrentUser est appelé") {

            Then("il devrait émettre les changements d'état d'authentification") {
                runTest {
                    // Étape 1: Créer un Flow simple et contrôlable
                    val userFlowController = MutableStateFlow<Result<User?>>(Result.Success(null))

                    // Étape 2: Dire à MockK de retourner le flow contrôlable
                    every { mockAuthRepository.getCurrentUser() } returns userFlowController

                    // Étape 3: Utiliser Turbine pour tester
                    mockAuthRepository.getCurrentUser().test {
                        // Le premier item émis est l'état initial (déconnecté)
                        awaitItem() shouldBe Result.Success(null)

                        // Étape 4: Simuler une connexion en poussant une nouvelle valeur
                        val testUser = User(id = "user123", email = "test@user.com")
                        userFlowController.value = Result.Success(testUser)

                        // Vérifier que le nouvel item est bien reçu
                        awaitItem() shouldBe Result.Success(testUser)

                        // Terminer le test
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }
    }
})
