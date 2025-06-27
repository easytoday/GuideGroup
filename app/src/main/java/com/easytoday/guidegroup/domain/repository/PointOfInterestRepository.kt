// app/src/main/java/com/easytoday/guidegroup/domain/repository/PointOfInterestRepository.kt
package com.easytoday.guidegroup.domain.repository

import com.easytoday.guidegroup.domain.model.PointOfInterest
import kotlinx.coroutines.flow.Flow

/**
 * Interface du référentiel pour les opérations liées aux points d'intérêt.
 * Définit les méthodes pour interagir avec les données des points d'intérêt.
 */
interface PointOfInterestRepository {

    /**
     * Récupère tous les points d'intérêt pour un groupe spécifique.
     * @param groupId L'ID du groupe.
     * @return Un flux (Flow) d'une liste de points d'intérêt.
     */
    fun getGroupPointsOfInterest(groupId: String): Flow<List<PointOfInterest>>

    /**
     * Ajoute un nouveau point d'intérêt à un groupe.
     * @param pointOfInterest Le point d'intérêt à ajouter.
     * @return L'ID du point d'intérêt ajouté.
     */
    suspend fun addPointOfInterest(pointOfInterest: PointOfInterest): String

    /**
     * Met à jour un point d'intérêt existant.
     * @param pointOfInterest Le point d'intérêt à mettre à jour.
     */
    suspend fun updatePointOfInterest(pointOfInterest: PointOfInterest)

    /**
     * Supprime un point d'intérêt par son ID.
     * @param pointOfInterestId L'ID du point d'intérêt à supprimer.
     */
    suspend fun deletePointOfInterest(pointOfInterestId: String)
}


