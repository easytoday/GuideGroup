package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.domain.model.MeetingPoint
import com.easytoday.guidegroup.domain.repository.MeetingPointRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

// Implémentation factice de MeetingPointRepository pour l'environnement de test (mock).
// Simule la gestion des points de rencontre pour les groupes.
class FakeMeetingPointRepositoryImpl @Inject constructor() : MeetingPointRepository {

    // Simule une base de données de points de rencontre en mémoire
    // La clé est le groupId, la valeur est le MeetingPoint pour ce groupe
    private val fakeMeetingPointsDb = MutableStateFlow<MutableMap<String, MeetingPoint>>(mutableMapOf())

    // Initialise avec quelques points de rencontre factices pour les tests
    init {
        val meetingPoint1 = MeetingPoint(
            groupId = "group1_id",
            name = "Fontaine St-Michel",
            latitude = 48.8530,
            longitude = 2.3439,
            address = "Place Saint-Michel, 75006 Paris",
            timestamp = Date(System.currentTimeMillis() - 10000)
        )
        val meetingPoint2 = MeetingPoint(
            groupId = "group2_id",
            name = "Louvre Pyramide",
            latitude = 48.8610,
            longitude = 2.3364,
            address = "Musée du Louvre, 75001 Paris",
            timestamp = Date(System.currentTimeMillis() - 5000)
        )
        fakeMeetingPointsDb.value[meetingPoint1.groupId] = meetingPoint1
        fakeMeetingPointsDb.value[meetingPoint2.groupId] = meetingPoint2
    }

    /**
     * Récupère le point de rencontre pour un groupe spécifique.
     * @param groupId L'ID du groupe.
     * @return Un flux (Flow) du point de rencontre du groupe, ou null si non trouvé.
     */
    override fun getGroupMeetingPoint(groupId: String): Flow<MeetingPoint?> {
        return fakeMeetingPointsDb.map { db ->
            delay(300) // Simule un délai
            db[groupId]
        }
    }

    /**
     * Définit ou met à jour le point de rencontre pour un groupe.
     * L'ID du groupe est utilisé comme clé dans la DB factice.
     * @param meetingPoint Le point de rencontre à définir/mettre à jour.
     */
    override suspend fun setMeetingPoint(meetingPoint: MeetingPoint) {
        delay(400) // Simule un délai
        fakeMeetingPointsDb.value[meetingPoint.groupId] = meetingPoint
        // Pour s'assurer que le MutableStateFlow émet une nouvelle valeur
        fakeMeetingPointsDb.value = fakeMeetingPointsDb.value.toMutableMap()
    }

    /**
     * Supprime le point de rencontre pour un groupe spécifique.
     * @param groupId L'ID du groupe.
     */
    override suspend fun deleteMeetingPoint(groupId: String) {
        delay(400) // Simule un délai
        fakeMeetingPointsDb.value.remove(groupId)
        // Pour s'assurer que le MutableStateFlow émet une nouvelle valeur
        fakeMeetingPointsDb.value = fakeMeetingPointsDb.value.toMutableMap()
    }
}

