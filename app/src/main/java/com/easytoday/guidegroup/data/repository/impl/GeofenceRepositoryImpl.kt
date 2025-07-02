package com.easytoday.guidegroup.data.repository.impl

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.GeofenceArea
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.repository.GeofenceRepository
import com.easytoday.guidegroup.geofencing.GeofenceTransitionsService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.firebase.firestore.ktx.snapshots
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implémentation du référentiel de géorepérage utilisant Firebase Firestore
 * pour la persistance des zones et l'API Geofencing de Google Play Services
 * pour la surveillance active.
 *
 * @param firestoreHelper L'aide pour les opérations Firestore.
 * @param geofencingClient Le client de l'API Geofencing.
 * @param context Le contexte de l'application, nécessaire pour créer le PendingIntent.
 */
class GeofenceRepositoryImpl @Inject constructor(
    private val firestoreHelper: FirestoreHelper,
    private val geofencingClient: GeofencingClient,
    @ApplicationContext private val context: Context
) : GeofenceRepository {

    private val TAG = "GeofenceRepositoryImpl"

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceTransitionsService::class.java)
        PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override suspend fun addGeofenceArea(geofenceArea: GeofenceArea): Result<Unit> {
        return try {
            firestoreHelper.db.collection("geofenceAreas").document(geofenceArea.id).set(geofenceArea).await()
            Log.d(TAG, "Zone de géorepérage '${geofenceArea.name}' ajoutée à Firestore.")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'ajout de la zone de géorepérage à Firestore: ${e.message}", e)
            Result.Error("Erreur Firestore: ${e.localizedMessage}", e)
        }
    }

    override suspend fun removeGeofenceArea(geofenceId: String): Result<Unit> {
        return try {
            firestoreHelper.db.collection("geofenceAreas").document(geofenceId).delete().await()
            Log.d(TAG, "Zone de géorepérage '$geofenceId' supprimée de Firestore.")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression de la zone de géorepérage de Firestore: ${e.message}", e)
            Result.Error("Erreur Firestore: ${e.localizedMessage}", e)
        }
    }

    override fun getGeofenceAreasForGroup(groupId: String): Flow<List<GeofenceArea>> {
        return firestoreHelper.db.collection("geofenceAreas")
            .whereEqualTo("groupId", groupId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(GeofenceArea::class.java) }
            }
    }

    override suspend fun startMonitoringGeofence(geofenceArea: GeofenceArea): Result<Unit> {
        val geofence = Geofence.Builder()
            .setRequestId(geofenceArea.id)
            .setCircularRegion(geofenceArea.latitude, geofenceArea.longitude, geofenceArea.radius)
            .setExpirationDuration(geofenceArea.expirationDurationMillis)
            .setTransitionTypes(geofenceArea.transitionTypes)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        return try {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).await()
            Log.d(TAG, "Surveillance de la géorepérage '${geofenceArea.name}' démarrée avec succès.")
            Result.Success(Unit)
        } catch (e: SecurityException) {
            Log.e(TAG, "Erreur de permission lors du démarrage de la surveillance de la géorepérage: ${e.message}", e)
            Result.Error("Permission de localisation manquante pour démarrer le géorepérage.", e)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur inattendue lors du démarrage de la surveillance de la géorepérage: ${e.message}", e)
            Result.Error("Erreur inattendue: ${e.localizedMessage}", e)
        }
    }

    override suspend fun stopMonitoringGeofence(geofenceIds: List<String>): Result<Unit> {
        return try {
            geofencingClient.removeGeofences(geofenceIds).await()
            Log.d(TAG, "Surveillance des géorepérages $geofenceIds arrêtée avec succès.")
            Result.Success(Unit)
        } catch (e: SecurityException) {
            Log.e(TAG, "Erreur de permission lors de l'arrêt de la surveillance des géorepérages: ${e.message}", e)
            Result.Error("Permission de localisation manquante pour arrêter le géorepérage.", e)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur inattendue lors de l'arrêt de la surveillance des géorepérages: ${e.message}", e)
            Result.Error("Erreur inattendue: ${e.localizedMessage}", e)
        }
    }
}