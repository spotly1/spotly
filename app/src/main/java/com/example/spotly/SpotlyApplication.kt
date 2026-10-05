package com.example.spotly

import android.app.Application
import com.cloudinary.android.MediaManager

class SpotlyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = mapOf(
            "cloud_name" to "iufz7yvd"
        )

        MediaManager.init(this, config)
    }
}