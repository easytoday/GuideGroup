// app/src/main/java/com/easytoday/guidegroup/domain/repository/LocationRepository.kt
package com.easytoday.guidegroup.domain.repository

import com.easytoday.guidegroup.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Interface du référentiel pour les opérations liées aux localisations.
 * Définit les méthodes pour interagir avec les données de localisation.
 */
interface LocationRepository {

    /**
     * Récupère la localisation d'un utilisateur spécifique.
     * @param userId L'ID de l'utilisateur dont la localisation doit être récupérée.
     * @return Un flux (Flow) de la localisation de l'utilisateur, ou null si non trouvée.
     */
    fun getUserLocation(userId: String): Flow<Location?>

    /**
     * Met à jour la localisation d'un utilisateur.
     * @param location La localisation à mettre à jour.
     */
    suspend fun updateLocation(location: Location)

    /**
     * Récupère les localisations de plusieurs membres pour un groupe donné.
     * @param groupId L'ID du groupe.
     * @param memberIds La liste des IDs des membres dont les localisations doivent être récupérées.
     * @return Un flux (Flow) d'une liste de localisations.
     */
    fun getMemberLocations(groupId: String, memberIds: List<String>): Flow<List<Location>>
}


