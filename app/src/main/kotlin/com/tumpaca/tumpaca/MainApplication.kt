package com.tumpaca.tumpaca

import android.app.Application
import android.util.Log
import com.tumpaca.tumpaca.model.TPRuntime

class MainApplication : Application() {
    companion object {
        private const val TAG = "MainApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
        TPRuntime.initialize(this)
    }
}
