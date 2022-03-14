package com.hootor.tmc_2.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.hootor.tmc_2.data.settings.SettingsRepositoryImpl
import com.hootor.tmc_2.domain.settings.SettingsRepository
import com.hootor.tmc_2.services.AuthInterceptor
import com.hootor.tmc_2.services.ServiceFactory
import com.hootor.tmc_2.services.TMCService
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@Module
interface RemoteModule {

    companion object {

        @ApplicationScope
        @Provides
        fun provideTMCService(factory: ServiceFactory): TMCService {
            return factory.makeTMCService()
        }
    }
}