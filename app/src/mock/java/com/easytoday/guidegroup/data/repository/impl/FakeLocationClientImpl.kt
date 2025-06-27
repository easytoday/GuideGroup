// app/src/mock/java/com/easytoday/guidegroup/data/repository/impl/fake/FakeLocationClientImpl.kt
package com.easytoday.guidegroup.data.repository.impl.fake

import android.location.Location
import com.easytoday.guidegroup.domain.repository.LocationClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implémentation factice (Fake) de LocationClient pour les tests et la prévisualisation.
 * Elle simule des mises à jour de localisation sans interagir avec les APIs système.
 */
class FakeLocationClientImpl @Inject constructor() : LocationClient {

    private var latitude = 48.8566 // Latitude de Paris
    private var longitude = 2.3522 // Longitude de Paris

    override fun getLocationUpdates(interval: Long): Flow<Location> = flow {
        while (true) {
            // Crée une fausse localisation
            val fakeLocation = Location("FakeProvider").apply {
                this.latitude = latitude
                this.longitude = longitude
                this.accuracy = 5.0f
                this.time = System.currentTimeMillis()
            }
            emit(fakeLocation)

            // Simule un petit déplacement pour voir des changements
            latitude += 0.0001
            longitude += 0.0001

            delay(interval) // Attend l'intervalle spécifié
        }
    }
}


