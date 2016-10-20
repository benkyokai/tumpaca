package com.tumpaca.tumpaca.util.cache

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

class BitmapCache() : Cache<Bitmap> {
    val TAG = "BitmapCache"

    private val MAX_SIZE = 32 * 1024 * 1024

    private val lruCache = object : LruCache<String, Bitmap>(MAX_SIZE) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.allocationByteCount
        }
    }

    override fun set(key: String, value: Bitmap) {
        val bitmap: Bitmap = value
        lruCache.put(key, bitmap)
    }

    override fun get(key: String): Bitmap? {
        // put でかならず Bitmap をいれていて、T は Bitmap を extends しているので問題なし
        val bitmap = lruCache.get(key)
        return bitmap
    }

    override fun getIfNoneAndSet(key: String, f: () -> Bitmap?): Bitmap? {
        // キャッシュにあればその値を返す
        get(key)?.let {
            Log.d(TAG, "bitmap cache hit")
            return it
        }

        // キャッシュになければラムダを実行した結果をキャッシュにセットして、その値を返す
        try {
            f()?.let {
                Log.d(TAG, "bitmap cache not hit: $lruCache")
                set(key, it)
                return it
            }
        } catch (e: Throwable) {
            Log.e(TAG, "BitmapCache fetch error: ${e.message}")
        }

        return null
    }
}

class AvatarUrlCache() : Cache<String> {
    val TAG = "AvatarUrlCache"

    private val MAX_SIZE = 2 * 1024 * 1024 // 2MB

    private val lruCache = object : LruCache<String, String>(MAX_SIZE) {
        override fun sizeOf(key: String, value: String): Int {
            return value.count()
        }
    }

    override fun set(key: String, value: String) {
        val url: String = value
        lruCache.put(key, url)
    }

    override fun get(key: String): String? {
        val url = lruCache.get(key) as String?
        return url
    }

    override fun getIfNoneAndSet(key: String, f: () -> String?): String? {
        // キャッシュにあればその値を返す
        get(key)?.let {
            Log.d(TAG, "avatar url cache hit")
            return it
        }

        // キャッシュになければラムダを実行した結果をキャッシュにセットして、その値を返す
        try {
            f()?.let {
                Log.d(TAG, "avatar url cache not hit: $lruCache")
                set(key, it)
                return it
            }
        } catch (e: Throwable) {
            Log.e(TAG, "AvatarUrlCache fetch error: ${e.message}")
        }

        return null
    }
}