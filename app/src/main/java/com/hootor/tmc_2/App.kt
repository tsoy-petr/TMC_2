package com.hootor.tmc_2

import android.app.Application
import com.hootor.tmc_2.di.DaggerApplicationComponent

class App: Application() {

    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

}