package com.hootor.tmc_2.di

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

@Module
interface DispatcherModule {

    companion object {

        @ApplicationScope
        @DefaultDispatcher
        @Provides
        fun providesDefaultDispatcher(): CoroutineContext = Dispatchers.Default

        @ApplicationScope
        @IoDispatcher
        @Provides
        fun providesIoDispatcher(): CoroutineContext = Dispatchers.IO

        @ApplicationScope
        @MainDispatcher
        @Provides
        fun providesMainDispatcher(): CoroutineContext = Dispatchers.Main
    }
}