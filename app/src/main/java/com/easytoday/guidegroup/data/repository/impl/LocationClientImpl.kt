// app/src/main/java/com/easytoday/guidegroup/data/location/LocationClientImpl.kt
package com.easytoday.guidegroup.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.easytoday.guidegroup.domain.repository.LocationClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Implémentation réelle de LocationClient utilisant FusedLocationProviderClient.
 * Cette classe doit être dans le dossier 'main' car elle interagit avec les APIs Android réelles.
 */
class LocationClientImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {

    @SuppressLint("MissingPermission") // La gestion des permissions est faite avant l'appel à cette fonction
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            // Vérifie si les services de localisation sont activés
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGpsEnabled && !isNetworkEnabled) {
                // Si la localisation est désactivée, on pourrait émettre une erreur ou un état spécifique
                // ou simplement ne pas émettre de localisation. Pour cet exemple, on continue.
                // launch { send(Location(null)) } // Exemple pour un cas d'erreur/état
                // close(LocationException("Location services are disabled.")) // Ou une exception
            }

            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
                .setWaitForAccurateLocation(false)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location ->
                        launch { send(location) }
                    }
                }
            }

            client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }
}

// Vous pouvez définir une exception personnalisée si nécessaire
class LocationException(message: String) : Exception(message)



