// app/src/main/java/com/easytoday/guidegroup/domain/usecase/AddGeofenceUseCase.kt
package com.easytoday.guidegroup.domain.usecase

import com.easytoday.guidegroup.domain.model.GeofenceArea
import com.easytoday.guidegroup.domain.repository.GeofenceRepository
import javax.inject.Inject

/**
 * Cas d'utilisation pour ajouter une nouvelle zone de géorepérage.
 *
 * @param geofenceRepository Le référentiel de géorepérage.
 */
class AddGeofenceUseCase @Inject constructor(
    private val geofenceRepository: GeofenceRepository
) {
    /**
     * Exécute l'ajout d'une zone de géorepérage.
     *
     * @param geofenceArea La zone de géorepérage à ajouter.
     * @return Un objet Result indiquant le succès ou l'échec.
     */
    suspend operator fun invoke(geofenceArea: GeofenceArea): Result<Unit> {
        return geofenceRepository.addGeofenceArea(geofenceArea)
    }
}


