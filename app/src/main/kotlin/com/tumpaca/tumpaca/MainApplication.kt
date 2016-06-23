package com.tumpaca.tumpaca

import android.app.Application
import android.util.Log
import com.tumpaca.tumpaca.util.TumblerService

class MainApplication: Application() {
    companion object {
        const val TAG = "MainApplication"
    }
    var tumblerService: TumblerService? = null
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
        tumblerService = TumblerService(this)
    }
}
