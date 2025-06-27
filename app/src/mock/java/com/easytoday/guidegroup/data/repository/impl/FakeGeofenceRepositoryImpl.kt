package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.domain.model.GeofenceArea
import com.easytoday.guidegroup.domain.repository.GeofenceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Implémentation factice de GeofenceRepository pour l'environnement de test (mock).
// Simule les opérations CRUD sur les zones de géorepérage et leur "surveillance".
class FakeGeofenceRepositoryImpl @Inject constructor() : GeofenceRepository {

    // Simule une base de données de zones de géorepérage en mémoire
    private val fakeGeofenceAreasDb = MutableStateFlow<MutableMap<String, GeofenceArea>>(mutableMapOf())

    // Initialise avec quelques zones de géorepérage factices pour les tests
    init {
        val geofence1 = GeofenceArea(
            id = "geo1_id",
            groupId = "group1_id",
            name = "Point de rendez-vous A",
            latitude = 48.8584,
            longitude = 2.2945,
            radius = 100f,
            expirationDurationMillis = 3600000L, // 1 heure
            transitionTypes = 1 // Geofence.GEOFENCE_TRANSITION_ENTER
        )
        val geofence2 = GeofenceArea(
            id = "geo2_id",
            groupId = "group1_id",
            name = "Zone de musée",
            latitude = 48.8606,
            longitude = 2.3376,
            radius = 50f,
            expirationDurationMillis = 7200000L, // 2 heures
            transitionTypes = 3 // Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofence3 = GeofenceArea(
            id = "geo3_id",
            groupId = "group2_id",
            name = "Cathédrale",
            latitude = 48.8529,
            longitude = 2.3499,
            radius = 75f,
            expirationDurationMillis = 86400000L, // 24 heures
            transitionTypes = 1
        )
        fakeGeofenceAreasDb.value[geofence1.id] = geofence1
        fakeGeofenceAreasDb.value[geofence2.id] = geofence2
        fakeGeofenceAreasDb.value[geofence3.id] = geofence3
    }

    /**
     * Ajoute une zone de géorepérage à Firestore (simulée).
     */
    override suspend fun addGeofenceArea(geofenceArea: GeofenceArea): Result<Unit> {
        delay(300) // Simule un délai
        fakeGeofenceAreasDb.value[geofenceArea.id] = geofenceArea
        // Pour s'assurer que le MutableStateFlow émet une nouvelle valeur
        fakeGeofenceAreasDb.value = fakeGeofenceAreasDb.value.toMutableMap()
        return Result.success(Unit) // Utilise le Result de Kotlin ou votre propre implémentation
    }

    /**
     * Supprime une zone de géorepérage de Firestore (simulée).
     */
    override suspend fun removeGeofenceArea(geofenceId: String): Result<Unit> {
        delay(300) // Simule un délai
        if (fakeGeofenceAreasDb.value.containsKey(geofenceId)) {
            fakeGeofenceAreasDb.value.remove(geofenceId)
            // Pour s'assurer que le MutableStateFlow émet une nouvelle valeur
            fakeGeofenceAreasDb.value = fakeGeofenceAreasDb.value.toMutableMap()
            return Result.success(Unit)
        } else {
            // Simule l'échec si l'ID n'existe pas
            return Result.failure(Exception("GeofenceArea with ID $geofenceId not found for removal."))
        }
    }

    /**
     * Récupère un flux de toutes les zones de géorepérage pour un groupe donné (simulé).
     */
    override fun getGeofenceAreasForGroup(groupId: String): Flow<List<GeofenceArea>> {
        return fakeGeofenceAreasDb.map { db ->
            delay(200) // Simule un léger délai
            db.values.filter { it.groupId == groupId }
        }
    }

    /**
     * Commence la surveillance d'une zone de géorepérage (simulée).
     * Simule un succès ou un échec de permission.
     */
    override suspend fun startMonitoringGeofence(geofenceArea: GeofenceArea): Result<Unit> {
        delay(500) // Simule un délai pour l'opération API
        // Vous pouvez ajouter une logique pour simuler une SecurityException
        // Par exemple, si le nom de la zone contient "no_permission"
        if (geofenceArea.name.contains("no_permission", ignoreCase = true)) {
            return Result.failure(SecurityException("Permission denied for monitoring geofence."))
        }
        // Simuler qu'elle est maintenant "activement surveillée"
        // Nous n'avons pas de vrai état de surveillance dans cette base de données simple,
        // mais l'opération est considérée comme réussie.
        return Result.success(Unit)
    }

    /**
     * Arrête la surveillance des zones de géorepérage (simulée).
     * Simule un succès ou un échec de permission.
     */
    override suspend fun stopMonitoringGeofence(geofenceIds: List<String>): Result<Unit> {
        delay(500) // Simule un délai pour l'opération API
        // Vous pouvez ajouter une logique pour simuler une SecurityException
        // Par exemple, si l'un des IDs est "permission_fail"
        if (geofenceIds.contains("permission_fail")) {
            return Result.failure(SecurityException("Permission denied for stopping geofence monitoring."))
        }
        // Simuler l'arrêt de la surveillance
        return Result.success(Unit)
    }
}

