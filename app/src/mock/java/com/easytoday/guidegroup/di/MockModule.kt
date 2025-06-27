// app/src/mock/java/com/easytoday/guidegroup/di/MockModule.kt
package com.easytoday.guidegroup.di
//liste des imports
import com.easytoday.guidegroup.data.location.LocationClientImpl
import com.easytoday.guidegroup.data.repository.impl.FakeAuthRepositoryImpl // normal pas encore créer
import com.easytoday.guidegroup.data.repository.impl.FakeGroupRepositoryImpl
import com.easytoday.guidegroup.data.repository.impl.FakeLocationRepositoryImpl
import com.easytoday.guidegroup.data.repository.impl.FakeMeetingPointRepositoryImpl
import com.easytoday.guidegroup.data.repository.impl.FakeMessageRepositoryImpl
import com.easytoday.guidegroup.data.repository.impl.FakePointOfInterestRepositoryImpl
import com.easytoday.guidegroup.data.repository.impl.FakeUserRepositoryImpl
import com.easytoday.guidegroup.data.repository.impl.fake.FakeLocationClientImpl
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
@MockBuild // <--- Marquez ce module comme étant pour les builds de mock
abstract class MockModule { // Utilisez 'abstract class' pour @Binds

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

    // --- NOUVEAU : Liaison pour LocationClient ---
    @Binds
    @Singleton
    abstract fun bindLocationClient(
        impl: FakeLocationClientImpl // Lie l'interface à l'implémentation factice
    ): LocationClient
    // --- FIN NOUVEAU ---
}

