package com.tutorlink.app.di

import android.content.Context
import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.TutoApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://tutoudec-1.onrender.com/"

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val path = original.url.encodedPath
            
            val builder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Connection", "keep-alive")
            
            // Solo agregar Token si NO es una ruta de autenticación pública
            if (!path.contains("/api/auth/")) {
                val token = runBlocking {
                    sessionManager.authToken.first()
                }
                if (!token.isNullOrEmpty()) {
                    // Usamos .header() para asegurar que el token sea único y esté limpio
                    builder.header("Authorization", "Bearer ${token.trim()}")
                }
            }
            
            val response = chain.proceed(builder.build())
            
            // Log de depuración para que puedas ver el error exacto en Logcat
            if (response.code == 403) {
                android.util.Log.e("API_AUTH", "403 Forbidden en: $path. Verifica que el rol coincida en el Backend.")
            }
            
            response
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideTutoApiService(retrofit: Retrofit): TutoApiService {
        return retrofit.create(TutoApiService::class.java)
    }
}
