package com.tumpaca.tumpaca.model

import android.graphics.Bitmap
import com.tumpaca.tumpaca.MainApplication
import com.tumpaca.tumpaca.model.TumblrService
import com.tumpaca.tumpaca.util.cache.BitmapCache

/**
 * Created by yabu on 7/21/16.
 */

object TPRuntime {
    val bitMapCache = BitmapCache<Bitmap>()
    var initialized = false
    var mainApplication: MainApplication? = null
    var tumblrService: TumblrService? = null
        private set

    fun initialize(application: MainApplication) {
        mainApplication = application
        tumblrService = TumblrService(application)
        initialized = true
    }
}