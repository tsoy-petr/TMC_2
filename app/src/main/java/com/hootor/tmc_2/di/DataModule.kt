package com.hootor.tmc_2.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.hootor.tmc_2.data.Prefs
import com.hootor.tmc_2.data.PrefsImpl
import com.hootor.tmc_2.data.media.MediaRepositoryImpl
import com.hootor.tmc_2.data.room.AppDatabase
import com.hootor.tmc_2.data.room.inventory.TMCDbInventoryDao
import com.hootor.tmc_2.data.room.inventory.TMCDbInventoryItemsDao
import com.hootor.tmc_2.data.scanningQrCode.ScanningRepoImpl
import com.hootor.tmc_2.data.settings.SettingsRepositoryImpl
import com.hootor.tmc_2.data.tmcTree.GetTMCTreeQrCodeRepositoryImpl
import com.hootor.tmc_2.domain.media.MediaRepository
import com.hootor.tmc_2.domain.scanning.ScanningTMCRepository
import com.hootor.tmc_2.domain.settings.SettingsRepository
import com.hootor.tmc_2.domain.tmc.tree.GetTMCTreeQrCodeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @ApplicationScope
    @Binds
    fun bindScanningTMCRepository(impl: ScanningRepoImpl): ScanningTMCRepository

    @ApplicationScope
    @Binds
    fun bindGetTMCTreeQrCodeRepositoryImpl(impl: GetTMCTreeQrCodeRepositoryImpl): GetTMCTreeQrCodeRepository

    companion object {

        @ApplicationScope
        @Provides
        fun bindGetMediaRepositoryImpl(application: Application): MediaRepository {
            return MediaRepositoryImpl(application)
        }

        @ApplicationScope
        @Provides
        fun provideSharedPreferences(application: Application): SharedPreferences {
            return application.getSharedPreferences(application.packageName, Context.MODE_PRIVATE)
        }

        @ApplicationScope
        @Provides
        fun providePref(sharedPreferences: SharedPreferences): Prefs {
            return PrefsImpl(sharedPreferences)
        }

        @ApplicationScope
        @Provides
        fun provideDatabase(application: Application): AppDatabase =
            Room.databaseBuilder(application, AppDatabase::class.java, "database.db")
                .build()

        @ApplicationScope
        @Provides
        fun provideTMCDbInventoryDao(database: AppDatabase):TMCDbInventoryDao = database.getTMCDbInventoryDao()

        @ApplicationScope
        @Provides
        fun provideTMCDbInventoryItemsDao(database: AppDatabase): TMCDbInventoryItemsDao = database.getTMCDbInventoryItemsDao()

    }

}