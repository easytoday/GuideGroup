package com.easytoday.guidegroup.di

import android.content.Context
import androidx.room.Room
import com.easytoday.guidegroup.data.local.GuideGroupDatabase
import com.easytoday.guidegroup.data.local.PointOfInterestDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // construction de la base de données
    @Provides
    @Singleton
    fun provideGuideGroupDatabase(
        @ApplicationContext context: Context // contexte de l'applocation fournie par Hilt
    ): GuideGroupDatabase {
        return Room.databaseBuilder(
            context,
            GuideGroupDatabase::class.java,
            "guidegroup.db"
        ).build()
    }

    // explique à hilt comment obtenir les DAO pour les POI
    @Provides
    @Singleton
    fun providePointOfInterestDao(
        database: GuideGroupDatabase
    ): PointOfInterestDao {
        return database.pointOfInterestDao()
    }

    // ajout des DAOs pour les autres tables (locations, etc.) dans le futur.
}
