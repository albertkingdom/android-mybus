package com.albertkingdom.mybusmap

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.realm.Realm
import timber.log.Timber

@HiltAndroidApp
class MyBusApp: Application() {
    override fun onCreate() {
        super.onCreate()
        // init Realm database
        Realm.init(this);

        // only show Log in DEBUG mode
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}