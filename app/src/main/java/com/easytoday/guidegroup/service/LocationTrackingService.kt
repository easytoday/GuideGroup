package com.easytoday.guidegroup.service

import com.easytoday.guidegroup.notification.NotificationHelper

import android.Manifest

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.easytoday.guidegroup.MainActivity // Assurez-vous que c'est le bon chemin vers votre MainActivity
import com.easytoday.guidegroup.R // Assurez-vous que vous avez un dossier res/drawable avec ic_launcher_foreground ou une icône de votre choix
import com.easytoday.guidegroup.domain.repository.LocationRepository
import com.easytoday.guidegroup.domain.repository.AuthRepository // Pour obtenir l'ID de l'utilisateur actuel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull // Pour obtenir la valeur actuelle du Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

// Annotation Hilt pour permettre l'injection de dépendances dans ce service
@AndroidEntryPoint
class LocationTrackingService : Service() {

    // Injectez LocationRepository et AuthRepository via Hilt
    @Inject
    lateinit var locationRepository: LocationRepository
    @Inject
    lateinit var authRepository: AuthRepository // Injecter AuthRepository pour l'ID utilisateur

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var currentGroupId: String? = null

    // CoroutineScope pour gérer les coroutines dans le service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Timber.d("LocationTrackingService onCreate")
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("LocationTrackingService onStartCommand: ${intent?.action}")
        when (intent?.action) {
            ACTION_START -> {
                currentGroupId = intent.getStringExtra(EXTRA_GROUP_ID)
                startLocationUpdates()
                // Démarrer le service en mode foreground
                startForeground(NOTIFICATION_ID, createNotification("Suivi de localisation activé"))
            }
            ACTION_STOP -> {
                stopLocationUpdates()
                stopSelf() // Arrête le service
            }
        }
        return START_STICKY // Le service sera recréé si le système le tue
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Ce service n'est pas un service lié
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("LocationTrackingService onDestroy")
        stopLocationUpdates()
        serviceScope.cancel() // Annule toutes les coroutines du scope
    }

    @SuppressLint("MissingPermission") // La vérification des permissions est faite avant l'appel à startService
    private fun startLocationUpdates() {
        // Double vérification des permissions pour une robustesse maximale
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Timber.e("Location permissions not granted in service. Cannot start updates.")
            // À ce stade, le service devrait être arrêté ou une notification d'erreur affichée
            stopSelf()
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
            //.setWaitForActivityUpdates(WAIT_FOR_ACTIVITY_UPDATES) <<-- deprecated il me semble
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Timber.d("New location: Lat=${location.latitude}, Lng=${location.longitude}, GroupID=${currentGroupId}")
                    updateLocationInFirestore(location)
                    // Mettre à jour la notification pour montrer que le suivi est actif
                    updateNotification("Localisation: ${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}")
                }
            }
        }

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback as LocationCallback, // Cast sécurisé car nous venons de l'assigner
                Looper.getMainLooper() // Utilise le Looper principal pour les callbacks
            )
            Timber.d("Location updates started.")
        } catch (e: SecurityException) {
            Timber.e(e, "Could not start location updates. SecurityException: Ensure all permissions are granted.")
            stopSelf() // Arrête le service si les permissions sont révoquées
        }
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
            Timber.d("Location updates stopped.")
        }
    }

    private fun updateLocationInFirestore(location: Location) {
        serviceScope.launch {
            // Obtenir l'utilisateur actuel. Le Flow renvoie un Result<User?>.
            // On récupère le premier élément du Flow et on gère les cas Success/Error/Loading.
            val currentUserResult = authRepository.getCurrentUser().firstOrNull()

            val userId: String? = when (currentUserResult) {
                is com.easytoday.guidegroup.domain.model.Result.Success -> {
                    // Si c'est un succès, extrait l'objet User et son ID
                    currentUserResult.data?.id // <-- Ici, on accède à 'id' si 'data' n'est pas null
                }
                else -> {
                    // Pour Result.Error, Result.Loading, ou si le Flow est vide, l'ID est null
                    Timber.w("Failed to get current user for location update: $currentUserResult")
                    null
                }
            }


            if (userId != null) {
                val locationModel = com.easytoday.guidegroup.domain.model.Location(
                    userId = userId, // <-- Maintenant userId est de type String?
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = Date(location.time)
                )
                try {
                    locationRepository.updateLocation(locationModel)
                    Timber.d("Location for user $userId updated in Firestore.")
                } catch (e: Exception) {
                    Timber.e(e, "Error updating location for user $userId in Firestore.")
                }
            } else {
                Timber.w("No current user ID found to update location. Stopping service.")
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "GuideGroup Location Service",
                NotificationManager.IMPORTANCE_LOW // Faible importance pour une notification discrète
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            // Ces flags permettent de ramener votre activité au premier plan si elle est déjà ouverte
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            // FLAG_IMMUTABLE est obligatoire depuis Android 6.0 (API 23) pour PendingIntent
            // FLAG_UPDATE_CURRENT permet de mettre à jour un PendingIntent existant
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Suivi de localisation GuideGroup")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Remplacez par l'icône de votre application
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Rend la notification non balayable tant que le service est actif
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(contentText))
    }

    companion object {
        const val CHANNEL_ID = "GuideGroupLocationChannel"
        const val NOTIFICATION_ID = 123
        const val ACTION_START = "ACTION_START_LOCATION_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_LOCATION_TRACKING"
        const val EXTRA_GROUP_ID = "groupId" // Clé pour passer l'ID du groupe au service
        const val UPDATE_INTERVAL = 5000L // Intervalle de mise à jour (5 secondes)
        const val FASTEST_UPDATE_INTERVAL = 3000L // Intervalle de mise à jour le plus rapide (3 secondes)
        const val WAIT_FOR_ACTIVITY_UPDATES = 1000L // Délai avant la livraison des mises à jour par lot (si activé)
    }
}

