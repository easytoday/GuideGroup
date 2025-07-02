package com.easytoday.guidegroup.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
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
     */
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }

    /**
     * Fournit une instance Singleton de GeofencingClient.
     */
    @Provides
    @Singleton
    fun provideGeofencingClient(
        @ApplicationContext app: Context
    ): GeofencingClient {
        return LocationServices.getGeofencingClient(app)
    }
}