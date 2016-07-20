package com.tumpaca.tumpaca.util.cache

import java.util.*

/**
 * Created by yabu on 7/20/16.
 */

interface Cache<T> {
    fun set(key: String, value: T)
    fun get(key: String): T?
}

class TPCache<T>: Cache<T> {
    val m = HashMap<String, T>()

    override fun set(key: String, value: T) {
        m.put(key, value)
    }

    override fun get(key: String): T? {
        return m.get(key)
    }
}