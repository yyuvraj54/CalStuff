package com.dusht.data.di

import com.dusht.core.logging.AppLogger
import com.dusht.data.BuildConfig
import com.dusht.data.network.TimberHttpLoggingInterceptor
import com.dusht.data.profile.ProfileGateRepositoryImpl
import com.dusht.data.profile.UserProfileRepositoryImpl
import com.dusht.data.session.DisplayNameStoreImpl
import com.dusht.shared.session.DisplayNameStore
import com.dusht.data.session.UserSessionRepositoryImpl
import com.dusht.shared.profile.ProfileGateRepository
import com.dusht.shared.profile.UserProfileRepository
import com.dusht.shared.session.UserSessionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {

    @Binds
    @Singleton
    abstract fun bindUserSessionRepository(impl: UserSessionRepositoryImpl): UserSessionRepository

    @Binds
    @Singleton
    abstract fun bindProfileGateRepository(impl: ProfileGateRepositoryImpl): ProfileGateRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindDisplayNameStore(impl: DisplayNameStoreImpl): DisplayNameStore
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val start = System.nanoTime()
                AppLogger.api(
                    message = "→ ${request.method} ${request.url}",
                    extras = mapOf("headers" to request.headers.toString())
                )
                val response = try {
                    chain.proceed(request)
                } catch (e: Exception) {
                    AppLogger.api(
                        message = "✗ ${request.method} ${request.url} failed",
                        extras = mapOf("error" to (e.message ?: e.javaClass.simpleName)),
                        throwable = e
                    )
                    throw e
                }
                val tookMs = (System.nanoTime() - start) / 1_000_000
                AppLogger.api(
                    message = "← ${request.method} ${request.url}",
                    extras = mapOf(
                        "code" to response.code,
                        "durationMs" to tookMs
                    )
                )
                response
            }
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor(TimberHttpLoggingInterceptor()).apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
        }
        return builder.build()
    }
}
