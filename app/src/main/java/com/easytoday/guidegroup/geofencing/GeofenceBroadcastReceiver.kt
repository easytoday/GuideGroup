package com.easytoday.guidegroup.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.easytoday.guidegroup.notification.NotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // CORRECTION : On remet la logique de traitement directement ici.
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Timber.e("GeofencingEvent est null. Impossible de traiter.")
            // Envoi d'une notification d'erreur pour le débogage
            NotificationHelper.sendGeofenceNotification(context, "Erreur Geofence", "Événement non reçu")
            return
        }

        if (geofencingEvent.hasError()) {
            val errorMessage = "Erreur de géorepérage: ${geofencingEvent.errorCode}"
            Timber.e(errorMessage)
            NotificationHelper.sendGeofenceNotification(context, "Erreur Geofence", errorMessage)
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences ?: emptyList()

            val transitionString = when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> "Entrée dans la zone"
                else -> "Sortie de la zone"
            }

            val triggeringGeofencesIdsList = triggeringGeofences.map { it.requestId }
            val notificationMessage = "$transitionString: ${triggeringGeofencesIdsList.joinToString(", ")}"

            NotificationHelper.sendGeofenceNotification(
                context,
                "Alerte de Géorepérage",
                notificationMessage
            )
        } else {
            Timber.e("Type de transition de géorepérage invalide: $geofenceTransition")
        }
    }
}