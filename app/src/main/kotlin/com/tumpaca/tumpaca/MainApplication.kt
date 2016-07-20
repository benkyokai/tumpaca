package com.tumpaca.tumpaca

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import com.tumpaca.tumpaca.util.TumblrService
import com.tumpaca.tumpaca.util.cache.TPCache

class MainApplication: Application() {
    companion object {
        private const val TAG = "MainApplication"
    }
    var tumblrService: TumblrService? = null
        private set

    var bitMapCache: TPCache<Bitmap>? = null
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
        tumblrService = TumblrService(this)
        bitMapCache = TPCache<Bitmap>()
    }
}
