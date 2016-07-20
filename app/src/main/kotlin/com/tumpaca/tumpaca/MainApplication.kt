package com.tumpaca.tumpaca

import android.app.Application
import android.util.Log
import com.tumpaca.tumpaca.util.TumblrService

class MainApplication: Application() {
    companion object {
        private const val TAG = "MainApplication"
    }
    var tumblrService: TumblrService? = null
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
        tumblrService = TumblrService(this)
    }
}
