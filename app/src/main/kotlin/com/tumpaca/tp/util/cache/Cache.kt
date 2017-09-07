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
    abstract protected val tag: String
    abstract protected val lruCache: LruCache<String, T>

    override fun set(key: String, value: T) {
        lruCache.put(key, value)
    }

    override fun get(key: String): T? = lruCache.get(key)

    override fun getIfNoneAndSet(key: String, f: () -> T?): T? {
        get(key)?.let {
            Log.d(tag, "cache hit: key=$key, cache=$lruCache, evict=${lruCache.evictionCount()}")
            return it
        }

        try {
            f()?.let {
                Log.d(tag, "cache not hit and set: key=$key, cache=$lruCache, evict=${lruCache.evictionCount()}")
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
    // 単位はバイト
    private val MAX_SIZE = 32 * 1024 * 1024
    override val lruCache = object : LruCache<String, Bitmap>(MAX_SIZE) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
    }
    override val tag = "BitmapCache"
}

class AvatarUrlCache : AbstractCache<String>() {
    // 単位は文字数。
    // 1URL = 100 文字として、100 アバター保存できれば十分と考える
    private val MAX_CHAR_COUNT = 100 * 100
    override val lruCache = object : LruCache<String, String>(MAX_CHAR_COUNT) {
        override fun sizeOf(key: String, value: String): Int = value.count()
    }
    override val tag = "AvatarUrlCache"
}

class GifCache : AbstractCache<ByteArray>() {
    private val MAX_SIZE = 32 * 1024 * 1024
    override val lruCache = object : LruCache<String, ByteArray>(MAX_SIZE) {
        override fun sizeOf(key: String, value: ByteArray): Int = value.size
    }
    override val tag = "GifCache"
}