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

// TODO Cache の統一化
class BitmapCache : Cache<Bitmap> {
    companion object {
        val TAG = "BitmapCache"
    }

    private val MAX_SIZE = 32 * 1024 * 1024

    private val lruCache = object : LruCache<String, Bitmap>(MAX_SIZE) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.allocationByteCount
        }
    }

    override fun set(key: String, value: Bitmap) {
        lruCache.put(key, value)
    }

    override fun get(key: String): Bitmap? {
        return lruCache.get(key)
    }

    override fun getIfNoneAndSet(key: String, f: () -> Bitmap?): Bitmap? {
        get(key)?.let {
            Log.d(BitmapCache.TAG, "cache hit: key=$key, cache=$lruCache, evict=${lruCache.evictionCount()}")
            return it
        }

        try {
            f()?.let {
                Log.d(BitmapCache.TAG, "cache not hit and set: key=$key, cache=$lruCache, evict=${lruCache.evictionCount()}")
                set(key, it)
                return it
            }
        } catch (e: Throwable) {
            Log.e(BitmapCache.TAG, "BitmapCache fetch error: ${e.message}")
        }

        return null
    }
}

class AvatarUrlCache : Cache<String> {
    companion object {
        val TAG = "AvatarUrlCache"
    }

    private val MAX_SIZE = 1 * 1024 * 1024

    private val lruCache = object : LruCache<String, String>(MAX_SIZE) {
        override fun sizeOf(key: String, value: String): Int {
            return value.count()
        }
    }

    override fun set(key: String, value: String) {
        lruCache.put(key, value)
    }

    override fun get(key: String): String? {
        return lruCache.get(key)
    }

    override fun getIfNoneAndSet(key: String, f: () -> String?): String? {
        get(key)?.let {
            Log.d(TAG, "cache hit: key=$key, cache=$lruCache, evict=${lruCache.evictionCount()}")
            return it
        }

        try {
            f()?.let {
                Log.d(TAG, "cache not hit and set: key=$key, cache=$lruCache, evict=${lruCache.evictionCount()}")
                set(key, it)
                return it
            }
        } catch (e: Throwable) {
            Log.e(TAG, "AvatarUrlCache fetch error: ${e.message}")
        }

        return null
    }
}