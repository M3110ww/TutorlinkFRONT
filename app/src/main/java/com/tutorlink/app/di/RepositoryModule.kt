package com.tutorlink.app.di

import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.TutoApiService
import com.tutorlink.app.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides @Singleton
    fun provideAuthRepository(api: TutoApiService, sm: SessionManager): AuthRepository =
        AuthRepository(api, sm)

    @Provides @Singleton
    fun provideTutorRepository(api: TutoApiService): TutorRepository = TutorRepository(api)

    @Provides @Singleton
    fun provideStudentRepository(api: TutoApiService): StudentRepository = StudentRepository(api)

    @Provides @Singleton
    fun provideSessionRepository(api: TutoApiService): SessionRepository = SessionRepository(api)

    @Provides @Singleton
    fun provideAvailabilityRepository(api: TutoApiService): AvailabilityRepository =
        AvailabilityRepository(api)

    @Provides @Singleton
    fun provideReviewRepository(api: TutoApiService): ReviewRepository = ReviewRepository(api)

    @Provides @Singleton
    fun provideAdminRepository(api: TutoApiService): AdminRepository = AdminRepository(api)
}
