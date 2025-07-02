package com.easytoday.guidegroup.domain.repository

import com.easytoday.guidegroup.domain.model.GeofenceArea
import com.easytoday.guidegroup.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Interface définissant les opérations liées au géorepérage.
 * Gère la persistance des zones de géorepérage et l'interaction avec l'API Geofencing d'Android.
 */
interface GeofenceRepository {
    /**
     * Ajoute une nouvelle zone de géorepérage à la base de données.
     *
     * @param geofenceArea L'objet GeofenceArea à ajouter.
     * @return Un objet Result indiquant le succès ou l'échec.
     */
    suspend fun addGeofenceArea(geofenceArea: GeofenceArea): Result<Unit>

    /**
     * Supprime une zone de géorepérage de la base de données.
     *
     * @param geofenceId L'ID de la zone de géorepérage à supprimer.
     * @return Un objet Result indiquant le succès ou l'échec.
     */
    suspend fun removeGeofenceArea(geofenceId: String): Result<Unit>

    /**
     * Récupère un flux de toutes les zones de géorepérage pour un groupe donné.
     *
     * @param groupId L'ID du groupe.
     * @return Un Flow émettant une liste de GeofenceArea.
     */
    fun getGeofenceAreasForGroup(groupId: String): Flow<List<GeofenceArea>>

    /**
     * Commence la surveillance d'une zone de géorepérage spécifique via l'API Geofencing d'Android.
     *
     * @param geofenceArea L'objet GeofenceArea à surveiller.
     * @return Un objet Result indiquant le succès ou l'échec de l'enregistrement de la géorepérage.
     */
    suspend fun startMonitoringGeofence(geofenceArea: GeofenceArea): Result<Unit>

    /**
     * Arrête la surveillance des zones de géorepérage spécifiées via l'API Geofencing d'Android.
     *
     * @param geofenceIds La liste des IDs des zones de géorepérage à arrêter de surveiller.
     * @return Un objet Result indiquant le succès ou l'échec de la suppression des géorepérages.
     */
    suspend fun stopMonitoringGeofence(geofenceIds: List<String>): Result<Unit>
}