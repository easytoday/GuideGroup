// app/src/main/java/com/easytoday/guidegroup/domain/usecase/SignInUseCase.kt
package com.easytoday.guidegroup.domain.usecase


import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Cas d'utilisation pour gérer la connexion des utilisateurs.
 * Interagit avec AuthRepository pour effectuer l'opération de connexion.
 */
class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Exécute le cas d'utilisation pour connecter un utilisateur.
     * @param email L'email de l'utilisateur.
     * @param password Le mot de passe de l'utilisateur.
     * @return Un Flow de [Result] indiquant le succès, le chargement ou l'échec de la connexion.
     */
    suspend operator fun invoke(email: String, password: String): Flow<Result<User>> {
        return authRepository.signIn(email, password)
    }
}




///**
// * Cas d'utilisation pour la connexion d'un utilisateur.
// * Encapsule la logique métier de l'opération de connexion.
// *
// * @param authRepository Le référentiel d'authentification à utiliser.
// */
//class SignInUseCase @Inject constructor(
//    private val authRepository: AuthRepository
//) {
//    /**
//     * Exécute la connexion de l'utilisateur.
//     *
//     * @param email L'email de l'utilisateur.
//     * @param password Le mot de passe de l'utilisateur.
//     * @return Un objet Result indiquant le succès (avec l'objet User) ou l'échec.
//     */
//    suspend operator fun invoke(email: String, password: String): Result<User> {
//        return authRepository.signIn(email, password)
//    }
//}


