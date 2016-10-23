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