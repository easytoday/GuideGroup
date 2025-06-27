// app/src/main/java/com/easytoday/guidegroup/di/AppModule.kt
package com.easytoday.guidegroup.di

// Assurez-vous que l'import de Context et ApplicationContext est toujours là si d'autres classes en ont besoin
import android.content.Context
import android.app.Application // Si vous fournissez d'autres choses qui ont besoin de l'Application brute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour fournir des dépendances au niveau de l'application (SingletonComponent).
 * Ne contient plus la méthode provideApplicationContext car Hilt la fournit déjà.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // La méthode provideApplicationContext() a été retirée car Hilt la fournit par défaut.
    // Laissez ce module vide s'il n'a pas d'autres @Provides ou @Binds.
    // Ou ajoutez ici d'autres @Provides pour des dépendances de base qui ne sont pas des Repositories
    // et qui ne sont pas spécifiques aux flavors.

    // Par exemple, si vous voulez toujours fournir FusedLocationProviderClient ici,
    // assurez-vous que son paramètre 'Context' est qualifié.
    /*
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context // Hilt fournit ce Context directement
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
    */
}


