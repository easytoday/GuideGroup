package com.easytoday.guidegroup.geofencing

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.easytoday.guidegroup.notification.NotificationHelper
import com.google.android.gms.location.Geofence
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class GeofenceWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_TRANSITION = "geofence_transition_type"
        const val KEY_GEOFENCE_IDS = "geofence_ids"
    }

    override suspend fun doWork(): Result {
        Timber.d("GeofenceWorker démarré.")

        val transitionType = inputData.getInt(KEY_TRANSITION, -1)
        val geofenceIds = inputData.getStringArray(KEY_GEOFENCE_IDS)?.toList() ?: emptyList()

        if (transitionType == -1 || geofenceIds.isEmpty()) {
            Timber.e("Données invalides reçues par le GeofenceWorker.")
            return Result.failure()
        }

        val transitionString = when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Entrée dans la zone"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Sortie de la zone"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "Séjour prolongé dans la zone"
            else -> "Transition inconnue"
        }

        val notificationMessage = "$transitionString: ${geofenceIds.joinToString(", ")}"

        NotificationHelper.sendGeofenceNotification(
            context,
            "Alerte de Géorepérage",
            notificationMessage
        )

        Timber.i("Notification de géorepérage envoyée : $notificationMessage")
        return Result.success()
    }
}