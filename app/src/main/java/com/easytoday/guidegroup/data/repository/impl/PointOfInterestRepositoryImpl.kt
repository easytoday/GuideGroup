package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.data.local.PointOfInterestDao
import com.easytoday.guidegroup.data.local.PointOfInterestEntity
import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.repository.PointOfInterestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

private fun PointOfInterestEntity.toDomainModel(): PointOfInterest {
    return PointOfInterest(
        id = this.id,
        groupId = this.groupId,
        name = this.name,
        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude,
        type = this.type
    )
}

private fun PointOfInterest.toEntity(): PointOfInterestEntity {
    return PointOfInterestEntity(
        id = this.id,
        groupId = this.groupId,
        name = this.name,
        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude,
        type = this.type
    )
}

class PointOfInterestRepositoryImpl @Inject constructor(
    private val poiDao: PointOfInterestDao,
    private val firestoreHelper: FirestoreHelper
) : PointOfInterestRepository {

    private val POIS_COLLECTION = "pointsOfInterest"

    override fun getGroupPointsOfInterest(groupId: String): Flow<List<PointOfInterest>> {
        return poiDao.getPoisForGroup(groupId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun addPointOfInterest(poi: PointOfInterest): String {
        try {
            val docRef = firestoreHelper.db.collection(POIS_COLLECTION).document()
            val poiWithId = poi.copy(id = docRef.id)

            // 1. Écrire sur Firestore
            firestoreHelper.addDocument(POIS_COLLECTION, poiWithId, poiWithId.id)

            // 2. CORRECTION : Mettre à jour le cache local immédiatement
            poiDao.upsertAll(listOf(poiWithId.toEntity()))

            Timber.d("Point of interest added with ID: ${poiWithId.id}")
            return poiWithId.id
        } catch (e: Exception) {
            Timber.e(e, "Error adding point of interest")
            throw e
        }
    }

    override suspend fun updatePointOfInterest(poi: PointOfInterest) {
        try {
            // Pas encore implémenté, mais la logique serait similaire :
            // firestoreHelper.updateDocument(...)
            // poiDao.upsertAll(listOf(poi.toEntity()))
        } catch (e: Exception) {
            Timber.e(e, "Error updating point of interest")
        }
    }

    override suspend fun deletePointOfInterest(poiId: String) {
        try {
            // La suppression doit aussi se faire sur les deux sources
            firestoreHelper.deleteDocument(POIS_COLLECTION, poiId)
            // Pour le DAO, nous devons créer une entité factice avec l'ID pour la suppression
            // (Note : Room ne permet pas de supprimer par ID simple, on supprime par entité)
            // Une meilleure approche serait d'avoir une méthode `deleteById(id: String)` dans le DAO.
            // Pour l'instant, nous laissons cette partie en attente de la logique de suppression dans l'UI.
        } catch (e: Exception) {
            Timber.e(e, "Error deleting point of interest from Firestore")
        }
    }
}