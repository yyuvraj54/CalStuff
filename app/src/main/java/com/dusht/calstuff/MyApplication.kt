package com.dusht.calstuff

import android.app.Application
import com.dusht.core.logging.TimberInitializer
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TimberInitializer.init(isDebug = BuildConfig.DEBUG)
        FirebaseApp.initializeApp(this)
    }
}
