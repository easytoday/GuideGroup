// app/src/main/java/com/easytoday/guidegroup/domain/repository/AuthRepository.kt
package com.easytoday.guidegroup.domain.repository

import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import kotlinx.coroutines.flow.Flow


/**
 * Interface du référentiel pour les opérations d'authentification.
 * Elle sert de contrat pour la couche de données et est utilisée par la couche de domaine.
 * Définit les méthodes pour l'inscription, la connexion et la déconnexion.
 */
interface AuthRepository {

    /**
     * Tente d'inscrire un nouvel utilisateur.
     * @param email L'email de l'utilisateur.
     * @param password Le mot de passe.
     * @param username Le nom d'utilisateur.
     * @param isGuide Indique si l'utilisateur est un guide. <--- NOUVEAU PARAMÈTRE
     * @return Un Flow de [Result] indiquant le succès, le chargement ou l'échec.
     */
    suspend fun signUp(email: String, password: String, username: String, isGuide: Boolean): Flow<Result<User>> // <--- MODIFIÉ ICI


    /**
     * Tente de connecter un utilisateur existant.
     * @param email L'email de l'utilisateur.
     * @param password Le mot de passe.
     * @return Un Flow de [Result] indiquant le succès, le chargement ou l'échec.
     */
    suspend fun signIn(email: String, password: String): Flow<Result<User>>

    /**
     * Tente de déconnecter l'utilisateur actuel.
     * @return Un Flow de [Result] indiquant le succès, le chargement ou l'échec.
     */
    suspend fun signOut(): Flow<Result<Unit>>

    /**
     * Récupère l'utilisateur actuellement authentifié.
     * @return Un Flow de l'objet [User] si un utilisateur est connecté, sinon null.
     */
    fun getCurrentUser(): Flow<Result<User?>> // correction Action 1.1.1


    /**
     * Récupère l'ID de l'utilisateur actuellement authentifié.
     * @return Un Flow de l'ID de l'utilisateur si un utilisateur est connecté, sinon null.
     */
    fun getCurrentUserId(): Flow<Result<String?>>
}

