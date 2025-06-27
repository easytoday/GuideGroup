// app/src/main/java/com/easytoday/guidegroup/domain/usecase/StopGeofenceMonitoringUseCase.kt
package com.easytoday.guidegroup.domain.usecase

import com.easytoday.guidegroup.domain.repository.GeofenceRepository
import javax.inject.Inject

/**
 * Cas d'utilisation pour arrêter la surveillance d'une ou plusieurs zones de géorepérage.
 *
 * @param geofenceRepository Le référentiel de géorepérage.
 */
class StopGeofenceMonitoringUseCase @Inject constructor(
    private val geofenceRepository: GeofenceRepository
) {
    /**
     * Exécute l'arrêt de la surveillance des zones de géorepérage.
     *
     * @param geofenceIds La liste des IDs des zones de géorepérage à arrêter de surveiller.
     * @return Un objet Result indiquant le succès ou l'échec.
     */
    suspend operator fun invoke(geofenceIds: List<String>): Result<Unit> {
        return geofenceRepository.stopMonitoringGeofence(geofenceIds)
    }
}


