package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.repository.PointOfInterestRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import javax.inject.Inject

/**
 * Implémentation concrète de [PointOfInterestRepository] utilisant Firestore.
 * Gère le stockage et la récupération des points d'intérêt.
 */
class PointOfInterestRepositoryImpl @Inject constructor(
    private val firestoreHelper: FirestoreHelper
    //private val firestore: FirebaseFirestore // Injecter Firestore pour les requêtes complexes
) : PointOfInterestRepository {

    private val POIS_COLLECTION = "pointsOfInterest"

    /**
     * Récupère un flux de points d'intérêt pour un groupe spécifique.
     * @param groupId L'ID du groupe.
     * @return Un Flow d'une liste de [PointOfInterest].
     */
    override fun getGroupPointsOfInterest(groupId: String): Flow<List<PointOfInterest>> {
        return firestoreHelper.getCollectionAsFlow<PointOfInterest>(
            firestoreHelper.db.collection(POIS_COLLECTION)
                .whereEqualTo("groupId", groupId) // Filtrer par groupId
        ).catch { e ->
            Timber.e("Error getting points of interest for group $groupId: ${e.message}", e)
            emit(emptyList())
        }
    }

    /**
     * Ajoute un nouveau point d'intérêt. L'ID du POI sera auto-généré par Firestore.
     * @param poi Le point d'intérêt à ajouter.
     * @return L'ID du POI ajouté.
     */
    override suspend fun addPointOfInterest(poi: PointOfInterest): String {
        return try {
            val docRef = firestoreHelper.db.collection(POIS_COLLECTION).document()
            val poiWithId = poi.copy(id = docRef.id)
            firestoreHelper.addDocument(POIS_COLLECTION, poiWithId, poiWithId.id)
            Timber.d("Point of interest added with ID: ${poiWithId.id}")
            poiWithId.id
        } catch (e: Exception) {
            Timber.e("Error adding point of interest: ${e.message}", e)
            throw e // Propage l'exception pour que le ViewModel puisse la gérer
        }
    }

    /**
     * Met à jour un point d'intérêt existant.
     * @param poi Le point d'intérêt à mettre à jour.
     */
    override suspend fun updatePointOfInterest(poi: PointOfInterest) {
        try {
            firestoreHelper.updateDocument(POIS_COLLECTION, poi.id, poi)
            Timber.d("Point of interest updated: ${poi.id}")
        } catch (e: Exception) {
            Timber.e("Error updating point of interest ${poi.id}: ${e.message}", e)
            // L'erreur est loggée, pas propagée directement ici selon le comportement actuel.
        }
    }

    /**
     * Supprime un point d'intérêt.
     * @param poiId L'ID du point d'intérêt à supprimer.
     */
    override suspend fun deletePointOfInterest(poiId: String) {
        try {
            firestoreHelper.deleteDocument(POIS_COLLECTION, poiId)
            Timber.d("Point of interest deleted: $poiId")
        } catch (e: Exception) {
            Timber.e("Error deleting point of interest $poiId: ${e.message}", e)
            // L'erreur est loggée, pas propagée directement ici selon le comportement actuel.
        }
    }
}


