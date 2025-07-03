package com.easytoday.guidegroup.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Timber.e("GeofencingEvent est null. Impossible de traiter.")
            return
        }

        if (geofencingEvent.hasError()) {
            val errorMessage = "Erreur de géorepérage: ${geofencingEvent.errorCode}"
            Timber.e(errorMessage)
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: emptyList()

        val workerData = Data.Builder()
            .putInt(GeofenceWorker.KEY_TRANSITION, geofenceTransition)
            .putStringArray(GeofenceWorker.KEY_GEOFENCE_IDS, triggeringGeofences.map { it.requestId }.toTypedArray())
            .build()

        val workRequest = OneTimeWorkRequestBuilder<GeofenceWorker>()
            .setInputData(workerData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        Timber.d("Broadcast reçu et Worker pour le géorepérage mis en file d'attente.")
    }
}