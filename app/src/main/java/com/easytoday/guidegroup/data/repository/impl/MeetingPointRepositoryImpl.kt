// app/src/main/java/com/easytoday/guidegroup/data/repository/impl/MeetingPointRepositoryImpl.kt
package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.MeetingPoint
import com.easytoday.guidegroup.domain.repository.MeetingPointRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber // Ajouté
import javax.inject.Inject

/**
 * Implémentation concrète de [MeetingPointRepository] utilisant Firestore.
 */
class MeetingPointRepositoryImpl @Inject constructor(
    private val firestoreHelper: FirestoreHelper
) : MeetingPointRepository {

    private val COLLECTION_NAME = "meetingPoints"

    /**
     * Récupère le point de rencontre pour un groupe spécifique.
     * Les points de rencontre sont stockés avec l'ID du groupe comme ID de document.
     * @param groupId L'ID du groupe.
     * @return Un flux (Flow) du point de rencontre du groupe, ou null si non trouvé.
     */
    override fun getGroupMeetingPoint(groupId: String): Flow<MeetingPoint?> {
        return firestoreHelper.getDocumentAsFlow<MeetingPoint>(COLLECTION_NAME, groupId)
            .catch { e ->
                Timber.e("Erreur lors de la récupération du point de rencontre pour le groupe $groupId: ${e.message}", e)
                emit(null)
            }
    }

    /**
     * Définit ou met à jour le point de rencontre pour un groupe.
     * L'ID du groupe est utilisé comme ID de document Firestore.
     * @param meetingPoint Le point de rencontre à définir/mettre à jour.
     */
    override suspend fun setMeetingPoint(meetingPoint: MeetingPoint) {
        try {
            firestoreHelper.addDocument(COLLECTION_NAME, meetingPoint, meetingPoint.groupId)
            Timber.d("Point de rencontre pour le groupe ${meetingPoint.groupId} défini/mis à jour avec succès.")
        } catch (e: Exception) {
            Timber.e("Erreur lors de la définition/mise à jour du point de rencontre pour le groupe ${meetingPoint.groupId}: ${e.message}", e)
        }
    }

    /**
     * Supprime le point de rencontre pour un groupe spécifique.
     * @param groupId L'ID du groupe.
     */
    override suspend fun deleteMeetingPoint(groupId: String) {
        try {
            firestoreHelper.deleteDocument(COLLECTION_NAME, groupId)
            Timber.d("Point de rencontre pour le groupe $groupId supprimé avec succès.")
        } catch (e: Exception) {
            Timber.e("Erreur lors de la suppression du point de rencontre pour le groupe $groupId: ${e.message}", e)
        }
    }
}

