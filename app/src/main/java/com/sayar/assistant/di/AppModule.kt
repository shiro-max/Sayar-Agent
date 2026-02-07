package com.sayar.assistant.di

import android.content.Context
import androidx.room.Room
import com.sayar.assistant.data.local.AppDatabase
import com.sayar.assistant.data.local.StudentDao
import com.sayar.assistant.data.repository.AuthRepositoryImpl
import com.sayar.assistant.data.repository.DriveRepositoryImpl
import com.sayar.assistant.domain.repository.AuthRepository
import com.sayar.assistant.domain.repository.DriveRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sayar_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideStudentDao(database: AppDatabase): StudentDao {
        return database.studentDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDriveRepository(impl: DriveRepositoryImpl): DriveRepository
}
