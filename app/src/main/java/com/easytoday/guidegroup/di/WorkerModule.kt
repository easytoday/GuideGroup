package com.easytoday.guidegroup.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Ce module est maintenant vide car la configuration de WorkManager
 * est gérée directement dans la classe GuideGroupApplication en implémentant
 * l'interface Configuration.Provider.
 *
 * Il peut être conservé pour de futures configurations ou supprimé.
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    // La configuration de base est déplacée vers GuideGroupApplication pour plus de simplicité.
}