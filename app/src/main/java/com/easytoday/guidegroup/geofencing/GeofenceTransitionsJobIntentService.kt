package com.easytoday.guidegroup.geofencing

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.easytoday.guidegroup.notification.NotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber

class GeofenceTransitionsJobIntentService : JobIntentService() {

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, GeofenceTransitionsJobIntentService::class.java, JOB_ID, work)
        }
    }

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Timber.e("GeofencingEvent est null dans le service.")
            return
        }

        if (geofencingEvent.hasError()) {
            Timber.e("Erreur de géorepérage dans le service: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: emptyList()

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val transitionString = when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> "Entrée dans la zone"
                else -> "Sortie de la zone"
            }
            val triggeringGeofencesIdsList = triggeringGeofences.map { it.requestId }
            val notificationMessage = "$transitionString: ${triggeringGeofencesIdsList.joinToString(", ")}"

            NotificationHelper.sendGeofenceNotification(
                this,
                "Alerte de Géorepérage",
                notificationMessage
            )
        }
    }
}