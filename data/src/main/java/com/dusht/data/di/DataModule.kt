package com.dusht.data.di

import android.content.Context
import androidx.room.Room
import com.dusht.core.logging.AppLogger
import com.dusht.data.BuildConfig
import com.dusht.data.local.CalStuffDatabase
import com.dusht.data.local.dao.NutritionDao
import com.dusht.data.local.dao.StreakDao
import com.dusht.data.local.dao.UserProfileDao
import com.dusht.data.network.TimberHttpLoggingInterceptor
import com.dusht.data.nutrition.NutritionRepositoryImpl
import com.dusht.data.nutrition.StreakRepositoryImpl
import com.dusht.data.nutrition.UserProfileRepositoryImpl
import com.dusht.data.profile.ProfileGateRepositoryImpl
import com.dusht.data.session.DisplayNameStoreImpl
import com.dusht.shared.session.DisplayNameStore
import com.dusht.data.session.UserSessionRepositoryImpl
import com.dusht.shared.profile.ProfileGateRepository
import com.dusht.shared.repository.NutritionRepository
import com.dusht.shared.repository.StreakRepository
import com.dusht.shared.repository.UserProfileRepository
import com.dusht.shared.session.UserSessionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {

    @Binds @Singleton
    abstract fun bindUserSessionRepository(impl: UserSessionRepositoryImpl): UserSessionRepository

    @Binds @Singleton
    abstract fun bindProfileGateRepository(impl: ProfileGateRepositoryImpl): ProfileGateRepository

    @Binds @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds @Singleton
    abstract fun bindDisplayNameStore(impl: DisplayNameStoreImpl): DisplayNameStore

    @Binds @Singleton
    abstract fun bindNutritionRepository(impl: NutritionRepositoryImpl): NutritionRepository

    @Binds @Singleton
    abstract fun bindStreakRepository(impl: StreakRepositoryImpl): StreakRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CalStuffDatabase =
        Room.databaseBuilder(context, CalStuffDatabase::class.java, "calstuff.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides @Singleton
    fun provideUserProfileDao(db: CalStuffDatabase): UserProfileDao = db.userProfileDao()

    @Provides @Singleton
    fun provideNutritionDao(db: CalStuffDatabase): NutritionDao = db.nutritionDao()

    @Provides @Singleton
    fun provideStreakDao(db: CalStuffDatabase): StreakDao = db.streakDao()

    @Provides @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
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
                    extras = mapOf("code" to response.code, "durationMs" to tookMs)
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
