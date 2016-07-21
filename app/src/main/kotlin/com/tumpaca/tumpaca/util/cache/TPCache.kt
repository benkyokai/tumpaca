package com.tumpaca.tumpaca.util.cache

import android.util.Log
import java.util.*

/**
 * Created by yabu on 7/20/16.
 */

interface Cache<T> {
    fun set(key: String, value: T)
    fun get(key: String): T?
    fun getIfNoneAndSet(key: String, f:() -> T?): T?
}

class TPCache<T>: Cache<T> {
    val TAG = "TPCache"
    val m = HashMap<String, T>()

    override fun set(key: String, value: T) {
        m.put(key, value)
    }

    override fun get(key: String): T? {
        return m[key]
    }

    override fun getIfNoneAndSet(key: String, f: () -> T?): T? {
        // キャッシュにあればその値を返す
        m[key]?.let {
            Log.d(TAG, "bitmap cache hit")
            return it
        }

        // キャッシュになければラムダを実行した結果をキャッシュにセットして、その値を返す
        f()?.let {
            set(key, it)
            return it
        }

        return null
    }
}