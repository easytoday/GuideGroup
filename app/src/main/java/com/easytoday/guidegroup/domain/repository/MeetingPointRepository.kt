// app/src/main/java/com/easytoday/guidegroup/domain/repository/MeetingPointRepository.kt
package com.easytoday.guidegroup.domain.repository

import com.easytoday.guidegroup.domain.model.MeetingPoint
import kotlinx.coroutines.flow.Flow

/**
 * Interface du référentiel pour les opérations liées aux points de rencontre.
 * Définit les méthodes pour interagir avec les données des points de rencontre.
 */
interface MeetingPointRepository {

    /**
     * Récupère le point de rencontre pour un groupe spécifique.
     * @param groupId L'ID du groupe.
     * @return Un flux (Flow) du point de rencontre du groupe, ou null si non trouvé.
     */
    fun getGroupMeetingPoint(groupId: String): Flow<MeetingPoint?>

    /**
     * Définit ou met à jour le point de rencontre pour un groupe.
     * @param meetingPoint Le point de rencontre à définir/mettre à jour.
     */
    suspend fun setMeetingPoint(meetingPoint: MeetingPoint)

    /**
     * Supprime le point de rencontre pour un groupe spécifique.
     * @param groupId L'ID du groupe.
     */
    suspend fun deleteMeetingPoint(groupId: String)
}


