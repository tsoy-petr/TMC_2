package com.hootor.tmc_2.di

import androidx.lifecycle.ViewModel
import com.hootor.tmc_2.screens.main.MainViewModel
import com.hootor.tmc_2.screens.main.scanning.tmc.ScanningTMCViewModel
import com.hootor.tmc_2.screens.main.scanning.qr.ScanningViewModel
import com.hootor.tmc_2.screens.main.settings.SettingsViewModel
import com.hootor.tmc_2.screens.splash.SplashViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    fun bindSplashViewModel(viewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScanningTMCViewModel::class)
    fun bindScanningTMCViewModel(viewModel: ScanningTMCViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScanningViewModel::class)
    fun bindScanningViewModel(viewModel: ScanningViewModel): ViewModel

}