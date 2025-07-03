package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.domain.model.GeofenceArea
// CORRECTION : Importer notre classe Result personnalisée
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.repository.GeofenceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FakeGeofenceRepositoryImpl @Inject constructor() : GeofenceRepository {

    private val fakeGeofenceAreasDb = MutableStateFlow<MutableMap<String, GeofenceArea>>(mutableMapOf())

    init {
        val geofence1 = GeofenceArea(id = "geo1_id", groupId = "group1_id", name = "Point de rendez-vous A", latitude = 48.8584, longitude = 2.2945, radius = 100f, expirationDurationMillis = 3600000L, transitionTypes = 1)
        val geofence2 = GeofenceArea(id = "geo2_id", groupId = "group1_id", name = "Zone de musée", latitude = 48.8606, longitude = 2.3376, radius = 50f, expirationDurationMillis = 7200000L, transitionTypes = 3)
        val geofence3 = GeofenceArea(id = "geo3_id", groupId = "group2_id", name = "Cathédrale", latitude = 48.8529, longitude = 2.3499, radius = 75f, expirationDurationMillis = 86400000L, transitionTypes = 1)
        fakeGeofenceAreasDb.value[geofence1.id] = geofence1
        fakeGeofenceAreasDb.value[geofence2.id] = geofence2
        fakeGeofenceAreasDb.value[geofence3.id] = geofence3
    }

    override suspend fun addGeofenceArea(geofenceArea: GeofenceArea): Result<Unit> {
        delay(300)
        val currentDb = fakeGeofenceAreasDb.value
        currentDb[geofenceArea.id] = geofenceArea
        fakeGeofenceAreasDb.value = currentDb
        // CORRECTION : Utiliser notre Result.Success
        return Result.Success(Unit)
    }

    override suspend fun removeGeofenceArea(geofenceId: String): Result<Unit> {
        delay(300)
        val currentDb = fakeGeofenceAreasDb.value
        return if (currentDb.containsKey(geofenceId)) {
            currentDb.remove(geofenceId)
            fakeGeofenceAreasDb.value = currentDb
            // CORRECTION : Utiliser notre Result.Success
            Result.Success(Unit)
        } else {
            // CORRECTION : Utiliser notre Result.Error
            Result.Error("GeofenceArea with ID $geofenceId not found.", Exception("Not Found"))
        }
    }

    override fun getGeofenceAreasForGroup(groupId: String): Flow<List<GeofenceArea>> {
        return fakeGeofenceAreasDb.map { db ->
            delay(200)
            db.values.filter { it.groupId == groupId }
        }
    }

    override suspend fun startMonitoringGeofence(geofenceArea: GeofenceArea): Result<Unit> {
        delay(500)
        return if (geofenceArea.name.contains("no_permission", ignoreCase = true)) {
            // CORRECTION : Utiliser notre Result.Error
            Result.Error("Permission denied for monitoring geofence.", SecurityException("Fake permission denied"))
        } else {
            // CORRECTION : Utiliser notre Result.Success
            Result.Success(Unit)
        }
    }

    override suspend fun stopMonitoringGeofence(geofenceIds: List<String>): Result<Unit> {
        delay(500)
        return if (geofenceIds.contains("permission_fail")) {
            // CORRECTION : Utiliser notre Result.Error
            Result.Error("Permission denied for stopping geofence monitoring.", SecurityException("Fake permission denied"))
        } else {
            // CORRECTION : Utiliser notre Result.Success
            Result.Success(Unit)
        }
    }
}