// app/src/prod/java/com/easytoday/guidegroup/di/ProdModule.kt  pour la prod
package com.easytoday.guidegroup.di

import com.easytoday.guidegroup.data.location.LocationClientImpl // Importez l'implémentation réelle
import com.easytoday.guidegroup.data.repository.impl.AuthRepositoryImpl
import com.easytoday.guidegroup.data.repository.impl.GroupRepositoryImpl
import com.easytoday.guidegroup.data.repository.impl.LocationRepositoryImpl
import com.easytoday.guidegroup.data.repository.impl.MeetingPointRepositoryImpl
import com.easytoday.guidegroup.data.repository.impl.MessageRepositoryImpl
import com.easytoday.guidegroup.data.repository.impl.PointOfInterestRepositoryImpl
import com.easytoday.guidegroup.data.repository.impl.UserRepositoryImpl
import com.easytoday.guidegroup.domain.repository.LocationClient // Importez l'interface
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.repository.GroupRepository
import com.easytoday.guidegroup.domain.repository.LocationRepository
import com.easytoday.guidegroup.domain.repository.MeetingPointRepository
import com.easytoday.guidegroup.domain.repository.MessageRepository
import com.easytoday.guidegroup.domain.repository.PointOfInterestRepository
import com.easytoday.guidegroup.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdModule { // Utilisez 'abstract class' pour @Binds

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindPointOfInterestRepository(impl: PointOfInterestRepositoryImpl): PointOfInterestRepository

    @Binds
    @Singleton
    abstract fun bindMeetingPointRepository(impl: MeetingPointRepositoryImpl): MeetingPointRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    // Ajoutez tous les autres Binds pour vos dépôts réels ici
    // --- NOUVEAU : Liaison pour LocationClient ---
    @Binds
    @Singleton
    abstract fun bindLocationClient(
        impl: LocationClientImpl // Lie l'interface à l'implémentation réelle
    ): LocationClient
    // --- FIN NOUVEAU ---

}

