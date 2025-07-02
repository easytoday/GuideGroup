package com.easytoday.guidegroup.di

import com.easytoday.guidegroup.data.repository.impl.*
import com.easytoday.guidegroup.data.repository.impl.fake.FakeLocationClientImpl
import com.easytoday.guidegroup.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@MockBuild
abstract class MockModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FakeAuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(impl: FakeGroupRepositoryImpl): GroupRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: FakeMessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: FakeLocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindPointOfInterestRepository(impl: FakePointOfInterestRepositoryImpl): PointOfInterestRepository

    @Binds
    @Singleton
    abstract fun bindMeetingPointRepository(impl: FakeMeetingPointRepositoryImpl): MeetingPointRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: FakeUserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindLocationClient(impl: FakeLocationClientImpl): LocationClient

    @Binds
    @Singleton
    abstract fun bindGeofenceRepository(impl: FakeGeofenceRepositoryImpl): GeofenceRepository
}