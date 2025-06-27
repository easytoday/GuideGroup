// app/src/main/java/com/easytoday/guidegroup/GuideGroupApplication.kt
package com.easytoday.guidegroup

import android.app.Application
import androidx.media3.common.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber // Si vous utilisez Timber

/**
 * Classe Application personnalisée pour l'initialisation de Hilt.
 * L'annotation @HiltAndroidApp déclenche la génération de code Hilt,
 * * c'est le point d'entrée de Hilt pour la génération de code
 * y compris le composant d'application qui fournit le Context.
 */
@HiltAndroidApp
class GuideGroupApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialiser Timber si vous l'utilisez
        if (BuildConfig.DEBUG) { // Utilisez BuildConfig.DEBUG pour n'activer Timber qu'en mode debug
            Timber.plant(Timber.DebugTree())
        }
        // Pour les builds de production, vous pourriez planter un CrashReportingTree
        // Timber.plant(CrashReportingTree())
    }
}

