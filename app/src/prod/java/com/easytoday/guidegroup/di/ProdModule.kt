package com.easytoday.guidegroup.di

import com.easytoday.guidegroup.data.location.LocationClientImpl
import com.easytoday.guidegroup.data.repository.impl.*
import com.easytoday.guidegroup.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdModule {

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

    @Binds
    @Singleton
    abstract fun bindLocationClient(impl: LocationClientImpl): LocationClient

    @Binds
    @Singleton
    abstract fun bindGeofenceRepository(impl: GeofenceRepositoryImpl): GeofenceRepository
}