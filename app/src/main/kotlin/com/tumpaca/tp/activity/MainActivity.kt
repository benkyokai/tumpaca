package com.tumpaca.tp.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.tumpaca.tp.R
import com.tumpaca.tp.fragment.AuthFragment
import com.tumpaca.tp.model.TPRuntime

/**
 * Tumpaca クラスのメインアクティビティ。
 */
class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate()")
        setContentView(R.layout.activity_main)

        TPRuntime.mainApplication.analytics.logEvent("launched", null)

        if (savedInstanceState == null) {
            val start = AuthFragment()
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.replace(R.id.fragment_container, start)
            ft.commit()
        }
    }
}
