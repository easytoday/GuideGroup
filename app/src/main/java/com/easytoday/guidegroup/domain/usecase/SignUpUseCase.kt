package com.easytoday.guidegroup.domain.usecase

import com.easytoday.guidegroup.domain.model.Result
import kotlinx.coroutines.flow.Flow
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Cas d'utilisation pour gérer l'inscription des nouveaux utilisateurs.
 * Interagit avec le AuthRepository pour effectuer l'opération d'inscription.
 */
class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Exécute le cas d'utilisation pour inscrire un nouvel utilisateur.
     * @param email L'email du nouvel utilisateur.
     * @param password Le mot de passe du nouvel utilisateur.
     * @param username Le nom d'utilisateur du nouvel utilisateur.
     * @param isGuide Le rôle de l'utilisateur (true si guide, false si membre). <--- NOUVEAU/RÉ-ACTIVÉ
     * @return Un Flow de [Result] indiquant le succès, le chargement ou l'échec de l'inscription.
     */
    suspend operator fun invoke(email: String, password: String, username: String, isGuide: Boolean): Flow<Result<User>> { // <--- MODIFIÉ ICI
        return authRepository.signUp(email, password, username, isGuide) // <--- PASSEZ isGuide AU REPOSITORY
    }
}

