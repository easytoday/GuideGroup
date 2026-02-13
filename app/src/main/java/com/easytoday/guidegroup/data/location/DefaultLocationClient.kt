package com.easytoday.guidegroup.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
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
import timber.log.Timber // Timber ou Log pour le débogage

import javax.inject.Inject

/**
 * Implémentation par défaut de [LocationClient] utilisant [FusedLocationProviderClient].
 * Fournit des mises à jour de localisation en temps réel via un Flow.
 */
class DefaultLocationClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {

    /**
     * Récupère un flux de mises à jour de localisation.
     * Les permissions sont supposées être gérées au niveau de l'UI/ViewModel.
     *
     * @param interval L'intervalle de temps souhaité en millisecondes entre les mises à jour.
     * @return Un Flow de [Location]. Émet une erreur si les permissions sont manquantes ou si les services de localisation sont désactivés.
     */
    @SuppressLint("MissingPermission") // La vérification des permissions est gérée par hasLocationPermission()
    override fun getLocationUpdates(interval: Long): Flow<Location> = callbackFlow {
        // 1. Vérification des permissions
        if (!context.hasLocationPermission()) {
            close(LocationClient.LocationException("Missing location permission."))
            return@callbackFlow
        }

        // 2. Vérification de l'activation du GPS/réseau
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            close(LocationClient.LocationException("GPS or network is not enabled."))
            return@callbackFlow
        }

        // 3. Configuration de la requête de localisation
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
            .setMinUpdateIntervalMillis(interval / 2) // Recevoir des mises à jour même plus fréquemment si disponibles
            .setWaitForAccurateLocation(true) // Attendre une localisation plus précise
            .build()

        // 4. Création du callback de localisation
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.lastOrNull()?.let { location ->
                    Timber.d("New location received: Lat ${location.latitude}, Lng ${location.longitude}")
                    // Envoyer la localisation au Flow
                    launch { send(location) }
                }
            }
        }

        // 5. Demande des mises à jour de localisation
        client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            .addOnFailureListener { e ->
                Timber.e("Error requesting location updates: ${e.message}", e)
                close(LocationClient.LocationException("Failed to request location updates: ${e.localizedMessage}"))
            }

        // 6. Logique pour retirer les mises à jour lorsque le Flow est fermé
        awaitClose {
            Timber.d("Removing location updates.")
            client.removeLocationUpdates(locationCallback)
        }
    }
}

// Fonction d'extension pour vérifier les permissions de localisation
fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}


