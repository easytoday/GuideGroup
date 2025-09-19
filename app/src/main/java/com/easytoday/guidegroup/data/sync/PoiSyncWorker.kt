package com.easytoday.guidegroup.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.data.local.PointOfInterestDao
import com.easytoday.guidegroup.data.local.PointOfInterestEntity
import com.easytoday.guidegroup.domain.model.PointOfInterest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

// CORRECTION : Le champ creatorId est maintenant inclus dans la conversion.
private fun PointOfInterest.toEntity(): PointOfInterestEntity {
    return PointOfInterestEntity(
        id = this.id,
        groupId = this.groupId,
        creatorId = this.creatorId,
        name = this.name,
        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude,
        type = this.type
    )
}

@HiltWorker
class PoiSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestoreHelper: FirestoreHelper,
    private val poiDao: PointOfInterestDao
) : CoroutineWorker(context, workerParams) {

    private val POIS_COLLECTION = "pointsOfInterest"

    override suspend fun doWork(): Result {
        return try {
            val groupId = inputData.getString("groupId")
            if (groupId.isNullOrBlank()) {
                Timber.e("PoiSyncWorker: groupId est manquant.")
                return Result.failure()
            }

            Timber.d("PoiSyncWorker: Démarrage de la synchronisation pour le groupe $groupId")

            val remotePois = firestoreHelper.getCollectionAsFlow<PointOfInterest>(
                firestoreHelper.db.collection(POIS_COLLECTION).whereEqualTo("groupId", groupId)
            ).first()

            poiDao.deleteAllForGroup(groupId)
            poiDao.upsertAll(remotePois.map { it.toEntity() })

            Timber.d("PoiSyncWorker: ${remotePois.size} POI synchronisés pour le groupe $groupId")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "PoiSyncWorker: Échec de la synchronisation.")
            Result.retry()
        }
    }
}