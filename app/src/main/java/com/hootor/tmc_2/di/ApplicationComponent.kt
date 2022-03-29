package com.hootor.tmc_2.di

import android.app.Application
import com.hootor.tmc_2.screens.main.MainActivity
import com.hootor.tmc_2.screens.main.photo.UploadPhotoFragment
import com.hootor.tmc_2.screens.main.scanning.tmc.ScanningTMCFragment
import com.hootor.tmc_2.screens.main.scanning.qr.ScanningQRFragment
import com.hootor.tmc_2.screens.main.settings.SettingsFragment
import com.hootor.tmc_2.screens.main.tmcTree.TMCTreeFragment
import com.hootor.tmc_2.screens.splash.SplashFragment
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(
    modules = [
        DataModule::class,
        ViewModelModule::class,
        RemoteModule::class,
        DispatcherModule::class
    ]
)
interface ApplicationComponent {

    fun inject(fragment: SplashFragment)
    fun inject(fragment: SettingsFragment)
    fun inject(fragment: ScanningTMCFragment)
    fun inject(fragment: ScanningQRFragment)
    fun inject(fragment: TMCTreeFragment)
    fun inject(fragment: UploadPhotoFragment)

    fun inject(activity: MainActivity)

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance application: Application,
        ): ApplicationComponent
    }
}