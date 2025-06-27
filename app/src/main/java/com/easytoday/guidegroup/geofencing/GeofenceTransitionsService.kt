package com.easytoday.guidegroup.geofencing

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.easytoday.guidegroup.notification.NotificationHelper // Assurez-vous que cette classe existe

/**
 * IntentService qui gère les transitions de géorepérage déclenchées par l'API Geofencing.
 * Il est responsable de l'analyse de l'événement et de l'envoi de notifications.
 */
class GeofenceTransitionsService : IntentService("GeofenceTransitionsService") {

    companion object {
        private const val TAG = "GeofenceTransitionsService"
    }

    /**
     * Cette méthode est appelée sur un thread de travail séparé pour gérer l'Intent.
     */
    override fun onHandleIntent(intent: Intent?) {
        // IMPORTANT : Gérer la nullabilité de l'intent
        if (intent == null) {
            Log.e(TAG, "Intent reçu dans onHandleIntent est null.")
            return
        }

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // IMPORTANT : Vérifier si geofencingEvent est null après la création
        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent est null après la création à partir de l'Intent.")
            NotificationHelper.sendGeofenceNotification(
                this,
                "Erreur Géorepérage",
                "Impossible de récupérer l'événement de géorepérage."
            )
            return
        }

        // Vérifie s'il y a eu une erreur lors de la récupération de l'événement.
        if (geofencingEvent.hasError()) {
            val errorMessage = "Erreur de géorepérage: ${geofencingEvent.errorCode}"
            Log.e(TAG, errorMessage)
            // Envoi d'une notification d'erreur au guide si nécessaire
            NotificationHelper.sendGeofenceNotification(
                this,
                "Erreur Géorepérage",
                "Une erreur est survenue lors de la détection de géorepérage: $errorMessage"
            )
            return
        }

        // Récupère le type de transition de géorepérage (entrée, sortie, séjour).
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Récupère la liste des géorepérages qui ont déclenché l'événement.
        // IMPORTANT : triggeringGeofences est une List<Geofence>! (non-nullable List of non-nullable Geofences)
        // La méthode retourne List<Geofence!> en interne Java, que Kotlin mappe à List<Geofence>.
        // L'erreur précédente suggérait un nullable, ce qui est souvent une mauvaise inférence ou un cas edge.
        // Assurons-nous qu'elle est traitée comme non-nullable si le event n'a pas d'erreur.
        val triggeringGeofences: List<Geofence> = geofencingEvent.triggeringGeofences ?: emptyList()


        // Vérifie si la transition est un type que nous surveillons.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
        ) {
            // Construit les détails de la notification.
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                geofenceTransition,
                triggeringGeofences // Passer la liste non-nullable
            )
            Log.i(TAG, geofenceTransitionDetails)

            // Envoie la notification au guide.
            NotificationHelper.sendGeofenceNotification(
                this,
                "Alerte Géorepérage",
                geofenceTransitionDetails
            )
        } else {
            // Log les transitions inconnues.
            Log.e(TAG, "Transition de géorepérage invalide: $geofenceTransition")
        }
    }

    /**
     * Construit une chaîne de caractères décrivant la transition de géorepérage.
     *
     * @param geofenceTransition Le type de transition.
     * @param triggeringGeofences La liste des géorepérages déclenchés.
     * @return Une chaîne de caractères descriptive.
     */
    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        val geofenceTransitionString = when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Entrée dans la zone"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Sortie de la zone"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "Séjour dans la zone"
            else -> "Transition inconnue"
        }

        // Récupère les IDs des géorepérages déclenchés.
        val triggeringGeofencesIdsList = triggeringGeofences.map { it.requestId }
        return "$geofenceTransitionString: ${triggeringGeofencesIdsList.joinToString(", ")}"
    }
}

