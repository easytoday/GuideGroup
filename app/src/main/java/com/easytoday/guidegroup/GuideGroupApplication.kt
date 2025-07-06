package com.easytoday.guidegroup

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.easytoday.guidegroup.notification.NotificationHelper // <-- Assurez-vous que cet import est présent
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class GuideGroupApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // CORRECTION : On crée le canal de notification au démarrage de l'application.
        NotificationHelper.createNotificationChannel(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}