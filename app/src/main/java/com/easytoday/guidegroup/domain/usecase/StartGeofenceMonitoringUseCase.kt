package com.easytoday.guidegroup.domain.usecase

import com.easytoday.guidegroup.domain.model.GeofenceArea
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.repository.GeofenceRepository
import javax.inject.Inject

/**
 * Cas d'utilisation pour démarrer la surveillance d'une zone de géorepérage.
 *
 * @param geofenceRepository Le référentiel de géorepérage.
 */
class StartGeofenceMonitoringUseCase @Inject constructor(
    private val geofenceRepository: GeofenceRepository
) {
    /**
     * Exécute le démarrage de la surveillance d'une zone de géorepérage.
     *
     * @param geofenceArea La zone de géorepérage à surveiller.
     * @return Un objet Result indiquant le succès ou l'échec.
     */
    suspend operator fun invoke(geofenceArea: GeofenceArea): Result<Unit> {
        return geofenceRepository.startMonitoringGeofence(geofenceArea)
    }
}