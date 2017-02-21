package com.tumpaca.tp.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * SharedPreferencesにデータを保存するためのユーティリティ
 * Created by yabu on 2016/10/24.
 */
class TPSettings(val ctx: Context) {

    companion object {
        private const val TAG = "TPSettings"
        private const val EXCLUDE_MY_POSTS = "EXCLUDE_MY_POSTS"
        private const val EXCLUDE_PHOTO = "EXCLUDE_PHOTO"
        private const val HIGH_RESOLUTION_PHOTO = "HIGH_RESOLUTION_PHOTO"
        private const val SHOW_AD_POSTS = "SHOW_AD_POSTS"
    }

    /**
     * 自分のポストを表示するかどうかの設定
     */
    var excludeMyPosts: Boolean
        get() = getBoolean(EXCLUDE_MY_POSTS, false)
        set(value) = save(EXCLUDE_MY_POSTS, value)

    /**
     * 写真、動画、音声ポストを除外するかどうかの設定
     */
    var excludePhoto: Boolean
        get() = getBoolean(EXCLUDE_PHOTO, false)
        set(value) = save(EXCLUDE_PHOTO, value)

    /**
     * 高画質な写真を読み込むかどうかの設定
     */
    var highResolutionPhoto: Boolean
        get() = getBoolean(HIGH_RESOLUTION_PHOTO, false)
        set(value) = save(HIGH_RESOLUTION_PHOTO, value)

    /**
     * ダッシュボードのPostとして広告を挟むかどうかの設定。今のところ隠し設定で必ずOFF。
     */
    var showAdPosts: Boolean
        get() = getBoolean(SHOW_AD_POSTS, false)
        set(value) = save(SHOW_AD_POSTS, value)

    private fun save(key: String, value: Any): Unit {
        val data = ctx.getSharedPreferences("DataSave", Context.MODE_PRIVATE)
        val editor = data.edit()

        when (value) {
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is String -> editor.putString(key, value)
            else -> Log.v(TAG, "value type must be Int, Boolean or String.")
        }
        editor.apply()
    }

    private fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    private fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    private fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue)
    }

    val sharedPreferences: SharedPreferences
        get() = ctx.getSharedPreferences("DataSave", Context.MODE_PRIVATE)

}