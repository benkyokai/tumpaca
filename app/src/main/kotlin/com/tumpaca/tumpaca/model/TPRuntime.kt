package com.tumpaca.tumpaca.model

import android.graphics.Bitmap
import com.tumpaca.tumpaca.MainApplication
import com.tumpaca.tumpaca.util.cache.AvatarUrlCache
import com.tumpaca.tumpaca.util.cache.BitmapCache

/**
 * Created by yabu on 7/21/16.
 */

object TPRuntime {
    val bitMapCache = BitmapCache()
    val avatarUrlCache = AvatarUrlCache()
    var initialized = false
    lateinit var mainApplication: MainApplication
    lateinit var tumblrService: TumblrService
        private set

    fun initialize(application: MainApplication) {
        mainApplication = application
        tumblrService = TumblrService(application)
        initialized = true
    }
}