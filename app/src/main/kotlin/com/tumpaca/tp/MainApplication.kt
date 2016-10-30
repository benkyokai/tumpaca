package com.tumpaca.tp

import android.app.Application
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.tumpaca.tp.model.TPRuntime

class MainApplication : Application() {
    companion object {
        private const val TAG = "MainApplication"
    }

    lateinit var analytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
        TPRuntime.initialize(this)

        analytics = FirebaseAnalytics.getInstance(this)
        if (BuildConfig.FIREBASE_ENABLED) {
            analytics.setAnalyticsCollectionEnabled(true)
        }
    }
}
