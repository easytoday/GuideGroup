package com.easytoday.guidegroup.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.easytoday.guidegroup.MainActivity // Votre activité principale
import com.easytoday.guidegroup.R // Pour les ressources (icône de notification)

/**
 * Classe utilitaire pour créer et envoyer des notifications Android.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "group_guide_channel"
    private const val CHANNEL_NAME = "Notifications du Groupe Guide"
    private const val NOTIFICATION_ID_GEOFENCE = 1001
    private const val NOTIFICATION_ID_CHAT = 1002
    // Ajoutez d'autres IDs de notification si nécessaire

    /**
     * Crée le canal de notification pour Android Oreo (API 26) et les versions ultérieures.
     * Doit être appelé au démarrage de l'application (ex: dans guidegroupApplication ou MainActivity).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Canal pour les notifications importantes de l'application Group Guide"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Envoie une notification de géorepérage.
     *
     * @param context Le contexte de l'application.
     * @param title Le titre de la notification.
     * @param message Le corps du message de la notification.
     */
    fun sendGeofenceNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            // MODIFICATION ICI : Utilisez une icône existante ou assurez-vous que ic_notification existe
            // R.drawable.ic_launcher_foreground est une icône par défaut souvent présente.
            // REMPLACEZ PAR VOTRE VRAIE ICÔNE SI ELLE N'EST PAS ic_notification
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TEMPORAIRE: Assurez-vous d'avoir cette ressource ou remplacez-la
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // La notification disparaît au clic

        with(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
            notify(NOTIFICATION_ID_GEOFENCE, builder.build())
        }
    }

    /**
     * Envoie une notification de message de chat.
     *
     * @param context Le contexte de l'application.
     * @param title Le titre de la notification.
     * @param message Le corps du message de la notification.
     */
    fun sendChatMessageNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            // MODIFICATION ICI : Utilisez une icône existante ou assurez-vous que ic_notification existe
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TEMPORAIRE: Assurez-vous d'avoir cette ressource ou remplacez-la
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Priorité par défaut pour les messages de chat
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
            notify(NOTIFICATION_ID_CHAT, builder.build())
        }
    }
}

