package com.tumpaca.tp.util.cache

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache

/**
 * Created by yabu on 7/20/16.
 */
interface Cache<T> {
    fun set(key: String, value: T)
    fun get(key: String): T?
    fun getIfNoneAndSet(key: String, f: () -> T?): T?
}

abstract class AbstractCache<T> : Cache<T> {
    abstract fun getTag(): String
    abstract fun getLruCache(): LruCache<String, T>

    override fun set(key: String, value: T) {
        getLruCache().put(key, value)
    }

    override fun get(key: String): T? {
        return getLruCache().get(key)
    }

    override fun getIfNoneAndSet(key: String, f: () -> T?): T? {
        val tag = getTag()
        val cache = getLruCache()

        get(key)?.let {
            Log.d(tag, "cache hit: key=$key, cache=$cache, evict=${cache.evictionCount()}")
            return it
        }

        try {
            f()?.let {
                Log.d(tag, "cache not hit and set: key=$key, cache=$cache, evict=${cache.evictionCount()}")
                set(key, it)
                return it
            }
        } catch (e: Throwable) {
            Log.e(tag, "BitmapCache fetch error: ${e.message}", e)
        }

        return null
    }
}

class BitmapCache : AbstractCache<Bitmap>() {
    private val MAX_SIZE = 32 * 1024 * 1024
    private val lruCache = object : LruCache<String, Bitmap>(MAX_SIZE) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
    }

    override fun getTag(): String = "BitmapCache"
    override fun getLruCache(): LruCache<String, Bitmap> = lruCache
}

class AvatarUrlCache : AbstractCache<String>() {
    private val MAX_SIZE = 1 * 1024 * 1024
    private val lruCache = object : LruCache<String, String>(MAX_SIZE) {
        override fun sizeOf(key: String, value: String): Int = value.count()
    }

    override fun getTag(): String = "AvatarUrlCache"
    override fun getLruCache(): LruCache<String, String> = lruCache
}