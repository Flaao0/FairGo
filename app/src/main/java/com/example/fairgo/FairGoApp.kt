package com.example.fairgo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.yandex.mapkit.MapKitFactory

@HiltAndroidApp
class FairGoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("170c30c2-472c-42cc-9871-912d32fbc7e2")
        MapKitFactory.initialize(this)
    }
}

