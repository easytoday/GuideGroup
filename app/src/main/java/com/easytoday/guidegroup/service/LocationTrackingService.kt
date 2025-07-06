package com.easytoday.guidegroup.service

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
import com.easytoday.guidegroup.MainActivity
import com.easytoday.guidegroup.R
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.repository.LocationRepository
import com.easytoday.guidegroup.domain.repository.TrackingStateRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var locationRepository: LocationRepository
    @Inject
    lateinit var authRepository: AuthRepository
    @Inject
    lateinit var trackingStateRepository: TrackingStateRepository

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startLocationUpdates()
                startForeground(NOTIFICATION_ID, createNotification("Suivi de localisation activé"))
                trackingStateRepository.setTrackingState(true)
                Timber.d("LocationTrackingService a démarré et mis l'état à true")
            }
            ACTION_STOP -> {
                stopLocationUpdates()
                trackingStateRepository.setTrackingState(false)
                stopSelf()
                Timber.d("LocationTrackingService s'est arrêté et a mis l'état à false")
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        trackingStateRepository.setTrackingState(false)
        serviceScope.cancel()
        Timber.d("LocationTrackingService détruit, état mis à false")
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateLocationInFirestore(location)
                    updateNotification("Localisation: ${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}")
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
        }
    }

    private fun updateLocationInFirestore(location: Location) {
        serviceScope.launch {
            val currentUserResult = authRepository.getCurrentUser().firstOrNull()

            val userId: String? = when (currentUserResult) {
                is Result.Success -> currentUserResult.data?.id
                else -> null
            }

            if (userId != null) {
                val locationModel = com.easytoday.guidegroup.domain.model.Location(
                    userId = userId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = Date(location.time)
                )
                try {
                    locationRepository.updateLocation(locationModel)
                } catch (e: Exception) {
                    Timber.e(e, "Error updating location for user $userId in Firestore.")
                }
            } else {
                Timber.w("No current user ID found. Stopping service.")
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "GuideGroup Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Suivi de localisation GuideGroup")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
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
        const val EXTRA_GROUP_ID = "groupId"
        const val UPDATE_INTERVAL = 5000L
        const val FASTEST_UPDATE_INTERVAL = 3000L
    }
}