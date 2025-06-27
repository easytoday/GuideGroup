// app/src/main/java/com/easytoday/guidegroup/di/LocationModule.kt
package com.easytoday.guidegroup.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour fournir les dépendances liées à la localisation.
 */
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    /**
     * Fournit une instance Singleton de FusedLocationProviderClient.
     * Hilt injectera le Context qualifié par @ApplicationContext automatiquement.
     */
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context // Hilt fournit ce Context directement
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }

    // Vous pourriez avoir d'autres @Provides ou @Binds ici si nécessaire
}


