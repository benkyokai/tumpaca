package com.tumpaca.tumpaca

import android.app.Application
import com.tumpaca.tumpaca.util.TumblerService

class MainApplication: Application() {
    var tumblerService: TumblerService? = null

    override fun onCreate() {
        super.onCreate()
        tumblerService = TumblerService(this)
    }
}
