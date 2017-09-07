package com.tumpaca.tp.model

import com.tumpaca.tp.MainApplication
import com.tumpaca.tp.util.cache.AvatarUrlCache
import com.tumpaca.tp.util.cache.BitmapCache
import com.tumpaca.tp.util.cache.GifCache

/**
 * 実行環境
 * Created by yabu on 7/21/16.
 */

object TPRuntime {
    val bitMapCache = BitmapCache()
    val avatarUrlCache = AvatarUrlCache()
    val gifCache = GifCache()
    var initialized = false
    lateinit var settings: TPSettings
    lateinit var mainApplication: MainApplication
    lateinit var tumblrService: TumblrService
        private set

    fun initialize(application: MainApplication) {
        mainApplication = application
        tumblrService = TumblrService(application)
        initialized = true
        settings = TPSettings(mainApplication)
    }
}