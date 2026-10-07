package com.example.spotly

import android.app.Application
import com.example.spotly.network.initializeImageService

class SpotlyApplication : Application() {
    val container by lazy { AppContainer(this) }
    override fun onCreate() {
        super.onCreate()
        initializeImageService(this)
    }
}
