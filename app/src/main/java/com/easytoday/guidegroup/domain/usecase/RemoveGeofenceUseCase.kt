// app/src/main/java/com/easytoday/guidegroup/domain/usecase/RemoveGeofenceUseCase.kt
package com.easytoday.guidegroup.domain.usecase

import com.easytoday.guidegroup.domain.repository.GeofenceRepository
import javax.inject.Inject

/**
 * Cas d'utilisation pour supprimer une zone de géorepérage.
 *
 * @param geofenceRepository Le référentiel de géorepérage.
 */
class RemoveGeofenceUseCase @Inject constructor(
    private val geofenceRepository: GeofenceRepository
) {
    /**
     * Exécute la suppression d'une zone de géorepérage.
     *
     * @param geofenceId L'ID de la zone de géorepérage à supprimer.
     * @return Un objet Result indiquant le succès ou l'échec.
     */
    suspend operator fun invoke(geofenceId: String): Result<Unit> {
        return geofenceRepository.removeGeofenceArea(geofenceId)
    }
}


