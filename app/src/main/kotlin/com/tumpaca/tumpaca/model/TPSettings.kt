package com.tumpaca.tumpaca.model

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
    }

    /**
     * 自分のポストを表示するかどうかの設定
     */
    private var mIsExcludeMyPosts: Boolean? = null

    fun isExcludeMyPosts(): Boolean {
        if (mIsExcludeMyPosts == null) {
            mIsExcludeMyPosts = getBoolean(EXCLUDE_MY_POSTS, false)
        }
        return mIsExcludeMyPosts!!
    }

    fun setExcludeMyPosts(isExcludeMyPosts: Boolean): Unit {
        mIsExcludeMyPosts = isExcludeMyPosts
        save(EXCLUDE_MY_POSTS, isExcludeMyPosts)
    }

    /**
     * 写真、動画、音声ポストを除外するかどうかの設定
     */
    private var mExcludePhoto: Boolean? = null

    fun isExcludePhoto(): Boolean {
        if (mExcludePhoto == null) {
            mExcludePhoto = getBoolean(EXCLUDE_PHOTO, false)
        }
        return mExcludePhoto!!
    }

    fun setExcludePhoto(excludePhoto: Boolean): Unit {
        mExcludePhoto = excludePhoto
        save(EXCLUDE_PHOTO, excludePhoto)
    }

    /**
     * 高画質な写真を読み込むかどうかの設定
     */
    private var mIsHighResolutionPhoto: Boolean? = null

    fun isHighResolutionPhoto(): Boolean {
        if (mIsHighResolutionPhoto == null) {
            mIsHighResolutionPhoto = getBoolean(HIGH_RESOLUTION_PHOTO, false)
        }
        return mIsHighResolutionPhoto!!
    }

    fun setHighResolutionPhoto(isHighResolutionPhoto: Boolean): Unit {
        mIsHighResolutionPhoto = isHighResolutionPhoto
        save(HIGH_RESOLUTION_PHOTO, isHighResolutionPhoto)
    }

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
        val data = getSharedPreferences()
        return data.getInt(key, defaultValue)
    }

    private fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val data = getSharedPreferences()
        return data.getBoolean(key, defaultValue)
    }

    private fun getString(key: String, defaultValue: String): String {
        val data = getSharedPreferences()
        return data.getString(key, defaultValue)
    }

    private fun getSharedPreferences(): SharedPreferences =
            ctx.getSharedPreferences("DataSave", Context.MODE_PRIVATE)

}