package com.easytoday.guidegroup.domain.repository

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Interface pour un client de localisation.
 * Définit la méthode pour obtenir des mises à jour de localisation en temps réel.
 */
interface LocationClient {
    /**
     * Retourne un Flow de mises à jour de localisation.
     * @param interval L'intervalle de temps souhaité en millisecondes pour les mises à jour.
     * @return Un Flow de [Location]. Émet une erreur si les permissions sont manquantes ou si les services de localisation sont désactivés.
     */
    fun getLocationUpdates(interval: Long): Flow<Location>

    /**
     * Exception personnalisée pour les erreurs liées à la localisation.
     */
    class LocationException(message: String) : Exception(message)
}


