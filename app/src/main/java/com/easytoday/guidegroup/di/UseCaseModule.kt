// app/src/main/java/com/easytoday/guidegroup/di/UseCaseModule.kt
package com.easytoday.guidegroup.di

import com.easytoday.guidegroup.domain.repository.*
import com.easytoday.guidegroup.domain.usecase.*

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * Module Dagger Hilt pour fournir les cas d'utilisation (Use Cases).
 * Les cas d'utilisation sont fournis en tant que Singletons et dépendent des interfaces de référentiels.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideSignInUseCase(authRepository: AuthRepository): SignInUseCase {
        return SignInUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideSignUpUseCase(authRepository: AuthRepository): SignUpUseCase {
        return SignUpUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideSendMessageUseCase(messageRepository: MessageRepository): SendMessageUseCase {
        return SendMessageUseCase(messageRepository)
    }

    @Provides
    @Singleton
    fun provideAddGeofenceUseCase(geofenceRepository: GeofenceRepository): AddGeofenceUseCase {
        return AddGeofenceUseCase(geofenceRepository)
    }

    @Provides
    @Singleton
    fun provideRemoveGeofenceUseCase(geofenceRepository: GeofenceRepository): RemoveGeofenceUseCase {
        return RemoveGeofenceUseCase(geofenceRepository)
    }

    @Provides
    @Singleton
    fun provideStartGeofenceMonitoringUseCase(geofenceRepository: GeofenceRepository): StartGeofenceMonitoringUseCase {
        return StartGeofenceMonitoringUseCase(geofenceRepository)
    }

    @Provides
    @Singleton
    fun provideStopGeofenceMonitoringUseCase(geofenceRepository: GeofenceRepository): StopGeofenceMonitoringUseCase {
        return StopGeofenceMonitoringUseCase(geofenceRepository)
    }

    // ajout d'autres use cases ici au fur et à mesure qu'ils sont créés.
    // Par exemple, pour les groupes:
    // @Provides
    // @Singleton
    // fun provideCreateGroupUseCase(groupRepository: GroupRepository): CreateGroupUseCase {
    //     return CreateGroupUseCase(groupRepository)
    // }
    // @Provides
    // @Singleton
    // fun provideGetGroupUseCase(groupRepository: GroupRepository): GetGroupUseCase {
    //     return GetGroupUseCase(groupRepository)
    // }
}

